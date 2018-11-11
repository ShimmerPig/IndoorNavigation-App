package com.fengmap.FMDemoNavigationAdvance.adapter;
import android.content.Context;

import com.fengmap.FMDemoNavigationAdvance.R;
import com.fengmap.android.map.marker.FMModel;

import java.util.ArrayList;

/**
 * 搜索列表适配
 *
 * @author hezutao@fengmap.com
 * @version 2.0.0
 */
public class SearchBarAdapter extends CommonFilterAdapter<FMModel> {

    public SearchBarAdapter(Context context, ArrayList<FMModel> mapModels) {
        super(context, mapModels, R.layout.layout_item_model_search);
    }

    @Override
    public void convert(ViewHolder viewHolder, FMModel mapNode, int position) {
        viewHolder.setText(R.id.txt_model_name, mapNode.getName());
    }

}