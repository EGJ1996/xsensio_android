package com.xsensio.nfcsensorcomm;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.androidplot.Plot;
import com.androidplot.ui.Anchor;
import com.androidplot.ui.HorizontalPositioning;
import com.androidplot.ui.PositionMetrics;
import com.androidplot.ui.VerticalPositioning;
import com.androidplot.ui.widget.TextLabelWidget;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.ScalingXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.xsensio.nfcsensorcomm.model.DataPoint;
import com.xsensio.nfcsensorcomm.model.DataPointSeries;
import com.xsensio.nfcsensorcomm.model.PlotMetadata;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XYPlotWrapper {

    private final XYPlot mPlot;

    private final PlotMetadata mPlotMetadata;

    private final List<DataPoint> mDataPoints;

    private double mMinXValue = Double.MAX_VALUE;
    private double mMaxXValue = Double.MIN_VALUE;

    private double mMinYValue = Double.MAX_VALUE;
    private double mMaxYValue = Double.MIN_VALUE;

    private final boolean mAreXValuesNegative;
    private final boolean mAreYValuesNegative;

    public XYPlotWrapper(XYPlot plotToWrap, PlotMetadata plotMetadata, List<DataPoint> dataPoints) {

        if (dataPoints.size() >= 1) {
            DataPoint dp = dataPoints.get(0);
            mAreXValuesNegative = dp.getX() < 0;
            mAreYValuesNegative = dp.getY() < 0;
        } else {
            mAreXValuesNegative = false;
            mAreYValuesNegative = false;
        }

        mPlot = plotToWrap;
        mPlotMetadata = plotMetadata;

        Log.d("XYPlotWrapper", "# points: " + dataPoints.size());

        mDataPoints = new ArrayList<>();
        for (DataPoint dp : dataPoints) {
            double x = dp.getX();
            double y = dp.getY();

            if (plotMetadata.isXAxisLogarithmic()) {
                // Needed to flip the plot in the values are negative since the absolute value must be taken to apply log
                if (mAreXValuesNegative) {
                    x = -Math.log10(Math.abs(x));
                } else {
                    x = Math.log10(x);
                }
            }

            if (plotMetadata.isYAxisLogarithmic()) {
                // Needed to flip the plot in the values are negative since the absolute value must be taken to apply log
                if (mAreYValuesNegative) {
                    y = -Math.log10(Math.abs(y));
                } else {
                    y = Math.log10(y);
                }
            }

            updateMinMaxValues(x, y);
            mDataPoints.add(new DataPoint(x, y));
        }

        mPlot.clear();
        setupPlot();
    }

    private double computeMedian(List<Double> values)  {
        Collections.sort(values);
        double median = 0.0;
        if (values.size() % 2 == 0) {
            return (values.get(values.size() / 2) + values.get(values.size() / 2 - 1)) / 2;
        } else {
            return values.get(values.size() / 2);
        }
    }

    private void updateMinMaxValues(double x, double y) {
        if (x < mMinXValue) {
            mMinXValue = x;
        }
        if (x > mMaxXValue) {
            mMaxXValue = x;
        }
        if (y < mMinYValue) {
            mMinYValue = y;
        }
        if (y > mMaxYValue) {
            mMaxYValue = y;
        }
    }

    private void setupPlot() {

        // Set margins larger so that enough decimals can be displayed
        mPlot.getGraph().setMarginBottom(100);
        mPlot.getGraph().setMarginLeft(100);

        // Set Plot title (e.g. "Concentration vs Time")
        mPlot.setTitle(mPlotMetadata.getPlotTitle());

        // Set X-axis title
        mPlot.getDomainTitle().setText(mPlotMetadata.getXAxisLabel() + " " + mPlotMetadata.getXAxisUnitLabel());

        // Set Y-axis title
        TextLabelWidget rangeWLabelWidget = mPlot.getRangeTitle();
        rangeWLabelWidget.setText(mPlotMetadata.getYAxisLabel() + " " + mPlotMetadata.getYAxisUnitLabel());
        mPlot.setRangeTitle(rangeWLabelWidget);

        // Set X-axis title position
        mPlot.getDomainTitle().setPositionMetrics(new PositionMetrics(
                0,
                HorizontalPositioning.ABSOLUTE_FROM_CENTER,
                30,
                VerticalPositioning.ABSOLUTE_FROM_BOTTOM,
                Anchor.BOTTOM_MIDDLE));

        // Set Y-axis title position
        mPlot.getRangeTitle().setPositionMetrics(new PositionMetrics(
                20,
                HorizontalPositioning.ABSOLUTE_FROM_LEFT,
                0,
                VerticalPositioning.ABSOLUTE_FROM_CENTER,
                Anchor.BOTTOM_MIDDLE));

        // Set X-Axis boundaries
        //double xMinMaxDifference = mMaxXValue-mMinXValue;
        mPlot.setDomainBoundaries(mMinXValue,
                mMaxXValue, BoundaryMode.FIXED);


        // Set Y-Axis boundaries
        /*double yMinMaxDifference = mMaxYValue-mMinYValue;
        mPlot.setRangeBoundaries(mMinYValue-0.1*yMinMaxDifference,
                mMaxYValue+0.1*yMinMaxDifference, BoundaryMode.FIXED);
        */

        if (mPlotMetadata.isCenterForYAxisManual()) {
            mPlot.setRangeBoundaries(mPlotMetadata.getMinValueYAxis(),
                    mPlotMetadata.getMaxValueYAxis(), BoundaryMode.FIXED);
        } else {
            mPlot.setRangeBoundaries(mMinYValue,
                    mMaxYValue, BoundaryMode.AUTO);
        }

        /*mPlot.setRangeBoundaries(mMinYValue,
                mMaxYValue, BoundaryMode.AUTO);*/

        mPlot.setBorderStyle(Plot.BorderStyle.NONE, null, null);

        XYSeries series = new DataPointSeries("", mDataPoints);

        final LineAndPointFormatter seriesFormat = new LineAndPointFormatter(null, Color.GREEN, Color.WHITE, null);

        final ScalingXYSeries scalingSeries1 = new ScalingXYSeries(series, 0, ScalingXYSeries.Mode.Y_ONLY);
        mPlot.addSeries(scalingSeries1, seriesFormat);

        // Set labels format of X-axis
        mPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {

                double abscissaValue = ((Number) obj).floatValue();

                NumberFormat formatter = new DecimalFormat("#0.000");

                String numberAsString = formatter.format(abscissaValue);
                if (mPlotMetadata.isXAxisLogarithmic()) {
                    if (mAreXValuesNegative) {
                        numberAsString = formatter.format(Math.abs(abscissaValue));
                        if (abscissaValue > 0) {
                            numberAsString = "-" + numberAsString;
                        }
                        numberAsString = "-10^" + numberAsString;
                    } else {
                        numberAsString = "10^" + formatter.format(abscissaValue);
                    }
                } else {
                    numberAsString = formatter.format(abscissaValue);
                }
                return toAppendTo.append(numberAsString);
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                // unused
                return null;
            }
        });

        // Set labels format of Y-axis
        mPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {

                double ordinateValue = ((Number) obj).floatValue();

                NumberFormat formatter = new DecimalFormat("#0.000");

                String numberAsString = formatter.format(ordinateValue);
                if (mPlotMetadata.isYAxisLogarithmic()) {
                    if (mAreYValuesNegative) {
                        numberAsString = formatter.format(Math.abs(ordinateValue));
                        if (ordinateValue > 0) {
                            numberAsString = "-" + numberAsString;
                        }
                        numberAsString = "-10^" + numberAsString;
                    } else {
                        numberAsString = "10^" + formatter.format(ordinateValue);
                    }
                } else {
                    numberAsString = formatter.format(ordinateValue);
                }
                return toAppendTo.append(numberAsString);
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                // unused
                return null;
            }
        });


        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);

        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float scale = valueAnimator.getAnimatedFraction();
                scalingSeries1.setScale(scale);
                mPlot.redraw();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                // the animation is over, so show point labels:
                //seriesFormat.getPointLabelFormatter().getTextPaint().setColor(Color.BLACK);
                mPlot.redraw();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });

        animator.setDuration(0);
        animator.start();


    }
}
