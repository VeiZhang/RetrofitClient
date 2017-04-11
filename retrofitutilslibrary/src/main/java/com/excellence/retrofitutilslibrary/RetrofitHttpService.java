package com.excellence.retrofitutilslibrary;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import rx.Observable;
import retrofit2.http.GET;
import retrofit2.http.Url;
import retrofit2.http.QueryMap;
import retrofit2.http.HeaderMap;
import retrofit2.http.Streaming;

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
	@GET
	Call<String> get(@Url String url, @QueryMap Map<String, String> params, @HeaderMap Map<String, String> headers);

	@GET
	Observable<String> obGet(@Url String url, @QueryMap Map<String, String> params, @HeaderMap Map<String, String> headers);

	@Streaming
	@GET
	Call<ResponseBody> download(@Url String url, @QueryMap Map<String, String> params, @HeaderMap Map<String, String> headers);
}
