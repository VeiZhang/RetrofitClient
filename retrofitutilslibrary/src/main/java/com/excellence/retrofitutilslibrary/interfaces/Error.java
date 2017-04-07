package com.excellence.retrofitutilslibrary.interfaces;

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
	 * @param code 错误代码 {@code 0}:异常错误<br>{@code >0}:网络返回值
	 * @param error 错误信息
	 */
	void error(int code, String error);
}
