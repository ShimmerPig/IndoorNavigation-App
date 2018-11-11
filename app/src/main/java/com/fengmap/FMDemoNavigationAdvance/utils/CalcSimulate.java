package com.fengmap.FMDemoNavigationAdvance.utils;

import com.fengmap.android.map.geometry.FMMapCoord;

import java.util.ArrayList;
import java.util.Random;

/**
 * 模拟计算
 *
 * @author hezutao@fengmap.com
 * @version 2.0.0
 */
public class CalcSimulate {

    private static Random sRandom;

    /**
     * 计算两点距离
     *
     * @param mapCoord 坐标
     * @return 两点距离
     */
    public static double lengthVec2d(FMMapCoord mapCoord) {
        return Math.sqrt(mapCoord.x * mapCoord.x + mapCoord.y * mapCoord.y);
    }

    /**
     * 计算两点间距离
     *
     * @param src  坐标1
     * @param dest 坐标2
     * @return 两点距离
     */
    public static double lengthVec2d(FMMapCoord src, FMMapCoord dest) {
        return Math.sqrt(Math.pow(src.x - dest.x, 2) + Math.pow(src.y - dest.y, 2));
    }

    /**
     * 坐标向量
     *
     * @param mapCoord 坐标
     */
    public static void normalizeVec2d(FMMapCoord mapCoord) {
        double norm = lengthVec2d(mapCoord);

        if (norm > 0.0) {
            double inv = 1.0 / norm;
            mapCoord.x *= inv;
            mapCoord.y *= inv;
        }
    }

    /**
     * 计算垂直向量
     *
     * @param mapCoord 坐标
     * @return 垂直向量
     */
    public static FMMapCoord calcVerticalNormal(FMMapCoord mapCoord) {
        FMMapCoord vertical = new FMMapCoord();

        vertical.x = mapCoord.x * Math.cos(Math.PI / 2) - mapCoord.y * Math.sin(Math.PI / 2);
        vertical.y = mapCoord.x * Math.sin(Math.PI / 2) + mapCoord.y * Math.cos(Math.PI / 2);

        normalizeVec2d(vertical);
        return vertical;
    }

    /**
     * 对数值进行随机处理
     *
     * @param value 传入数值
     * @return 随机值
     */
    public static double randByRange(float value) {
        if (sRandom == null) {
            sRandom = new Random();
        }

        if (sRandom.nextInt() % 2 == 0) {
            return sRandom.nextDouble() * value;
        } else {
            return -sRandom.nextDouble() * value;
        }
    }

    /**
     * 通过传递进入原始点集合做线型差值和垂直向量做偏移
     *
     * @param points       点集合
     * @param speed        行走速度
     * @param verticalDist 垂直方向偏移
     * @return
     */
    public static ArrayList<FMMapCoord> calcSimulateLocationPoints(ArrayList<FMMapCoord> points, float speed,
                                                                   float verticalDist) {
        if (points.size() < 2) {
            return points;
        }

        ArrayList<FMMapCoord> simulatePoints = new ArrayList<>();

        for (int i = 1; i < points.size(); i++) {
            FMMapCoord normal = new FMMapCoord(points.get(i).x - points.get(i - 1).x,
                points.get(i).y - points.get(i - 1).y);

            double length = lengthVec2d(normal);
            normalizeVec2d(normal);

            FMMapCoord vertical = calcVerticalNormal(normal);

            float offset = 0.0f;
            while (offset < length) {
                FMMapCoord point = points.get(i - 1);

                FMMapCoord simulatePoint = calcSimulatePoint(point, normal, vertical, offset, verticalDist);
                simulatePoints.add(simulatePoint);
                offset += speed;
            }

            if (i == points.size() - 1) {
                FMMapCoord mapCoord = points.get(points.size() - 1);
                simulatePoints.add(mapCoord.clone());
            }
        }
        return simulatePoints;
    }

    /**
     * 生成最终模拟坐标
     *
     * @param origin       起点坐标
     * @param normal       分段向量
     * @param vertical     垂直向量
     * @param offset       行走偏移
     * @param verticalDist 垂直偏移
     * @return 模拟坐标点
     */
    public static FMMapCoord calcSimulatePoint(FMMapCoord origin, FMMapCoord normal, FMMapCoord vertical,
                                               float offset, float verticalDist) {
        FMMapCoord offsetPoint = new FMMapCoord(normal.x * offset, normal.y * offset);
        double rand = randByRange(verticalDist);
        FMMapCoord randPoint = new FMMapCoord(vertical.x * rand, vertical.y * rand);

        double x = origin.x + offsetPoint.x + randPoint.x;
        double y = origin.y + offsetPoint.y + randPoint.y;
        return new FMMapCoord(x, y);
    }
}
