package com.fengmap.FMDemoNavigationAdvance.utils;

import com.fengmap.android.map.geometry.FMGeoCoord;

import java.util.List;
import java.util.TimerTask;

/**
 * 模拟真实定位的任务。
 * Created by FengMap on 2018/2/5.
 */

public class SimulateLocateTask extends TimerTask {
    private List<FMGeoCoord> mData;
    private int mIndex;
    private OnLocateCallback mCallback;

    public SimulateLocateTask(List<FMGeoCoord> data) {
        mData = data;
    }


    public void setOnLocateCallback(OnLocateCallback callback) {
        mCallback = callback;
    }


    public synchronized int getIndex() {
        return mIndex;
    }

    public synchronized void setIndex(int index) {
        mIndex = index;
    }

    @Override
    public void run() {
        if (mData == null || mData.isEmpty() || mIndex >= mData.size()) {
            return;
        }

        if (mCallback != null)  {
            mCallback.onLocate(mData.get(mIndex).clone());
        }


        ++mIndex;
    }


    /**
     * 监听接口。
     */
    public interface OnLocateCallback {
        void onLocate(FMGeoCoord geoCoord);
    }
}
