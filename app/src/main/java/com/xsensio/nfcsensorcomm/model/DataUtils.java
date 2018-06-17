package com.xsensio.nfcsensorcomm.model;

import android.util.Log;

import com.xsensio.nfcsensorcomm.Utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Class gathering helper functions that process the data received from the sensors
 */
public final class DataUtils {

    private static final String TAG = "DataUtils";

    private DataUtils() {}

    /**
     * Group provided {@code bytes} according to {@code numBitsPerNumber} and map them
     * to decimal numbers between 0 and {@code valueRef} (i.e. each group of bytes is mapped
     * to one of the 2^{@code numBitsPerNumber} possible decimal numbers).
     * @param bytes
     * @param numBytesPerNumber
     * @param numBitsPerNumber
     * @param valueRef
     * @return a list of decimal numbers
     * @throws IllegalArgumentException
     */
    public static List<Double> bytesToDecimals(List<Byte> bytes, int numBytesPerNumber, int numBitsPerNumber, double valueRef) {
        Log.d(TAG, "Num bytes: " + bytes.size());
        Log.d(TAG, "Num bytes per number: " + numBytesPerNumber);
        Log.d(TAG, "Num bits per Number: " + numBitsPerNumber);
        Log.d(TAG, "valueRef: " + valueRef);
        if (((int) Math.ceil((double)numBitsPerNumber/8)) > 9) {
            throw new IllegalArgumentException("Number of bits " + numBitsPerNumber + " cannot fit in " + numBytesPerNumber + " of bytes");
        }

        // Compute the stepsize between every two decimal numbers
        double stepSize = valueRef/ (Math.pow(2, numBitsPerNumber)-1);

        /*
        Log.d(TAG, "Bytes to convert to decimals: " + Utils.bytesToHexString(Utils.bytesListToArray(bytes)));
        Log.d(TAG, "Ref value: " + valueRef);
        Log.d(TAG, "Step size: " + stepSize);
        Log.d(TAG, "Num bytes per sample (2): " + numBytesPerNumber);
        */
        // Convert grouped-bytes into integers (there are numBytesPerNumber bytes per integer)
        List<Integer> groupedBytesAsIntegers = bytesToInteger(bytes, numBytesPerNumber, numBitsPerNumber);

        /* Map integers to decimals between 0 and the reference value {@code valueRef}
         * (e.g. voltage ref = 3,3 volts, so output decimals are between 0 and 3.3)
         */
        List<Double> readoutsAsDecimals = new ArrayList<>();
        for (Integer number : groupedBytesAsIntegers) {
            //Log.d(TAG, "Bytes GROUPED as decimal: " + number * stepSize);
            readoutsAsDecimals.add(number * stepSize);
        }

        return readoutsAsDecimals;
    }

    /**
     * Group provided {@code bytes} according to {@code numBitsPerNumber} and map them
     * to integer numbers between 0 and 2^{@code numBitsPerNumber}.
     * @param bytes
     * @param numBytesPerNumber
     * @param numBitsPerNumber
     * @return a list of integer numbers
     */
    public static List<Integer> bytesToInteger(List<Byte> bytes, int numBytesPerNumber, int numBitsPerNumber) {

        if (((int) Math.ceil((double)numBitsPerNumber/8)) > 9) {
            throw new IllegalArgumentException("Number of bits " + numBitsPerNumber + " cannot fit in " + numBytesPerNumber + " of bytes");
        }

        List<Integer> readoutsAsInteger = new ArrayList<>();

        // Group bytes according to numBytesPerNumber (from left-to-right)
        for (int i = 0; i < bytes.size(); i += numBytesPerNumber) {
            List<Byte> bytesGrouped = new ArrayList<>();

            for (int j = 0; j < numBytesPerNumber; j++) {
                bytesGrouped.add(bytes.get(i+j));
            }

            //Log.d(TAG, "Bytes GROUPED as int: " + Utils.bytesToHexString(Utils.bytesListToArray(bytesGrouped)));

            /* Reverse order of bytes in the group because Utils.bytesToInt() read byte-array
             * from right-to-left:
             */
            //Collections.reverse(bytesGrouped);

            //Log.d(TAG, "Bytes GROUPED as int (reversed): " + Utils.bytesToHexString(Utils.bytesListToArray(bytesGrouped)));

            // Transform
            int bytesGroupedAsInteger = Utils.bytesToInt(Utils.bytesListToArray(bytesGrouped));

            // Aadd the integer to the list
            readoutsAsInteger.add(bytesGroupedAsInteger);
        }

        return readoutsAsInteger;
    }

    /**
     * Generate a time-axis from 0 according to {@code samplingFrequency} and use timeScaleAsString
     * to determine the scale of time. Possible values are defined in {@link #timeScaleToDouble}
     * @param numPoints
     * @param timescale
     * @param samplingFrequency
     * @return a list a points representing the time axis
     */
    public static List<Double> getTimeAxisPoints(int numPoints, TimescaleEnum timescale, double samplingFrequency) {

        // Get the timescale
        double timescaleCoeff = timeScaleToDouble(timescale);

        // Compute the sampling period
        double samplingPeriod = (1/ samplingFrequency) * timescaleCoeff;
       // Log.d(TAG, "Sampling period: " + samplingPeriod);

        List<Double> timeAxisPoints = new ArrayList<>();

        // Generate points of the time axis
        double timer = 0.0;
        for (int i = 0; i < numPoints; i++) {
            timeAxisPoints.add(timer);
            timer += samplingPeriod;
        }

        return timeAxisPoints;
    }

    /**
     * Return the timescale corresponding to the provided String, the reference is the second
     * e.g. "second" returns 1, "millisecond" returns 1000
     * @param timescale
     * @return
     */
    public static double timeScaleToDouble(TimescaleEnum timescale) {
        if (timescale == TimescaleEnum.SECOND) {
            return 1;
        } else if (timescale == TimescaleEnum.MILLISECOND) {
            return 1000;
        } else { // TimescaleEnum.MICROSECOND
            return 1000000;
        }
    }

    public static List<Double> computeDerivatives(List<Double> signal, int numSamplesForDerivative, double samplingPeriod) {

        double dt = samplingPeriod;
        //Log.d(TAG, "dt=" + dt);

        List<Double> derivatives = new LinkedList<>();

        for (int i = 0; i < signal.size()-numSamplesForDerivative; i++) {
            // Compute the discrete derivative over n samples
            double dy = (signal.get(i+numSamplesForDerivative)-signal.get(i)) / (numSamplesForDerivative*dt);
            //Log.d(TAG, "Current derivative (i=" + i + ") is " + dy);
            derivatives.add(dy);
        }

        return derivatives;
    }

    public double[] std(List<Double> values){
        double average=computeAverage(values);
        double sum=0;
        int count=0;
        for (Double value : values) { sum+=(value-average)*(value-average); }
        double squaredMean=sum/count;
        return new double[]{Math.sqrt(squaredMean),average};
    }


    public static double computeAverage(List<Double> values) {

        if (values.size() == 0) {
            return 0;
        }

        double sum = 0.0;
        int count = 0;

        for (Double val : values) {
            sum += val;
            count++;
        }
        return sum / count;
    }
}
