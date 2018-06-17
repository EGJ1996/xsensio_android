package com.xsensio.nfcsensorcomm.model;

/**
 * Represents metadata of a plot, for instance the label of the axis, whether the scale should be linear or logarithmic, etc.
 */
public class PlotMetadata {

    private String mPlotTitle;

    private String mXAxisLabel;
    private String mXAxisUnitLabel;
    private String mYAxisLabel;
    private String mYAxisUnitLabel;

    private boolean mIsXAxisLogarithmic = false;
    private boolean mIsYAxisLogarithmic;

    private boolean mIsCenterForXAxisManual = false;
    private double mManualValueXAxis = 0.0;

    private boolean mIsCenterForYAxisManual = false;
    private double mMinValueYAxis;
    private double mMaxValueYAxis;
    private double mManualValueYAxis = 0.0;

    public PlotMetadata(String plotTitle,
                        String xAxisLabel,
                        String xAxisUnitLabel,
                        String yAxisLabel,
                        String yAxisUnitLabel,
                        boolean isYAxisLogarithmic) {
        mPlotTitle = plotTitle;
        mXAxisLabel = xAxisLabel;
        mXAxisUnitLabel = xAxisUnitLabel;
        mYAxisLabel = yAxisLabel;
        mYAxisUnitLabel = yAxisUnitLabel;
        mIsYAxisLogarithmic = isYAxisLogarithmic;
    }

    public String getPlotTitle() {
        return mPlotTitle;
    }

    public void setPlotTitle(String plotTitle) {
        mPlotTitle = plotTitle;
    }

    public String getXAxisLabel() {
        return mXAxisLabel;
    }

    public void setXAxisLabel(String xAxisLabel) {
        mXAxisLabel = xAxisLabel;
    }

    public String getXAxisUnitLabel() {
        return mXAxisUnitLabel;
    }

    public void setXAxisUnitLabel(String xAxisUnitLabel) {
        this.mXAxisUnitLabel = xAxisUnitLabel;
    }

    public String getYAxisLabel() {
        return mYAxisLabel;
    }

    public void setYAxisLabel(String yAxisLabel) {
        mYAxisLabel = yAxisLabel;
    }

    public String getYAxisUnitLabel() {
        return mYAxisUnitLabel;
    }

    public void setmYAxisUnitLabel(String mYAxisUnitLabel) {
        this.mYAxisUnitLabel = mYAxisUnitLabel;
    }


    public boolean isXAxisLogarithmic() {
        return mIsXAxisLogarithmic;
    }

    public void setIsXAxisLogarithmic(boolean isXAxisLogarithmic) {
        mIsXAxisLogarithmic = isXAxisLogarithmic;
    }

    public boolean isYAxisLogarithmic() {
        return mIsYAxisLogarithmic;
    }

    public void setIsYAxisLogarithmic(boolean isYAxisLogarithmic) {
        mIsYAxisLogarithmic = isYAxisLogarithmic;
    }

    public boolean isCenterForXAxisManual() {
        return mIsCenterForXAxisManual;
    }

    public void setCenterForXAxisManual(boolean isCenterForXAxisManual) {
        mIsCenterForXAxisManual = isCenterForXAxisManual;
    }

    public double getManualValueXAxis() {
        return mManualValueXAxis;
    }

    public void setManualValueXAxis(double value) {
        mManualValueXAxis = value;
    }

    public boolean isCenterForYAxisManual() {
        return mIsCenterForYAxisManual;
    }

    public void setCenterForYAxisManual(boolean isCenterForYAxisManual) {
        mIsCenterForYAxisManual = isCenterForYAxisManual;
    }

    public double getManualValueYAxis() {
        return mManualValueYAxis;
    }

    public void setManualValueYAxis(double value) {
        mManualValueYAxis = value;
    }

    public void setMinValueYAxis(double value) {
        mMinValueYAxis = value;
    }

    public double getMinValueYAxis() {
        return mMinValueYAxis;
    }

    public void setMaxValueYAxis(double value) {
        mMaxValueYAxis = value;
    }

    public double getMaxValueYAxis() {
        return mMaxValueYAxis;
    }

}
