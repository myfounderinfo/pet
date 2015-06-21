package com.globalLibrary.util;

import android.util.Log;

public final class LogUtil {
    private static final String TAG = "LogUtil";
    private static boolean isLog = true;
    private static int logLevel = Log.DEBUG;

    public static void d(String msg) {
        log(msg, null, Log.DEBUG);
    }

    private static void log(String msg, Throwable tr, int level) {
        if (isLog && logLevel <= level) {
            String info = getLogClassInfo();
            switch (level) {
                case Log.INFO:
                    if (tr == null) {
                        Log.i(TAG, info + " - " + msg);
                    } else {
                        Log.i(TAG, info, tr);
                    }
                    break;
                case Log.DEBUG:
                    if (tr == null) {
                        Log.d(TAG, info + " - " + msg);
                    } else {
                        Log.d(TAG, info, tr);
                    }
                    break;
                case Log.VERBOSE:
                    if (tr == null) {
                        Log.v(TAG, info + " - " + msg);
                    } else {
                        Log.v(TAG, info, tr);
                    }
                    break;
                case Log.ERROR:
                    if (tr == null) {
                        Log.e(TAG, info + " - " + msg);
                    } else {
                        Log.e(TAG, info, tr);
                    }
                    break;
                case Log.WARN:
                    if (tr == null) {
                        Log.w(TAG, info + " - " + msg);
                    } else {
                        Log.w(TAG, info, tr);
                    }
                    break;
            }
        }
    }

    private static String getLogClassInfo() {
        StringBuilder result = new StringBuilder();
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace != null) {
            for (StackTraceElement element : stackTrace) {
                if (element.isNativeMethod()) {
                    continue;
                }
                String name = element.getClassName();
                if (name.equals(LogUtil.class.getName())) {
                    continue;
                }
                if (name.equals(Thread.class.getName())) {
                    continue;
                }
                result.append("[").append(Thread.currentThread().getName()).append(":");
                result.append(element.getFileName()).append(":").append(element.getLineNumber()).append(" ");
                result.append(element.getMethodName()).append("]");
                break;
            }
        }
        return result.toString();
    }

    public static void d(Throwable tr) {
        log(null, tr, Log.DEBUG);
    }

    public static void e(String msg) {
        log(msg, null, Log.ERROR);
    }

    public static void e(Throwable tr) {
        log(null, tr, Log.ERROR);
    }

    public static void i(String msg) {
        log(msg, null, Log.INFO);
    }

    public static void i(Throwable tr) {
        log(null, tr, Log.INFO);
    }

    public static void isLog(boolean log) {
        LogUtil.isLog = log;
    }

    public static void logLevel(int level) {
        LogUtil.logLevel = level;
    }

    public static void simpleThrowable(Throwable throwable) {
        w(throwable.getClass().getName() + " - " + throwable.getMessage());
    }

    public static void w(String msg) {
        log(msg, null, Log.WARN);
    }

    /**
     * 打印函数调用堆栈信息
     *
     * @param depth 打印深度
     */
    public static void stackTrace(int depth) {
        StringBuilder sb = new StringBuilder();
        int size = 0;
        Throwable throwable = new Throwable();
        for (StackTraceElement element : throwable.getStackTrace()) {
            if (element.isNativeMethod() || element.getClassName().equals(LogUtil.class.getName())) {
                continue;
            }
            if (size >= depth) {
                break;
            }
            if (sb.length() > 0) {
                sb.insert(0, " -> ");
            }
            sb.insert(0, element.getMethodName()).insert(0, ").").insert(0, element.getLineNumber()).insert(0, "(").insert(0, element.getFileName());
            size++;
        }
        log(sb.toString(), null, Log.DEBUG);
    }

    public static void v(String msg) {
        log(msg, null, Log.VERBOSE);
    }

    public static void v(Throwable tr) {
        log(null, tr, Log.VERBOSE);
    }

    public static void w(Throwable tr) {
        log(null, tr, Log.WARN);
    }

    private LogUtil() {
    }
}
