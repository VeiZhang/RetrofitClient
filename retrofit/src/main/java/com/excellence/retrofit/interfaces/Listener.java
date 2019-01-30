package com.excellence.retrofit.interfaces;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     date   : 2017/10/18
 *     desc   : 服务器数据返回接口
 * </pre>
 */

public class Listener<T> implements IListener<T> {

    @Override
    public void onSuccess(T result) {

    }

    @Override
    public void onError(Throwable t) {

    }
}
