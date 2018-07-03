package com.excellence.retrofit.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

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
	@SuppressLint("MissingPermission")
	public static boolean isNetworkAvailable(Context context)
	{
		NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isAvailable();
	}

	/**
	 * 检测是否在主线程中
	 */
	public static void checkMainThread()
	{
		if (Looper.myLooper() != Looper.getMainLooper())
		{
			throw new RuntimeException("Client should be main thread!!!");
		}
	}

	public static void checkNULL(Object object, String message)
	{
		if (object == null)
		{
			throw new NullPointerException(message);
		}
	}
}
