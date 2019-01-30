package com.excellence.retrofit.interfaces;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/4/10
 *     desc   : 下载接口实现
 * </pre>
 */

public class DownloadListener implements IDownloadListener {

    @Override
    public void onPreExecute(long fileSize) {

    }

    @Override
    public void onProgressChange(long fileSize, long downloadedSize) {

    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onError(Throwable t) {

    }

    @Override
    public void onSuccess() {

    }
}
