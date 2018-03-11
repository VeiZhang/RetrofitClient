package com.excellence.retrofit.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.excellence.retrofit.HttpRequest;
import com.excellence.retrofit.interfaces.IListener;

import java.io.File;

public class PostActivity extends BaseActivity implements View.OnClickListener
{
	TextView mResultText = null;
	Button mPostForm = null;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post);

		mResultText = (TextView) findViewById(R.id.result_text);
		mPostForm = (Button) findViewById(R.id.post_form);

		mPostForm.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.post_form:
			postForm();
			break;
		}
	}

	private void postForm()
	{
		/**
		 * new HttpRequest.Builder().tag(this).url("http://192.168.7.53:8306/bloodSugar/glucometer/register").param("data", "{\"accountName\":\"test3\",\"accountPwd\":\"123\",\"accountMail\":\"XXXX@qq.com\",\"type\":1}").build().postForm(mStringIListener);
		 */
		new HttpRequest.Builder().tag(this).url("http://192.168.7.53:8306/bloodSugar/glucometer/uploadPicture").param("accountId", "1510543592133").param("pid", "0").build().uploadFile("file", new File("/sdcard/123.jpg"),
				String.class, new IListener<String>()
				{
					@Override
					public void onSuccess(String result)
					{
						System.out.println(result);
					}

					@Override
					public void onError(Throwable t)
					{
						t.printStackTrace();
					}
				});
	}

	private IListener<String> mStringIListener = new IListener<String>()
	{
		@Override
		public void onSuccess(String result)
		{
			mResultText.setText("结果：");
			mResultText.append(result);
		}

		@Override
		public void onError(Throwable t)
		{
			mResultText.setText("结果：异常");
			t.printStackTrace();
		}
	};
}
