package com.fengmap.FMDemoNavigationAdvance.adapter;

import android.content.Context;

import com.fengmap.FMDemoNavigationAdvance.R;
import com.fengmap.FMDemoNavigationAdvance.bean.Store;

import java.util.ArrayList;

/**
 * 搜索列表适配器
 *
 * @author hezutao@fengmap.com
 * @version 2.0.0
 */
public class SearchStoreAdapter extends CommonFilterAdapter<Store> {

    public SearchStoreAdapter(Context context, ArrayList<Store> goods) {
        super(context, goods, R.layout.layout_item_model_search);
    }

    @Override
    public void convert(ViewHolder viewHolder, Store store, int position) {
        viewHolder.setText(R.id.txt_model_name, store.NAME);
    }
}