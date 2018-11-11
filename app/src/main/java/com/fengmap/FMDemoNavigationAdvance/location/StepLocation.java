package com.fengmap.FMDemoNavigationAdvance.location;

import android.util.Log;

import com.fengmap.FMDemoNavigationAdvance.bean.AMapPoint;

public class StepLocation {
    //上一次的位置
    public  AMapPoint lastAMapPoint;
    private static class LazyHolder {
        private static StepLocation INSTANCE = new StepLocation();
    }
    public static  StepLocation getInstance() {
        return LazyHolder.INSTANCE;
    }
    private StepLocation(){
        Log.i("pig","StepLocation 初始化");
        //起始位置
        lastAMapPoint=new AMapPoint(12961647.576796599, 4861814.63807118, 0,30,0);
    }
    public static void refresh(){
        LazyHolder.INSTANCE=null;
        LazyHolder.INSTANCE = new StepLocation();
    }
}
