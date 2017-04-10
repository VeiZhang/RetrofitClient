package com.excellence.retrofitutilslibrary;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.excellence.retrofitutilslibrary.interfaces.Error;
import com.excellence.retrofitutilslibrary.interfaces.Success;
import com.excellence.retrofitutilslibrary.utils.OkHttpProvider;
import com.excellence.retrofitutilslibrary.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
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
 *     desc   : Retrofit封装
 *     			<ul>
 *     			   <li>get、post请求封装</li>
 *     			   <li>参数可配置</li>
 *     			   <li>异步统一回调接口</li>
 *     			   <li>单个界面所有请求取消</li>
 *     			   <li>缓存策略</li>
 *     			</ul>
 * </pre>
 */

public class RetrofitUtils
{
	public static final String TAG = RetrofitUtils.class.getSimpleName();

	private RetrofitHttpService mService = null;
	private String mBaseUrl = null;
	private OkHttpClient mClient = null;

	/**
	 * 请求参数
	 */
	private Map<String, String> mParams = new HashMap<>();

	/**
	 * 请求头
	 */
	private Map<String, String> mHeaders = new HashMap<>();

	/**
	 * 网络请求队列
	 */
	private static final Map<String, Call> CALL_MAP = new HashMap<>();

	/**
	 * 网络请求标识
	 */
	private Object mTag = null;

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
		private Context mContext = null;
		private String mBaseUrl = null;
		private OkHttpClient mClient = null;
		private List<Converter.Factory> mConverterFactories = new ArrayList<>();
		private List<CallAdapter.Factory> mCallAdapterFactories = new ArrayList<>();

		public Builder(@NonNull Context context)
		{
			mContext = context;
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
				mClient = OkHttpProvider.okHttpClient(mContext);
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

	public void get(@NonNull final String requestUrl, @NonNull final Success successCall, @NonNull final Error errorCall)
	{
		Call call = mService.get(requestUrl, checkParams(mParams), checkHeaders(mHeaders));
		addCall(mTag, requestUrl, call);
		call.enqueue(new Callback<String>()
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
					errorCall.error(new Throwable(Utils.inputStream2String(response.errorBody().byteStream())));
				}
				if (mTag != null)
					removeCall(requestUrl);
			}

			@Override
			public void onFailure(Call<String> call, Throwable t)
			{
				if (!call.isCanceled())
					errorCall.error(t);
				if (mTag != null)
					removeCall(requestUrl);
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

	public RetrofitUtils setTag(Object tag)
	{
		mTag = tag;
		return this;
	}

	/**
	 * 添加网络请求队列，以 tag + url 作为标识
	 *
	 * @param tag 标签
	 * @param url 请求链接
	 * @param call 网络请求
	 */
	private synchronized void addCall(Object tag, String url, Call call)
	{
		if (tag == null)
			return;
		synchronized (CALL_MAP)
		{
			CALL_MAP.put(tag.toString() + url, call);
		}
	}

	/**
	 * 取消单个网络请求
	 *
	 * @param url 请求链接
	 */
	private static synchronized void removeCall(String url)
	{
		synchronized (CALL_MAP)
		{
			for (String key : CALL_MAP.keySet())
			{
				if (key.contains(url))
				{
					url = key;
					break;
				}
			}
			CALL_MAP.remove(url);
		}
	}

	/**
	 * 取消单个界面的所有请求，或取消某个tag的所有请求
	 *
	 * @param tag 标签
	 */
	public static synchronized void cancel(Object tag)
	{
		if (tag == null)
			return;
		synchronized (CALL_MAP)
		{
			for (String key : CALL_MAP.keySet())
			{
				if (key.startsWith(tag.toString()))
				{
					CALL_MAP.get(key).cancel();
					removeCall(key);
				}
			}
		}

	}

}
