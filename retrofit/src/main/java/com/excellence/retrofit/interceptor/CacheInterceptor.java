package com.excellence.retrofit.interceptor;

import android.content.Context;

import com.excellence.retrofit.utils.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static com.excellence.retrofit.utils.Utils.isNetworkAvailable;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/4/8
 *     desc   : 离线缓存拦截器
 * </pre>
 */

public class CacheInterceptor implements Interceptor {

    private static final String TAG = CacheInterceptor.class.getSimpleName();

    public static final String HEADER_PRAGMA = "Pragma";
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";
    public static final long DEFAULT_CACHE_TIME = 4 * 7 * 24 * 60 * 60;

    private Context mContext = null;
    private long mCacheTime = 0;

    public CacheInterceptor(Context context) {
        this(context, DEFAULT_CACHE_TIME);
    }

    public CacheInterceptor(Context context, long cacheTime) {
        mContext = context;
        mCacheTime = cacheTime;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        if (!isNetworkAvailable(mContext)) {
            Logger.i(TAG, "network is invalid");

            /**
             * 离线缓存设置有效期限，不使用{@link okhttp3.CacheControl#FORCE_CACHE}，因为它有个很大的默认超时时间{@link Integer.MAX_VALUE}
             * 新建一个{@link CacheControl}
             */
            CacheControl cacheControl = new CacheControl.Builder().onlyIfCached().maxStale((int) mCacheTime, TimeUnit.SECONDS).build();
            request = request.newBuilder().cacheControl(cacheControl).build();
            Response response = chain.proceed(request);
            /**
             * 离线缓存，重新设置请求
             * max-stale设置缓存策略，及超时策略
             */
            return response.newBuilder().removeHeader(HEADER_PRAGMA).removeHeader(HEADER_CACHE_CONTROL).header(HEADER_CACHE_CONTROL, "public, only-if-cached, max-stale=" + mCacheTime).build();
        }
        return chain.proceed(request);
    }
}
