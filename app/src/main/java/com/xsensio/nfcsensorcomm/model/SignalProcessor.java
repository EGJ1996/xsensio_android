package com.xsensio.nfcsensorcomm.model;

import android.preference.ListPreference;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
//Bilguun
//This one contains following functions related to processing signal
public final class SignalProcessor {
    public static double[] samplingFrequencies={380,724,1297,2168,3271,6706,13368,27593,56306,83892,112359,169491,1};
    //todo 2019 May, change to 1.79
    private static final String TAG = "SignalProcessor";
    private static double saturateThresh=1.79;
    private static double meanzeroThresh=0.1;
    private static TreeMap<Double,Double> tempProfile=initializeTemp();

    //Creating map, used in estimator in temperatureMapper function
    private static TreeMap<Double,Double> initializeTemp(){
        TreeMap<Double,Double> tmp=new TreeMap<>();
//        tmp.put(1.375219,-55.0);
//        tmp.put(1.350441,-50.0);
//        tmp.put(1.300593,-40.0);
//        tmp.put(1.250398,-30.0);
//        tmp.put(1.199884,-20.0);
//        tmp.put(1.149070,-10.0);
//        tmp.put(1.097987,0.0);
//        tmp.put(1.046647,10.0);
//        tmp.put(0.995050,20.0);
//        tmp.put(0.943227,30.0);
//        tmp.put(0.891178,40.0);
//        tmp.put(0.838882,50.0);
//        tmp.put(0.786360,60.0);
//        tmp.put(0.733608,70.0);
//        tmp.put(0.680654,80.0);
//        tmp.put(0.627490,90.0);
//        tmp.put(0.574117,100.0);
//        tmp.put(0.520551,110.0);
//        tmp.put(0.466760,120.0);
//        tmp.put(0.412739,130.0);
//        tmp.put(0.358164,140.0);
//        tmp.put(0.302785,150.0);
        tmp.put(0.521,30.0); // by junrui, 20190428, calibrate temperature from MCU internal temp sensor
        tmp.put(0.516,25.0);
        return tmp;
    }

    public static List<Double> temperatureMapper(List<Double> values){
        LinearFunctionEstimator estimator=new LinearFunctionEstimator(tempProfile);
        List<Double> vals=estimator.getEstimates(values);
        return estimator.getEstimates(values);
    }

    //This function calculates mean value of list with 2 modes
    //1st mode: onlyPositive=true; considers only values higher than meanzeroThresh
    // (So that we can get average value of derivative)
    //2nd mode: onlyPositive=false; This mode is used to calculate mean of temperature
    public static double mean(List<Double> values, boolean onlyPositive){
        double total=0;
        int cnt=0;
        for (Double value : values) {
            if(Double.isInfinite(value) || Double.isNaN(value)){ continue;}
            if(onlyPositive){
                if(value>meanzeroThresh){
                    total+=value;
                    cnt+=1;
                }
            } else {
                total+=value;
                cnt+=1;
            }
        }
        return total/cnt;
    }

    //Returns value of optimal sample rate for given signal, based on DerivativeReport sampling frequency adjustment
    public static double getRightSampleRate(List<Double> mSamples,int sample_freq_idx){
        double max=0;
        int notsaturated=1000;
        for (Double mSample : mSamples) {
            if(mSample>max){ max=mSample; }
            if(mSample>saturateThresh){ notsaturated-=1; }
        }
        return ((1000*max)/(2.5*notsaturated))*samplingFrequencies[sample_freq_idx];
    }

    //Take derivative based on algorithm described in DerivativeReport
    //Declaring parameters
    //Note that these are only optimized for specific use case, so if error occur feel free to modify them
    private static int dt=50;
    private static int length=5;
    private static double thresh=0.002;
    private static double scaler=0.130;//119.97;

    //todo 2019 May, change
    public static double SlopeCalc(List<Double> mSamples,double mEffectiveSamplingFrequency){
        //Preparing variables
        int k=mSamples.size();
        int last = k-1;
        double voltage_mean;
        int ended = 0;
        int final_portion = 50;
        int start = 10;
        double min = 10000;
//        double min_tot = 10000;
        double max = 0;
//        double max_tot = 0;
        int max_dist = 0;
        int distance = 0;
        int last_new = 0;
        int start_new;
        int z = 0;
        int interval_start = 0;
        int interval_last = 0;
        double slope;
        // mSampleTime and mSampleValue are part of the sample time and mSamples lists,
        // containing samples in the longest slope, for calculating the slope value
        // mSampleTime is calculated from mEffectiveSamplingFrequency and the number of the sample
        List<Double> mSampleTime = new ArrayList<>();
        List<Double> mSampleValue = new ArrayList<>();
        double samplingPeriod = 1/mEffectiveSamplingFrequency;
        double min_slope = 0.0001/samplingPeriod;
        // sample_Voltage is the data array of the whole mSamples list
        Double[] sample_voltage = mSamples.toArray(new Double[k]);

        for (int i = start; i <= last; i++){
            // finds a local minimum
            if(sample_voltage[i] < min){
                start_new = i;
                min = sample_voltage[i];
                // look for the next maximum after the local minimum
                for (int ii = start_new; ii <= last; ii++){
                    if(sample_voltage[ii] > max){
                        last_new = ii;
                        z = 0;
                        max = sample_voltage[ii];
//                        if(max > max_tot){
//                            max_tot = max;
//                        }
                    } else{
                        z = z+1;
                    }
                    // if the code cannot find a bigger maximum after a long time, z>50,
                    // it means that the signal saturated. Therefore, the last_new we found
                    // is the end of one integration before it saturates.
                    if(z > 50){
                        distance = last_new - start_new;
                        z = 0;
                        i = ii;
                        min = 10000;
                        break;
                    }
                }
                max = 0; // reinitialize max value, in order to start another search
                if(distance > max_dist){
                    max_dist = distance;
                    interval_start = start_new;
                    interval_last = last_new;
                }
            }
        }
        start = interval_start;
        last = interval_last;
        k = last - 50;
        if (k > start) {

            for(int i = start; i < k; i++){
                mSampleTime.add(samplingPeriod * i);
                mSampleValue.add(sample_voltage[i]);
            }
            int SlopeSize = mSampleTime.size();
            Double[] sample_time = mSampleTime.toArray(new Double[SlopeSize]);
            Double[] sample_Value = mSampleValue.toArray(new Double[SlopeSize]);

            voltage_mean = mean(mSampleValue, false);
            double time_mean = mean(mSampleTime, false);
            double numerator = 0;
            double denominator = 0;
            for(int i = 0; i < SlopeSize; i++){
                numerator = numerator + (sample_Value[i] - voltage_mean) * (sample_time[i] - time_mean);
                denominator = denominator + (sample_time[i] - time_mean) * (sample_time[i] - time_mean);
            }
            if (numerator * denominator == 0) {
                slope = 0;
            }else{
                slope = numerator / denominator;
                if(slope < 0){
                    slope = 0;
                }
//                    double a = voltage_mean - time_mean * slope;
//                    Double[] fit = new Double[SlopeSize];
//                Log.d(TAG, "slopetest: " + slope);
//                    for (int i = 0; i < SlopeSize; i++){
//                        fit[i] = sample_time[i] * slope + a;
//                    }
//                    double standard_dev = 0;
//                    double new_term = 0;
//                    for(int i=0; i<SlopeSize; i++){
//                        Log.d(TAG, "i: " + i);
//                        new_term = (sample_voltage[i] - fit[i] * fit[i]);
//                        standard_dev = standard_dev + new_term;
//                    }
//                    standard_dev = Math.sqrt(standard_dev/(SlopeSize));
//                    double control = (max - min)/5;
//                    if (control < standard_dev){
//                        slope = 0;
//                    }
                Log.d(TAG, "start: " + start);
                Log.d(TAG, "last: " + last);
                Log.d(TAG, "slope1: " + slope);
                Log.d(TAG, "time_mean: " + time_mean);
                Log.d(TAG, "sample_period: " + samplingPeriod);
                Log.d(TAG, "denominator: " + denominator);
                Log.d(TAG, "numerator: " + numerator);
                Log.d(TAG, "samplefreq: " + mEffectiveSamplingFrequency);
            }
        }else{
            slope = 0;
        }

        return slope;
    }

    public static List<Double> derivative(List<Double> mSamples,double mEffectiveSamplingFrequency){
        //Preparing variables
        int ls=mSamples.size();
        //Since backward path is only forward path with dt time delay, we don't have to declare new variable
        double[] forward=new double[ls+dt];
        //This array will indicate whether backward and forward derivatives have significant difference
        boolean[] diff=new boolean[ls+dt];
        Double[] series=mSamples.toArray(new Double[ls]);
        for (int i = 0; i < forward.length; i++) { forward[i]=0; diff[i]=false; }
        //Calculating forward derivatives and diff series at the same time
        for (int i = 0; i < ls; i++) {
            if(i<ls-dt){ forward[i+dt]=(series[i+dt]-series[i])/dt; }
            if((i>=950)||(i<50)){ diff[i+dt]=true; }
            else{ diff[i+dt]=(Math.abs(forward[i+dt]-forward[i])>thresh); }
        }
        //Now we are detecting ripples in diff signal by analyzing rising and falling edges and saving their location
        List<Integer> riseEdges=new ArrayList<>();
        boolean currentstate=false;
        for (int i = 0; i < ls+dt-length; i++) {
            if(currentstate!=diff[i]){
                boolean valid=true;
                for (int j = 0; j < length; j++){
                    if (diff[j+i]==currentstate){
                        valid=false;
                        break;
                    }
                }
                if (valid){
                    //Note: rising and falling edges must occur sequentially
                    currentstate=diff[i]; //True False True False
                    riseEdges.add(i-50);
                }
            }
        }
        //Mixing forward and backward paths according to currentstates
        double[] result=new double[ls];
        for (int i = 0; i < result.length; i++) { result[i]=0; }
        boolean riseEdge=false;
        for (int i = 0; i < riseEdges.size(); i++) {
            riseEdge= !riseEdge;
            int loc=riseEdges.get(i);
            if (riseEdge){
                for(int j=loc;j<loc+dt;j++){ result[j]=forward[j]; }
            } else {
                if (i == (riseEdges.size() - 1)) {
                    for(int j=loc;j<ls+dt;j++){ result[j-dt]=forward[j]; }
                } else {
                    for(int j=loc;j<riseEdges.get(i + 1)+dt;j++){ result[j-dt]=forward[j]; }
                }
            }
        }
        //Converting the result into appropriate type, while scaling the signal into original amplitude
        List<Double> res=new ArrayList<>();
        for (double v : result) { res.add(v*scaler*mEffectiveSamplingFrequency); }
        return res;
    }

    //This function will return index of closest possible frequency of board to given frequency.
    public static int closest(double frequency){
        double min_diff=99999999;
        int min_i=0;
        for (int i = 0; i < samplingFrequencies.length-1; i++) {
            double diff=Math.abs(samplingFrequencies[i]-frequency);
            if(min_diff>diff){
                min_diff=diff;
                min_i=i;
            }
        }
        return min_i;
    }
}