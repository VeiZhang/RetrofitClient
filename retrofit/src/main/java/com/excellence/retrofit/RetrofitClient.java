package com.excellence.retrofit;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.excellence.retrofit.interfaces.DownloadListener;
import com.excellence.retrofit.interfaces.IListener;
import com.excellence.retrofit.utils.OkHttpProvider;
import com.excellence.retrofit.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.excellence.retrofit.interceptor.DownloadInterceptor.DOWNLOAD;
import static com.excellence.retrofit.utils.Utils.checkHeaders;
import static com.excellence.retrofit.utils.Utils.checkParams;
import static com.excellence.retrofit.utils.Utils.checkURL;
import static java.net.HttpURLConnection.HTTP_OK;

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

	private RetrofitClient(RetrofitHttpService service, String baseUrl, Map<String, String> headers, Map<String, String> params, OkHttpClient client)
	{
		mService = service;
		mBaseUrl = baseUrl;
		mClient = client;
		mHeaders = headers;
		mParams = params;

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

	/**
	 * Get请求字符串数据
	 *
	 * @param tag 网络请求标识
	 * @param url 请求链接
	 * @param listener 结果回调
	 */
	public void get(final Object tag, @NonNull final String url, final IListener listener)
	{
		Call<String> call = mService.get(checkURL(url), checkParams(mParams), checkHeaders(mHeaders));
		addCall(tag, url, call);
		call.enqueue(new Callback<String>()
		{
			@Override
			public void onResponse(Call<String> call, Response<String> response)
			{
				if (response.code() == HTTP_OK)
				{
					handleSuccess(listener, response.body());
				}
				else
				{
					String errorMsg = Utils.inputStream2String(response.errorBody().byteStream());
					if (!TextUtils.isEmpty(errorMsg))
						handleError(listener, new Throwable(errorMsg));
					else
					{
						// 离线时使用缓存出现异常，如果没有上次缓存，出现异常时是没有打印信息的，添加自定义异常信息方便识别
						handleError(listener, new Throwable("There may be no cache data!"));
					}
				}
				removeCall(tag, url);
			}

			@Override
			public void onFailure(Call<String> call, Throwable t)
			{
				if (!call.isCanceled())
				{
					handleError(listener, t);
				}
				removeCall(tag, url);
			}
		});
	}

	private void handleSuccess(IListener listener, String result)
	{
		if (listener != null)
			listener.onSuccess(result);
	}

	private void handleError(IListener listener, Throwable t)
	{
		if (listener != null)
			listener.onError(t);
	}

	/**
	 * RxJava结合Get请求字符串数据
	 *
	 * @param tag 网络请求标识
	 * @param url 请求链接
	 * @param listener 结果回调
	 */
	public void obGet(final Object tag, @NonNull final String url, final IListener listener)
	{
		Observable<String> observable = mService.obGet(checkURL(url), checkParams(mParams), checkHeaders(mHeaders));
		Subscription subscription = observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<String>()
		{
			@Override
			public void onNext(String result)
			{
				handleSuccess(listener, result);
				removeCall(tag, url);
			}

			@Override
			public void onCompleted()
			{

			}

			@Override
			public void onError(Throwable e)
			{
				handleError(listener, e);
				removeCall(tag, url);
			}
		});
		addCall(tag, url, subscription);
	}

	/**
	 * 下载
	 *
	 * @param tag 网络请求标识
	 * @param url 请求链接
	 * @param path 文件保存地址
	 * @param listener 下载监听
	 */
	public void download(final Object tag, @NonNull final String url, @NonNull final String path, @NonNull final DownloadListener listener)
	{
		// 辨别文件下载、非文件下载的标识，避免下载时使用缓存
		mHeaders.put(DOWNLOAD, DOWNLOAD);
		Call<ResponseBody> call = mService.download(checkURL(url), checkParams(mParams), checkHeaders(mHeaders));
		addCall(tag, url, call);
		call.enqueue(new Callback<ResponseBody>()
		{
			@Override
			public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response)
			{
				if (response.code() == HTTP_OK)
				{
					new AsyncTask<Void, Long, Void>()
					{
						@Override
						protected Void doInBackground(Void... params)
						{
							writetoFile(listener, path, response.body());
							removeCall(tag, url);
							return null;
						}

					}.execute();
				}
				else
				{
					String errorMsg = Utils.inputStream2String(response.errorBody().byteStream());
					listener.onError(new Throwable(errorMsg));
					removeCall(tag, url);
				}
			}

			@Override
			public void onFailure(Call<ResponseBody> call, Throwable t)
			{
				if (!call.isCanceled())
				{
					listener.onError(t);
				}
				removeCall(tag, url);
			}
		});
	}

	/**
	 * RxJava结合下载
	 * 
	 * @param tag 网络请求标识
	 * @param url 请求链接
	 * @param path 文件保存地址
	 * @param listener 下载监听
	 */
	public void obDownload(final Object tag, @NonNull final String url, @NonNull final String path, @NonNull final DownloadListener listener)
	{
		// 辨别文件下载、非文件下载的标识，避免下载时使用缓存
		mHeaders.put(DOWNLOAD, DOWNLOAD);
		Observable<ResponseBody> observable = mService.obDownload(checkURL(url), checkParams(mParams), checkHeaders(mHeaders));
		Subscription subscription = observable.subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe(new Subscriber<ResponseBody>()
		{
			@Override
			public void onNext(ResponseBody response)
			{
				writetoFile(listener, path, response);
				removeCall(tag, url);
			}

			@Override
			public void onCompleted()
			{

			}

			@Override
			public void onError(Throwable e)
			{
				listener.onError(e);
				removeCall(tag, url);
			}
		});
		addCall(tag, url, subscription);
	}

	/**
	 * 写入流文件
	 *
	 * @param listener 下载监听
	 * @param path 文件保存路径
	 * @param response 文件流信息
	 */
	private void writetoFile(DownloadListener listener, String path, ResponseBody response)
	{
		File file = new File(path);
		InputStream in = null;
		OutputStream out = null;
		try
		{
			long fileSize = response.contentLength();
			long downloadedSize = 0;
			int read = 0;
			byte[] fileReader = new byte[1024 * 4];
			onPreExecute(listener, fileSize);
			in = response.byteStream();
			out = new FileOutputStream(file);
			while (true)
			{
				read = in.read(fileReader);
				if (read == -1)
					break;
				out.write(fileReader, 0, read);
				downloadedSize += read;
				onProgressChange(listener, fileSize, downloadedSize);
			}
			out.flush();
			onSuccess(listener);
		}
		catch (Exception e)
		{
			onError(listener, e);
		}
		finally
		{
			try
			{
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * 下载准备，可直接在UI线程更新界面
	 *
	 * @param listener 下载监听
	 * @param fileSize 文件总长度
	 */
	private void onPreExecute(final DownloadListener listener, final long fileSize)
	{
		mResponsePoster.execute(new Runnable()
		{
			@Override
			public void run()
			{
				listener.onPreExecute(fileSize);
			}
		});
	}

	/**
	 * 下载进度刷新，可直接在UI线程更新界面
	 *
	 * @param listener 下载监听
	 * @param fileSize 文件总长度
	 * @param downloadedSize 下载长度
	 */
	private void onProgressChange(final DownloadListener listener, final long fileSize, final long downloadedSize)
	{
		mResponsePoster.execute(new Runnable()
		{
			@Override
			public void run()
			{
				listener.onProgressChange(fileSize, downloadedSize);
			}
		});
	}

	/**
	 * 下载成功，可直接在UI线程更新界面
	 *
	 * @param listener 下载监听
	 */
	private void onSuccess(final DownloadListener listener)
	{
		mResponsePoster.execute(new Runnable()
		{
			@Override
			public void run()
			{
				listener.onSuccess();
			}
		});
	}

	/**
	 * 下载失败，可直接在UI线程更新界面
	 *
	 * @param listener 下载监听
	 * @param e 异常信息
	 */
	private void onError(final DownloadListener listener, final Exception e)
	{
		mResponsePoster.execute(new Runnable()
		{
			@Override
			public void run()
			{
				listener.onError(e);
			}
		});
	}

	/**
	 * 添加网络请求队列，以 tag + url 作为标识
	 *
	 * @param tag 标签
	 * @param url 请求链接
	 * @param request 网络请求
	 */
	private synchronized void addCall(Object tag, String url, Object request)
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
	private static synchronized void removeCall(Object tag, String url)
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
	 * 执行{@link #cancelAll}、{@link #cancel}时，删除队列里的某个网络请求
	 * 注意{@link #cancelAll}，由于遍历删除，使用迭代
	 *
	 * @param request 网络请求{@link Call}、{@link Subscription}
	 */
	private static synchronized void removeCall(Object request)
	{
		if (request == null)
			return;
		synchronized (CALL_MAP)
		{
			if (CALL_MAP.containsValue(request))
			{
				Collection<Object> requests = CALL_MAP.values();
				requests.remove(request);
			}
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
					cancel(key);
				}
			}
		}
	}

	/**
	 * 取消所有网络请求
	 */
	public static synchronized void cancelAll()
	{
		for (String key : CALL_MAP.keySet())
		{
			cancel(key);
		}
	}

	/**
	 * 取消单个网络请求
	 *
	 * @param key 请求标识：tag.toString() + url
	 */
	private static void cancel(String key)
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

		removeCall(request);
	}

	/**
	 * 使用该方式创建{@link #RetrofitClient}的单例
	 */
	public static class Builder
	{
		private Context mContext = null;
		private String mBaseUrl = null;
		private OkHttpClient mClient = null;
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
			RetrofitHttpService service = retrofit.create(RetrofitHttpService.class);
			mInstance = new RetrofitClient(service, mBaseUrl, mHeaders, mParams, mClient);
			return mInstance;
		}
	}
}
