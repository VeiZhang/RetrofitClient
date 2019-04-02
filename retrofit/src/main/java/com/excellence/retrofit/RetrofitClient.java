package com.excellence.retrofit;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.excellence.retrofit.interceptor.CacheInterceptor;
import com.excellence.retrofit.interceptor.CacheOnlineInterceptor;
import com.excellence.retrofit.interceptor.DownloadInterceptor;
import com.excellence.retrofit.interceptor.LoggingInterceptor;
import com.excellence.retrofit.utils.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import io.reactivex.disposables.Disposable;
import okhttp3.Cache;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static com.excellence.retrofit.interceptor.CacheInterceptor.DEFAULT_CACHE_TIME;
import static com.excellence.retrofit.utils.HttpUtils.checkURL;
import static com.excellence.retrofit.utils.Utils.checkNULL;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/4/7
 *     desc   : Retrofit封装
 *     			<ul>
 *     			   <li>get请求封装</li>
 *     			   <li>请求头和参数统一配置，分开配置</li>
 *     			   <li>异步统一回调接口</li>
 *     			   <li>单个请求、单个界面请求、所有请求取消</li>
 *     			   <li>缓存策略：在线缓存、离线缓存</li>
 *     			   <li>下载</li>
 *     			</ul>
 * </pre>
 */

public class RetrofitClient {

    private static final String TAG = RetrofitClient.class.getSimpleName();

    private static RetrofitClient mInstance = null;
    private Retrofit mRetrofit = null;
    private RetrofitHttpService mService = null;

    /**
     * 全局请求头
     */
    private Map<String, String> mHeaders = null;

    /**
     * 全局请求参数
     */
    private Map<String, String> mParams = null;

    /**
     * 网络请求队列
     */
    private static final Map<String, Object> CALL_MAP = new HashMap<>();

    private Executor mResponsePoster = null;

    public static RetrofitClient getInstance() {
        checkNULL(mInstance, "please init " + Builder.class.getName());
        return mInstance;
    }

    private RetrofitClient(Builder builder) {
        mRetrofit = builder.mRetrofit;
        mService = builder.mService;
        mHeaders = builder.mHeaders;
        mParams = builder.mParams;

        final Handler handler = new Handler(Looper.getMainLooper());
        mResponsePoster = new Executor() {
            @Override
            public void execute(@NonNull Runnable command) {
                handler.post(command);
            }
        };
    }

    public Retrofit getRetrofit() {
        return mRetrofit;
    }

    protected RetrofitHttpService getService() {
        return mService;
    }

    protected Map<String, String> getHeaders() {
        return mHeaders;
    }

    protected Map<String, String> getParams() {
        return mParams;
    }

    protected Executor getResponsePoster() {
        return mResponsePoster;
    }

    /**
     * 判断请求队列是否存在，以 tag 作为标识
     *
     * @param tag
     * @return
     */
    public static boolean containsRequest(Object tag) {
        return containsRequest(tag, null);
    }

    /**
     * 判断请求队列是否存在，以 tag + url 作为标识
     *
     * @param tag
     * @param url
     * @return
     */
    public static boolean containsRequest(Object tag, String url) {
        if (tag == null) {
            return false;
        }
        return CALL_MAP.containsKey(tag.toString() + url);
    }

    /**
     * 由于开放自定义Service，谨慎使用
     *
     * 添加网络请求队列，以 tag + url 作为标识
     *
     * @param tag 标签
     * @param url 请求链接
     * @param request 网络请求
     */
    public static synchronized void addRequest(Object tag, String url, Object request) {
        if (tag == null) {
            return;
        }
        synchronized (CALL_MAP) {
            CALL_MAP.put(tag.toString() + url, request);
        }
    }

    /**
     * 由于开放自定义Service，谨慎使用
     *
     * 根据tag + url 标识删除队列里的完成、错误的某个网络请求
     *
     * @param tag 标签
     * @param url 请求链接
     */
    public static synchronized void removeRequest(Object tag, String url) {
        if (tag == null) {
            return;
        }
        synchronized (CALL_MAP) {
            String key = tag.toString() + url;
            if (CALL_MAP.containsKey(key)) {
                CALL_MAP.remove(key);
            }
        }
    }

    /**
     * 取消单个界面的所有请求，或取消某个tag的所有请求
     * 如果想取消单个请求，请传入 标识：tag.toString() + url
     *
     * 遍历删除，使用迭代
     *
     * @param tag 标签
     */
    public static synchronized void cancel(Object tag) {
        checkNULL(mInstance, "please init " + Builder.class.getName());
        if (tag == null) {
            return;
        }
        synchronized (CALL_MAP) {
            for (Iterator<String> iterator = CALL_MAP.keySet().iterator(); iterator.hasNext(); ) {
                String key = iterator.next();
                if (key.startsWith(tag.toString())) {
                    mInstance.cancel(key);
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 通过正则表达式取消请求
     *
     * @param pattern
     */
    public static synchronized void cancel(Pattern pattern) {
        checkNULL(mInstance, "please init " + Builder.class.getName());
        if (pattern == null) {
            return;
        }
        synchronized (CALL_MAP) {
            for (Iterator<String> iterator = CALL_MAP.keySet().iterator(); iterator.hasNext(); ) {
                String key = iterator.next();
                if (pattern.matcher(key).matches()) {
                    mInstance.cancel(key);
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 取消所有网络请求
     */
    public static synchronized void cancelAll() {
        checkNULL(mInstance, "please init " + Builder.class.getName());
        for (String key : CALL_MAP.keySet()) {
            mInstance.cancel(key);
        }
        CALL_MAP.clear();
    }

    /**
     * 取消单个网络请求
     *
     * @param key 请求标识：tag.toString() + url
     */
    private void cancel(String key) {
        Object request = CALL_MAP.get(key);
        if (request instanceof Call) {
            if (!((Call) request).isCanceled()) {
                ((Call) request).cancel();
            }
        } else if (request instanceof Disposable) {
            if (!((Disposable) request).isDisposed()) {
                ((Disposable) request).dispose();
            }
        }
    }

    /**
     * 使用该方式创建{@link #RetrofitClient}的单例
     */
    public static class Builder {

        private static final long DEFAULT_CACHE_SIZE = 20 * 1024 * 1024;
        private static final long DEFAULT_TIMEOUT = 5;

        private Context mContext = null;
        private Retrofit.Builder mRetrofitBuilder = null;
        private OkHttpClient mOkHttpClient = null;
        private OkHttpClient.Builder mOkHttpClientBuilder = null;
        private Retrofit mRetrofit = null;
        private RetrofitHttpService mService = null;
        private boolean cacheEnable = false;
        private Cache mCache = null;
        private long mCacheTime = DEFAULT_CACHE_TIME;
        private long mCacheOnlineTime = 0;
        private boolean isDefaultConvertFactory = true;
        private List<Converter.Factory> mConverterFactories = new ArrayList<>();
        private boolean isDefaultCallFactory = true;
        private List<CallAdapter.Factory> mCallAdapterFactories = new ArrayList<>();
        /**
         * 配置全局请求头
         */
        private Map<String, String> mHeaders = new HashMap<>();

        /**
         * 配置全局请求参数
         */
        private Map<String, String> mParams = new HashMap<>();

        public Builder(@NonNull Context context) {
            mContext = context.getApplicationContext();
            mRetrofitBuilder = new Retrofit.Builder();
            mOkHttpClientBuilder = new OkHttpClient.Builder();
        }

        /**
         * 设置baseUrl
         *
         * @param baseUrl
         * @return
         */
        public Builder baseUrl(@NonNull String baseUrl) {
            checkURL(baseUrl);
            if (!baseUrl.endsWith("/")) {
                baseUrl += "/";
            }
            mRetrofitBuilder.baseUrl(baseUrl);
            return this;
        }

        /**
         * 是否开启缓存
         *
         * 默认缓存
         * 位置: /sdcard/Android/data/YourPackageName/cache/
         *      /data/data/package/cache
         * 大小: {@link #DEFAULT_CACHE_SIZE}
         *
         * @param cacheEnable
         * @return
         */
        public Builder cacheEnable(boolean cacheEnable) {
            this.cacheEnable = cacheEnable;
            return this;
        }

        /**
         * 自定义缓存
         * @see CacheInterceptor
         *
         * @param cache
         * @return
         */
        public Builder cache(Cache cache) {
            mCache = cache;
            if (mCache != null) {
                cacheEnable(true);
            }
            return this;
        }

        /**
         * 设置离线缓存有效期限，默认4周{@link CacheInterceptor#DEFAULT_CACHE_TIME}
         * @see CacheInterceptor
         *
         * @param cacheTime 单位：s
         * @return
         */
        public Builder cacheTime(long cacheTime) {
            mCacheTime = cacheTime;
            cacheEnable(true);
            return this;
        }

        /**
         * 设置在线缓存有效期限，默认0s：每次都重新请求
         * @see CacheInterceptor
         *
         * @param cacheOnlineTime  单位：s
         * @return
         */
        public Builder cacheOnlineTime(long cacheOnlineTime) {
            mCacheOnlineTime = cacheOnlineTime;
            cacheEnable(true);
            return this;
        }

        /**
         * 是否自动重连
         *
         * @param retryOnConnectionFailure
         * @return
         */
        public Builder retryOnConnectionFailure(boolean retryOnConnectionFailure) {
            mOkHttpClientBuilder.retryOnConnectionFailure(retryOnConnectionFailure);
            return this;
        }

        /**
         * 添加拦截器
         *
         * @param interceptor
         * @return
         */
        public Builder addInterceptor(@NonNull Interceptor interceptor) {
            mOkHttpClientBuilder.addInterceptor(interceptor);
            return this;
        }

        /**
         * 添加网络拦截器
         *
         * @param interceptor
         * @return
         */
        public Builder addNetworkInterceptor(@NonNull Interceptor interceptor) {
            mOkHttpClientBuilder.addNetworkInterceptor(interceptor);
            return this;
        }

        /**
         * 设置转换工厂
         *
         * @param factory
         * @return
         */
        public Builder addConverterFactory(@NonNull Converter.Factory factory) {
            if (factory instanceof ScalarsConverterFactory) {
                isDefaultConvertFactory = false;
            }
            mConverterFactories.add(factory);
            return this;
        }

        /**
         * 设置回调工厂
         *
         * @param factory
         * @return
         */
        public Builder addCallAdapterFactory(@NonNull CallAdapter.Factory factory) {
            if (factory instanceof RxJava2CallAdapterFactory) {
                isDefaultCallFactory = false;
            }
            mCallAdapterFactories.add(factory);
            return this;
        }

        /**
         * 设置连接超时，<0时，默认为{@link #DEFAULT_TIMEOUT}s
         *
         * @param timeout
         * @param timeUnit
         * @return
         */
        public Builder connectTimeout(long timeout, TimeUnit timeUnit) {
            if (timeout < 0) {
                mOkHttpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
            } else {
                mOkHttpClientBuilder.connectTimeout(timeout, timeUnit);
            }
            return this;
        }

        /**
         * 设置连接超时，单位为s；<0时，默认为{@link #DEFAULT_TIMEOUT}s
         *
         * @param timeout
         * @return
         */
        public Builder connectTimeout(long timeout) {
            return connectTimeout(timeout, TimeUnit.SECONDS);
        }

        /**
         * 设置读超时，<0时，默认为{@link #DEFAULT_TIMEOUT}s
         *
         * @param timeout
         * @param timeUnit
         * @return
         */
        public Builder readTimeout(long timeout, TimeUnit timeUnit) {
            if (timeout < 0) {
                mOkHttpClientBuilder.readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
            } else {
                mOkHttpClientBuilder.readTimeout(timeout, timeUnit);
            }
            return this;
        }

        /**
         * 设置读超时，单位为s；<0时，默认为{@link #DEFAULT_TIMEOUT}s
         *
         * @param timeout
         * @return
         */
        public Builder readTimeout(long timeout) {
            return readTimeout(timeout, TimeUnit.SECONDS);
        }

        /**
         * 设置写超时，<0时，默认{@link #DEFAULT_TIMEOUT}s
         *
         * @param timeout
         * @param timeUnit
         * @return
         */
        public Builder writeTimeout(long timeout, TimeUnit timeUnit) {
            if (timeout < 0) {
                mOkHttpClientBuilder.writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
            } else {
                mOkHttpClientBuilder.writeTimeout(timeout, timeUnit);
            }
            return this;
        }

        /**
         * 设置写超时，单位为s；<0时，默认{@link #DEFAULT_TIMEOUT}s
         *
         * @param timeout
         * @return
         */
        public Builder writeTimeout(long timeout) {
            return writeTimeout(timeout, TimeUnit.SECONDS);
        }

        /**
         * 设置统一请求头
         *
         * @param key 键
         * @param value 键值
         * @return
         */
        public Builder header(String key, String value) {
            mHeaders.put(key, value);
            return this;
        }

        /**
         * 设置统一请求头集合
         *
         * @param headers 集合
         * @return
         */
        public Builder headers(Map<String, String> headers) {
            mHeaders.putAll(headers);
            return this;
        }

        /**
         * 设置统一请求参数
         *
         * @param key 键
         * @param value 键值
         * @return
         */
        public Builder param(String key, String value) {
            mParams.put(key, value);
            return this;
        }

        /**
         * 设置统一请求参数集合
         *
         * @param params 参数集
         * @return
         */
        public Builder params(Map<String, String> params) {
            mParams.putAll(params);
            return this;
        }

        /**
         * 添加Log打印
         *
         * @param isEnable
         * @return
         */
        public Builder addLog(boolean isEnable) {
            Logger.isEnabled(isEnable);
            if (isEnable) {
                mOkHttpClientBuilder.addInterceptor(new LoggingInterceptor());
            }
            return this;
        }

        /**
         * 配置此客户端以跟踪重定向。默认true，自动重定向；如果false，会拦截到状态码30X
         *
         * @param followRedirects
         * @return
         */
        public Builder followRedirects(boolean followRedirects) {
            mOkHttpClientBuilder.followRedirects(followRedirects);
            return this;
        }

        /**
         * 配置此客户端以跟踪从HTTPS到HTTP以及从HTTP到HTTPS的重定向。默认true
         * 如果未设置，将遵循协议重定向。
         *
         * @param followProtocolRedirects
         * @return
         */
        public Builder followSslRedirects(boolean followProtocolRedirects) {
            mOkHttpClientBuilder.followSslRedirects(followProtocolRedirects);
            return this;
        }

        /**
         * 设置CookieStore
         *
         * @param cookieJar
         */
        public Builder cookieJar(CookieJar cookieJar) {
            mOkHttpClientBuilder.cookieJar(cookieJar);
            return this;
        }

        /**
         * 自定义{@link OkHttpClient}，其他自定义设置会被覆盖，如{@link #followRedirects(boolean)}
         *
         * @param okHttpClient
         * @return
         */
        public Builder okHttpClient(OkHttpClient okHttpClient) {
            mOkHttpClient = okHttpClient;
            return this;
        }

        /**
         * 只执行一次，单例
         *
         * @return
         */
        public void build() {
            if (mInstance != null) {
                return;
            }

            /**
             * 防止下载文件缓存
             */
            mOkHttpClientBuilder.addNetworkInterceptor(new DownloadInterceptor());

            if (cacheEnable) {
                /**
                 * 默认开启的缓存{@link #cacheEnable}
                 */
                if (mCache == null) {
                    File cacheFile = mContext.getExternalCacheDir();
                    Logger.i(TAG, "cache external: " + cacheFile);
                    if (cacheFile == null) {
                        cacheFile = mContext.getCacheDir();
                        Logger.i(TAG, "cache: " + cacheFile);
                    }
                    cache(new Cache(cacheFile, DEFAULT_CACHE_SIZE));
                }
                mOkHttpClientBuilder.cache(mCache);
                CacheInterceptor cacheInterceptor = new CacheInterceptor(mContext, mCacheTime);
                CacheOnlineInterceptor cacheOnlineInterceptor = new CacheOnlineInterceptor(mCacheOnlineTime);
                mOkHttpClientBuilder.addInterceptor(cacheInterceptor);
                mOkHttpClientBuilder.addNetworkInterceptor(cacheInterceptor);
                mOkHttpClientBuilder.addNetworkInterceptor(cacheOnlineInterceptor);
            }

            if (mOkHttpClient == null) {
                mOkHttpClient = mOkHttpClientBuilder.build();
            }
            mRetrofitBuilder.client(mOkHttpClient);

            if (isDefaultConvertFactory) {
                // 默认支持字符串数据转换
                mRetrofitBuilder.addConverterFactory(ScalarsConverterFactory.create());
            }
            for (Converter.Factory converterFactory : mConverterFactories) {
                mRetrofitBuilder.addConverterFactory(converterFactory);
            }

            if (isDefaultCallFactory) {
                // 默认支持RxJava回调
                mRetrofitBuilder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());
            }
            for (CallAdapter.Factory callAdapterFactory : mCallAdapterFactories) {
                mRetrofitBuilder.addCallAdapterFactory(callAdapterFactory);
            }

            mRetrofit = mRetrofitBuilder.build();
            mService = mRetrofit.create(RetrofitHttpService.class);
            mInstance = new RetrofitClient(this);
        }
    }
}
