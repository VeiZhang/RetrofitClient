package com.excellence.retrofitutilslibrary;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

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
