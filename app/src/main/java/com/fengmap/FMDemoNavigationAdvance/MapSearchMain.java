package com.fengmap.FMDemoNavigationAdvance;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fengmap.FMDemoNavigationAdvance.map.BaseSearchActivity;
import com.fengmap.FMDemoNavigationAdvance.map.FMSearchAnalysisAssociateBusiness;
import com.fengmap.FMDemoNavigationAdvance.utils.ViewHelper;
import com.fengmap.android.FMMapSDK;

public class MapSearchMain extends Activity {
    private final DemoInfo[]mDemos={
//            new DemoInfo(R.string.demo_title_main_base, R.string.demo_title_base, FMSearchAnalysisBaseInfo.class),
//            new DemoInfo(R.string.demo_title_main_type, R.string.demo_title_type, FMSearchAnalysisByType.class),
//            new DemoInfo(R.string.demo_title_main_facility, R.string.demo_title_facility, FMSearchAnalysisByFacility.class),
//            new DemoInfo(R.string.demo_title_main_input, R.string.demo_title_input, FMSearchAnalysisByInput.class),
            new DemoInfo(R.string.demo_title_main_business, R.string.demo_title_business, FMSearchAnalysisAssociateBusiness.class),
//            new DemoInfo(R.string.demo_title_main_bound, R.string.demo_title_bound, FMSearchAnalysisBound.class),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_search_main);

        TextView titleView = ViewHelper.getView(MapSearchMain.this, R.id.navigation_bar);
        titleView.setText(getString(R.string.demo_title_main, FMMapSDK.getVersion()));

        ListView listView = (ListView) findViewById(R.id.listView);
        // 添加ListItem，设置事件响应
        listView.setAdapter(new DemoListAdapter());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClick(position);
            }
        });

    }

    /**
     * 列表点击
     *
     * @param position 列表索引
     */
    void onListItemClick(int position) {
        Intent intent = new Intent(MapSearchMain.this, mDemos[position].mClazz);
        intent.putExtra(BaseSearchActivity.EXTRA_TITLE, mDemos[position].mTitle);
        this.startActivity(intent);
    }


    /**
     * Demo列表适配器
     */
    private class DemoListAdapter extends BaseAdapter {

        @Override
        public View getView(int index, View convertView, ViewGroup parent) {
            convertView = View.inflate(MapSearchMain.this,
                    R.layout.demo_info_item, null);
            TextView title = (TextView) convertView.findViewById(R.id.title);
            title.setText(mDemos[index].mMainTitle);
            return convertView;
        }

        @Override
        public int getCount() {
            return mDemos.length;
        }

        @Override
        public Object getItem(int index) {
            return mDemos[index];
        }

        @Override
        public long getItemId(int id) {
            return id;
        }
    }
    /**
     * Demo标题与要跳转的类
     */
    class DemoInfo {
        private final int mTitle;
        private final int mMainTitle;
        private final Class<? extends Activity> mClazz;

        DemoInfo(int mainTitle, int title,
                 Class<? extends Activity> clazz) {
            this.mMainTitle = mainTitle;
            this.mTitle = title;
            this.mClazz = clazz;
        }
    }
}
