package com.xsensio.nfcsensorcomm.model;

import java.util.List;

/**
 * Abstraction of a Real->Real, x->f(x) function estimator. The estimator uses reference
 * (x, f(x)) pairs of values and perform interpolation to return an estimate f(x_0) for any x_0,
 */
public interface FunctionEstimator {

    /** Return the interpolated estimate corresponding to the provided value */
    double getEstimate(double x);

    /** Return a list of interpolated estimates corresponding to the provided list of values */
    List<Double> getEstimates(List<Double> values);
}
