package com.xsensio.nfcsensorcomm.model.virtualsensor;

import android.content.Context;
import android.os.Parcel;
import android.util.Log;

import com.xsensio.nfcsensorcomm.model.CalibrationProfile;
import com.xsensio.nfcsensorcomm.model.DataPoint;
import com.xsensio.nfcsensorcomm.model.PhoneMcuCommand;
import com.xsensio.nfcsensorcomm.model.TimescaleEnum;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of VirtualSensor for Sensor 1 Case 2
 */
public class Sensor1Case2 extends VirtualSensorCase2 {

    private static final String TAG = "Sensor1Case2";

    /** Virtual sensor definition */
    private static final VirtualSensorDefinition VIRTUAL_SENSOR_DEFINITION = VirtualSensorDefinitionCase2.SENSOR_1;

    public Sensor1Case2(Context context, PhoneMcuCommand command) {
        super(context, command);
    }

    @Override
    public VirtualSensorDefinition getVirtualSensorDefinition() {
        return VIRTUAL_SENSOR_DEFINITION;
    }

    @Override
    protected Double applyFunctionOnMappedData(Double mappedData) {
        return mappedData;
    }

    ///////////// Parcelable interface implementation /////////////

    protected Sensor1Case2(Parcel in) {
        super(in);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Sensor1Case2> CREATOR = new Creator<Sensor1Case2>() {
        @Override
        public Sensor1Case2 createFromParcel(Parcel in) {
            return new Sensor1Case2(in);
        }

        @Override
        public Sensor1Case2[] newArray(int size) {
            return new Sensor1Case2[size];
        }
    };
}