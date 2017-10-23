package com.excellence.retrofit.interceptor;

import android.content.Context;
import android.text.TextUtils;

import com.excellence.retrofit.utils.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static com.excellence.retrofit.utils.Utils.isNetworkAvailable;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/4/8
 *     desc   : 缓存拦截器
 * </pre>
 */

public class CacheInterceptor implements Interceptor
{
	public static final String TAG = CacheInterceptor.class.getSimpleName();
	public static final long DEFAULT_CACHE_TIME = 4 * 7 * 24 * 60 * 60;

	private Context mContext = null;
	private long mCacheTime = 0;
	private long mCacheOnlineTime = 0;

	public CacheInterceptor(Context context)
	{
		this(context, DEFAULT_CACHE_TIME, 0);
	}

	public CacheInterceptor(Context context, long cacheTime, long cacheOnlineTime)
	{
		mContext = context;
		mCacheTime = cacheTime;
		mCacheOnlineTime = cacheOnlineTime;
	}

	@Override
	public Response intercept(Chain chain) throws IOException
	{
		Request request = chain.request();

		if (isNetworkAvailable(mContext))
		{
			Logger.i(TAG, "network is valid");

			Response response = chain.proceed(request);
			/**
			 * 在线缓存，如果服务器支持缓存，则使用服务器的配置；否则使用自定义的在线缓存有效期限
			 */
			String cacheControl = request.header("Cache-Control");
			if (TextUtils.isEmpty(cacheControl))
			{
				/**
				 * 清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除无法生效
				 * max-age设置缓存超时时间，超过该时间则新请求数据，否则使用缓存数据
				 *
				 * 对于长期无变化的数据可以设置；对于实时更新的数据，则设置max-age 为 0
				 */
				return response.newBuilder().removeHeader("Pragma").removeHeader("Cache-Control").header("Cache-Control", "max-age=" + mCacheOnlineTime).build();
			}
			else
				return response;
		}
		else
		{
			Logger.i(TAG, "network is invalid");

			/**
			 * 离线缓存设置有效期限，不使用{@link okhttp3.CacheControl#FORCE_CACHE}，因为它有个很大的默认超时时间{@link Integer.MAX_VALUE}
			 * 新建一个{@link CacheControl}
			 */
			CacheControl cacheControl = new CacheControl.Builder().onlyIfCached().maxStale((int) mCacheTime, TimeUnit.SECONDS).build();
			request = request.newBuilder().cacheControl(cacheControl).build();
			Response response = chain.proceed(request);
			/**
			 * 离线缓存，重新设置请求
			 * max-stale设置缓存策略，及超时策略
			 */
			return response.newBuilder().removeHeader("Pragma").removeHeader("Cache-Control").header("Cache-Control", "public, only-if-cached, max-stale=" + mCacheTime).build();
		}
	}
}
