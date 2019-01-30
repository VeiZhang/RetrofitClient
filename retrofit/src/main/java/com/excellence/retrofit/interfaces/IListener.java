package com.excellence.retrofit.interfaces;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     date   : 2017/10/18
 *     desc   : 服务器数据返回接口
 * </pre>
 */

public interface IListener<T> {

    /**
     *
     * @param result 服务器字符串数据
     */
    void onSuccess(T result);

    /**
     *
     * @param t 异常
     */
    void onError(Throwable t);
}
