package com.fengmap.FMDemoNavigationAdvance.bean;

import org.litepal.crud.LitePalSupport;

/**
 * 商品信息
 *
 * @author hezutao@fengmap.com
 * @version 2.0.0
 */
//将其修改为数据库中的store对象
public class Store extends LitePalSupport {
    public String FID;
    public String NAME;
    public String FLOOR;
    public String GROUP;
    public String TYPE;
    public String X;
    public String Y;

    public String getFID() {
        return FID;
    }

    public String getFLOOR() {
        return FLOOR;
    }

    public String getGROUP() {
        return GROUP;
    }

    public String getNAME() {
        return NAME;
    }

    public String getTYPE() {
        return TYPE;
    }

    public String getX() {
        return X;
    }

    public String getY() {
        return Y;
    }

    public void setFID(String FID) {
        this.FID = FID;
    }

    public void setFLOOR(String FLOOR) {
        this.FLOOR = FLOOR;
    }

    public void setGROUP(String GROUP) {
        this.GROUP = GROUP;
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
    }

    public void setTYPE(String TYPE) {
        this.TYPE = TYPE;
    }

    public void setX(String x) {
        X = x;
    }

    public void setY(String y) {
        Y = y;
    }

    @Override
    public String toString() {
        return NAME;
    }
}
