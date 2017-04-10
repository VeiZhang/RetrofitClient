package com.excellence.retrofitutils;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.excellence.retrofitutilslibrary.RetrofitUtils;
import com.excellence.retrofitutilslibrary.interfaces.Error;
import com.excellence.retrofitutilslibrary.interfaces.Success;

public class MainActivity extends AppCompatActivity
{
	private static final String BASE_URL = "http://gank.io/api/";
	private static final String REQUEST_URL = "http://gank.io/api/data/%E7%A6%8F%E5%88%A9/1/1";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		RetrofitUtils utils = new RetrofitUtils.Builder(this).baseUrl(BASE_URL).build();
		utils.setHeader("Cache-Time", "24 * 3600").setTag(this).get(REQUEST_URL, new Success()
		{
			@Override
			public void success(String result)
			{
				System.out.println(result);
			}
		}, new Error()
		{
			@Override
			public void error(int code, String error)
			{
				System.out.println(code + " ### " + error);
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
