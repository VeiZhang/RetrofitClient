package com.excellence.retrofitutilslibrary.utils;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/4/10
 *     desc   : 下载文件拦截器
 *              检测是否是文件下载，文件下载则清除头信息，重新构造请求
 * </pre>
 */

public class DownloadInterceptor implements Interceptor
{
	public static final String DOWNLOAD = "RetrofitDownload";

	@Override
	public Response intercept(Chain chain) throws IOException
	{
		Request request = chain.request();
		if (!Utils.isURLEmpty(request.header(DOWNLOAD)))
		{
			// 文件下载，重新构造网络请求，避免缓存
            HttpUrl httpUrl = request.url();
			Request newRequest = request.newBuilder().url(httpUrl).build();
			return chain.proceed(newRequest);
		}
		// 非文件下载，继续请求
		return chain.proceed(request);
	}
}
