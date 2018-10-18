package com.excellence.retrofit.sample;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.excellence.retrofit.HttpRequest;
import com.excellence.retrofit.RetrofitClient;
import com.excellence.retrofit.interfaces.Listener;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

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

		userRequest();
	}

	/**
	 * 自定义retrofit service
	 */
	private void userRequest()
	{
		Retrofit retrofit = RetrofitClient.getInstance().getRetrofit();
		UserService service = retrofit.create(UserService.class);
		/**
		 * 设置{@link RetrofitClient.Builder#followRedirects(boolean)} = false，可拦截30X重定向，不让OkHttp自动重定向
		 */
		Call<ResponseBody> call = service.get("http://veizhang.github.io", new HashMap<String, String>(), new HashMap<String, String>());
		call.enqueue(new Callback<ResponseBody>()
		{
			@Override
			public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
			{
				try
				{
					System.out.println(response.code());
					System.out.println(response.body().string());
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(Call<ResponseBody> call, Throwable throwable)
			{
				throwable.printStackTrace();
			}
		});

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
		/**
		 * 字符串请求
		 * new HttpRequest.Builder().tag(this).url(REQUEST_URL).build().get(mStringListener);
		 */
		/**
		 * JSONObject请求
		 */
		new HttpRequest.Builder().tag(this).url(REQUEST_URL).build().get(new TypeToken<Gank>()
		{
		}.getType(), mJSONObjectListener);
	}

	private void obGet()
	{
		/**
		 * 字符串请求
		 *
		 * new HttpRequest.Builder().tag(this).url(REQUEST_URL1).build().obGet(mStringListener);
		 */
		/**
		 * JSONObject请求
		 */
		new HttpRequest.Builder().tag(this).url(REQUEST_URL1).build().obGet(Gank.class, mJSONObjectListener);
	}

	private Listener<Gank> mJSONObjectListener = new Listener<Gank>()
	{
		@Override
		public void onSuccess(Gank result)
		{
			mResultTextView.setText(result.toString());
		}

		@Override
		public void onError(Throwable t)
		{
			mResultTextView.setText(R.string.request_failed);
			t.printStackTrace();
		}
	};

	private Listener<String> mStringListener = new Listener<String>()
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
	};
}
