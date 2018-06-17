package com.xsensio.nfcsensorcomm.model;

import java.util.Comparator;


/**
 * Comparator of {@link DataPoint} used for instance to sort a collection of DataPoints
 */
public class DataPointComparator implements Comparator<DataPoint> {

    boolean mSortOnX;
    boolean mIsAsc;

    public DataPointComparator(boolean sortOnX, boolean isAsc) {
        mSortOnX = sortOnX;
        mIsAsc = isAsc;
    }

    @Override
    public int compare(DataPoint p1, DataPoint p2) {

        if (mSortOnX) { // Sort on x
            if (mIsAsc) { // Sort in ascending order
                return Double.compare(p1.getX(), p2.getX());
            } else { // Sort in descending order
                return Double.compare(p2.getX(), p1.getX());
            }
        } else { // Sort on y
            if (mIsAsc) { // Sort in ascending order
                return Double.compare(p1.getY(), p2.getY());
            } else { // Sort in descending order-
                return Double.compare(p2.getY(), p1.getY());
            }
        }
    }
}
