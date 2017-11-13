package com.excellence.retrofit.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/4/7
 *     desc   : 工具类
 * </pre>
 */

public class Utils
{
	/**
	 * 输入流转字符串
	 *
	 * @param inputStream 输入流
	 * @return 字符串
	 */
	public static String inputStream2String(@NonNull InputStream inputStream)
	{
		StringBuilder result = new StringBuilder();
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				result.append(line);
			}
			reader.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result.toString();
	}

	/**
	 * 检测网络是否可用
	 *
	 * @param context 上下文
	 * @return {@code true}：可用<br>{@code false}：不可用
	 */
	public static boolean isNetworkAvailable(Context context)
	{
		NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isAvailable();
	}

	public static String checkURL(String url)
	{
		if (isURLEmpty(url))
			throw new NullPointerException("url can not be null");
		return url;
	}

	/**
	 * 判断链接是否为空
	 *
	 * @param url 链接
	 * @return {@code true}：空<br>{@code false}：不为空
	 */
	public static boolean isURLEmpty(String url)
	{
		return TextUtils.isEmpty(url) || url.equalsIgnoreCase("null");
	}

	/**
	 * 检测请求参数，不能为空
	 *
	 * @param params 请求参数集合
	 * @return 请求参数集合
	 */
	public static Map<String, String> checkParams(Map<String, String> params)
	{
		if (params == null)
			params = new HashMap<>();
		for (Map.Entry<String, String> entry : params.entrySet())
		{
			if (entry.getValue() == null)
				params.put(entry.getKey(), "");
		}
		return params;
	}

	/**
	 * 检测请求头信息，不能为空
	 *
	 * @param headers 请求头
	 * @return 请求头集合
	 */
	public static Map<String, String> checkHeaders(Map<String, String> headers)
	{
		if (headers == null)
			headers = new HashMap<>();
		for (Map.Entry<String, String> entry : headers.entrySet())
		{
			if (entry.getValue() == null)
				headers.put(entry.getKey(), "");
		}
		return headers;
	}

	/**
	 * 检测是否在主线程中
	 */
	public static void checkMainThread()
	{
		if (Looper.myLooper() != Looper.getMainLooper())
			throw new RuntimeException("Client should be main thread!!!");
	}

	public static RequestBody createImage(File file)
	{
		checkNotNULL(file, "file not null");
		return RequestBody.create(MediaType.parse("image/*"), file);
	}

	private static void checkNotNULL(Object object, String message)
	{
		if (object == null)
			throw new NullPointerException(message);
	}
}
