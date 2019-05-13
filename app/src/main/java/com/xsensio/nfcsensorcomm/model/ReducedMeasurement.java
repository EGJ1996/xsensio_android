package com.xsensio.nfcsensorcomm.model;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.time.LocalDateTime;

public class ReducedMeasurement {
    private LocalDateTime dateTime;
    private double phVal;
    private double sodiumVal;
    private double temperatureVal;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ReducedMeasurement(String str) {
        String[] vals=str.split(",");
        this.dateTime = LocalDateTime.parse(vals[0]);
        this.phVal = Double.valueOf(vals[1]);
        this.sodiumVal = Double.valueOf(vals[2]);
        this.temperatureVal = Double.valueOf(vals[3]);
    }

    public ReducedMeasurement(LocalDateTime dateTime, double phVal, double sodiumVal, double temperatureVal) {
        this.dateTime = dateTime;
        this.phVal = phVal;
        this.sodiumVal = sodiumVal;
        this.temperatureVal = temperatureVal;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public double getPhVal() {
        return phVal;
    }

    public void setPhVal(double phVal) {
        this.phVal = phVal;
    }

    public double getSodiumVal() {
        return sodiumVal;
    }

    public void setSodiumVal(double sodiumVal) {
        this.sodiumVal = sodiumVal;
    }

    public double getTemperatureVal() {
        return temperatureVal;
    }

    public void setTemperatureVal(double temperatureVal) {
        this.temperatureVal = temperatureVal;
    }

    public boolean isPhValid(){
        return Double.isFinite(phVal);
    }

    public boolean isSodiumValid(){
        return Double.isFinite(sodiumVal);
    }

    public boolean isTemperatureValid(){
        return Double.isFinite(temperatureVal);
    }

    @Override
    public String toString() {
        StringBuilder builder=new StringBuilder();
        builder.append(dateTime).append(",");
        builder.append(phVal).append(",");
        builder.append(sodiumVal).append(",");
        builder.append(temperatureVal).append("\n");
        return builder.toString();
    }

    public String getTimeStamp(){
        StringBuilder builder=new StringBuilder();
        builder.append(dateTime.getYear()).append("/");
        builder.append(dateTime.getMonthValue()).append("/");
        builder.append(dateTime.getDayOfMonth()).append(" ");
        builder.append(dateTime.getHour()).append(":");
        builder.append(dateTime.getMinute());
        return builder.toString();
    }
}
