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

public class HttpDownloadTask implements IDownloadListener
{
	private Executor mResponsePoster = null;
	private IDownloadListener mListener = null;

	public HttpDownloadTask(Executor responsePoster, IDownloadListener listener)
	{
		mResponsePoster = responsePoster;
		mListener = listener;
	}

	public void writeFile(String path, ResponseBody response)
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
			onPreExecute(fileSize);
			in = response.byteStream();
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
				onProgressChange(fileSize, downloadedSize);
			}
			out.flush();
			onSuccess();
		}
		catch (Exception e)
		{
			onError(e);
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
