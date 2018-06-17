package com.xsensio.nfcsensorcomm.model.virtualsensor;

import android.content.Context;
import android.os.Parcel;

import com.xsensio.nfcsensorcomm.model.PhoneMcuCommand;

/**
 * Implementation of VirtualSensor for Sensor 3 Case 2
 */
public class Sensor3Case2 extends VirtualSensorCase2 {

    private static final String TAG = "Sensor3Case2";

    /** Virtual sensor definition */
    private static final VirtualSensorDefinition VIRTUAL_SENSOR_DEFINITION = VirtualSensorDefinitionCase2.SENSOR_3;

    public Sensor3Case2(Context context, PhoneMcuCommand command) {
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

    protected Sensor3Case2(Parcel in) {
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

    public static final Creator<Sensor3Case2> CREATOR = new Creator<Sensor3Case2>() {
        @Override
        public Sensor3Case2 createFromParcel(Parcel in) {
            return new Sensor3Case2(in);
        }

        @Override
        public Sensor3Case2[] newArray(int size) {
            return new Sensor3Case2[size];
        }
    };
}