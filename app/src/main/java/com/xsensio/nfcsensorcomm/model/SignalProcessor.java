package com.xsensio.nfcsensorcomm.model;

import android.preference.ListPreference;

import java.util.ArrayList;
import java.util.List;

public final class SignalProcessor {
    //Parameters
    public static double dt = 0;
    public static List<Double> signal= new ArrayList<>();
    public static int windowSize=50;
    public static double diffThresh=2;
    public static double rippleMinLength=1.8*windowSize;
    public static double zeroThresh=2;
    public static double filterTolerance=2;

    //Global variables
    private static double upper=0;
    private static double lower=99999;
    private static double total=0;
    private static double count=0;

    private static void signalCharacteristics(double forward){
        if(Math.abs(forward)>zeroThresh){
            total=total+forward;
            count=count+1;
            if(forward>upper){ upper=forward; }
            if(forward<lower){ lower=forward; }
        }
    }

    private static List<Integer> tmpRipple=new ArrayList<>();
    private static List<List<Integer>> ripples=new ArrayList<>();
    private static void rippleTracker(double forward, double backward, int i){
        double diff=Math.abs(forward-backward);
        if(tmpRipple.size()==0){
            if(diff>diffThresh){ tmpRipple.add(i); }
            else { signalCharacteristics(forward); }
        } else {
            if(diff<diffThresh){
                if(tmpRipple.size()>rippleMinLength){ ripples.add(tmpRipple); }
                tmpRipple=new ArrayList<>();
            } else { tmpRipple.add(i); }
        }
    }

    private static double[] df;
    private static double[] db;
    private static void derivativeLoop(){
        df=new double[signal.size()];
        db=new double[signal.size()];
        upper=0;
        lower=99999;
        total=0;
        count=0;
        tmpRipple=new ArrayList<>();
        ripples=new ArrayList<>();
        for (int i = 0; i < signal.size(); i++) {
            double forward=0;
            if(i<(signal.size()-windowSize)){ forward=(signal.get(i+windowSize)-signal.get(i))/(dt*windowSize); }
            double backward=0;
            if(i>windowSize){ backward=(signal.get(i)-signal.get(i-windowSize))/(dt*windowSize); }
            df[i]=forward;
            db[i]=backward;

            rippleTracker(forward,backward,i);
        }
        if(tmpRipple.size()>rippleMinLength){ ripples.add(tmpRipple); }
        tmpRipple=new ArrayList<>();
    }

    private static void combiner(){
        for (List<Integer> ripple : ripples) {
            int start=ripple.get(0);
            int mid=start+ripple.size()/2;
            for (int i = start; i < mid; i++) {
                df[i]=db[i];
            }
        }
        for (int i = signal.size()-windowSize; i < signal.size(); i++) {
            df[i]=db[i];
        }
    }

    private static List<Double> cutter(){
        double diff=upper-lower;
        double mean=total/count;
        List<Double> result=new ArrayList<>();
        for (int i = 0; i < df.length; i++) {
            if(Math.abs(df[i]-mean)>(filterTolerance*diff)){ result.add((double)0); }
            else { result.add(df[i]); }
        }
        return result;
    }

    public static List<Double> derivative(List<Double> input, double samplePeriod){
        dt=samplePeriod;
        signal=input;
        derivativeLoop();
        combiner();
        return cutter();
    }

    public static double mean(List<Double> target){
        double total=0;
        double count=0;
        for (Double forward : target) {
            if(Math.abs(forward)>zeroThresh){
                total=total+forward;
                count=count+1;
            }
        }
        if(count==0){return 0;}
        else {return total/count;}
    }


    //Part related to calibrate samplerate
    private static List<Double> sampleChange(List<Double> target){
        double saturated=0;
        double peak=0;
        for (Double sample : target) {
            if(sample>2.4){
                saturated+=1;
            } else {
                if(sample>peak){
                    peak=sample;
                }
            }
        }
        List<Double> up_down_sample=new ArrayList<>();
        up_down_sample.add(1/(1-saturated/1000));
        up_down_sample.add(2.5/peak);
        return up_down_sample;
    }

    public static int closest(double target){
        double[] arr={0.1,0.2,0.4,0.8,1,2,4,8,16,24,32,48};
        double min=999;
        int min_i=0;
        for (int i = 0; i < arr.length; i++) {
            double diff=Math.abs(arr[i]-target);
            if(min>diff){
                min=diff; min_i=i;
            }
        }
        return min_i;
    }

    public static double getRightSampleRate(List<Double> target, int current_sample_id){
        double[] samplerates={380,724,1297,2168,3271,6706,13368,27593,56306,83892,112359,169491};
        double current_samplerate=samplerates[current_sample_id];
        List<Double> up_down_sample=sampleChange(target);
        double ideal_samplerate;
        if(up_down_sample.get(0)>up_down_sample.get(1)){
            //increase sample rate
            ideal_samplerate=current_samplerate*up_down_sample.get(0);
        } else {
            //decrease sample rate
            ideal_samplerate=current_samplerate/up_down_sample.get(1);
        }
        return ideal_samplerate;
    }
}
