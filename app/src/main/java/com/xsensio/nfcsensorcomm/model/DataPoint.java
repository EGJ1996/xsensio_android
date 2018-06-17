package com.xsensio.nfcsensorcomm.model;


import java.util.List;

/**
 * Represent a point in two dimensions. Typically used for grouping data that will be plotted.
 */
public class DataPoint {

    private double mX;
    private double mY;

    public DataPoint(double x, double y) {
        mX = x;
        mY = y;
    }

    public double getX() {
        return mX;
    }

    public double getY() {
        return mY;
    }

    public static double computeAverageOverX(List<DataPoint> dataPoints) {
        double sum = 0.0;
        int count = 0;
        for (DataPoint dp : dataPoints) {
            sum += dp.getX();
            count++;
        }
        if (count >= 1) {
            return sum / (double) count;
        } else {
            return 0;
        }
    }

    public static double computeAverageOverY(List<DataPoint> dataPoints) {
        double sum = 0.0;
        int count = 0;
        for (DataPoint dp : dataPoints) {
            sum += dp.getY();
            count++;
        }
        if (count >= 1) {
            return sum / (double) count;
        } else {
            return 0;
        }
    }
}