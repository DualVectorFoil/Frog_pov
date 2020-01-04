package com.erjiguan.frog.invokehelper;

import android.util.Log;

import androidx.annotation.Nullable;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class InvokeHelper {

    private static final String TAG = "InvokeHelper";

    public static boolean ENABLE_INVOKE = true;

    private static ThreadLocal<Set<String>> mInvokeMethodSet = new ThreadLocal<Set<String>>() {
        @Nullable
        @Override
        protected Set<String> initialValue() {
            return new HashSet<String>();
        }
    };

    public static boolean isEnable(String methodInfo) {
        if (!ENABLE_INVOKE) {
            return false;
        } else if (mInvokeMethodSet.get().contains(methodInfo)) {
            mInvokeMethodSet.get().remove(methodInfo);
            return false;
        }
        return true;
    }

    public static Object invoke(Object self, Class<?> selfType, String selfClassName, String methodName,
                                Object[] args, Class[] parameterTypes, int pid, int tid, int uid, long time) {
        Log.d(TAG, "self == null (is static): " + (self == null) + ", selfType: " + selfType.getName() + ", selfClassName: " + selfClassName
                + ", methodName: " + methodName + ", args == null: " + (args == null)
                + ", parameterTypes == null: " + (parameterTypes == null) + ", pid: " + pid + ", tid: " + tid + ", uid: " + uid + ", time: " + time);

        Method method;
        if (args != null && args.length > 0) {
            try {
                if (self == null) {
                    method = selfType.getDeclaredMethod(methodName, parameterTypes);
                } else {
                    method = self.getClass().getDeclaredMethod(methodName, parameterTypes);
                }
                addMethodInfo(selfClassName, methodName, parameterTypes, pid, tid, uid);
                method.setAccessible(true);
                method.invoke(self, args);
            } catch (Exception e) {
                Log.e(TAG, "method invoke failed, selfClassName: " + selfClassName + ", err: " + e);
            }
        } else {
            try {
                if (self == null) {
                    method = selfType.getDeclaredMethod(methodName);
                } else {
                    method = self.getClass().getDeclaredMethod(methodName);
                }
                addMethodInfo(selfClassName, methodName, parameterTypes, pid, tid, uid);
                method.setAccessible(true);
                method.invoke(self);
            } catch (Exception e) {
                Log.e(TAG, "method invoke failed, selfClassName: " + selfClassName + ", err: " + e);
            }
        }

        return null;
    }

    private static void addMethodInfo(String selfClassName, String methodName, Class[] parameterTypes, int pid, int tid, int uid) {
        StringBuilder methodInfo = new StringBuilder();
        methodInfo.append(selfClassName);
        methodInfo.append("#");
        methodInfo.append(methodName);
        methodInfo.append("#");
        if (parameterTypes != null) {
            for (Class parameterType : parameterTypes) {
                methodInfo.append(parameterType.getName());
                methodInfo.append("$");
            }
        }
        methodInfo.append("#");
        methodInfo.append(pid);
        methodInfo.append("#");
        methodInfo.append(tid);
        methodInfo.append("#");
        methodInfo.append(uid);
        mInvokeMethodSet.get().add(methodInfo.toString());
    }
}
