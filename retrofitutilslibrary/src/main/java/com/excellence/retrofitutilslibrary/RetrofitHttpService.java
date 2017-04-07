package com.excellence.retrofitutilslibrary;

import java.util.Map;

import rx.Observer;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;
import retrofit2.http.QueryMap;
import retrofit2.http.FieldMap;
import retrofit2.http.HeaderMap;

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
	Call<String> get(@Url String url);

	@GET
	Call<String> get(@Url String url, @QueryMap Map<String, String> params, @HeaderMap Map<String, String> time);

	@POST
	Call<String> post(@Url String url, @FieldMap Map<String, String> params, @HeaderMap Map<String, String> time);

	@GET
	Observer<String> obGet(@Url String url, @QueryMap Map<String, String> params, @HeaderMap Map<String, String> time);

	@POST
	Observer<String> obPost(@Url String url, @FieldMap Map<String, String> params, @HeaderMap Map<String, String> time);
}
