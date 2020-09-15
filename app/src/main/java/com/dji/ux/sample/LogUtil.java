package com.dji.ux.sample;

import android.util.Log;


/**
 * $activityName
 *
 * @author LiuTao
 * @date 2019/2/20/020
 */


public class LogUtil {
    private static boolean IS_DEBUG = BuildConfig.DEBUG;
    private static String TAG = "LogUtil";
    /**
     * 写文件的锁对象
     */
    private static final Object mLogLock = new Object();



    public static void i(String tag, String message) {
        if (StringUtils.isEmpty(tag)) {
            tag = TAG;
        }
        if (IS_DEBUG) {
            Log.i(tag + "-->:" + getTargetStackTraceElement(), message);
        }
    }

    public static void e(String tag, String message) {
        if (StringUtils.isEmpty(tag)) {
            tag = TAG;
        }
        if (IS_DEBUG) {
            Log.e(tag + "-->" + getTargetStackTraceElement(), message);
        }
    }

    public static void e(String tag, String message, Throwable tr) {
        if (StringUtils.isEmpty(tag)) {
            tag = TAG;
        }
        if (IS_DEBUG) {
            Log.e(tag + "-->" + getTargetStackTraceElement(), message, tr);
        }
    }

    public static void w(String tag, String message) {
        if (StringUtils.isEmpty(tag)) {
            tag = TAG;
        }
        if (IS_DEBUG) {
            Log.w(tag + "-->:" + getTargetStackTraceElement(), message);
        }
    }

    public static void v(String tag, String message) {
        if (StringUtils.isEmpty(tag)) {
            tag = TAG;
        }
        if (IS_DEBUG) {
            Log.v(tag + "-->:" + getTargetStackTraceElement(), message);
        }
    }

    public static void d(String tag, String message) {
        if (StringUtils.isEmpty(tag)) {
            tag = TAG;
        }
        if (IS_DEBUG) {
            Log.d(tag + "-->:" + getTargetStackTraceElement(), message);
        }
    }

    private static String getTargetStackTraceElement() {
        StackTraceElement targetStackTrace = null;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length >= 4) {
            targetStackTrace = stackTrace[4];
        }
        String s = "";
        if (null != targetStackTrace) {
            s = "(" + targetStackTrace.getFileName() + ":"
                    + targetStackTrace.getLineNumber() + ")";
        }
        return s;
    }

    /**
     * 把日志存储到文件中
     *
     * @param log  需要存储的日志
     * @param path 存储路径
     *  LogUtil.log2File(sbody, FileUtils.getDir("history") + System.currentTimeMillis());
     */
    public static void log2File(String log, String path) {
        log2File(log, path, true);
    }

    public static void log2File(String log, String path, boolean append) {
        synchronized (mLogLock) {
            FileUtils.writeFile(log + "\r\n", path, append);
        }
    }
}
