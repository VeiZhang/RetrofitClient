# 基于Retrofit网络框架的封装

## 实现功能
* get请求封装
* 请求头和参数统一配置，分开配置
* 异步统一回调接口
* 单个请求、单个界面请求、所有请求取消
* 缓存策略：在线缓存、离线缓存
* 下载

## 权限
```
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```

## 初始化
```java
// 默认支持：ScalarsConverterFactory、RxJavaCallAdapterFactory
new RetrofitUtils.Builder(this).baseUrl(BASE_URL).build();
```

## 使用注意

* 如果不想使用缓存策略，建议每次请求都重新设置Cache-Time，避免上次请求缓存头信息干扰

    默认缓存位置/sdcard/Android/data/YourPackageName/cache/

