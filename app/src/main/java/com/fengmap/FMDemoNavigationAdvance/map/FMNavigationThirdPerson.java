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
 * 第三人称导航
 * <p>模拟以第三人称导航地图，地图的转向不随人的转向而转向
 *
 * @author yangbin@fengmap.com
 * @version 2.1.0
 */
public class FMNavigationThirdPerson extends BaseActivity implements View.OnClickListener, OnFMNavigationListener {
    // 约束过定位标注
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

        // 创建模拟导航对象
        mNavigation = new FMSimulateNavigation(mFMMap);

        // 创建模拟导航配置对象
        mNaviOption = new FMNaviOption();

        // 设置跟随模式，默认跟随
        mNaviOption.setFollowPosition(true);

        // 设置不跟随角度（第三人视角），默认跟随
        mNaviOption.setFollowAngle(false);

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

    /**
     * 更新楼层控件显示楼层名称
     */
    @Override
    public void updateLocateGroupView() {
        String groupName = ConvertUtils.convertToFloorName(mFMMap, mFMMap.getFocusGroupId());
        ViewHelper.setViewText(FMNavigationThirdPerson.this, R.id.btn_group_control, groupName);
    }

    /**
     * 更新约束定位点。
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
    public void onCrossGroupId(int lastGroupId, final int currGroupId) {
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
