package com.excellence.retrofit.sample;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.excellence.retrofit.RetrofitClient;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/4/11
 *     desc   :
 * </pre>
 */

public class BaseActivity extends Activity
{
	protected static final String BASE_URL = "http://gank.io/api/";
	protected static final String REQUEST_URL = "http://gank.io/api/data/%E7%A6%8F%E5%88%A9/1/0";
	protected static final String REQUEST_URL1 = "http://gank.io/api/history/content/2/1";
	protected static final String DOWNLOAD_URL = "http://download.game.yy.com/duowanapp/m/Duowan20140427.apk";
	protected static final String DOWNLOAD_URL1 = "http://andl.guopan.cn/101919-3889-1467370568.apk";

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		new RetrofitClient.Builder(this).baseUrl(BASE_URL).addLog(true).cacheEnable(true).build();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		RetrofitClient.cancel(this);
	}
}
