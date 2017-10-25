package com.excellence.retrofit;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/4/7
 *     desc   :
 * </pre>
 */

public interface RetrofitHttpService
{
	/**
	 * GET请求
	 *
	 * @param url
	 * @param params
	 * @param headers
	 * @return
	 */
	@GET
	Call<String> get(@Url String url, @QueryMap Map<String, String> params, @HeaderMap Map<String, String> headers);

	/**
	 * RxJava + GET请求
	 *
	 * @param url
	 * @param params
	 * @param headers
	 * @return
	 */
	@GET
	Observable<String> obGet(@Url String url, @QueryMap Map<String, String> params, @HeaderMap Map<String, String> headers);

	/**
	 * POST表单的方式发送键值对
	 *
	 * @param url
	 * @param params
	 * @return
	 */
	@FormUrlEncoded
	@POST
	Call<String> post(@Url String url, @FieldMap Map<String, String> params);

	/**
	 * POST发送json/xml
	 *
	 * @param url
	 * @param object
	 * @return
	 */
	@POST
	Call<String> post(@Url String url, @Body Object object);

	/**
	 * POST发送json/xml
	 *
	 * @param url
	 * @param requestBody
	 * @return
	 */
	@POST
	Call<String> post(@Url String url, @Body RequestBody requestBody);

	/**
	 * GET下载
	 *
	 * @param url
	 * @param params
	 * @param headers
	 * @return
	 */
	@Streaming
	@GET
	Call<ResponseBody> download(@Url String url, @QueryMap Map<String, String> params, @HeaderMap Map<String, String> headers);

	/**
	 * RxJava + GET下载
	 *
	 * @param url
	 * @param params
	 * @param headers
	 * @return
	 */
	@Streaming
	@GET
	Observable<ResponseBody> obDownload(@Url String url, @QueryMap Map<String, String> params, @HeaderMap Map<String, String> headers);
}
