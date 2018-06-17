package com.xsensio.nfcsensorcomm.model.virtualsensor;

import android.content.Context;
import android.os.Parcel;

import com.xsensio.nfcsensorcomm.model.PhoneMcuCommand;

/**
 * Implementation of VirtualSensor for Sensor 2 Case 3
 */
public class Sensor2Case3 extends VirtualSensorCase3 {

    private static final String TAG = "Sensor2Case3";

    /** Virtual sensor definition */
    public static final VirtualSensorDefinition VIRTUAL_SENSOR_DEFINITION = VirtualSensorDefinitionCase3.SENSOR_2;

    public Sensor2Case3(Context context, PhoneMcuCommand command) {
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

    protected Sensor2Case3(Parcel in) {
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

    public static final Creator<Sensor2Case3> CREATOR = new Creator<Sensor2Case3>() {
        @Override
        public Sensor2Case3 createFromParcel(Parcel in) {
            return new Sensor2Case3(in);
        }

        @Override
        public Sensor2Case3[] newArray(int size) {
            return new Sensor2Case3[size];
        }
    };
}