package com.excellence.retrofit.interceptor;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/4/10
 *     desc   : 网络请求信息拦截器
 * </pre>
 */

public class LoggingInterceptor implements Interceptor
{
	public static final String TAG = LoggingInterceptor.class.getSimpleName();

	@Override
	public okhttp3.Response intercept(Chain chain) throws IOException
	{
		Request request = chain.request();
		Response response = chain.proceed(request);
		Log.i(TAG, "发送请求 " + request.url());
		Log.i(TAG, "发送请求头 " + request.headers());
		Log.i(TAG, "接收响应 " + response.headers());
		return response;
	}
}
