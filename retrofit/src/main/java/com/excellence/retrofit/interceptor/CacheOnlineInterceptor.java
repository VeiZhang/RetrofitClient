package com.excellence.retrofit.interceptor;

import android.text.TextUtils;

import com.excellence.retrofit.utils.Logger;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static com.excellence.retrofit.interceptor.CacheInterceptor.HEADER_CACHE_CONTROL;
import static com.excellence.retrofit.interceptor.CacheInterceptor.HEADER_PRAGMA;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     date   : 2017/10/23
 *     desc   : 在线缓存拦截器
 * </pre>
 */

public class CacheOnlineInterceptor implements Interceptor
{
	public static final String TAG = CacheOnlineInterceptor.class.getSimpleName();

	private long mCacheOnlineTime = 0;

	public CacheOnlineInterceptor()
	{
		this(0);
	}

	public CacheOnlineInterceptor(long cacheOnlineTime)
	{
		mCacheOnlineTime = cacheOnlineTime;
	}

	@Override
	public Response intercept(Chain chain) throws IOException
	{
		Logger.i(TAG, "network is valid");

		Request request = chain.request();
		Response response = chain.proceed(request);
		/**
		 * 在线缓存，如果服务器支持缓存，则使用服务器的配置；否则使用自定义的在线缓存有效期限
		 */
		String cacheControl = request.header(HEADER_CACHE_CONTROL);
		if (TextUtils.isEmpty(cacheControl))
		{
			/**
			 * 清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除无法生效
			 * max-age设置缓存超时时间，超过该时间则新请求数据，否则使用缓存数据
			 *
			 * 对于长期无变化的数据可以设置；对于实时更新的数据，则设置max-age 为 0
			 */
			return response.newBuilder().removeHeader(HEADER_PRAGMA).removeHeader(HEADER_CACHE_CONTROL).header(HEADER_CACHE_CONTROL, "max-age=" + mCacheOnlineTime).build();
		}
		else
		{
			return response;
		}
	}
}
