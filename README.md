# 基于Retrofit网络框架的封装

[![Download][icon_download]][download]

## 实现功能
* get请求封装
* 请求头和参数统一配置，分开配置
* 异步统一回调接口
* 单个请求、单个界面请求、所有请求取消
* 缓存策略：在线缓存、离线缓存
* 下载
* 上传

## 使用

### 引用
```
compile 'com.excellence:retrofit:_latestVersion'
```

### 权限
```
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```

### 网络请求

* 初始化
可以在Application中初始化
```java
// 默认支持：ScalarsConverterFactory、RxJavaCallAdapterFactory
// addLog : 是否开启Log打印
// cacheEnable : 是否开启缓存，默认不开启：开启后，默认每个请求都缓存
// 单个请求的缓存控制，可设置HttpRequest.Builder#cacheEnable
// cache : 自定义缓存目录，设置缓存目录后，缓存自动开启，无需设置cacheEnable
···
new RetrofitUtils.Builder(this).baseUrl(BASE_URL).addLog(true).cacheEnable(true).build();
```

* 创建请求
    * GET
    ```
    new HttpRequest.Builder().tag(this).url(REQUEST_URL).build().get();
    ```
    * POST
    ```
    new HttpRequest.Builder().tag(this).url(REQUEST_URL).build().postForm();
    ```
    * 下载
    ```
    new HttpRequest.Builder().tag(this).url(REQUEST_URL).build().download();
    ```
    * 上传
    ```
    new HttpRequest.Builder().tag(this).url(REQUEST_URL).build().uploadFile();
    ```

### 使用注意

* 缓存默认位置`/sdcard/Android/data/YourPackageName/cache/`，可以初始化时自定义

* 如果想针对单个请求，不使用缓存，可以考虑添加头信息`mHeaders.put(DOWNLOAD, DOWNLOAD)`，当做下载请求，则不会使用缓存机制

### 混淆

```
###-----------保持 retrofit client 不被混淆------------
-keep class com.excellence.retrofit.RetrofitHttpService { *; }

###-----------保持 retrofit 不被混淆------------
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-dontwarn javax.annotation.**

###-----------保持 okhttp 不被混淆------------
-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *;}
-dontwarn okio.**
```

## 版本更新

| 版本 | 描述 |
| --- | ---- |
| [1.0.5][RetrofitClient1.0.5] | 判断请求队列是否存在 **2018-10-30** |
| [1.0.4][RetrofitClient1.0.4] | 可自定义OkHttp，开放请求队列的增加与删除 **2018-10-25** |
| [1.0.3][RetrofitClient1.0.3] | 开放retrofit对象，可自定义创建请求Service **2018-8-1** |
| [1.0.2][RetrofitClient1.0.2] | 分离下载封装，优化请求接口 **2018-7-5** |
| [1.0.1][RetrofitClient1.0.1] | 优化请求和新增异常处理 **2018-3-13** |
| [1.0.0][RetrofitClient1.0.0] | 创建网络请求：GET、POST、下载、上传 **2017-11-14** |

<!-- 网站链接 -->

[download]:https://bintray.com/veizhang/maven/retrofit/_latestVersion "Latest version"

<!-- 图片链接 -->

[icon_download]:https://api.bintray.com/packages/veizhang/maven/retrofit/images/download.svg

<!-- 版本 -->

[RetrofitClient1.0.5]:https://bintray.com/veizhang/maven/retrofit/1.0.5
[RetrofitClient1.0.4]:https://bintray.com/veizhang/maven/retrofit/1.0.4
[RetrofitClient1.0.3]:https://bintray.com/veizhang/maven/retrofit/1.0.3
[RetrofitClient1.0.2]:https://bintray.com/veizhang/maven/retrofit/1.0.2
[RetrofitClient1.0.1]:https://bintray.com/veizhang/maven/retrofit/1.0.1
[RetrofitClient1.0.0]:https://bintray.com/veizhang/maven/retrofit/1.0.0


<!--


同步请求、异步请求：部分请求，下载没有做

下载断点、下载进度：https://www.jianshu.com/p/8a67302a3377 （不一定重写，哪种方便用哪种）重写ResponseBody的方式、https://www.jianshu.com/p/982a005de665 https://blog.csdn.net/ljd2038/article/details/51189334

上传断点、进度

批量上传

图文上传

New：

参数不能为空，要么设置""，否则会报错：java.lang.IllegalArgumentException: Query map contained null value for key

处理异常时，根据是否开启缓存配置，来处理，有可能网页返回的就是空

-->