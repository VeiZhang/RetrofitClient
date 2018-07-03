package com.excellence.retrofit;

import com.excellence.retrofit.interfaces.IDownloadListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import com.excellence.retrofit.utils.HttpUtils.ResponseType;

import static com.excellence.retrofit.interceptor.DownloadInterceptor.DOWNLOAD;
import static com.excellence.retrofit.utils.HttpUtils.printHeader;
import static com.excellence.retrofit.utils.Utils.checkHeaders;
import static com.excellence.retrofit.utils.Utils.checkParams;
import static com.excellence.retrofit.utils.Utils.checkURL;
import static com.excellence.retrofit.utils.Utils.inputStream2String;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     date   : 2017/10/19
 *     desc   : 文件下载请求
 * </pre>
 */

public class HttpDownloadRequest
{
	private RetrofitClient mRetrofitClient = null;
	private RetrofitHttpService mHttpService = null;
	private Executor mResponsePoster = null;
	private String mUrl = null;
	private String mPath = null;
	private Map<String, String> mHeaders = null;
	private Map<String, String> mParams = null;
	private ResponseType mResponseType = ResponseType.ASYNC;
	private boolean isLog = false;

	private HttpDownloadRequest(Builder builder)
	{
		mUrl = builder.mUrl;
		mPath = builder.mPath;
		mHeaders = builder.mHeaders;
		mParams = builder.mParams;
		mResponseType = builder.mResponseType;
		isLog = builder.isLog;

		mRetrofitClient = RetrofitClient.getInstance();
		mHttpService = mRetrofitClient.getService();
		mResponsePoster = mRetrofitClient.getResponsePoster();
	}

	public static class Builder
	{
		private String mUrl = null;
		private String mPath = null;
		private Map<String, String> mHeaders = new HashMap<>();
		private Map<String, String> mParams = new HashMap<>();
		private ResponseType mResponseType = ResponseType.ASYNC;
		private boolean isLog = false;

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

		public Builder path(String path)
		{
			mPath = path;
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

		/**
		 * 开启头信息打印
		 *
		 * @param isLog
		 * @return
		 */
		public Builder isLog(boolean isLog)
		{
			this.isLog = isLog;
			return this;
		}

		public HttpDownloadRequest build()
		{
			return new HttpDownloadRequest(this);
		}
	}

	/**
	 * 下载
	 *
	 * @param listener 下载监听
	 */
	public void download(IDownloadListener listener)
	{
		addRequestInfo();
		IDownloadListener downloadListener = new HttpDownloadListener(listener);
		Call<ResponseBody> call = mHttpService.download(checkURL(mUrl), checkParams(mParams), checkHeaders(mHeaders));
		switch (mResponseType)
		{
		case ASYNC:
			handleAsyncTask(downloadListener, call);
			break;

		case SYNC:
			handleSyncTask(downloadListener, call);
			break;
		}
	}

	/**
	 * 异步下载处理
	 *
	 * @param listener
	 * @param call
	 */
	private void handleAsyncTask(final IDownloadListener listener, Call<ResponseBody> call)
	{
		call.enqueue(new Callback<ResponseBody>()
		{
			@Override
			public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response)
			{
				if (response.code() == HTTP_OK)
				{
					Observable.create(new Observable.OnSubscribe<Integer>()
					{
						@Override
						public void call(Subscriber<? super Integer> subscriber)
						{
							dynamicTransmission(listener, response);
						}
					}).subscribeOn(Schedulers.io()).subscribe();
				}
				else
				{
					String errorMsg = inputStream2String(response.errorBody().byteStream());
					listener.onError(new Throwable(errorMsg));
				}
			}

			@Override
			public void onFailure(Call<ResponseBody> call, Throwable t)
			{
				if (!call.isCanceled())
				{
					listener.onError(t);
				}
			}
		});
	}

	/**
	 * 同步请求处理
	 *
	 * @param listener
	 * @param call
	 */
	private void handleSyncTask(final IDownloadListener listener, Call<ResponseBody> call)
	{
		try
		{
			Response<ResponseBody> response = call.execute();
			if (response.code() == HTTP_OK)
			{
				dynamicTransmission(listener, response);
			}
			else
			{
				String errorMsg = inputStream2String(response.errorBody().byteStream());
				listener.onError(new Throwable(errorMsg));
			}
		}
		catch (Exception e)
		{
			listener.onError(e);
		}
	}

	/**
	 * RxJava结合下载
	 *
	 * @param listener 下载监听
	 */
	public void obDownload(IDownloadListener listener)
	{
		addRequestInfo();
		final IDownloadListener downloadListener = new HttpDownloadListener(listener);
		Observable<Response<ResponseBody>> observable = mHttpService.obDownload(checkURL(mUrl), checkParams(mParams), checkHeaders(mHeaders));
		switch (mResponseType)
		{
		case ASYNC:
			observable = observable.subscribeOn(Schedulers.io()).observeOn(Schedulers.io());
			break;

		case SYNC:
			break;
		}
		observable.subscribe(new Subscriber<Response<ResponseBody>>()
		{
			@Override
			public void onNext(Response<ResponseBody> response)
			{
				dynamicTransmission(downloadListener, response);
			}

			@Override
			public void onCompleted()
			{

			}

			@Override
			public void onError(Throwable e)
			{
				downloadListener.onError(e);
			}
		});
	}

	private void dynamicTransmission(IDownloadListener listener, Response<ResponseBody> response)
	{
		File file = new File(mPath);
		InputStream in = null;
		OutputStream out = null;
		try
		{
			if (isLog)
			{
				printHeader(response.headers());
			}
			long fileSize = response.body().contentLength();
			listener.onPreExecute(fileSize);

			long downloadedSize = 0;
			int read = 0;
			byte[] fileReader = new byte[1024 * 4];
			in = response.body().byteStream();
			out = new FileOutputStream(file);
			while (true)
			{
				read = in.read(fileReader);
				if (read == -1)
				{
					break;
				}
				out.write(fileReader, 0, read);
				downloadedSize += read;
				listener.onProgressChange(fileSize, downloadedSize);
			}
			out.flush();
			listener.onSuccess();
		}
		catch (Exception e)
		{
			listener.onError(e);
		}
		finally
		{
			try
			{
				if (in != null)
				{
					in.close();
				}
				if (out != null)
				{
					out.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
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
		mHeaders.put(DOWNLOAD, DOWNLOAD);
	}

	private class HttpDownloadListener implements IDownloadListener
	{

		private IDownloadListener mListener = null;

		public HttpDownloadListener(IDownloadListener listener)
		{
			mListener = listener;
		}

		/**
		 * 下载准备，可直接在UI线程更新界面
		 *
		 * @param fileSize 下载文件长度
		 */
		@Override
		public void onPreExecute(final long fileSize)
		{
			if (mListener != null)
			{
				mResponsePoster.execute(new Runnable()
				{
					@Override
					public void run()
					{
						mListener.onPreExecute(fileSize);
					}
				});
			}
		}

		/**
		 * 下载进度刷新，可直接在UI线程更新界面
		 *
		 * @param fileSize 文件长度
		 * @param downloadedSize 下载长度
		 */
		@Override
		public void onProgressChange(final long fileSize, final long downloadedSize)
		{
			if (mListener != null)
			{
				mResponsePoster.execute(new Runnable()
				{
					@Override
					public void run()
					{
						mListener.onProgressChange(fileSize, downloadedSize);
					}
				});
			}
		}

		/**
		 * 取消
		 */
		@Override
		public void onCancel()
		{
			if (mListener != null)
			{
				mResponsePoster.execute(new Runnable()
				{
					@Override
					public void run()
					{
						mListener.onCancel();
					}
				});
			}
		}

		/**
		 * 下载失败，可直接在UI线程更新界面
		 *
		 * @param t
		 */
		@Override
		public void onError(final Throwable t)
		{
			if (mListener != null)
			{
				mResponsePoster.execute(new Runnable()
				{
					@Override
					public void run()
					{
						mListener.onError(t);
					}
				});
			}
		}

		/**
		 * 下载成功，可直接在UI线程更新界面
		 */
		@Override
		public void onSuccess()
		{
			if (mListener != null)
			{
				mResponsePoster.execute(new Runnable()
				{
					@Override
					public void run()
					{
						mListener.onSuccess();
					}
				});
			}
		}
	}
}
