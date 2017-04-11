package com.excellence.retrofitutils;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.excellence.retrofitutilslibrary.interfaces.DownloadListener;

import java.io.File;

public class DownloadActivity extends BaseActivity implements View.OnClickListener
{

	private ProgressBar mDownloadProgress = null;
	private ProgressBar mObDownloadProgress = null;
	private Button mDownloadBtn = null;
	private Button mObDownloadBtn = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);

		mDownloadProgress = (ProgressBar) findViewById(R.id.download_progress);
		mObDownloadProgress = (ProgressBar) findViewById(R.id.obdownload_progress);
		mDownloadBtn = (Button) findViewById(R.id.download_btn);
		mObDownloadBtn = (Button) findViewById(R.id.obdownload_btn);

		mDownloadBtn.setOnClickListener(this);
		mObDownloadBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.download_btn:
			download();
			break;

		case R.id.obdownload_btn:
			obDownload();
			break;
		}
	}

	private void download()
	{
		mRetrofitUtils.download(DOWNLOAD_URL, Environment.getExternalStorageDirectory() + File.separator + "Duowan.apk", new DownloadListener()
		{
			@Override
			public void onPreExecute(long fileSize)
			{
				super.onPreExecute(fileSize);
				mDownloadBtn.setVisibility(View.INVISIBLE);
				mDownloadBtn.setText(R.string.start);
				mDownloadProgress.setMax((int) fileSize);
			}

			@Override
			public void onProgressChange(long fileSize, long downloadedSize)
			{
				super.onProgressChange(fileSize, downloadedSize);
				mDownloadProgress.setProgress((int) downloadedSize);
			}

			@Override
			public void onCancel()
			{
				super.onCancel();
			}

			@Override
			public void onError(Throwable t)
			{
				super.onError(t);
				mDownloadBtn.setVisibility(View.VISIBLE);
				mDownloadBtn.setText(R.string.failed);
				t.printStackTrace();
			}

			@Override
			public void onSuccess()
			{
				super.onSuccess();
				mDownloadBtn.setVisibility(View.VISIBLE);
				mDownloadBtn.setText(R.string.success);
			}
		});
	}

	/**
	 * 下载监听接口可选
	 */
	private void obDownload()
	{
		mRetrofitUtils.obDownload(DOWNLOAD_URL1, Environment.getExternalStorageDirectory() + File.separator + "Game.apk", new DownloadListener()
		{
			@Override
			public void onPreExecute(long fileSize)
			{
				super.onPreExecute(fileSize);
				mObDownloadBtn.setVisibility(View.INVISIBLE);
				mObDownloadBtn.setText(R.string.start);
				mObDownloadProgress.setMax((int) fileSize);
			}

			@Override
			public void onProgressChange(long fileSize, long downloadedSize)
			{
				super.onProgressChange(fileSize, downloadedSize);
				mObDownloadProgress.setProgress((int) downloadedSize);
			}

			@Override
			public void onError(Throwable t)
			{
				super.onError(t);
				mObDownloadBtn.setVisibility(View.VISIBLE);
				mObDownloadBtn.setText(R.string.failed);
				t.printStackTrace();
			}

			@Override
			public void onSuccess()
			{
				super.onSuccess();
				mObDownloadBtn.setVisibility(View.VISIBLE);
				mObDownloadBtn.setText(R.string.success);
			}
		});
	}
}
