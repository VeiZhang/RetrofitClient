package com.excellence.retrofit;

import static com.excellence.retrofit.interceptor.DownloadInterceptor.DOWNLOAD;
import static com.excellence.retrofit.utils.HttpUtils.checkHeaders;
import static com.excellence.retrofit.utils.HttpUtils.checkParams;
import static com.excellence.retrofit.utils.HttpUtils.checkURL;
import static com.excellence.retrofit.utils.HttpUtils.createImage;
import static com.excellence.retrofit.utils.Utils.inputStream2String;
import static java.net.HttpURLConnection.HTTP_OK;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.excellence.retrofit.interfaces.IListener;
import com.excellence.retrofit.utils.HttpUtils.ResponseType;
import com.google.gson.Gson;

import android.text.TextUtils;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
	private Object mTag = null;
	private String mUrl = null;
	private Map<String, String> mHeaders = null;
	private Map<String, String> mParams = null;
	private boolean isCacheEnable = true;
	private ResponseType mResponseType = ResponseType.ASYNC;

	private HttpRequest(Builder builder)
	{
		mTag = builder.mTag;
		mUrl = builder.mUrl;
		mHeaders = builder.mHeaders;
		mParams = builder.mParams;
		isCacheEnable = builder.isCacheEnable;
		mResponseType = builder.mResponseType;

		mRetrofitClient = RetrofitClient.getInstance();
		mHttpService = mRetrofitClient.getService();
	}

	public static class Builder
	{
		private Object mTag = null;
		private String mUrl = null;
		private Map<String, String> mHeaders = new HashMap<>();
		private Map<String, String> mParams = new HashMap<>();
		private boolean isCacheEnable = true;
		private ResponseType mResponseType = ResponseType.ASYNC;

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

		/**
		 * 单个请求是否使用缓存：默认使用缓存
		 *
		 * @param isCacheEnable
		 * @return
		 */
		public Builder cacheEnable(boolean isCacheEnable)
		{
			this.isCacheEnable = isCacheEnable;
			return this;
		}

		/**
		 * 异步请求：{@link ResponseType#ASYNC}
		 * 同步请求：{@link ResponseType#SYNC}
		 * 默认异步请求
		 *
		 * @param responseType
		 * @return
		 */
		public Builder responseType(ResponseType responseType)
		{
			mResponseType = responseType;
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
	public <T> void get(Type type, IListener<T> listener)
	{
		addRequestInfo();
		Call<String> call = mHttpService.get(checkURL(mUrl), checkParams(mParams), checkHeaders(mHeaders));
		switch (mResponseType)
		{
		case ASYNC:
			handleAsyncTask(type, listener, call);
			break;

		case SYNC:
			handleSyncTask(type, listener, call);
			break;
		}
	}

	/**
	 * 同步请求处理
	 *
	 * @param type
	 * @param listener
	 * @param call
	 * @param <T>
	 */
	private <T> void handleSyncTask(Type type, IListener<T> listener, Call<String> call)
	{
		addRequest(call);
		try
		{
			Response<String> response = call.execute();
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
				{
					handleSuccess(listener, (T) response.body());
				}
			}
			else
			{
				String errorMsg = inputStream2String(response.errorBody().byteStream());
				if (!TextUtils.isEmpty(errorMsg))
				{
					handleError(listener, new Throwable(errorMsg));
				}
				else
				{
					// 离线时使用缓存出现异常，如果没有上次缓存，出现异常时是没有打印信息的，添加自定义异常信息方便识别
					handleError(listener, new Throwable("There may be no cache data!"));
				}
			}
		}
		catch (Exception e)
		{
			handleError(listener, e);
		}
		removeRequest();
	}

	/**
	 * 异步请求处理
	 *
	 * @param type
	 * @param listener
	 * @param call
	 * @param <T>
	 */
	private <T> void handleAsyncTask(final Type type, final IListener<T> listener, Call<String> call)
	{
		addRequest(call);
		call.enqueue(new Callback<String>()
		{
			@Override
			public void onResponse(Call<String> call, Response<String> response)
			{
				try
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
						{
							handleSuccess(listener, (T) response.body());
						}
					}
					else
					{
						String errorMsg = inputStream2String(response.errorBody().byteStream());
						if (!TextUtils.isEmpty(errorMsg))
						{
							handleError(listener, new Throwable(errorMsg));
						}
						else
						{
							// 离线时使用缓存出现异常，如果没有上次缓存，出现异常时是没有打印信息的，添加自定义异常信息方便识别
							handleError(listener, new Throwable("There may be no cache data!"));
						}
					}
				}
				catch (Exception e)
				{
					handleError(listener, e);
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
	public <T> void obGet(final Type type, final IListener<T> listener)
	{
		addRequestInfo();
		Observable<String> observable = mHttpService.obGet(checkURL(mUrl), checkParams(mParams), checkHeaders(mHeaders));
		switch (mResponseType)
		{
		case ASYNC:
			observable = observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
			break;

		case SYNC:
			break;
		}

		Disposable disposable = observable.subscribe(new Consumer<String>()
		{
			@Override
			public void accept(String response) throws Exception
			{
				try
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
					{
						handleSuccess(listener, (T) response);
					}
				}
				catch (Exception e)
				{
					handleError(listener, e);
				}
				removeRequest();
			}
		}, new Consumer<Throwable>()
		{
			@Override
			public void accept(Throwable throwable) throws Exception
			{
				handleError(listener, throwable);
				removeRequest();
			}
		});
		addRequest(disposable);
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
	public <T> void postForm(final Type type, final IListener<T> listener)
	{
		addRequestInfo();
		Call<String> call = mHttpService.post(checkURL(mUrl), checkParams(mParams));
		switch (mResponseType)
		{
		case ASYNC:
			handleAsyncTask(type, listener, call);
			break;

		case SYNC:
			handleSyncTask(type, listener, call);
			break;
		}
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
	 * 上传文件
	 * 
	 * @param fileKey 服务器上传文件对应的参数key
	 * @param file 上传文件
	 * @param type
	 * @param listener
	 * @param <T>
	 */
	public <T> void uploadFile(String fileKey, File file, final Type type, final IListener<T> listener)
	{
		addRequestInfo();
		RequestBody requestImg = createImage(file);
		MultipartBody.Part body = MultipartBody.Part.createFormData(fileKey, file.getName(), requestImg);
		Call<String> call = mHttpService.uploadFile(checkURL(mUrl), checkParams(mParams), body);
		switch (mResponseType)
		{
		case ASYNC:
			handleAsyncTask(type, listener, call);
			break;

		case SYNC:
			handleSyncTask(type, listener, call);
			break;
		}
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
		// 辨别文件下载、非文件下载的标识，避免下载时使用缓存
		if (!isCacheEnable)
		{
			mHeaders.put(DOWNLOAD, DOWNLOAD);
		}
	}

	private <T> void handleSuccess(IListener<T> listener, T result)
	{
		if (listener != null)
		{
			listener.onSuccess(result);
		}
	}

	private <T> void handleError(IListener<T> listener, Throwable t)
	{
		if (listener != null)
		{
			listener.onError(t);
		}
	}

	private void addRequest(Object request)
	{
		RetrofitClient.addRequest(mTag, mUrl, request);
	}

	private void removeRequest()
	{
		RetrofitClient.removeRequest(mTag, mUrl);
	}
}
