package com.fengmap.FMDemoNavigationAdvance.utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;

//用来与服务器交互的httputil
public class HttpUtil {
    //这里传入的是请求的地址，并且注册一个回调来处理服务器的响应
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback){
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
