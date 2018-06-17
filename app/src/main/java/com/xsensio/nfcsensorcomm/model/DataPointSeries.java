package com.xsensio.nfcsensorcomm.model;


import com.androidplot.xy.XYSeries;

import java.util.List;

/**
 * Created by Michael Heiniger on 22.07.17.
 */

public final class DataPointSeries implements XYSeries {

    /**
     * Legend of the graph
     */
    private String mTitle;

    /**
     * Data points series representing the graph
     */
    private List<DataPoint> mSeries;

    public DataPointSeries(String title, List<DataPoint> series) {
        mTitle = title;
        mSeries = series;
    }

    @Override
    public int size() {
        return mSeries.size();
    }

    @Override
    public Number getX(int index) {
        return mSeries.get(index).getX();
    }

    @Override
    public Number getY(int index) {
        return mSeries.get(index).getY();
    }

    @Override
    public String getTitle() {
        return mTitle;
    }
}
