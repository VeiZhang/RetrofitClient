package com.excellence.retrofit.utils;

import android.util.Log;

import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Headers;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2018/7/3
 *     desc   :
 * </pre> 
 */
public class HttpUtils
{
	public static final String TAG = HttpUtils.class.getSimpleName();

	public enum ResponseType
	{
		ASYNC, SYNC
	}

	/**
	 * 打印全部请求头信息
	 *
	 * @param headers
	 */
	public static void printHeader(Headers headers)
	{
		if (headers == null)
		{
			Log.i(TAG, "printHeader: header is null");
			return;
		}

		for (String key : headers.names())
		{
			Log.i(TAG, "[key : " + key + "][value : " + headers.get(key) + "]");
		}
	}
}
