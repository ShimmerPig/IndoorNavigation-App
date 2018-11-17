package com.fengmap.FMDemoNavigationAdvance;

import android.app.Application;

import com.fengmap.android.FMMapSDK;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import org.litepal.LitePalApplication;

/**
 * 应用层初始化
 *
 * @author hezutao@fengmap.com
 * @version 2.0.0
 */
//由于配置时出现两个name的问题，所以这里修改其继承关系 by Pig
public class DemoApplication extends LitePalApplication /*Application*/ {

    @Override
    public void onCreate() {
        super.onCreate();
        // 在使用 SDK 各组间之前初始化 context 信息，传入 Application
        FMMapSDK.init(this);
        // 请勿在“ =”与 appid 之间添加任务空字符或者转义符
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5ab378a0");
    }

}