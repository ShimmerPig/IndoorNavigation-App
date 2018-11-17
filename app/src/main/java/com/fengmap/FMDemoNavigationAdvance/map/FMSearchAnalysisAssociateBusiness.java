package com.fengmap.FMDemoNavigationAdvance.map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fengmap.FMDemoNavigationAdvance.R;
import com.fengmap.FMDemoNavigationAdvance.adapter.SearchStoreAdapter;
import com.fengmap.FMDemoNavigationAdvance.bean.Store;
import com.fengmap.FMDemoNavigationAdvance.utils.ConvertUtils;
import com.fengmap.FMDemoNavigationAdvance.utils.FileUtils;
import com.fengmap.FMDemoNavigationAdvance.utils.HttpUtil;
import com.fengmap.FMDemoNavigationAdvance.utils.JSONUtils;
import com.fengmap.FMDemoNavigationAdvance.utils.Utility;
import com.fengmap.FMDemoNavigationAdvance.utils.ViewHelper;
import com.fengmap.FMDemoNavigationAdvance.widget.ImageViewCheckBox;
import com.fengmap.FMDemoNavigationAdvance.widget.KeyBoardUtils;
import com.fengmap.FMDemoNavigationAdvance.widget.SearchBar;
import com.fengmap.android.analysis.navi.FMActualNavigation;
import com.fengmap.android.analysis.navi.FMNaviOption;
import com.fengmap.android.analysis.navi.FMNavigationInfo;
import com.fengmap.android.analysis.navi.FMSimulateNavigation;
import com.fengmap.android.analysis.navi.OnFMNavigationListener;
import com.fengmap.android.map.FMViewMode;
import com.fengmap.android.map.geometry.FMGeoCoord;
import com.fengmap.android.map.geometry.FMMapCoord;
import com.fengmap.android.map.marker.FMImageMarker;
import com.fengmap.android.map.marker.FMLocationMarker;
import com.fengmap.android.map.marker.FMModel;
import com.fengmap.android.widget.FMSwitchFloorComponent;
import com.google.gson.reflect.TypeToken;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechSynthesizer;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FMSearchAnalysisAssociateBusiness extends BaseSearchActivity implements SearchBar.OnSearchResultCallback,
        AdapterView.OnItemClickListener,
        View.OnClickListener,
        ImageViewCheckBox.OnCheckStateChangedListener,
        OnFMNavigationListener {
    private SearchBar mSearchBar;
    private SearchStoreAdapter mSearchAdapter;
    private List<Store> mStores;
    private FMModel mClickedModel;

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
    //构造真实的导航对象
    public static FMActualNavigation sActualNavigation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fmsearch_analysis_associate_business);
        //搜索框
        mSearchBar = ViewHelper.getView(FMSearchAnalysisAssociateBusiness.this, R.id.search_title_bar);
        mSearchBar.setOnSearchResultCallback(this);
        mSearchBar.setOnItemClickListener(this);

        //设置终点
        mEndCoord = new FMGeoCoord(2, new FMMapCoord(12961662.565714367, 4861818.338024983));
        //创建语音合成SpeechSynthesizer对象
        createSynthesizer();
    }

    @Override
    public void onMapInitSuccess(String path) {
        super.onMapInitSuccess(path);
        //TODO返回的是一个store的数组--mStores
        readStoresFromJson();

        //检查楼层切换控件是否为null
        if (mSwitchFloorComponent == null) {
            //初始化楼层切换控件
            initSwitchFloorComponent();
        }
        //为每个控件设置监听事件
        ViewHelper.setViewCheckedChangeListener(FMSearchAnalysisAssociateBusiness.this, R.id.btn_pig_3d, this);
        ViewHelper.setViewCheckedChangeListener(FMSearchAnalysisAssociateBusiness.this, R.id.btn_pig_locate, this);
        ViewHelper.setViewCheckedChangeListener(FMSearchAnalysisAssociateBusiness.this, R.id.btn_pig_view, this);

        //创建真实导航对象
        mNavigation=new FMActualNavigation(mMap);

        //创建真实导航配置对象
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
        //将父类的导航对象转成一个真实的导航对象
        FMActualNavigation actualNavigation=(FMActualNavigation)mNavigation;
        //子类中真实导航对象与父类建立连接
        sActualNavigation=actualNavigation;
        //开始导航
        actualNavigation.start();
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
        int groupSize = mMap.getFMMapInfo().getGroupSize();
        int position = groupSize - mMap.getFocusGroupId();
        mSwitchFloorComponent.setSelected(position);
    }
    /**
     * 设置地图2、3D效果
     */
    private void setViewMode() {
        if (mMap.getCurrentFMViewMode() == FMViewMode.FMVIEW_MODE_2D) {
            mMap.setFMViewMode(FMViewMode.FMVIEW_MODE_3D);
        } else {
            mMap.setFMViewMode(FMViewMode.FMVIEW_MODE_2D);
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
        // 剩余时间
        int timeByWalk = ConvertUtils.getTimeByWalk(info.getSurplusDistance());

        // 导航路段描述
        String description = info.getNaviText();

        String viewText = getResources().getString(R.string.label_walk_format, info.getSurplusDistance(),
                timeByWalk, description);

        ViewHelper.setViewText(FMSearchAnalysisAssociateBusiness.this, R.id.txt_pig_info, viewText);

        if (!description.equals(mLastDescription)) {
            mLastDescription = description;
            startSpeaking(mLastDescription);
        }
    }

    private void updateNavigationOption() {
        mNaviOption.setFollowAngle(mIsFirstView);
        mNaviOption.setFollowPosition(mHasFollowed);
    }

    //设置控件的监听事件
    @Override
    public void onCheckStateChanged(View view, boolean isChecked) {
        switch (view.getId()) {
            case R.id.btn_pig_3d: {
                setViewMode();
            }
            break;
            case R.id.btn_pig_view: {
                setViewState(isChecked);
            }
            break;
            case R.id.btn_pig_locate: {
                setFollowState(isChecked);
            }
            break;
            default:
                break;
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
                mMap.setFocusByGroupId(groupId, null);
                return true;
            }
        });

        mSwitchFloorComponent.setFloorDataFromFMMapInfo(mMap.getFMMapInfo(), mMap.getFocusGroupId());

        addSwitchFloorComponent();
    }

    /**
     * 添加楼层切换按钮
     */
    private void addSwitchFloorComponent() {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        FrameLayout viewGroup = (FrameLayout) findViewById(R.id.layout_pig_group_control);
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //关闭软键盘
        KeyBoardUtils.closeKeybord(mSearchBar.getCompleteText(), FMSearchAnalysisAssociateBusiness.this);

        Store store = (Store) parent.getItemAtPosition(position);
        displayStoreView(store);

        //切换楼层
        int groupId = Integer.parseInt(store.GROUP);
        if (groupId != mMap.getFocusGroupId()) {
            mMap.setFocusByGroupId(groupId, null);
        }

        //查找模型
        FMModel model = mMap.getFMLayerProxy().queryFMModelByFid(store.FID);
        FMMapCoord mapCoord = model.getCenterMapCoord();
        mMap.moveToCenter(mapCoord, false);

        //添加图片
        clearImageMarker();
        FMImageMarker imageMarker = ViewHelper.buildImageMarker(getResources(), mapCoord);
        mImageLayers.get(groupId).addMarker(imageMarker);

        clearStatus(model);
    }

    @Override
    public void onSearchCallback(String keyword) {
        //地图未显示前，不执行搜索事件
        boolean isCompleted = mMap.getMapFirstRenderCompleted();
        if (!isCompleted) {
            return;
        }

        ArrayList<Store> datas = queryStoreByKeyword(keyword);
        if (mSearchAdapter == null) {
            mSearchAdapter = new SearchStoreAdapter(FMSearchAnalysisAssociateBusiness.this, datas);
            mSearchBar.setAdapter(mSearchAdapter);
        } else {
            mSearchAdapter.setDatas(datas);
            mSearchAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 通过关键字查询商品
     *
     * @param keyword 关键字
     * @return
     */
    private ArrayList<Store> queryStoreByKeyword(String keyword) {
        ArrayList<Store> result = new ArrayList<>();
        for (Store store : mStores) {
            if (store.NAME.contains(keyword)) {
                result.add(store);
            }
        }
        return result;
    }

    /**
     * 获取商品信息
     *
     *
     * @return
     */
    //TODO--第一次从服务器上读取
    //TODO--以后从数据库中读取数据
    private void readStoresFromJson() {
//        String json = FileUtils.readStringFromAssets(getApplicationContext(), fileName);
//        return JSONUtils.fromJson(json, new TypeToken<List<Store>>() {
//        });
        //优先从数据库中查询
        mStores=LitePal.findAll(Store.class);
//        if(mStores.size()>0){
//            //return storeList;
//        }
        //查不到再去服务器上查询
        if(mStores.size()<=0){
            //查询的url
            String address="http://www.shimmerpig.natapp1.cc/app/buyer/store/applist";
            //传入的是查询的url以及要查询的数据类型
            //将服务器上的数据save到数据库中，然后再从数据库中查询
            queryFromServer(address,"store");
        }
        //return storeList;
    }

    //进度对话框
    private ProgressDialog progressDialog;

    //显示
    private void showProgressDialog(){
        if(progressDialog==null){
            progressDialog=new ProgressDialog(this);
            progressDialog.setMessage("PigPig正在从服务器上读取数据...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    Context context=this;
    //关闭
    private void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }

    //将服务器上的数据save到数据库中，然后再从数据库中查询
    private void queryFromServer(String address,final String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(context,"加载失败...",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean resault=false;
                if("store".equals(type)){
                    resault=Utility.handleStoreResponse(responseText);
                }
                if(resault){
                    closeProgressDialog();
                    mStores=LitePal.findAll(Store.class);
                }
            }
        });
    }

    /**
     * 显示商品详情
     *
     * @param store 商品
     */
    private void displayStoreView(Store store) {
        ViewHelper.setViewVisibility(FMSearchAnalysisAssociateBusiness.this, R.id.bottom_view, View.VISIBLE);

        TextView textView = ViewHelper.getView(FMSearchAnalysisAssociateBusiness.this, R.id.txt_content);
        textView.setText("FID: " + store.FID + "\nNAME: " + store.NAME + "\n" +
                "FLOOR: " + store.FLOOR + "\nGROUP: " + store.GROUP + "\nTYPE: " + store.TYPE);
    }

    /**
     * 清除模型的点击效果
     *
     * @param model 模型
     */
    private void clearStatus(FMModel model) {
        if (mClickedModel != null) {
            mClickedModel.setSelected(false);
        }
        mClickedModel = model;
        mClickedModel.setSelected(true);
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
                mMap.setFocusByGroupId(currGroupId, null);
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
                ViewHelper.setViewText(FMSearchAnalysisAssociateBusiness.this, R.id.txt_info, info);

                startSpeaking(description);
            }
        });
    }

}
