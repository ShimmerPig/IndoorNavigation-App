package com.fengmap.FMDemoNavigationAdvance;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fengmap.FMDemoNavigationAdvance.map.BaseActivity;
import com.fengmap.FMDemoNavigationAdvance.map.FMNavigationApplication;
import com.fengmap.FMDemoNavigationAdvance.map.FMNavigationConstraint;
import com.fengmap.FMDemoNavigationAdvance.map.FMNavigationFirstPerson;
import com.fengmap.FMDemoNavigationAdvance.map.FMNavigationRecalc;
import com.fengmap.FMDemoNavigationAdvance.map.FMNavigationThirdPerson;
import com.fengmap.FMDemoNavigationAdvance.utils.ViewHelper;
import com.fengmap.android.FMMapSDK;

/**
 * 主页面
 * <p>在Android6.0以上版本使用fengmap地图之前，应注意android.permission.WRITE_EXTERNAL_STORAGE、
 * permission:android.permission.READ_EXTERNAL_STORAGE权限申请
 *
 * @author hezutao@fengmap.com
 * @version 2.0.0
 */
public class MapApiDemoMain extends Activity {

    private final DemoInfo[] mDemos = {
            //這裡的起點默認為已知的
            //需要進行的是終點的決定
            //進行查詢工作
        new DemoInfo(R.string.demo_title_main_search,R.string.demo_title_search,MapSearchMain.class),
        new DemoInfo(R.string.demo_title_main_constraint, R.string.demo_title_constraint, FMNavigationConstraint.class),
        new DemoInfo(R.string.demo_title_main_recalc, R.string.demo_title_recalc, FMNavigationRecalc.class),
        new DemoInfo(R.string.demo_title_main_first_person, R.string.demo_title_first_person, FMNavigationFirstPerson.class),
        new DemoInfo(R.string.demo_title_main_third_person, R.string.demo_title_third_person, FMNavigationThirdPerson.class),
        new DemoInfo(R.string.demo_title_main_application, R.string.demo_title_application, FMNavigationApplication.class)
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView titleView = ViewHelper.getView(MapApiDemoMain.this, R.id.navigation_bar);
        titleView.setText(getString(R.string.demo_title_main, FMMapSDK.getVersion()));

        ListView listView = (ListView) findViewById(R.id.listView);
        // 添加ListItem，设置事件响应
        listView.setAdapter(new DemoListAdapter());
        listView.setOnItemClickListener(new OnItemClickListener() {
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
        Intent intent = new Intent(MapApiDemoMain.this, mDemos[position].mClazz);
        intent.putExtra(BaseActivity.EXTRA_TITLE, mDemos[position].mTitle);
        this.startActivity(intent);
    }

    /**
     * Demo列表适配器
     */
    private class DemoListAdapter extends BaseAdapter {

        @Override
        public View getView(int index, View convertView, ViewGroup parent) {
            convertView = View.inflate(MapApiDemoMain.this,
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
        private final int mMainTitle;
        private final int mTitle;
        private final Class<? extends Activity> mClazz;

        DemoInfo(int mainTitle, int title,
                 Class<? extends Activity> clazz) {
            this.mMainTitle = mainTitle;
            this.mTitle = title;
            this.mClazz = clazz;
        }
    }
}