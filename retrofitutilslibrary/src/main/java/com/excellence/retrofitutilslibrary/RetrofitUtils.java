package com.excellence.retrofitutilslibrary;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.excellence.retrofitutilslibrary.interfaces.Error;
import com.excellence.retrofitutilslibrary.interfaces.Success;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/4/7
 *     desc   :
 * </pre>
 */

public class RetrofitUtils
{
	public static final String TAG = RetrofitUtils.class.getSimpleName();

	private RetrofitHttpService mService = null;
	private String mBaseUrl = null;
	private OkHttpClient mClient = null;
	private Map<String, String> mParams = new HashMap<>();
	private Map<String, String> mHeaders = new HashMap<>();

	private RetrofitUtils(RetrofitHttpService service, String baseUrl, OkHttpClient client)
	{
		mService = service;
		mBaseUrl = baseUrl;
		mClient = client;
	}

	public RetrofitHttpService getService()
	{
		return mService;
	}

	public String getBaseUrl()
	{
		return mBaseUrl;
	}

	public OkHttpClient getClient()
	{
		return mClient;
	}

	public static class Builder
	{
		private String mBaseUrl = null;
		private OkHttpClient mClient = null;
		private List<Converter.Factory> mConverterFactories = new ArrayList<>();
		private List<CallAdapter.Factory> mCallAdapterFactories = new ArrayList<>();

		public Builder()
		{
		}

		public Builder baseUrl(@NonNull String baseUrl)
		{
			mBaseUrl = baseUrl;
			return this;
		}

		public Builder client(@NonNull OkHttpClient client)
		{
			mClient = client;
			return this;
		}

		public Builder addConverterFactory(@NonNull Converter.Factory factory)
		{
			mConverterFactories.add(factory);
			return this;
		}

		public Builder addCallAdapterFactory(@NonNull CallAdapter.Factory factory)
		{
			mCallAdapterFactories.add(factory);
			return this;
		}

		public RetrofitUtils build()
		{
			if (TextUtils.isEmpty(mBaseUrl))
				throw new NullPointerException("base url can not be null");
			if (!mBaseUrl.endsWith("/"))
				mBaseUrl += "/";

			if (mClient == null)
			{
				mClient = new OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();
			}

			if (mConverterFactories.isEmpty())
				mConverterFactories.add(ScalarsConverterFactory.create());

			Retrofit.Builder builder = new Retrofit.Builder();
			builder.baseUrl(mBaseUrl);
			builder.client(mClient);
			for (Converter.Factory converterFactory : mConverterFactories)
				builder.addConverterFactory(converterFactory);
			for (CallAdapter.Factory callAdapterFactory : mCallAdapterFactories)
				builder.addCallAdapterFactory(callAdapterFactory);
			Retrofit retrofit = builder.build();
			RetrofitHttpService service = retrofit.create(RetrofitHttpService.class);
			return new RetrofitUtils(service, mBaseUrl, mClient);
		}
	}

	public RetrofitUtils setParam(String key, String value)
	{
		mParams.put(key, value);
		return this;
	}

	public RetrofitUtils setParams(Map<String, String> params)
	{
		mParams.putAll(params);
		return this;
	}

	public RetrofitUtils setHeader(String key, String value)
	{
		mHeaders.put(key, value);
		return this;
	}

	public RetrofitUtils setHeaders(Map<String, String> headers)
	{
		mHeaders.putAll(headers);
		return this;
	}

	public void get(@NonNull String requestUrl, @NonNull final Success successCall, @NonNull final Error errorCall)
	{
		mService.get(requestUrl, checkParams(mParams), checkHeaders(mHeaders)).enqueue(new Callback<String>()
		{
			@Override
			public void onResponse(Call<String> call, Response<String> response)
			{
				if (response.code() == HTTP_OK)
				{
					successCall.success(response.body());
				}
				else
				{
					errorCall.error(response.code(), Utils.inputStream2String(response.errorBody().byteStream()));
				}
			}

			@Override
			public void onFailure(Call<String> call, Throwable t)
			{
				if (!call.isCanceled())
					errorCall.error(0, Utils.printException(t));
			}
		});
	}

	/**
	 * 检验请求参数，不能为空
	 *
	 * @param params 请求参数
	 * @return 请求参数
	 */
	private Map<String, String> checkParams(Map<String, String> params)
	{
		if (params == null)
			params = new HashMap<>();
		for (Map.Entry<String, String> entry : params.entrySet())
		{
			if (entry.getValue() == null)
				params.put(entry.getKey(), "");
		}
		return params;
	}

	private Map<String, String> checkHeaders(Map<String, String> headers)
	{
		if (headers == null)
			headers = new HashMap<>();
		for (Map.Entry<String, String> entry : headers.entrySet())
		{
			if (entry.getValue() == null)
				headers.put(entry.getKey(), "");
		}
		return headers;
	}

	private static class LoggingInterceptor implements Interceptor
	{
		@Override
		public okhttp3.Response intercept(Chain chain) throws IOException
		{
			Request request = chain.request();
			Log.e(request.url().toString(), String.format("发送请求 %s on %s%n%s", request.url(), chain.connection(), request.headers()));
			return chain.proceed(request);
		}
	}
}
