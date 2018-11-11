package com.fengmap.FMDemoNavigationAdvance;

import android.app.Application;

import com.fengmap.android.FMMapSDK;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

/**
 * 应用层初始化
 *
 * @author hezutao@fengmap.com
 * @version 2.0.0
 */
public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 在使用 SDK 各组间之前初始化 context 信息，传入 Application
        FMMapSDK.init(this);
        // 请勿在“ =”与 appid 之间添加任务空字符或者转义符
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5ab378a0");
    }

}