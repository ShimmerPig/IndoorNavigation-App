package com.fengmap.FMDemoNavigationAdvance.widget;

import android.view.View;

import com.fengmap.FMDemoNavigationAdvance.utils.TaskFilter;

/**
 * 重复单击监听
 *
 * @author hezutao@fengmap.com
 * @version 2.0.0
 */
public abstract class OnSingleClickListener implements View.OnClickListener {

    @Override
    public void onClick(View view) {
        if (TaskFilter.filter()) {
            onSingleClick(view);
        }
    }

    /**
     * 用于为外部提供的覆写方法，以实现点击事件
     *
     * @param view 被点击的控件
     */
    public abstract void onSingleClick(View view);
}
