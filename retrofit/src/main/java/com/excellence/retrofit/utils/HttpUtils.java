package com.excellence.retrofit.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.excellence.retrofit.utils.Utils.checkNULL;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2018/7/3
 *     desc   :
 * </pre> 
 */
public class HttpUtils {

    private static final String TAG = HttpUtils.class.getSimpleName();

    public enum ResponseType {
        ASYNC,
        SYNC
    }

    /**
     * 打印全部请求头信息
     *
     * @param headers
     */
    public static void printHeader(Headers headers) {
        if (headers == null) {
            Log.i(TAG, "printHeader: header is null");
            return;
        }

        for (String key : headers.names()) {
            Log.i(TAG, "[key : " + key + "][value : " + headers.get(key) + "]");
        }
    }

    public static String checkURL(String url) {
        if (isURLEmpty(url)) {
            throw new NullPointerException("url can not be null");
        }
        return url;
    }

    /**
     * 判断链接是否为空
     *
     * @param url 链接
     * @return {@code true}：空<br>{@code false}：不为空
     */
    public static boolean isURLEmpty(String url) {
        return TextUtils.isEmpty(url) || url.equalsIgnoreCase("null");
    }

    /**
     * 检测请求参数，不能为空
     *
     * @param params 请求参数集合
     * @return 请求参数集合
     */
    public static Map<String, String> checkParams(Map<String, String> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() == null) {
                params.put(entry.getKey(), "");
            }
        }
        return params;
    }

    /**
     * 检测请求头信息，不能为空
     *
     * @param headers 请求头
     * @return 请求头集合
     */
    public static Map<String, String> checkHeaders(Map<String, String> headers) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getValue() == null) {
                headers.put(entry.getKey(), "");
            }
        }
        return headers;
    }

    public static RequestBody createImage(File file) {
        checkNULL(file, "file not null");
        return RequestBody.create(MediaType.parse("image/*"), file);
    }
}
