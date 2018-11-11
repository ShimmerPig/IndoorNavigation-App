package com.fengmap.FMDemoNavigationAdvance.widget;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * 软键盘工具类
 *
 * @author hezutao@fengmap.com
 * @version 2.0.0
 */
public class KeyBoardUtils {
    /**
     * 打卡软键盘
     *
     * @param editText 输入框
     * @param context  上下文
     */
    public static void openKeybord(EditText editText, Context context) {
        InputMethodManager imm = (InputMethodManager) context
            .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.RESULT_SHOWN);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
            InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /**
     * 关闭软键盘
     *
     * @param editText 输入框
     * @param context  上下文
     */
    public static void closeKeybord(EditText editText, Context context) {
        InputMethodManager imm = (InputMethodManager) context
            .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    /**
     * 关闭软键盘
     *
     * @param windowToken 视图
     * @param context     上下文
     */
    public static void hideSoftInputMode(Context context, View windowToken) {
        InputMethodManager imm = ((InputMethodManager) context
            .getSystemService(Context.INPUT_METHOD_SERVICE));
        imm.hideSoftInputFromWindow(windowToken.getWindowToken(),
            InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 弹出软键盘
     *
     * @param context     上下文
     * @param windowToken 视图
     */
    public static void showSoftInputMode(Context context, View windowToken) {
        final InputMethodManager imm = (InputMethodManager) context
            .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(windowToken, InputMethodManager.SHOW_FORCED);
    }

}
