package com.fengmap.FMDemoNavigationAdvance.map;

import android.os.Bundle;
import android.view.View;

import com.fengmap.FMDemoNavigationAdvance.R;
import com.fengmap.FMDemoNavigationAdvance.utils.ConvertUtils;
import com.fengmap.FMDemoNavigationAdvance.utils.FileUtils;
import com.fengmap.FMDemoNavigationAdvance.utils.SimulateLocateTask;
import com.fengmap.FMDemoNavigationAdvance.utils.ViewHelper;
import com.fengmap.android.analysis.navi.FMActualNavigation;
import com.fengmap.android.analysis.navi.FMNaviOption;
import com.fengmap.android.analysis.navi.FMNavigationInfo;
import com.fengmap.android.analysis.navi.OnFMNavigationListener;
import com.fengmap.android.map.geometry.FMGeoCoord;
import com.fengmap.android.map.geometry.FMMapCoord;
import com.fengmap.android.map.marker.FMLocationMarker;
import com.fengmap.android.utils.FMLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;

/**
 * 路径偏移重新规划。
 * <p>模拟定位点偏移导航规划线是否约束定位点至导航线上：如果定位点离导航线小于5米，则认为行走人员
 * 还在规划的导航线上行走，并将定位点约束至导航线；如果偏移距离大于5米，则提示用户是否重新规划线路。
 *
 * @author yanbin@fengmap.com
 * @version 2.1.0
 */
public class FMNavigationRecalc extends BaseActivity implements View.OnClickListener, OnFMNavigationListener {
    // 最大约束距离
    private static final float DEFAULT_MAX_DISTANCE = 5.0f;

    // 定位标注
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
        setContentView(R.layout.activity_navigation_recalc);

        mStartCoord = new FMGeoCoord(2, new FMMapCoord(12961607.949377403, 4861858.712984723));
        mEndCoord = new FMGeoCoord(2, new FMMapCoord(12961662.565714367, 4861818.338024983));

        initLocateData();
    }

    // 初始化定位数据
    void initLocateData() {
        byte[] content = FileUtils.readAssetsFile(this, "simulationLocation.json");
        String str = new String(content);
        try {
            JSONObject jsonObject = new JSONObject(str);

            // 1层的数据
            JSONArray array1 = jsonObject.getJSONArray("offset");
            int len = array1.length();
            for (int i=0; i<len; i+=2) {
                String sx = array1.getString(i);
                String sy = array1.getString(i+1);

                FMGeoCoord coord = new FMGeoCoord(2,
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

        mFMMap.setFocusByGroupId(2, null);

        // 创建模拟导航对象
        mNavigation = new FMActualNavigation(mFMMap);

        // 创建模拟导航配置对象
        mNaviOption = new FMNaviOption();

        // 设置跟随模式，默认跟随
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
    public void updateLocateGroupView() {
        String groupName = ConvertUtils.convertToFloorName(mFMMap, mFMMap.getFocusGroupId());
        ViewHelper.setViewText(FMNavigationRecalc.this, R.id.btn_group_control, groupName);
    }

    /**
     * 更新约束定位点。
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
    public void startNavigation() {
        final FMActualNavigation actualNavigation = (FMActualNavigation) mNavigation;
        actualNavigation.start();

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        int index = 0;

        if (mLocateTask != null) {
            index = mLocateTask.getIndex();
            mLocateTask.setOnLocateCallback(null);
            mLocateTask = null;
        }

        mTimer = new Timer();
        mLocateTask = new SimulateLocateTask((ArrayList<FMGeoCoord>) mLocatePoints.clone());
        mLocateTask.setIndex(index);

        mLocateTask.setOnLocateCallback(new SimulateLocateTask.OnLocateCallback() {
            @Override
            public void onLocate(FMGeoCoord geoCoord) {

                // 里面会做路径约束
                actualNavigation.locate(geoCoord, 0);
            }
        });
        mTimer.schedule(mLocateTask, 50, 1000);
    }

    /**
     * 开始模拟行走路线
     *
     * @param currentCoord 当前已经偏移的坐标
     */
    private void scheduleResetRoute(FMGeoCoord currentCoord) {
        // 取消任务
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        mNavigation.clear();

        analyzeNavigation(currentCoord, mEndCoord);
        if (isRouteCalculated) {
            startNavigation();
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
                // 定位位置的偏移距离
                double offset = navigationInfo.getOffsetDistance();

                FMLog.le("offset distance", ""+ offset);

                // 被约束过的点
                FMGeoCoord contraintedCoord = navigationInfo.getPosition();
                if (offset > DEFAULT_MAX_DISTANCE) {
                    // 重新规划路径
                    scheduleResetRoute(navigationInfo.getRealPosition());
                    return;
                }

                // 更新定位标志物
                updateHandledMarker(contraintedCoord, navigationInfo.getAngle());
            }
        });
    }

    @Override
    public void onComplete() {
        // nothing
        if (mLocateTask != null) {
            mLocateTask.setIndex(0);
        }

        // 为重复"开始导航"，做准备
        mNavigation.clear();
        analyzeNavigation(mStartCoord, mEndCoord);
    }
}
