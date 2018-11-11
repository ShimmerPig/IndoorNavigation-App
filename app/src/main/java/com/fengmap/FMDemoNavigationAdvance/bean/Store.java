package com.fengmap.FMDemoNavigationAdvance.bean;

/**
 * 商品信息
 *
 * @author hezutao@fengmap.com
 * @version 2.0.0
 */
public class Store {
    public String FID;
    public String NAME;
    public String FLOOR;
    public String GROUP;
    public String TYPE;
    public String X;
    public String Y;

    @Override
    public String toString() {
        return NAME;
    }
}
