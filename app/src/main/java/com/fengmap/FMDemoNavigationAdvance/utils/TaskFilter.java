package com.fengmap.FMDemoNavigationAdvance.utils;

/**
 * 任务执行过滤
 *
 * @author hezutao@fengmap.com
 * @version 2.0.0
 */
public class TaskFilter {
    private static final long INTERVAL = 500L; // 任务执行时间间隔
    private static long sLastClickTime = 0L; // 上一次执行的时间

    /**
     * 任务过滤
     *
     * @return {@code true}可以执行任务,{@code false}不能执行任务
     */
    public static boolean filter() {
        long time = System.currentTimeMillis();
        long diffTime = time - sLastClickTime;
        if (diffTime > INTERVAL) {
            sLastClickTime = time;
            return true;
        }
        return false;
    }
}
