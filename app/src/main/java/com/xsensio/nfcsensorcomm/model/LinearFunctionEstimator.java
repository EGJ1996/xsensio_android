package com.xsensio.nfcsensorcomm.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Linear function estimator: based on the provided pairs (x, f(x)) where x->f(x) and x, f(x)
 * are scalar, this class returns estimates f(x_0) for any input x_0 using linear interpolation
 * between reference pairs (x, f(x)).
 */
public class LinearFunctionEstimator implements FunctionEstimator {

    private static final String TAG = "LinearFunctionEstimator";

    /** Store a reference point and the Subfunction to apply for a point falling after
     * the reference point and before the next one. */
    private List<EstimatorFunction> mInterpolationFunction;

    public LinearFunctionEstimator(TreeMap<Double, Double> functionMap) {

        if (functionMap == null || functionMap.isEmpty()) {
            throw new IllegalArgumentException("Provided functionMap cannot be null or empty");
        }

        /**
         * Compute the interpolation function from the mappings
         * TreeMap represents a Real->Real x->f(x) function for some (x, f(x)) pairs
         */
        mInterpolationFunction = computeInterpolationFunction(new TreeMap<> (functionMap));
    }

    /**
     * Compute a List containing a point (x,f(x)) and the slope between (x,f(x)) and the next
     * reference point.
     * .
     * This Map can then be used to compute an x-axis value corresponding to a known y-axis value.
     *
     * Provided functionMap represents a Real->Real, x->f(x) function for some (x, f(x)) pairs
     *
     * Note: TreeMaps are sorted on the Keys in ascending order by default
     */
    private List<EstimatorFunction> computeInterpolationFunction(TreeMap<Double, Double> functionMap) {

        List<EstimatorFunction> interpolationFunction = new ArrayList<>();

        for (Map.Entry<Double, Double> entry : functionMap.entrySet()) {
            if (entry.getKey() != functionMap.lastKey()) {

                double refX = entry.getKey();

                double refY = entry.getValue();

                // Retrieve the closest higher x-axis value (the closest neighbor "on the right")
                double nextRefX = functionMap.higherKey(entry.getKey());

                // Retrieve the corresponding y-axis value
                double nextRefY = functionMap.get(nextRefX);

                // Compute the slope for the linear interpolation
                double slope = (nextRefY - refY) / (nextRefX - refX);

                Log.d(TAG, "Slope: " + slope);

                /**
                 * Store the necessary data to compute the f(x_0) estimates from
                 * the provided (x_0)
                 */
                //Log.d(TAG, "Add entry into interpolation function: (" + refX + "," + refY +"), " + slope);
                interpolationFunction.add(new EstimatorFunction(refX, refY, slope));
            }
        }
        return interpolationFunction;
    }

    @Override
    public double getEstimate(double x) {

        // Find the Subfunction with the highest reference x-axis value smaller than the value of x
        EstimatorFunction sf = mInterpolationFunction.get(0);
        for (EstimatorFunction sfCandidate : mInterpolationFunction) {
            if (sfCandidate.getRefX() <= x) {
                sf = sfCandidate;
            } else {
                break; // the highest Key smaller than x is found
            }
        }

        double yEstimate = sf.evaluate(x);

        //Log.d(TAG, "Estimated Y = " + yEstimate);

        return yEstimate;
    }

    @Override
    public List<Double> getEstimates(List<Double> xValues) {

        List<Double> yEstimates = new ArrayList<>();
        for (Double value : xValues) {
            yEstimates.add(getEstimate(value));
        }
        return yEstimates;
    }

    private class EstimatorFunction {

        private double mRefX;
        private double mRefY;
        private double mSlope;

        public EstimatorFunction(double lowerRefX, double refY, double slope) {
            mRefX = lowerRefX;
            mRefY = refY;
            mSlope = slope;
        }

        public double evaluate(double x) {

            //Log.d(TAG, "X = " + x);
            //Log.d(TAG, "Reference X = " + mRefX);
            //Log.d(TAG, "Reference Y = " + mRefY);

            if (mRefX <= x) {
                return mRefY + mSlope * (x-mRefX);
            } else {
                return mRefY - mSlope * (mRefX-x);
            }
        }

        public double getRefX() {
            return mRefX;
        }
    }
}