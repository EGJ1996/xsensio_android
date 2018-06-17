package com.xsensio.nfcsensorcomm.model.virtualsensor;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;

/**
 * Abstraction representing a pair (Sensor, Readout Case) since potentially all such pairs
 * can differ in their processing.
 */
public interface VirtualSensor extends Parcelable, Serializable {

    void saveReadoutBytesReceived(List<Byte> readoutBytes);

    int getNumBytesToReceive();

    VirtualSensorDefinition getVirtualSensorDefinition();

    void setReadDataDuration(long duration);

    long getReadDataDuration();

}
