package com.xsensio.nfcsensorcomm.model;

import android.preference.ListPreference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class SignalProcessor {
    public static double[] samplingFrequencies={380,724,1297,2168,3271,6706,13368,27593,56306,83892,112359,169491,1};
    private static double saturateThresh=2.49;
    private static double meanzeroThresh=0.1;
    private static TreeMap<Double,Double> tempProfile=initializeTemp();

    private static TreeMap<Double,Double> initializeTemp(){
        TreeMap<Double,Double> tmp=new TreeMap<>();
        tmp.put(1.375219,-55.0);
        tmp.put(1.350441,-50.0);
        tmp.put(1.300593,-40.0);
        tmp.put(1.250398,-30.0);
        tmp.put(1.199884,-20.0);
        tmp.put(1.149070,-10.0);
        tmp.put(1.097987,0.0);
        tmp.put(1.046647,10.0);
        tmp.put(0.995050,20.0);
        tmp.put(0.943227,30.0);
        tmp.put(0.891178,40.0);
        tmp.put(0.838882,50.0);
        tmp.put(0.786360,60.0);
        tmp.put(0.733608,70.0);
        tmp.put(0.680654,80.0);
        tmp.put(0.627490,90.0);
        tmp.put(0.574117,100.0);
        tmp.put(0.520551,110.0);
        tmp.put(0.466760,120.0);
        tmp.put(0.412739,130.0);
        tmp.put(0.358164,140.0);
        tmp.put(0.302785,150.0);
        return tmp;
    }

    public static List<Double> temperatureMapper(List<Double> values){
        LinearFunctionEstimator estimator=new LinearFunctionEstimator(tempProfile);
        List<Double> vals=estimator.getEstimates(values);
        return estimator.getEstimates(values);
    }

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

    public static double getRightSampleRate(List<Double> mSamples,int sample_freq_idx){
        double max=0;
        int notsaturated=1000;
        for (Double mSample : mSamples) {
            if(mSample>max){ max=mSample; }
            if(mSample>saturateThresh){ notsaturated-=1; }
        }
        return ((1000*max)/(2.5*notsaturated))*samplingFrequencies[sample_freq_idx];
    }

    private static int dt=50;
    private static int length=5;
    private static double thresh=0.002;
    private static double scaler=0.130;//119.97;
    public static List<Double> derivative(List<Double> mSamples,double mEffectiveSamplingFrequency){
        int ls=mSamples.size();
        double[] forward=new double[ls+dt];
        boolean[] diff=new boolean[ls+dt];
        Double[] series=mSamples.toArray(new Double[ls]);
        for (int i = 0; i < forward.length; i++) { forward[i]=0; diff[i]=false; }

        for (int i = 0; i < ls; i++) {
            if(i<ls-dt){ forward[i+dt]=(series[i+dt]-series[i])/dt; }
            if((i>=950)||(i<50)){ diff[i+dt]=true; }
            else{ diff[i+dt]=(Math.abs(forward[i+dt]-forward[i])>thresh); }
        }

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
                    currentstate=diff[i]; //True False True False
                    riseEdges.add(i-50);
                }
            }
        }

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

        List<Double> res=new ArrayList<>();
        for (double v : result) { res.add(v*scaler*mEffectiveSamplingFrequency); }
        return res;
    }

    public static int closest(double frequency){
        double min_diff=99999999;
        int min_i=0;
        for (int i = 0; i < samplingFrequencies.length; i++) {
            double diff=Math.abs(samplingFrequencies[i]-frequency);
            if(min_diff>diff){
                min_diff=diff;
                min_i=i;
            }
        }
        return min_i;
    }
}
