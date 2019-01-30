package com.excellence.retrofit.utils;

import android.text.TextUtils;
import android.util.Log;

import com.excellence.retrofit.BuildConfig;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     date   : 2017/10/20
 *     desc   : 调试打印
 * </pre>
 */

public final class Logger {

    private static final String TAG = Logger.class.getSimpleName();

    private static boolean isEnabled = BuildConfig.DEBUG;

    private enum LogLevel {
        VERBOSE,
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    public static void v(String tag, String msg, Throwable t) {
        print(LogLevel.VERBOSE, tag, msg, t);
    }

    public static void v(String tag, String msg) {
        v(tag, msg, null);
    }

    public static void v(String msg) {
        v(TAG, msg);
    }

    public static void d(String tag, String msg, Throwable t) {
        print(LogLevel.DEBUG, tag, msg, t);
    }

    public static void d(String tag, String msg) {
        d(tag, msg, null);
    }

    public static void d(String msg) {
        d(TAG, msg);
    }

    public static void i(String tag, String msg, Throwable t) {
        print(LogLevel.INFO, tag, msg, t);
    }

    public static void i(String tag, String msg) {
        i(tag, msg, null);
    }

    public static void i(String msg) {
        i(TAG, msg);
    }

    public static void w(String tag, String msg, Throwable t) {
        print(LogLevel.WARN, tag, msg, t);
    }

    public static void w(String tag, String msg) {
        w(tag, msg, null);
    }

    public static void w(String msg) {
        w(TAG, msg);
    }

    public static void e(String tag, String msg, Throwable t) {
        print(LogLevel.ERROR, tag, msg, t);
    }

    public static void e(String tag, String msg) {
        e(tag, msg, null);
    }

    public static void e(String msg) {
        e(TAG, msg);
    }

    private static void print(LogLevel level, String tag, String msg, Throwable tr) {
        if (!isEnabled) {
            return;
        }

        if (TextUtils.isEmpty(tag)) {
            tag = TAG;
        }

        if (msg == null) {
            msg = "";
        }

        StackTraceElement element = getTargetStackTraceElement();

        if (element != null) {
            String log = element.getClassName() + "." + element.getMethodName() + "(" + element.getFileName() + ":" + element.getLineNumber() + ")";
            msg = log + "\n" + msg;
        }

        switch (level) {
            case VERBOSE:
                Log.v(tag, msg, tr);
                break;

            case DEBUG:
                Log.d(tag, msg, tr);
                break;

            case INFO:
                Log.i(tag, msg, tr);
                break;

            case WARN:
                Log.w(tag, msg, tr);
                break;

            case ERROR:
                Log.e(tag, msg, tr);
                break;
        }

    }

    private static StackTraceElement getTargetStackTraceElement() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();

        if (sts == null) {
            return null;
        }

        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }

            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }

            if (st.getClassName().equals(Logger.class.getName())) {
                continue;
            }

            return st;
        }
        return null;
    }

    public static void isEnabled(boolean isEnabled) {
        Logger.isEnabled = isEnabled;
    }

    public static boolean isEnabled() {
        return isEnabled;
    }

}
