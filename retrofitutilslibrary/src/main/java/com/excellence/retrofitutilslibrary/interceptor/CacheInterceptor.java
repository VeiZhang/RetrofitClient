package com.excellence.retrofitutilslibrary.interceptor;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.excellence.retrofitutilslibrary.utils.Utils;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/4/8
 *     desc   : 缓存拦截器：Get请求才缓存
 * </pre>
 */

public class CacheInterceptor implements Interceptor
{
	public static final String TAG = CacheInterceptor.class.getSimpleName();

	private Context mContext = null;

	public CacheInterceptor(Context context)
	{
		mContext = context;
	}

	@Override
	public Response intercept(Chain chain) throws IOException
	{
		Request request = chain.request();
		// 自定义缓存超时时间，在请求头信息里设置
		String cache = request.header("Cache-Time");
		if (!TextUtils.isEmpty(cache))
		{
			if (Utils.isNetworkAvailable(mContext))
			{
				Log.e(TAG, "network is valid");
				Response response = chain.proceed(request);
				// 清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除无法生效
				return response.newBuilder().removeHeader("Pragma").removeHeader("Cache-Control").header("Cache-Control", "max-age=" + cache).build();
			}
			else
			{
				Log.e(TAG, "network is invalid");
				// 离线缓存，重新设置请求
				request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
				Response response = chain.proceed(request);
				return response.newBuilder().removeHeader("Pragma").removeHeader("Cache-Control").header("Cache-Control", "public, only-if-cached, max-stale=" + cache).build();
			}
		}
		else
			return chain.proceed(request);
	}
}
