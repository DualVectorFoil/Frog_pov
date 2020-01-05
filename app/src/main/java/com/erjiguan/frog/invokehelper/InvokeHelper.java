package com.erjiguan.frog.invokehelper;

import android.util.Log;

import androidx.annotation.Nullable;

import com.esotericsoftware.kryo.Kryo;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class InvokeHelper {

    private static final String TAG = "InvokeHelper";

    public static boolean ENABLE_INVOKE = true;
    public static boolean ENABLE_LUA_SCRIPT = true;

    private static final ThreadLocal<Set<String>> mInvokeMethodSet = new ThreadLocal<Set<String>>() {
        @Nullable
        @Override
        protected Set<String> initialValue() {
            return new HashSet<String>();
        }
    };

    private static final ThreadLocal<Kryo> kryos = new ThreadLocal<Kryo>() {
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            // Configure the Kryo instance.
            return kryo;
        };
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

        processBeforeMethod(self, selfType, selfClassName, methodName, args, parameterTypes);

        Method method = null;
        Object res = null;
        if (args != null && args.length > 0) {
            try {
                if (self == null) {
                    method = selfType.getDeclaredMethod(methodName, parameterTypes);
                } else {
                    method = self.getClass().getDeclaredMethod(methodName, parameterTypes);
                }
                addMethodInfo(selfClassName, methodName, parameterTypes, pid, tid, uid);
                method.setAccessible(true);
                res = method.invoke(self, args);
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
                res = method.invoke(self);
            } catch (Exception e) {
                Log.e(TAG, "method invoke failed, selfClassName: " + selfClassName + ", err: " + e);
            }
        }

        processAfterMethod(self, selfType, selfClassName, methodName, args, parameterTypes, res);

        return res;
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

    private static void processBeforeMethod(Object self, Class<?> selfType, String selfClassName, String methodName,
                                            Object[] args, Class[] parameterTypes) {
        writeMethodInfo(self, args, null);
    }

    private static void processAfterMethod(Object self, Class<?> selfType, String selfClassName, String methodName,
                                           Object[] args, Class[] parameterTypes, Object res) {
        writeMethodInfo(self, args, res);
    }

    private static void writeMethodInfo(Object self, Object[] args, Object res) {
        if (self != null) {

        }

        if (args != null) {
            for (Object arg : args) {

            }
        }

        if (res != null) {

        }
    }

    private static void writeObject(Object obj, String path) {

    }
}
