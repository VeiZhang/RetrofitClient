package com.excellence.retrofit.interceptor;

import com.excellence.retrofit.utils.Logger;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static com.excellence.retrofit.utils.Utils.isURLEmpty;

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
	public static final String TAG = DownloadInterceptor.class.getSimpleName();

	public static final String DOWNLOAD = "RetrofitDownload";

	@Override
	public Response intercept(Chain chain) throws IOException
	{
		Request request = chain.request();
		if (!isURLEmpty(request.header(DOWNLOAD)))
		{
            Logger.i(TAG, "下载文件");
			/**
			 * 文件下载，重新构造网络请求，强制使用网络请求，避免缓存
			 * 服务器支持缓存时，还是可以下载到缓存目录中，如何不缓存，因为占用大多缓存空间
			 */
			HttpUrl httpUrl = request.url();
			Request newRequest = request.newBuilder().url(httpUrl).removeHeader("Cache-Control").removeHeader("Cache-Time").cacheControl(CacheControl.FORCE_NETWORK).build();
			return chain.proceed(newRequest);
		}
		// 非文件下载，继续请求
		return chain.proceed(request);
	}
}
