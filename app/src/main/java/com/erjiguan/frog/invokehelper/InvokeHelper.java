package com.erjiguan.frog.invokehelper;

import android.util.Log;

import java.lang.reflect.Method;

public class InvokeHelper {

    private static final String TAG = "InvokeHelper";

    public static boolean ENABLE_INVOKE = true;

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
                    method.invoke(null, args);
                } else {
                    method = self.getClass().getDeclaredMethod(methodName, parameterTypes);
                    method.invoke(self, args);
                }
            } catch (Exception e) {
                Log.e(TAG, "method invoke failed, selfClassName: " + selfClassName + ", err: " + e);
            }
        } else {
            try {
                if (self == null) {
                    method = selfType.getDeclaredMethod(methodName);
                    method.invoke(null);
                } else {
                    method = self.getClass().getDeclaredMethod(methodName);
                    method.invoke(self);
                }
            } catch (Exception e) {
                Log.e(TAG, "method invoke failed, selfClassName: " + selfClassName + ", err: " + e);
            }
        }

        return null;
    }
}
