package com.excellence.retrofitutilslibrary.utils;

import android.content.Context;
import java.util.concurrent.TimeUnit;
import okhttp3.Cache;
import okhttp3.OkHttpClient;

import com.excellence.retrofitutilslibrary.interceptor.CacheInterceptor;
import com.excellence.retrofitutilslibrary.interceptor.DownloadInterceptor;
import com.excellence.retrofitutilslibrary.interceptor.LoggingInterceptor;

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
	private static final int CACHE_MAX_SIZE = 10 * 1024 * 1024;
	private static final int DEFAULT_CONNECT_TIME = 5;
	private static final int DEFAULT_READ_TIME = 10;
	private static final int DEFAULT_WRITE_TIME = 10;

	private static OkHttpClient mOkHttpClient = null;

	public static OkHttpClient okHttpClient(Context context)
	{
		if (mOkHttpClient == null)
			synchronized (OkHttpProvider.class)
			{
				if (mOkHttpClient == null)
				{
					/** 默认缓存位置/sdcard/Android/data/YourPackageName/cache/ **/
					CacheInterceptor cacheInterceptor = new CacheInterceptor(context);
					/**
					 * {@link okhttp3.OkHttpClient.Builder#addInterceptor(Interceptor)}
					 * {@link okhttp3.OkHttpClient.Builder#addNetworkInterceptor(Interceptor)}
					 * 需要同时添加 {@link CacheInterceptor} 缓存拦截器，在离线情况下才能读取缓存数据，
					 * 只添加其中一个拦截器则不能读取，并且会出现异常
					 **/
					mOkHttpClient = new OkHttpClient.Builder().
							addInterceptor(new LoggingInterceptor()).
							addInterceptor(new DownloadInterceptor()).
							addInterceptor(cacheInterceptor).
							addNetworkInterceptor(cacheInterceptor).
							cache(new Cache(context.getExternalCacheDir(), CACHE_MAX_SIZE)).
                            retryOnConnectionFailure(true).
                            connectTimeout(DEFAULT_CONNECT_TIME, TimeUnit.SECONDS).
							readTimeout(DEFAULT_READ_TIME, TimeUnit.SECONDS).
							writeTimeout(DEFAULT_WRITE_TIME, TimeUnit.SECONDS).
							build();
				}
			}
		return mOkHttpClient;
	}

}
