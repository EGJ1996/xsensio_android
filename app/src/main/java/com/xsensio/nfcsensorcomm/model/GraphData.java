package com.xsensio.nfcsensorcomm.model;


import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for DataPoints which provides helper functions related to data plots.
 */
public class GraphData {

    private List<DataPoint> mDatapoints;

    private boolean mIsXLogscale;
    private boolean mIsYLogscale;

    private double mMinXAxis;
    private double mMaxXAxis;

    private double mMinYAxis;
    private double mMaxYAxis;

    public GraphData(List<DataPoint> dataPoints, boolean isXLogscale, boolean isYLogscale) {
        mDatapoints = new ArrayList<>(dataPoints);

        mIsXLogscale = isXLogscale;
        mIsYLogscale = isYLogscale;

        // Compute min and max of each dimension
        findMinMax();
    }

    public List<DataPoint> getDatapoints() {

        List<DataPoint> datapoints = new ArrayList<>();

        double x = 0.0;
        double y = 0.0;
        for (DataPoint dp : mDatapoints) {
            x = dp.getX();
            y = dp.getY();
            if (mIsXLogscale) {
                x = Math.log10(Math.abs(x));
            }
            if (mIsYLogscale) {
                y = Math.log10(Math.abs(y));
            }
            datapoints.add(new DataPoint(x, y));
        }

        return datapoints;
    }

    /*public double getAverageXValue() {

        double sum = 0;
        for (DataPoint val : mDatapoints) {
            sum += val.getX();
        }

        return sum / mDatapoints.size();
    }*/

    /**
     * Return the minimum value of the X-Axis
     * @return
     */
    public double getMinXAxis() {
        return mMinXAxis;
    }

    /**
     * Return the maximum value of the X-Axis
     * @return
     */
    public double getMaxXAxis() {
        return mMaxXAxis;
    }

    /**
     * Return the minimum value of the Y-Axis
     * @return
     */
    public double getMinYAxis() {
        return mMinYAxis;
    }

    /**
     * Return the maximum value of the Y-Axis
     * @return
     */
    public double getMaxYAxis() {
        return mMaxYAxis;
    }

    private void findMinMax() {

        mMinXAxis = Double.MAX_VALUE;
        mMaxXAxis = Double.MIN_VALUE;

        mMinYAxis = Double.MAX_VALUE;
        mMaxYAxis = Double.MIN_VALUE;

        for (DataPoint p : mDatapoints) {
            if (p.getX() < mMinXAxis) {
                mMinXAxis = p.getX();
            }

            if (p.getX() > mMaxXAxis) {
                mMaxXAxis = p.getX();
            }

            if (p.getY() < mMinYAxis) {
                mMinYAxis = p.getY();
            }

            if (p.getY() > mMaxYAxis) {
                mMaxYAxis = p.getY();
            }
        }
    }
}