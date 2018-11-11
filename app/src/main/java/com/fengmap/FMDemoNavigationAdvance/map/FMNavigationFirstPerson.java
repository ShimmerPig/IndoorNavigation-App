package com.fengmap.FMDemoNavigationAdvance.map;

import android.os.Bundle;
import android.view.View;

import com.fengmap.FMDemoNavigationAdvance.R;
import com.fengmap.FMDemoNavigationAdvance.utils.ConvertUtils;
import com.fengmap.FMDemoNavigationAdvance.utils.ViewHelper;
import com.fengmap.android.analysis.navi.FMNaviOption;
import com.fengmap.android.analysis.navi.FMNavigationInfo;
import com.fengmap.android.analysis.navi.FMSimulateNavigation;
import com.fengmap.android.analysis.navi.OnFMNavigationListener;
import com.fengmap.android.map.geometry.FMGeoCoord;
import com.fengmap.android.map.marker.FMLocationMarker;

/**
 * 第一人称导航
 * <p>模拟第一人称导航示例，地图跟随着人方向转动，在真实导航中，获取手机转向的方向，设置为地图的转向
 *
 * @author hezutao@fengmap.com
 * @version 2.0.0
 */
public class FMNavigationFirstPerson extends BaseActivity implements View.OnClickListener, OnFMNavigationListener {

    /**
     * 约束过定位标注
     */
    private FMLocationMarker mHandledMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
    }

    @Override
    public void onBackPressed() {
        if (!isMapLoaded)
            return;

        // 释放资源
        mNavigation.clear();
        mNavigation.release();

        super.onBackPressed();
    }

    @Override
    public void onMapInitSuccess(String path) {
        super.onMapInitSuccess(path);
        ViewHelper.setViewClickListener(FMNavigationFirstPerson.this, R.id.btn_start_navigation, this);

        // 创建模拟导航对象
        mNavigation = new FMSimulateNavigation(mFMMap);

        // 创建模拟导航配置对象
        mNaviOption = new FMNaviOption();

        // 设置跟随模式，默认跟随
        mNaviOption.setFollowPosition(true);

        // 设置跟随角度（第一人视角），默认跟随
        mNaviOption.setFollowAngle(true);

        // 点移距离视图中心点超过最大距离5米，就会触发移动动画；若设为0，则实时居中
        mNaviOption.setNeedMoveToCenterMaxDistance(NAVI_MOVE_CENTER_MAX_DISTANCE);

        // 设置导航开始时的缩放级别，true: 导航结束时恢复开始前的缩放级别，false：保持现状
        mNaviOption.setZoomLevel(NAVI_ZOOM_LEVEL, false);

        // 设置配置
        mNavigation.setNaviOption(mNaviOption);

        // 设置导航监听接口
        mNavigation.setOnNavigationListener(this);

        // 路径规划
        analyzeNavigation(mStartCoord, mEndCoord);

        isMapLoaded = true;
    }

    @Override
    public void startNavigation() {
        FMSimulateNavigation simulateNavigation = (FMSimulateNavigation) mNavigation;
        // 3米每秒。
        simulateNavigation.simulate(3.0f);
    }

    @Override
    public void updateLocateGroupView() {
        String groupName = ConvertUtils.convertToFloorName(mFMMap, mFMMap.getFocusGroupId());
        ViewHelper.setViewText(FMNavigationFirstPerson.this, R.id.btn_group_control, groupName);
    }

    /**
     * 更新约束定位点
     *
     * @param coord 坐标
     */
    private void updateHandledMarker(FMGeoCoord coord, float angle) {
        if (mHandledMarker == null) {
            mHandledMarker = ViewHelper.buildLocationMarker(coord.getGroupId(), coord.getCoord(), angle);
            mLocationLayer.addMarker(mHandledMarker);
        } else {
            mHandledMarker.updateAngleAndPosition(coord.getGroupId(), angle, coord.getCoord());
        }
    }

    @Override
    public void onCrossGroupId(final int lastGroupId, final int currGroupId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFMMap.setFocusByGroupId(currGroupId, null);
                updateLocateGroupView();
            }
        });
    }

    @Override
    public void onWalking(final FMNavigationInfo navigationInfo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 更新定位标志物
                updateHandledMarker(navigationInfo.getPosition(), navigationInfo.getAngle());
            }
        });
    }


    @Override
    public void onComplete() {
    }

}
