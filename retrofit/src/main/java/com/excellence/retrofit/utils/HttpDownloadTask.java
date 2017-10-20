package com.excellence.retrofit.utils;

import com.excellence.retrofit.interfaces.IDownloadListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;

import okhttp3.ResponseBody;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     date   : 2017/10/19
 *     desc   : 文件工具类
 * </pre>
 */

public class HttpDownloadTask
{
	private HandleListener mHandleListener = null;
	private Executor mResponsePoster = null;

	public HttpDownloadTask()
	{
		mHandleListener = new HandleListener();
	}

	public void writeFile(String path, ResponseBody response, IDownloadListener listener, Executor responsePoster)
	{
		mHandleListener.setListener(listener);
		mResponsePoster = responsePoster;

		File file = new File(path);
		InputStream in = null;
		OutputStream out = null;
		try
		{
			long fileSize = response.contentLength();
			long downloadedSize = 0;
			int read = 0;
			byte[] fileReader = new byte[1024 * 4];
			mHandleListener.onPreExecute(fileSize);
			in = response.byteStream();
			out = new FileOutputStream(file);
			while (true)
			{
				read = in.read(fileReader);
				if (read == -1)
					break;
				out.write(fileReader, 0, read);
				downloadedSize += read;
				mHandleListener.onProgressChange(fileSize, downloadedSize);
			}
			out.flush();
			mHandleListener.onSuccess();
		}
		catch (Exception e)
		{
			mHandleListener.onError(e);
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

	private class HandleListener implements IDownloadListener
	{
		private IDownloadListener mListener = null;

		public void setListener(IDownloadListener listener)
		{
			mListener = listener;
		}

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
