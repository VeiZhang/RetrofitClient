package com.excellence.retrofit;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.excellence.retrofit.interfaces.DownloadListener;
import com.excellence.retrofit.interfaces.IListener;
import com.excellence.retrofit.utils.OkHttpProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import rx.Subscription;

import static com.excellence.retrofit.utils.Utils.checkURL;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/4/7
 *     desc   : Retrofit封装
 *     			<ul>
 *     			   <li>get请求封装</li>
 *     			   <li>请求头和参数统一配置，分开配置</li>
 *     			   <li>异步统一回调接口</li>
 *     			   <li>单个界面所有请求取消</li>
 *     			   <li>缓存策略</li>
 *     			   <li>下载</li>
 *     			</ul>
 * </pre>
 */

public class RetrofitClient
{
	public static final String TAG = RetrofitClient.class.getSimpleName();

	private static RetrofitClient mInstance = null;
	private RetrofitHttpService mService = null;
	private String mBaseUrl = null;
	private OkHttpClient mClient = null;
	/**
	 * 全局请求头
	 */
	private Map<String, String> mHeaders = null;

	/**
	 * 全局请求参数
	 */
	private Map<String, String> mParams = null;

	/**
	 * 网络请求队列
	 */
	private static final Map<String, Object> CALL_MAP = new HashMap<>();

	private Executor mResponsePoster = null;

	public static RetrofitClient getInstance()
	{
		if (mInstance == null)
			throw new RuntimeException("Pls init " + Builder.class.getName());
		return mInstance;
	}

	public RetrofitClient(Builder builder)
	{
		mService = builder.mService;
		mBaseUrl = builder.mBaseUrl;
		mClient = builder.mClient;
		mHeaders = builder.mHeaders;
		mParams = builder.mParams;

		final Handler handler = new Handler(Looper.getMainLooper());
		mResponsePoster = new Executor()
		{
			@Override
			public void execute(@NonNull Runnable command)
			{
				handler.post(command);
			}
		};
	}

	protected RetrofitHttpService getService()
	{
		return mService;
	}

	protected String getBaseUrl()
	{
		return mBaseUrl;
	}

	protected OkHttpClient getClient()
	{
		return mClient;
	}

	protected Map<String, String> getHeaders()
	{
		return mHeaders;
	}

	protected Map<String, String> getParams()
	{
		return mParams;
	}

	protected Executor getResponsePoster()
	{
		return mResponsePoster;
	}

	/**
	 * Get请求字符串数据，推荐链式请求{@link HttpRequest#get}
	 *
	 * @param tag 网络请求标识
	 * @param url 请求链接
	 * @param listener 结果回调
	 */
	@Deprecated
	public void get(final Object tag, @NonNull final String url, final IListener listener)
	{
		new HttpRequest.Builder().tag(tag).url(url).listener(listener).build().get();
	}

	/**
	 * RxJava结合Get请求字符串数据，推荐链式请求{@link HttpRequest#obGet}
	 *
	 * @param tag 网络请求标识
	 * @param url 请求链接
	 * @param listener 结果回调
	 */
	@Deprecated
	public void obGet(final Object tag, @NonNull final String url, final IListener listener)
	{
		new HttpRequest.Builder().tag(tag).url(url).listener(listener).build().obGet();
	}

	/**
	 * 下载，推荐链式请求{@link HttpRequest#download}
	 *
	 * @param tag 网络请求标识
	 * @param url 请求链接
	 * @param path 文件保存地址
	 * @param listener 下载监听
	 */
	@Deprecated
	public void download(final Object tag, @NonNull final String url, @NonNull final String path, @NonNull final DownloadListener listener)
	{
		new HttpRequest.Builder().tag(tag).url(url).path(path).downloadListener(listener).build().download();
	}

	/**
	 * RxJava结合下载，推荐链式请求{@link HttpRequest#obDownload}
	 *
	 * @param tag 网络请求标识
	 * @param url 请求链接
	 * @param path 文件保存地址
	 * @param listener 下载监听
	 */
	@Deprecated
	public void obDownload(final Object tag, @NonNull final String url, @NonNull final String path, @NonNull final DownloadListener listener)
	{
		new HttpRequest.Builder().tag(tag).url(url).path(path).downloadListener(listener).build().obDownload();
	}

	/**
	 * 添加网络请求队列，以 tag + url 作为标识
	 *
	 * @param tag 标签
	 * @param url 请求链接
	 * @param request 网络请求
	 */
	protected synchronized void addCall(Object tag, String url, Object request)
	{
		if (tag == null)
			return;
		synchronized (CALL_MAP)
		{
			CALL_MAP.put(tag.toString() + url, request);
		}
	}

	/**
	 * 根据tag + url 标识删除队列里的完成、错误的某个网络请求
	 *
	 * @param tag 标签
	 * @param url 请求链接
	 */
	protected synchronized void removeCall(Object tag, String url)
	{
		if (tag == null)
			return;
		synchronized (CALL_MAP)
		{
			String key = tag.toString() + url;
			if (CALL_MAP.containsKey(key))
				CALL_MAP.remove(key);
		}
	}

	/**
	 * 取消单个界面的所有请求，或取消某个tag的所有请求
	 * 如果想取消单个请求，请传入 标识：tag.toString() + url
	 *
	 * 遍历删除，使用迭代
	 *
	 * @param tag 标签
	 */
	public synchronized void cancel(Object tag)
	{
		if (tag == null)
			return;
		synchronized (CALL_MAP)
		{
			for (Iterator<String> iterator = CALL_MAP.keySet().iterator(); iterator.hasNext();)
			{
				String key = iterator.next();
				if (key.startsWith(tag.toString()))
				{
					cancel(key);
					iterator.remove();
				}
			}
		}
	}

	/**
	 * 取消所有网络请求
	 */
	public synchronized void cancelAll()
	{
		for (String key : CALL_MAP.keySet())
			cancel(key);
		CALL_MAP.clear();
	}

	/**
	 * 取消单个网络请求
	 *
	 * @param key 请求标识：tag.toString() + url
	 */
	private void cancel(String key)
	{
		Object request = CALL_MAP.get(key);
		if (request instanceof Call)
		{
			if (!((Call) request).isCanceled())
				((Call) request).cancel();
		}
		else if (request instanceof Subscription)
		{
			if (!((Subscription) request).isUnsubscribed())
				((Subscription) request).unsubscribe();
		}
	}

	/**
	 * 使用该方式创建{@link #RetrofitClient}的单例
	 */
	public static class Builder
	{
		private Context mContext = null;
		private String mBaseUrl = null;
		private OkHttpClient mClient = null;
		private RetrofitHttpService mService = null;
		private List<Converter.Factory> mConverterFactories = new ArrayList<>();
		private List<CallAdapter.Factory> mCallAdapterFactories = new ArrayList<>();
		/**
		 * 配置全局请求头
		 */
		private Map<String, String> mHeaders = new HashMap<>();

		/**
		 * 配置全局请求参数
		 */
		private Map<String, String> mParams = new HashMap<>();

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

		/**
		 * 设置统一请求头
		 *
		 * @param key 键
		 * @param value 键值
		 * @return
		 */
		public Builder setHeader(String key, String value)
		{
			mHeaders.put(key, value);
			return this;
		}

		/**
		 * 设置统一请求头集合
		 *
		 * @param headers 集合
		 * @return
		 */
		public Builder setHeaders(Map<String, String> headers)
		{
			mHeaders.putAll(headers);
			return this;
		}

		/**
		 * 设置统一请求参数
		 *
		 * @param key 键
		 * @param value 键值
		 * @return
		 */
		public Builder setParam(String key, String value)
		{
			mParams.put(key, value);
			return this;
		}

		/**
		 * 设置统一请求参数集合
		 *
		 * @param params 参数集
		 * @return
		 */
		public Builder setParams(Map<String, String> params)
		{
			mParams.putAll(params);
			return this;
		}

		/**
		 * 只执行一次，单例
		 *
		 * @return
		 */
		public RetrofitClient build()
		{
			if (mInstance != null)
				return mInstance;

			checkURL(mBaseUrl);
			if (!mBaseUrl.endsWith("/"))
				mBaseUrl += "/";

			if (mClient == null)
			{
				mClient = OkHttpProvider.okHttpClient(mContext);
			}

			if (mConverterFactories.isEmpty())
			{
				// 默认支持字符串数据
				mConverterFactories.add(ScalarsConverterFactory.create());
			}

			if (mCallAdapterFactories.isEmpty())
			{
				// 默认支持RxJava
				mCallAdapterFactories.add(RxJavaCallAdapterFactory.create());
			}

			Retrofit.Builder builder = new Retrofit.Builder();
			builder.baseUrl(mBaseUrl);
			builder.client(mClient);
			for (Converter.Factory converterFactory : mConverterFactories)
				builder.addConverterFactory(converterFactory);
			for (CallAdapter.Factory callAdapterFactory : mCallAdapterFactories)
				builder.addCallAdapterFactory(callAdapterFactory);
			Retrofit retrofit = builder.build();
			mService = retrofit.create(RetrofitHttpService.class);
			mInstance = new RetrofitClient(this);
			return mInstance;
		}
	}
}
