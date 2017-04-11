package com.excellence.retrofitutils;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.excellence.retrofitutilslibrary.RetrofitUtils;
import com.excellence.retrofitutilslibrary.interfaces.DownloadListener;
import com.excellence.retrofitutilslibrary.interfaces.Error;
import com.excellence.retrofitutilslibrary.interfaces.Success;

public class MainActivity extends AppCompatActivity
{
	private static final String BASE_URL = "http://gank.io/api/";
	private static final String REQUEST_URL = "http://gank.io/api/data/%E7%A6%8F%E5%88%A9/1/0";
	private static final String DOWNLOAD_URL = "http://download.game.yy.com/duowanapp/m/Duowan20140427.apk";
	private static final String QQ_URL = "http://gdown.baidu.com/data/wisegame/dc429998555b7d4d/QQ_398.apk";
	private static final String GAME_URL = "http://andl.guopan.cn/101919-3889-1467370568.apk";

	private RetrofitUtils mRetrofitUtils = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mRetrofitUtils = new RetrofitUtils.Builder(this).baseUrl(BASE_URL).build();
		// get();
		// obGet();
		// download();
		obDownload();
	}

	private void get()
	{
		mRetrofitUtils.setHeader("Cache-Time", "24 * 3600").setTag(this).get(REQUEST_URL, new Success()
		{
			@Override
			public void success(String result)
			{
				System.out.println(result);
			}
		}, new Error()
		{
			@Override
			public void error(Throwable t)
			{
				t.printStackTrace();
			}
		});
	}

	private void obGet()
	{
		mRetrofitUtils.setHeader("Cache-Time", "24 * 3600").setTag(this).obGet(REQUEST_URL, new Success()
		{
			@Override
			public void success(String result)
			{
				System.out.println(result);
			}
		}, new Error()
		{
			@Override
			public void error(Throwable t)
			{
				t.printStackTrace();
			}
		});
	}

	private void download()
	{
		mRetrofitUtils.download(QQ_URL, "/sdcard/QQ.apk", new DownloadListener()
		{
			@Override
			public void onPreExecute(long fileSize)
			{
				super.onPreExecute(fileSize);
				System.out.println("pre :" + fileSize);
			}

			@Override
			public void onProgressChange(long fileSize, long downloadedSize)
			{
				super.onProgressChange(fileSize, downloadedSize);
				// System.out.println(fileSize + ":" + downloadedSize);
			}

			@Override
			public void onCancel()
			{
				super.onCancel();
				System.out.println("cancel");
			}

			@Override
			public void onError(Throwable t)
			{
				super.onError(t);
				t.printStackTrace();
			}

			@Override
			public void onSuccess()
			{
				super.onSuccess();
				System.out.println("success");
			}
		});
	}

	private void obDownload()
	{
		mRetrofitUtils.obDownload(QQ_URL, "/sdcard/QQ.apk", new DownloadListener()
		{
			@Override
			public void onPreExecute(long fileSize)
			{
				super.onPreExecute(fileSize);
				System.out.println("pre :" + fileSize);
			}

			@Override
			public void onProgressChange(long fileSize, long downloadedSize)
			{
				super.onProgressChange(fileSize, downloadedSize);
				System.out.println(fileSize + ":" + downloadedSize);
			}

			@Override
			public void onCancel()
			{
				super.onCancel();
				System.out.println("cancel");
			}

			@Override
			public void onError(Throwable t)
			{
				super.onError(t);
				t.printStackTrace();
			}

			@Override
			public void onSuccess()
			{
				super.onSuccess();
				System.out.println("success");
			}
		});
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		RetrofitUtils.cancel(this);
	}
}
