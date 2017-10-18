package com.excellence.retrofit.sample;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.excellence.retrofit.interfaces.IListener;

public class GetActivity extends BaseActivity implements View.OnClickListener
{
	private Button mGetBtn = null;
	private Button mObGetBtn = null;
	private TextView mRequestTitleView = null;
	private TextView mResultTextView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_get);

		mGetBtn = (Button) findViewById(R.id.get_request);
		mObGetBtn = (Button) findViewById(R.id.obget_request);
		mRequestTitleView = ((TextView) findViewById(R.id.get_text_title));
		mResultTextView = (TextView) findViewById(R.id.get_text_result);

		mGetBtn.setOnClickListener(this);
		mObGetBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.get_request:
			mRequestTitleView.setText(R.string.get_request);
			mRequestTitleView.append("\n");
			mRequestTitleView.append(REQUEST_URL);
			get();
			break;

		case R.id.obget_request:
			mRequestTitleView.setText(R.string.obget_request);
			mRequestTitleView.append("\n");
			mRequestTitleView.append(REQUEST_URL1);
			obGet();
			break;
		}
	}

	private void get()
	{
		mRetrofitClient.setHeader("Cache-Time", "24 * 3600").setTag(this).get(REQUEST_URL, new IListener()
		{
			@Override
			public void onSuccess(String result)
			{
				mResultTextView.setText(result);
			}

			@Override
			public void onError(Throwable t)
			{
				mResultTextView.setText(R.string.request_failed);
				t.printStackTrace();
			}
		});
	}

	private void obGet()
	{
		mRetrofitClient.setHeader("Cache-Time", "24 * 3600").setTag(this).obGet(REQUEST_URL1, new IListener()
		{
			@Override
			public void onSuccess(String result)
			{
				mResultTextView.setText(result);
			}

			@Override
			public void onError(Throwable t)
			{
				mResultTextView.setText(R.string.request_failed);
				t.printStackTrace();
			}
		});
	}
}
