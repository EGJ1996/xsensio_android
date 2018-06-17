package com.xsensio.nfcsensorcomm.model.virtualsensor;

import android.content.Context;
import android.os.Parcel;

import com.xsensio.nfcsensorcomm.model.CalibrationProfile;
import com.xsensio.nfcsensorcomm.model.DataPoint;
import com.xsensio.nfcsensorcomm.model.PhoneMcuCommand;
import com.xsensio.nfcsensorcomm.model.TimescaleEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of VirtualSensor for Sensor 3 Case 1
 */
public class Sensor3Case1 extends VirtualSensorCase1 {

    private static final String TAG = "Sensor3Case1";

    /** Virtual sensor definition */
    public static final VirtualSensorDefinition VIRTUAL_SENSOR_DEFINITION = VirtualSensorDefinitionCase1.SENSOR_3;

    public Sensor3Case1(Context context, PhoneMcuCommand command) {
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

    protected Sensor3Case1(Parcel in) {
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

    public static final Creator<Sensor3Case1> CREATOR = new Creator<Sensor3Case1>() {
        @Override
        public Sensor3Case1 createFromParcel(Parcel in) {
            return new Sensor3Case1(in);
        }

        @Override
        public Sensor3Case1[] newArray(int size) {
            return new Sensor3Case1[size];
        }
    };
}