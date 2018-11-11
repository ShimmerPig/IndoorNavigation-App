package com.fengmap.FMDemoNavigationAdvance.adapter;

import android.content.Context;
import android.widget.Filter;
import android.widget.Filterable;

import com.fengmap.android.map.marker.FMModel;

import java.util.ArrayList;
import java.util.List;

/**
 * FilterAdapter 过滤搜索
 *
 * @author hezutao@fengmap.com
 * @version 2.0.0
 */
public abstract class CommonFilterAdapter<T> extends CommonAdapter<T> implements Filterable {

    private ArrayFilter mArrayFilter;
    //未过滤数据
    private ArrayList<T> mUnfiltered;

    public CommonFilterAdapter(Context context, List<T> datas, int itemLayoutId) {
        super(context, datas, itemLayoutId);
    }

    @Override
    public Filter getFilter() {
        if (mArrayFilter == null) {
            mArrayFilter = new CommonFilterAdapter.ArrayFilter();
        }
        return mArrayFilter;
    }

    /**
     * 数据过滤
     */
    private class ArrayFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
            if (mUnfiltered == null) {
                mUnfiltered = new ArrayList<T>(mDatas);
            }

            results.values = mDatas;
            results.count = mDatas.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            mDatas = (List<T>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            if (resultValue instanceof FMModel) {
                FMModel model = (FMModel) resultValue;
                return model.getName();
            }
            return super.convertResultToString(resultValue);
        }
    }
}
