package com.fengmap.FMDemoNavigationAdvance.map;

import android.os.Bundle;

import com.fengmap.FMDemoNavigationAdvance.R;
import com.fengmap.FMDemoNavigationAdvance.utils.ConvertUtils;
import com.fengmap.FMDemoNavigationAdvance.utils.FileUtils;
import com.fengmap.FMDemoNavigationAdvance.utils.SimulateLocateTask;
import com.fengmap.FMDemoNavigationAdvance.utils.ViewHelper;
import com.fengmap.android.analysis.navi.FMActualNavigation;
import com.fengmap.android.analysis.navi.FMNaviOption;
import com.fengmap.android.analysis.navi.FMNavigationInfo;
import com.fengmap.android.analysis.navi.FMSimulateNavigation;
import com.fengmap.android.analysis.navi.OnFMNavigationListener;
import com.fengmap.android.map.geometry.FMGeoCoord;
import com.fengmap.android.map.geometry.FMMapCoord;
import com.fengmap.android.map.marker.FMLocationMarker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * 导航路径约束。
 * <p>导航约束一般用于导航场景中，可以修正定位返回的坐标在导航路径上
 *
 * @author yangbin@fengmap.com
 * @version 2.1.0
 */
public class FMNavigationConstraint extends BaseActivity implements OnFMNavigationListener {
    // 真实定位标注
    private FMLocationMarker mRealMarker;

    // 约束过定位标注
    private FMLocationMarker mHandledMarker;

    // 模拟定位点集合
    private ArrayList<FMGeoCoord> mLocatePoints = new ArrayList<>();

    // 模拟定位系统的点回调任务
    private SimulateLocateTask mLocateTask;

    // 计时器
    private Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        initLocateData();
    }

    // 初始化定位数据
    void initLocateData() {
        byte[] content = FileUtils.readAssetsFile(this, "simulationLocation.json");
        String str = new String(content);
        try {
            JSONObject jsonObject = new JSONObject(str);

            // 1层的数据
            JSONArray array1 = jsonObject.getJSONArray("1");
            int len = array1.length();
            for (int i=0; i<len; i+=2) {
                String sx = array1.getString(i);
                String sy = array1.getString(i+1);

                FMGeoCoord coord = new FMGeoCoord(1,
                        new FMMapCoord(Double.parseDouble(sx), Double.parseDouble(sy)));
                mLocatePoints.add(coord);
            }

            // 6层的数据
            JSONArray array6 = jsonObject.getJSONArray("6");
            len = array6.length();
            for (int i=0; i<len; i+=2) {
                String sx = array6.getString(i);
                String sy = array6.getString(i+1);

                FMGeoCoord coord = new FMGeoCoord(6,
                        new FMMapCoord(Double.parseDouble(sx), Double.parseDouble(sy)));
                mLocatePoints.add(coord);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (!isMapLoaded)
            return;

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (mLocateTask != null) {
            mLocateTask.setOnLocateCallback(null);
            mLocateTask = null;
        }

        // 释放资源
        mNavigation.clear();
        mNavigation.release();

        // 清除定位点集合
        mLocatePoints.clear();

        super.onBackPressed();
    }

    @Override
    public void onMapInitSuccess(String path) {
        super.onMapInitSuccess(path);

        // 创建模拟导航对象
        mNavigation = new FMActualNavigation(mFMMap);

        // 创建模拟导航配置对象
        mNaviOption = new FMNaviOption();

        // 设置不跟随模式，默认跟随
        mNaviOption.setFollowPosition(true);

        // 设置不跟随角度（第一人视角），默认跟随
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
        // 真实导航
        final FMActualNavigation actualNavigation = (FMActualNavigation) mNavigation;
        actualNavigation.start();

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (mLocateTask != null) {
            mLocateTask.setOnLocateCallback(null);
            mLocateTask = null;
        }

        mTimer = new Timer();
        mLocateTask = new SimulateLocateTask((List<FMGeoCoord>) mLocatePoints.clone());
        mLocateTask.setOnLocateCallback(new SimulateLocateTask.OnLocateCallback() {
            @Override
            public void onLocate(FMGeoCoord geoCoord) {

                // 里面会做路径约束
                actualNavigation.locate(geoCoord, 0);
            }
        });
        mTimer.schedule(mLocateTask, 50, 1000);
    }

    @Override
    public void updateLocateGroupView() {
        String groupName = ConvertUtils.convertToFloorName(mFMMap, mFMMap.getFocusGroupId());
        ViewHelper.setViewText(FMNavigationConstraint.this, R.id.btn_group_control, groupName);
    }

    /**
     * 更新真实定位点。
     *
     * @param coord 坐标
     */
    private void updateRealMarker(FMGeoCoord coord, float angle) {
        if (mRealMarker == null) {
            mRealMarker = ViewHelper.buildLocationMarker(coord.getGroupId(), coord.getCoord(), angle,
                    R.drawable.ic_location_red);
            mLocationLayer.addMarker(mRealMarker);
        } else {
            mRealMarker.updateAngleAndPosition(coord.getGroupId(), angle, coord.getCoord());
        }
    }

    /**
     * 更新处理过定位点。
     *
     * @param coord 坐标
     */
    private void updateHandledMarker(FMGeoCoord coord, float angle) {
        if (mHandledMarker == null) {
            mHandledMarker = ViewHelper.buildLocationMarker(coord.getGroupId(), coord.getCoord(), angle,
                    R.drawable.ic_location_green);
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
                updateRealMarker(navigationInfo.getRealPosition(), navigationInfo.getAngle());
            }
        });
    }

    @Override
    public void onComplete() {
        // nothing
    }

}
