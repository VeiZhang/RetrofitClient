package com.excellence.retrofit;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.excellence.retrofit.interfaces.IDownloadListener;
import com.excellence.retrofit.interfaces.IListener;
import com.excellence.retrofit.utils.HttpDownloadTask;
import com.google.gson.Gson;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.excellence.retrofit.interceptor.DownloadInterceptor.DOWNLOAD;
import static com.excellence.retrofit.utils.Utils.checkHeaders;
import static com.excellence.retrofit.utils.Utils.checkMainThread;
import static com.excellence.retrofit.utils.Utils.checkParams;
import static com.excellence.retrofit.utils.Utils.checkURL;
import static com.excellence.retrofit.utils.Utils.createImage;
import static com.excellence.retrofit.utils.Utils.inputStream2String;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     date   : 2017/10/19
 *     desc   : 网络请求创建工具
 * </pre>
 */

public class HttpRequest
{
	public static final String TAG = HttpRequest.class.getSimpleName();

	private RetrofitClient mRetrofitClient = null;
	private RetrofitHttpService mHttpService = null;
	private Executor mResponsePoster = null;
	private Object mTag = null;
	private String mUrl = null;
	private Map<String, String> mHeaders = null;
	private Map<String, String> mParams = null;

	protected HttpRequest(Builder builder)
	{
		mTag = builder.mTag;
		mUrl = builder.mUrl;
		mHeaders = builder.mHeaders;
		mParams = builder.mParams;

		mRetrofitClient = RetrofitClient.getInstance();
		mHttpService = mRetrofitClient.getService();
		mResponsePoster = mRetrofitClient.getResponsePoster();
	}

	public static class Builder
	{
		private Object mTag = null;
		private String mUrl = null;
		private Map<String, String> mHeaders = new HashMap<>();
		private Map<String, String> mParams = new HashMap<>();

		/**
		 * 设置网络请求标识，用于取消请求
		 *
		 * @param tag
		 * @return
		 */
		public Builder tag(Object tag)
		{
			mTag = tag;
			return this;
		}

		/**
		 * 请求地址
		 *
		 * @param url
		 * @return
		 */
		public Builder url(String url)
		{
			mUrl = url;
			return this;
		}

		/**
		 * 设置单个请求的请求头
		 *
		 * @param key 键
		 * @param value 键值
		 * @return
		 */
		public Builder header(String key, String value)
		{
			mHeaders.put(key, value);
			return this;
		}

		/**
		 * 设置单个请求的头集合
		 *
		 * @param headers 集合
		 * @return
		 */
		public Builder headers(Map<String, String> headers)
		{
			mHeaders.putAll(headers);
			return this;
		}

		/**
		 * 设置单个请求的参数
		 *
		 * @param key 键
		 * @param value 键值
		 * @return
		 */
		public Builder param(String key, String value)
		{
			mParams.put(key, value);
			return this;
		}

		/**
		 * 设置单个请求的参数集合
		 *
		 * @param params 参数集
		 * @return
		 */
		public Builder params(Map<String, String> params)
		{
			mParams.putAll(params);
			return this;
		}

		public HttpRequest build()
		{
			return new HttpRequest(this);
		}
	}

	/**
	 * Get请求json字符串、json对象
	 *
	 * @param listener 结果回调
	 */
	public <T> void get(final Class<T> type, final IListener<T> listener)
	{
		checkMainThread();
		addRequestInfo();
		Call<String> call = mHttpService.get(checkURL(mUrl), checkParams(mParams), checkHeaders(mHeaders));
		addRequest(call);
		call.enqueue(new Callback<String>()
		{
			@Override
			public void onResponse(Call<String> call, Response<String> response)
			{
				if (response.code() == HTTP_OK)
				{
					if (type != String.class)
					{
						/**
						 * json对象
						 */
						T result = new Gson().fromJson(response.body(), type);
						handleSuccess(listener, result);
					}
					else
						handleSuccess(listener, (T) response.body());
				}
				else
				{
					String errorMsg = inputStream2String(response.errorBody().byteStream());
					if (!TextUtils.isEmpty(errorMsg))
						handleError(listener, new Throwable(errorMsg));
					else
					{
						// 离线时使用缓存出现异常，如果没有上次缓存，出现异常时是没有打印信息的，添加自定义异常信息方便识别
						handleError(listener, new Throwable("There may be no cache data!"));
					}
				}
				removeRequest();
			}

			@Override
			public void onFailure(Call<String> call, Throwable t)
			{
				if (!call.isCanceled())
				{
					handleError(listener, t);
				}
				removeRequest();
			}
		});
	}

	/**
	 * GET请求，默认返回json字符串
	 *
	 * @param listener
	 */
	public void get(IListener<String> listener)
	{
		get(String.class, listener);
	}

	/**
	 * RxJava结合Get，请求json字符串、json对象
	 *
	 * @param listener 结果回调
	 */
	public <T> void obGet(final Class<T> type, final IListener<T> listener)
	{
		checkMainThread();
		addRequestInfo();
		Observable<String> observable = mHttpService.obGet(checkURL(mUrl), checkParams(mParams), checkHeaders(mHeaders));
		Subscription subscription = observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<String>()
		{
			@Override
			public void onNext(String response)
			{
				if (type != String.class)
				{
					/**
					 * json对象
					 */
					T result = new Gson().fromJson(response, type);
					handleSuccess(listener, result);
				}
				else
					handleSuccess(listener, (T) response);
				removeRequest();
			}

			@Override
			public void onCompleted()
			{

			}

			@Override
			public void onError(Throwable e)
			{
				handleError(listener, e);
				removeRequest();
			}
		});
		addRequest(subscription);
	}

	/**
	 * RxJava结合Get，默认返回json字符串
	 *
	 * @param listener
	 */
	public void obGet(IListener<String> listener)
	{
		obGet(String.class, listener);
	}

	/**
	 * POST表单的方式发送键值对：发送{@link #mParams}
	 *
	 * @param type
	 * @param listener
	 * @param <T>
	 */
	public <T> void postForm(final Class<T> type, final IListener<T> listener)
	{
		checkMainThread();
		addRequestInfo();
		Call<String> call = mHttpService.post(checkURL(mUrl), checkParams(mParams));
		addRequest(call);
		call.enqueue(new Callback<String>()
		{
			@Override
			public void onResponse(Call<String> call, Response<String> response)
			{
				if (response.code() == HTTP_OK)
				{
					if (type != String.class)
					{
						/**
						 * json对象
						 */
						T result = new Gson().fromJson(response.body(), type);
						handleSuccess(listener, result);
					}
					else
						handleSuccess(listener, (T) response.body());
				}
				else
				{
					String errorMsg = inputStream2String(response.errorBody().byteStream());
					if (!TextUtils.isEmpty(errorMsg))
						handleError(listener, new Throwable(errorMsg));
					else
					{
						// 离线时使用缓存出现异常，如果没有上次缓存，出现异常时是没有打印信息的，添加自定义异常信息方便识别
						handleError(listener, new Throwable("There may be no cache data!"));
					}
				}
				removeRequest();
			}

			@Override
			public void onFailure(Call<String> call, Throwable t)
			{
				if (!call.isCanceled())
				{
					handleError(listener, t);
				}
				removeRequest();
			}
		});
	}

	/**
	 * POST表单的方式发送键值对：发送{@link #mParams}
	 *
	 * @param listener
	 */
	public void postForm(IListener<String> listener)
	{
		postForm(String.class, listener);
	}

	/**
	 * 下载
	 *
	 * @param path 文件保存路径
	 * @param listener 下载监听
	 */
	public void download(final String path, IDownloadListener listener)
	{
		checkMainThread();
		addRequestInfo();
		// 辨别文件下载、非文件下载的标识，避免下载时使用缓存
		mHeaders.put(DOWNLOAD, DOWNLOAD);
		Call<ResponseBody> call = mHttpService.download(checkURL(mUrl), checkParams(mParams), checkHeaders(mHeaders));
		addRequest(call);
		final HttpDownloadTask downloadTask = new HttpDownloadTask(mResponsePoster, listener);
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
							downloadTask.writeFile(path, response.body());
							removeRequest();
							return null;
						}

					}.execute();
				}
				else
				{
					String errorMsg = inputStream2String(response.errorBody().byteStream());
					downloadTask.onError(new Throwable(errorMsg));
					removeRequest();
				}
			}

			@Override
			public void onFailure(Call<ResponseBody> call, Throwable t)
			{
				if (!call.isCanceled())
				{
					downloadTask.onError(t);
				}
				removeRequest();
			}
		});
	}

	/**
	 * RxJava结合下载
	 *
	 * @param path 文件保存路径
	 * @param listener 下载监听
	 */
	public void obDownload(final String path, IDownloadListener listener)
	{
		checkMainThread();
		addRequestInfo();
		// 辨别文件下载、非文件下载的标识，避免下载时使用缓存
		mHeaders.put(DOWNLOAD, DOWNLOAD);
		Observable<ResponseBody> observable = mHttpService.obDownload(checkURL(mUrl), checkParams(mParams), checkHeaders(mHeaders));
		final HttpDownloadTask downloadTask = new HttpDownloadTask(mResponsePoster, listener);
		Subscription subscription = observable.subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe(new Subscriber<ResponseBody>()
		{
			@Override
			public void onNext(ResponseBody response)
			{
				downloadTask.writeFile(path, response);
				removeRequest();
			}

			@Override
			public void onCompleted()
			{

			}

			@Override
			public void onError(Throwable e)
			{
				downloadTask.onError(e);
				removeRequest();
			}
		});
		addRequest(subscription);
	}

	/**
	 * 上传文件
	 * 
	 * @param fileKey 服务器上传文件对应的参数key
	 * @param file 上传文件
	 * @param type
	 * @param listener
	 * @param <T>
	 */
	public <T> void uploadFile(String fileKey, File file, final Class<T> type, final IListener<T> listener)
	{
		checkMainThread();
		addRequestInfo();
		RequestBody requestImg = createImage(file);
		MultipartBody.Part body = MultipartBody.Part.createFormData(fileKey, file.getName(), requestImg);
		Call<String> call = mHttpService.uploadFile(checkURL(mUrl), checkParams(mParams), body);
		addRequest(call);
		call.enqueue(new Callback<String>()
		{
			@Override
			public void onResponse(Call<String> call, Response<String> response)
			{
				if (response.code() == HTTP_OK)
				{
					if (type != String.class)
					{
						/**
						 * json对象
						 */
						T result = new Gson().fromJson(response.body(), type);
						handleSuccess(listener, result);
					}
					else
						handleSuccess(listener, (T) response.body());
				}
				else
				{
					String errorMsg = inputStream2String(response.errorBody().byteStream());
					if (!TextUtils.isEmpty(errorMsg))
						handleError(listener, new Throwable(errorMsg));
					else
					{
						// 离线时使用缓存出现异常，如果没有上次缓存，出现异常时是没有打印信息的，添加自定义异常信息方便识别
						handleError(listener, new Throwable("There may be no cache data!"));
					}
				}
				removeRequest();
			}

			@Override
			public void onFailure(Call<String> call, Throwable t)
			{
				if (!call.isCanceled())
				{
					handleError(listener, t);
				}
				removeRequest();
			}
		});
	}

	/**
	 * 上传文件
	 *
	 * @param fileKey 服务器上传文件对应的参数key
	 * @param file 上传文件
	 * @param listener
	 */
	public void uploadFile(String fileKey, File file, final IListener<String> listener)
	{
		uploadFile(fileKey, file, String.class, listener);
	}

	/**
	 * 单个请求的头和参数覆盖全局请求的头和参数
	 */
	private void addRequestInfo()
	{
		Map<String, String> headers = new HashMap<>(mRetrofitClient.getHeaders());
		Map<String, String> params = new HashMap<>(mRetrofitClient.getParams());
		headers.putAll(mHeaders);
		params.putAll(mParams);
		mHeaders = headers;
		mParams = params;
	}

	private <T> void handleSuccess(IListener<T> listener, T result)
	{
		if (listener != null)
			listener.onSuccess(result);
	}

	private <T> void handleError(IListener<T> listener, Throwable t)
	{
		if (listener != null)
			listener.onError(t);
	}

	private void addRequest(Object request)
	{
		mRetrofitClient.addRequest(mTag, mUrl, request);
	}

	private void removeRequest()
	{
		mRetrofitClient.removeRequest(mTag, mUrl);
	}
}
