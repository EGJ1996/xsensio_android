package com.xsensio.nfcsensorcomm.mainactivity;

import android.widget.Button;

import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensor;

import java.util.ArrayList;

public class Global {
    public static volatile boolean nfc_set;
    public static volatile boolean data_read = false;
    public static Button global_button;
    public static ArrayList<VirtualSensor> global_sensors;
}
