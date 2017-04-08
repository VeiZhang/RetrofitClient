package com.excellence.retrofitutilslibrary.utils;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/4/7
 *     desc   : 工具类
 * </pre>
 */

public class Utils
{
	/**
	 * 输入流转字符串
	 *
	 * @param inputStream 输入流
	 * @return 字符串
	 */
	public static String inputStream2String(@NonNull InputStream inputStream)
	{
		StringBuilder result = new StringBuilder();
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				result.append(line);
			}
			reader.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result.toString();
	}

	/**
	 * 打印异常信息字符串
	 *
	 * @param t 异常信息
	 * @return 字符串
	 */
	public static String printException(@NonNull Throwable t)
	{
		StringBuilder exceptionStr = new StringBuilder();
		try
		{
			String msg = t.getMessage();
			if (!TextUtils.isEmpty(msg))
				exceptionStr.append(msg).append("\n");
			exceptionStr.append("Trace: ").append("\n");
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			t.printStackTrace(printWriter);
			Throwable cause = t.getCause();
			while (cause != null)
			{
				cause.printStackTrace(printWriter);
				cause = cause.getCause();
			}
			StringBuilder crash = new StringBuilder(writer.toString().replace("\t", ""));
			crash.insert(0, "\n");
			crash.delete(crash.lastIndexOf("\n"), crash.lastIndexOf("\n") + "\n".length());
			exceptionStr.append(crash);
			printWriter.close();
			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return exceptionStr.toString();
	}

}
