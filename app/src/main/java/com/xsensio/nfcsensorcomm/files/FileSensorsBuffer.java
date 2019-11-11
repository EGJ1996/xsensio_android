package com.xsensio.nfcsensorcomm.files;

import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensor;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class FileSensorsBuffer implements Serializable {
    public ArrayList<VirtualSensor> virtualSensors;
    public FileSensorsBuffer(ArrayList<VirtualSensor> sensors){
        virtualSensors=sensors;
    }
    public boolean isEmpty(){
        return virtualSensors.isEmpty();
    }
}
