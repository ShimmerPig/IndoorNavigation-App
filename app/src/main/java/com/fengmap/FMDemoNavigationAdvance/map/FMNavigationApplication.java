package com.fengmap.FMDemoNavigationAdvance.map;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.fengmap.FMDemoNavigationAdvance.R;
import com.fengmap.FMDemoNavigationAdvance.location.StepCountLocationService;
import com.fengmap.FMDemoNavigationAdvance.utils.ConvertUtils;
import com.fengmap.FMDemoNavigationAdvance.utils.ViewHelper;
import com.fengmap.FMDemoNavigationAdvance.widget.ImageViewCheckBox;
import com.fengmap.android.analysis.navi.FMActualNavigation;
import com.fengmap.android.analysis.navi.FMNaviOption;
import com.fengmap.android.analysis.navi.FMNavigationInfo;
import com.fengmap.android.analysis.navi.FMSimulateNavigation;
import com.fengmap.android.analysis.navi.OnFMNavigationListener;
import com.fengmap.android.map.FMViewMode;
import com.fengmap.android.map.geometry.FMGeoCoord;
import com.fengmap.android.map.geometry.FMMapCoord;
import com.fengmap.android.map.marker.FMLocationMarker;
import com.fengmap.android.widget.FMSwitchFloorComponent;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechSynthesizer;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yzq.zxinglibrary.android.CaptureActivity;

import java.util.List;

import com.yzq.zxinglibrary.common.Constant;
/**
 * 导航实例
 * <p>模拟真实导航如何使用地图进行导航显示
 *
 * @author yangbin@fengmap.com
 * @version 2.1.0
 */
public class FMNavigationApplication extends BaseActivity implements
        View.OnClickListener,
        ImageViewCheckBox.OnCheckStateChangedListener,
        OnFMNavigationListener {

    private int REQUEST_CODE_SCAN = 111;

    // 约束过的定位标注
    private FMLocationMarker mHandledMarker;

    // 是否为第一人称
    private boolean mIsFirstView = true;

    // 是否为跟随状态
    private boolean mHasFollowed = true;

    // 总共距离
    private double mTotalDistance;

    // 楼层切换控件
    private FMSwitchFloorComponent mSwitchFloorComponent;

    // 上一次文字描述
    private String mLastDescription;

    private SpeechSynthesizer mTts;

    //真实的导航对象
    public static FMActualNavigation mActualNavigation;


    private boolean service_is_run =true;
    MyReceiver receiver;
    private static Handler viewHandler;//用于更新UI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_app);
        mEndCoord = new FMGeoCoord(2, new FMMapCoord(12961662.565714367, 4861818.338024983));
        createSynthesizer();

        viewHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 10086:
                        Bundle bundle = msg.getData();
                        double ax = bundle.getDouble("ax");
                        double ay = bundle.getDouble("ay");
                        int az = bundle.getInt("az");
                        float degree=bundle.getFloat("degree");
                        //接入真实定位位置
                        //里面会做路径约束, 约束后的数据通过导航监听回调返回
                        //一直在调locate--里面会调onwalking
                        Log.i("pig","调locate时的ax:"+ax+"ay:"+ay+"degree:"+degree);
                        FMNavigationApplication.mActualNavigation.locate(new FMGeoCoord(1,
                                new FMMapCoord(ax, ay)),degree);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    public void onMapInitSuccess(String path) {
        super.onMapInitSuccess(path);

        if (mSwitchFloorComponent == null) {
            initSwitchFloorComponent();
        }

        ViewHelper.setViewCheckedChangeListener(FMNavigationApplication.this, R.id.btn_3d, this);
        ViewHelper.setViewCheckedChangeListener(FMNavigationApplication.this, R.id.btn_locate, this);
        ViewHelper.setViewCheckedChangeListener(FMNavigationApplication.this, R.id.btn_view, this);
        ViewHelper.setViewCheckedChangeListener(FMNavigationApplication.this,R.id.btn_sao,this);

        // 创建模拟导航对象
        //mNavigation = new FMSimulateNavigation(mFMMap);
        //创建真实导航对象
        mNavigation=new FMActualNavigation((mFMMap));
        mActualNavigation=(FMActualNavigation)mNavigation;

        // 创建模拟导航配置对象
        mNaviOption = new FMNaviOption();

        // 设置跟随模式，默认跟随
        mNaviOption.setFollowPosition(mHasFollowed);

        // 设置跟随角度（第一人视角），默认跟随
        mNaviOption.setFollowAngle(mIsFirstView);

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

        // 总长
        mTotalDistance = mNavigation.getSceneRouteLength();

        isMapLoaded = true;
    }


    @Override
    public void startNavigation() {
        Log.i("pig","开始导航");
       // FMSimulateNavigation simulateNavigation = (FMSimulateNavigation) mNavigation;
//        // 3米每秒。
       // simulateNavigation.simulate(3.0f);
        FMActualNavigation actualNavigation=(FMActualNavigation)mNavigation;
        actualNavigation.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FMNavigationApplication.mActualNavigation.locate(new FMGeoCoord(1,
                        new FMMapCoord(12961650.576796599, 4861814.63807118)),270);
            }
        },2000);
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
    public void updateLocateGroupView() {
        int groupSize = mFMMap.getFMMapInfo().getGroupSize();
        int position = groupSize - mFMMap.getFocusGroupId();
        mSwitchFloorComponent.setSelected(position);
    }

    /**
     * 设置地图2、3D效果
     */
    private void setViewMode() {
        if (mFMMap.getCurrentFMViewMode() == FMViewMode.FMVIEW_MODE_2D) {
            mFMMap.setFMViewMode(FMViewMode.FMVIEW_MODE_3D);
        } else {
            mFMMap.setFMViewMode(FMViewMode.FMVIEW_MODE_2D);
        }
    }

    /**
     * 设置是否为第一人称
     *
     * @param enable true 第一人称
     *               false 第三人称
     */
    private void setViewState(boolean enable) {
        this.mIsFirstView = !enable;
        setFloorControlEnable();
    }

    /**
     * 设置跟随状态
     *
     * @param enable true 跟随
     *               false 不跟随
     */
    private void setFollowState(boolean enable) {
        mHasFollowed = enable;
        setFloorControlEnable();
    }

    /**
     * 设置楼层控件是否可用
     */
    private void setFloorControlEnable() {
        if (getFloorControlEnable()) {
            mSwitchFloorComponent.close();
            mSwitchFloorComponent.setEnabled(false);
        } else {
            mSwitchFloorComponent.setEnabled(true);
        }
    }

    /**
     * 楼层控件是否可以使用。
     */
    private boolean getFloorControlEnable() {
        return mHasFollowed || mIsFirstView;
    }


    /**
     * 更新行走距离和文字导航。
     */
    private void updateWalkRouteLine(FMNavigationInfo info) {
        Log.i("pig","更新剩余时间以及文字");
        // 剩余时间
        int timeByWalk = ConvertUtils.getTimeByWalk(info.getSurplusDistance());

        // 导航路段描述
        String description = info.getNaviText();

        String viewText = getResources().getString(R.string.label_walk_format, info.getSurplusDistance(),
                timeByWalk, description);

        ViewHelper.setViewText(FMNavigationApplication.this, R.id.txt_info, viewText);

        if (!description.equals(mLastDescription)) {
            mLastDescription = description;
            startSpeaking(mLastDescription);
        }
    }

    private void updateNavigationOption() {
        mNaviOption.setFollowAngle(mIsFirstView);
        mNaviOption.setFollowPosition(mHasFollowed);
    }

    @Override
    public void onCheckStateChanged(View view, boolean isChecked) {
        switch (view.getId()) {
            case R.id.btn_3d: {
                setViewMode();
            }
            break;
            case R.id.btn_view: {
                setViewState(isChecked);
            }
            break;
            case R.id.btn_locate: {
                setFollowState(isChecked);
                //点击这个按钮时开始定位
                try{
                    Getlocation();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            break;
            //点击下面的按钮，开始扫描二维码
            case R.id.btn_sao:
                Toast.makeText(FMNavigationApplication.this,"hh",Toast.LENGTH_SHORT).show();
//                //调用扫描二维码的方法
                AndPermission.with(this)
                        .permission(Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE)
                        .onGranted(new Action() {
                            @Override
                            public void onAction(List<String> permissions) {
                                Intent intent = new Intent(FMNavigationApplication.this, CaptureActivity.class);
                                /*ZxingConfig是配置类
                                 *可以设置是否显示底部布局，闪光灯，相册，
                                 * 是否播放提示音  震动
                                 * 设置扫描框颜色等
                                 * 也可以不传这个参数
                                 * */
//                                ZxingConfig config = new ZxingConfig();
//                                config.setPlayBeep(true);//是否播放扫描声音 默认为true
//                                config.setShake(true);//是否震动  默认为true
//                                config.setDecodeBarCode(true);//是否扫描条形码 默认为true
//                                config.setReactColor(R.color.colorAccent);//设置扫描框四个角的颜色 默认为白色
//                                config.setFrameLineColor(R.color.colorAccent);//设置扫描框边框颜色 默认无色
//                                config.setScanLineColor(R.color.colorAccent);//设置扫描线的颜色 默认白色
//                                config.setFullScreenScan(false);//是否全屏扫描  默认为true  设为false则只会在扫描框中扫描
//                                intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
                                startActivityForResult(intent, REQUEST_CODE_SCAN);
                            }
                        })
                        .onDenied(new Action() {
                            @Override
                            public void onAction(List<String> permissions) {
                                Uri packageURI = Uri.parse("package:" + getPackageName());
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                startActivity(intent);

                                Toast.makeText(FMNavigationApplication.this, "没有权限无法扫描呦", Toast.LENGTH_LONG).show();
                            }
                        }).start();

                break;
            default:
                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                //content中为扫描二维码后得到的编号，这里可以设置为数据库中的store编号
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                Toast.makeText(FMNavigationApplication.this,"扫描结果为："+content,Toast.LENGTH_SHORT).show();
            }
        }
    }
    /**
     * 楼层切换控件初始化
     */
    private void initSwitchFloorComponent() {
        mSwitchFloorComponent = new FMSwitchFloorComponent(this);
        //最多显示6个
        mSwitchFloorComponent.setMaxItemCount(6);
        mSwitchFloorComponent.setEnabled(false);
        mSwitchFloorComponent.setOnFMSwitchFloorComponentListener(new FMSwitchFloorComponent.OnFMSwitchFloorComponentListener() {
            @Override
            public boolean onItemSelected(int groupId, String floorName) {
                mFMMap.setFocusByGroupId(groupId, null);
                return true;
            }
        });

        mSwitchFloorComponent.setFloorDataFromFMMapInfo(mFMMap.getFMMapInfo(), mFMMap.getFocusGroupId());

        addSwitchFloorComponent();
    }

    /**
     * 添加楼层切换按钮
     */
    private void addSwitchFloorComponent() {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        FrameLayout viewGroup = (FrameLayout) findViewById(R.id.layout_group_control);
        viewGroup.addView(mSwitchFloorComponent, lp);
    }


    /**
     * 创建语音合成SpeechSynthesizer对象
     */
    private void createSynthesizer() {
        //1.创建 SpeechSynthesizer 对象, 第二个参数： 本地合成时传 InitListener
        mTts = SpeechSynthesizer.createSynthesizer(this, null);
        //2.合成参数设置，详见《 MSC Reference Manual》 SpeechSynthesizer 类
        //设置发音人（更多在线发音人，用户可参见科大讯飞附录13.2
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan"); //设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "100");//设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围 0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
    }

    /**
     * 开始语音合成
     *
     * @param inputStr 语音合成文字
     */
    private void startSpeaking(String inputStr) {
        mTts.stopSpeaking();
        mTts.startSpeaking(inputStr, null);
    }

    @Override
    public void onBackPressed() {
        if (!isMapLoaded)
            return;

        if (mTts != null) {
            mTts.destroy();
        }

        // 释放资源
        mNavigation.clear();
        mNavigation.release();

        super.onBackPressed();
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
        Log.i("pig","onWalking");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 更新定位标志物
                updateHandledMarker(navigationInfo.getPosition(), navigationInfo.getAngle());
                Log.i("pig",navigationInfo.getPosition()+"");
                Log.i("pig",navigationInfo.getPosition().getCoord()+"");
                // 更新路段显示信息
                updateWalkRouteLine(navigationInfo);

                // 更新导航配置
                updateNavigationOption();
            }
        });
    }

    @Override
    public void onComplete() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String description = "到达目的地";
                String info = getResources().getString(R.string.label_walk_format, 0f,
                        0, description);
                ViewHelper.setViewText(FMNavigationApplication.this, R.id.txt_info, info);

                startSpeaking(description);
            }
        });
    }
    //判断service是否已经启动了
    public static boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.fengmap.FMDemoNavigationAdvance.location.StepCountLocationService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    //定位
    public void Getlocation(){
        Log.i("pig","点击定位");
        boolean ser =isServiceRunning(this);
        if(ser){
            service_is_run=true;
            Log.i("pig","之前就启动service了");
        }else {
            //启动service
            service_is_run=false;
            startService(new Intent(this, StepCountLocationService.class));
        }
        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(30);
        filter.addAction("com.fengmap.FMDemoNavigationAdvance.location.StepCountLocationService");
        registerReceiver(receiver, filter);
    }
    boolean isfirstLocation =true;
    //获取广播数据
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            double ax = bundle.getDouble("ax");
            double ay = bundle.getDouble("ay");
            int az = bundle.getInt("az");
            int astate = bundle.getInt("astate");
            Log.i("pig","x="+ax+"y="+ay+"z="+az);
            //一直在接收广播，发送消息；
            switch (astate) {
                case 10086:
                    Log.i("pig","x="+ax+"y="+ay+"z="+az);
                    Message msg = new Message();
                    msg.what=10086;
                    msg.setData(bundle);//mes利用Bundle传递数据
                    viewHandler.sendMessage(msg);
                    if(isfirstLocation)Toast.makeText(FMNavigationApplication.this,"定位成功了",Toast.LENGTH_SHORT).show();
                    break;
//                case LOCATION_NET_ERROR:
//                    if(isfirstLocation)viewHandler.sendEmptyMessage(LOCATION_NET_ERROR);
//                    break;
//                case LOCATION_NO_IN_MAP:
//                    if(isfirstLocation)viewHandler.sendEmptyMessage(LOCATION_NO_IN_MAP);
//                    break;
//                case LOCATION_LOCATION_IP_ERROR:
//                    if(isfirstLocation)viewHandler.sendEmptyMessage(LOCATION_LOCATION_IP_ERROR);
//                    break;
//                case LOCATION_LOCATION_IP_NOSET:
//                    if(isfirstLocation)viewHandler.sendEmptyMessage(LOCATION_LOCATION_IP_NOSET);
//                    break;
                default:
                    break;
            }
            isfirstLocation=false;
        }
    }
}
