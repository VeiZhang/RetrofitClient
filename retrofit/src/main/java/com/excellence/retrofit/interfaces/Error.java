package com.excellence.retrofit.interfaces;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/4/7
 *     desc   : 错误回调接口
 * </pre>
 */

public interface Error
{
	/**
	 *
	 * @param t 异常
	 */
	void error(Throwable t);
}
