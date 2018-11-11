package com.fengmap.FMDemoNavigationAdvance.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.fengmap.FMDemoNavigationAdvance.R;
import com.fengmap.FMDemoNavigationAdvance.widget.OnSingleClickListener;
import com.fengmap.android.map.FMGroupInfo;
import com.fengmap.android.map.FMMap;
import com.fengmap.android.map.FMMapInfo;
import com.fengmap.android.map.marker.FMModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索结果
 *
 * @author hezutao@fengmap.com
 * @version 2.0.0
 */
public class AnalysisListAdapter extends CommonAdapter<FMModel> {

    private FMModel mLastClicked;
    private FMMap mMap;

    public AnalysisListAdapter(Context context, FMMap map, List<FMModel> datas) {
        super(context, datas, R.layout.item_analysis_result);
        this.mMap = map;
    }

    @Override
    public void convert(ViewHolder viewHolder, final FMModel model, int position) {
        String modelName = model.getName();
        if (TextUtils.isEmpty(modelName)) {
            modelName = "空";
        }
        String name = (position + 1) + "、" + modelName;
        viewHolder.setText(R.id.title, name);

        viewHolder.getConvertView().setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                onItemClick(model);
            }
        });

        String groupName = convertToGroupName(model.getGroupId());
        viewHolder.setText(R.id.group, groupName);
    }

    /**
     * 点击时间处理
     *
     * @param model 模型
     */
    private void onItemClick(FMModel model) {
        //清除上次聚焦效果
        if (!model.equals(mLastClicked)) {
            if (mLastClicked != null) {
                mLastClicked.setSelected(false);
            }

            this.mLastClicked = model;
            this.mLastClicked.setSelected(true);
        }

        //切换楼层并居中
        if (mMap.getFocusGroupId() != model.getGroupId()) {
            mMap.setFocusByGroupId(model.getGroupId(), null);
        }
        mMap.moveToCenter(model.getCenterMapCoord(), true);
    }

    /**
     * 将groupId转换为楼层名
     *
     * @param groupId 楼层id
     * @return
     */
    private String convertToGroupName(int groupId) {
        FMMapInfo mapInfo = mMap.getFMMapInfo();

        ArrayList<FMGroupInfo> groups = mapInfo.getGroups();
        for (int i = 0; i < groups.size(); i++) {
            FMGroupInfo groupInfo = groups.get(i);
            if (groupInfo.getGroupId() == groupId) {
                return groupInfo.getGroupName().toUpperCase();
            }
        }
        return null;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        if (mLastClicked != null) {
            mLastClicked.setSelected(false);
            mLastClicked = null;
        }
    }

}

