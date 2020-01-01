package com.erjiguan.frog.invokehelper;

import android.util.Log;

public class InvokeHelper {

    private static final String TAG = "InvokeHelper";

    public static Object invoke(Object self, String className, String methodName, Object[] args, int pid, int tid, int uid, long time) {
        Log.d(TAG, "" + self.getClass().getName() + " " + className + " " + methodName + " " + args.length + " " + pid + " " + tid + " " + uid + " " + time);

        return null;
    }
}
