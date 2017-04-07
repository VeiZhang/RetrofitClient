package com.excellence.retrofitutils;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.excellence.retrofitutilslibrary.RetrofitUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
{
	private static final String BASE_URL = "http://gank.io/api/";
	private static final String REQUET_URL = "http://gank.io/api/data/%E7%A6%8F%E5%88%A9/0/1";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		RetrofitUtils retrofitUtils = new RetrofitUtils.Builder().baseUrl(BASE_URL).build();
		retrofitUtils.getService().get(REQUET_URL).enqueue(new Callback<String>()
		{
			@Override
			public void onResponse(Call<String> call, Response<String> response)
			{
				if (response.isSuccessful() && response.code() == HttpURLConnection.HTTP_OK)
					System.out.println("" + response.body());
				else
					System.out.println(" " + stream2String(response.errorBody().byteStream()));
			}

			@Override
			public void onFailure(Call<String> call, Throwable t)
			{
				t.printStackTrace();
			}
		});
	}

	private String stream2String(InputStream inputStream)
	{
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder result = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				result.append(line);
			}
			reader.close();
			return result.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
