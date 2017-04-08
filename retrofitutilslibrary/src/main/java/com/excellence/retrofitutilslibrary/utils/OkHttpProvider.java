package com.excellence.retrofitutilslibrary.utils;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/4/8
 *     desc   : 默认封装的OkHttpClient配置
 * </pre>
 */

public class OkHttpProvider
{
	private static OkHttpClient mOkHttpClient = null;

	public static OkHttpClient okHttpClient()
	{
		if (mOkHttpClient == null)
			synchronized (OkHttpProvider.class)
			{
				if (mOkHttpClient == null)
					mOkHttpClient = new OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();
			}
		return mOkHttpClient;
	}

	private static class LoggingInterceptor implements Interceptor
	{
		@Override
		public okhttp3.Response intercept(Chain chain) throws IOException
		{
			Request request = chain.request();
			Log.e(request.url().toString(), String.format("发送请求 %s on %s%n%s", request.url(), chain.connection(), request.headers()));
			return chain.proceed(request);
		}
	}
}
