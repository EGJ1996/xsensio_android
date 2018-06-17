package com.xsensio.nfcsensorcomm.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.xsensio.nfcsensorcomm.Utils;
import com.xsensio.nfcsensorcomm.model.virtualsensor.Sensor1Case1;
import com.xsensio.nfcsensorcomm.model.virtualsensor.Sensor1Case2;
import com.xsensio.nfcsensorcomm.model.virtualsensor.Sensor1Case3;
import com.xsensio.nfcsensorcomm.model.virtualsensor.Sensor2Case1;
import com.xsensio.nfcsensorcomm.model.virtualsensor.Sensor2Case2;
import com.xsensio.nfcsensorcomm.model.virtualsensor.Sensor2Case3;
import com.xsensio.nfcsensorcomm.model.virtualsensor.Sensor3Case1;
import com.xsensio.nfcsensorcomm.model.virtualsensor.Sensor3Case2;
import com.xsensio.nfcsensorcomm.model.virtualsensor.Sensor3Case3;
import com.xsensio.nfcsensorcomm.model.virtualsensor.Sensor4Case1;
import com.xsensio.nfcsensorcomm.model.virtualsensor.Sensor4Case2;
import com.xsensio.nfcsensorcomm.model.virtualsensor.Sensor4Case3;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Represent a command sent from the Phone to the MCU. For instance, the Phone can send a command
 * telling the MCU to read and samples signals from sensors and send back the data.
 *
 *
 *
 * One command is 4 blocks of 4 bytes => 16 bytes in total
 *
 * The first byte of the first block is used to store 4 bits to designate the sensors concerned by the command (if any).
 *
 * Then 2 bytes are used to store the number of samples for Readout Case 1
 * 2 bytes are used to store the number of samples for Readout Case 2
 * 2 bytes are used to store the number of samples for Readout Case 3
 * 1 byte is used to select sensor (first 4 bytes), sampling rate (last 4 bytes)
 *
 * e.g. Read sensors 1,3 and 4 for readout case 1 (65535 samples) and readout case 3 (32771 samples):
 * first byte is 00001011
 * next 2 bytes are 11111111 11111111
 * next 2 bytes are 00000000 00000000
 * next 2 bytes are 10000000 00000011
 *
 */
public class PhoneMcuCommand implements Parcelable, Serializable {

    private static final String TAG = "PhoneMcuCommand";

    /** Number of bytes available for the content of one command from the phone to the MCU */
    public static final int COMMAND_SIZE = 16;

    /** Maximum number of sensors */
    public static final int MAX_NUM_SENSORS = 4;

    /** Number of bytes allocated to store (in the command to the MCU) the number of samples
     * that the MCU has to return
     */
    public static final int NUM_BYTES_TO_STORE_NUM_SAMPLES = 2;

    /** Indicates if the MCU should send data for sensor 1 */
    private boolean mSensor1;

    /** Indicates if the MCU should send data for sensor 2 */
    private boolean mSensor2;

    /** Indicates if the MCU should send data for sensor 3 */
    private boolean mSensor3;

    /** Indicates if the MCU should send data for sensor 4 */
    private boolean mSensor4;

    /** Number of samples that the MCU has to return for Readout Case 1
     * (same number for all sensors)
     */
    private int mNumSamplesForRoc1;

    /** Number of samples that the MCU has to return for Readout Case 2
     * (same number for all sensors)
     */
    private int mNumSamplesForRoc2;

    /** Number of samples that the MCU has to return for Readout Case 3
     * (same number for all sensors)
     */
    private int mNumSamplesForRoc3;

    /** sensor selector
    */
    private int mMuxSensor;

    /** Sampling rate
     */
    private int mSampleRate;

    /**
     * Create a new command for the MCU stating that it should read and sample data from sensors
     * and returns them.
     * @param context used to access app settings
     * @param sensor1 true if the MCU should return data from sensor 1, false if not
     * @param sensor2 true if the MCU should return data from sensor 2, false if not
     * @param sensor3 true if the MCU should return data from sensor 3, false if not
     * @param sensor4 true if the MCU should return data from sensor 4, false if not
     * @param numSamplesRoc1 Number of bytes the MCU should return for Readout Case 1
     *                     (all selected sensors)
     * @param numSamplesRoc2 Number of bytes the MCU should return for Readout Case 2
     *                     (all selected sensors)
     * @param numSamplesRoc3 Number of bytes the MCU should return for Readout Case 3
     *                     (all selected sensors)
     * @param muxSensor select which sensor
     * @param sampleRate sampling rate
     */
    public PhoneMcuCommand(Context context, boolean sensor1, boolean sensor2, boolean sensor3, boolean sensor4,
                            int numSamplesRoc1, int numSamplesRoc2, int numSamplesRoc3, int muxSensor, int sampleRate) {

        mSensor1 = sensor1;
        mSensor2 = sensor2;
        mSensor3 = sensor3;
        mSensor4 = sensor4;
        mNumSamplesForRoc1 = numSamplesRoc1;
        mNumSamplesForRoc2 = numSamplesRoc2;
        mNumSamplesForRoc3 = numSamplesRoc3;
        mMuxSensor = muxSensor;
        mSampleRate = sampleRate;
    }

    /**
     * Build a byte with bits corresponding to selected sensors equal to 1. Only bits b3,b2,b1,b0
     * are used where the format assumed is byte = b7,b6,b5,b4,b3,b2,b1,b0.
     * b3 indicates if sensor 1 is selected, b2 indicates if sensor 2 is selected,
     * b1 indicates if sensor 3 is selected, b0 indicates if sensor 4 is selected.
     * e.g if sensors 1 and 3 are selected, the returned byte is 00001010
     * @return byte
     */
    public byte getSelectedSensorsAsByte() {

        byte sensorsToReadMask = (byte) (0x00);

        if(mSensor1)
            sensorsToReadMask = (byte) (sensorsToReadMask | 0x01 << 3);

        if(mSensor2)
            sensorsToReadMask = (byte) (sensorsToReadMask | 0x01 << 2);

        if(mSensor3)
            sensorsToReadMask = (byte) (sensorsToReadMask | 0x01 << 1);

        if(mSensor4)
            sensorsToReadMask = (byte) (sensorsToReadMask | 0x01);

        return sensorsToReadMask;
    }

    public byte getMuxSampleByte(){
        byte tmp=(byte)((mSampleRate & 0x0f) << 4);
        tmp+=(byte)(mMuxSensor & 0x0f);
        return tmp;
    }

    public byte[] getCommandAsBytes() {
        byte[] command = Utils.createByteArrayWithzeros(COMMAND_SIZE);

        /**
         * Code the number of samples into bytes as if they were unsigned.
         * E.g. if the number of samples for readout case 1 is 800 and the number of bytes used to
         * send this number to the MCU is 2 (i.e. the highest number of samples we can specify
         * would be 65535), then the resulting bytes are 00000011 0010000 (in cell 0 and 1,
         * respectively)
         */
        byte[] roc1Bytes = Utils.intToBytes(mNumSamplesForRoc1);
        byte[] roc1BytesTrunc = Arrays.copyOfRange(roc1Bytes, roc1Bytes.length-NUM_BYTES_TO_STORE_NUM_SAMPLES, roc1Bytes.length);

        byte[] roc2Bytes = Utils.intToBytes(mNumSamplesForRoc2);
        byte[] roc2BytesTrunc = Arrays.copyOfRange(roc2Bytes, roc2Bytes.length-NUM_BYTES_TO_STORE_NUM_SAMPLES, roc2Bytes.length);

        byte[] roc3Bytes = Utils.intToBytes(mNumSamplesForRoc3);
        byte[] roc3BytesTrunc = Arrays.copyOfRange(roc3Bytes, roc3Bytes.length-NUM_BYTES_TO_STORE_NUM_SAMPLES, roc3Bytes.length);

        command[0] = getSelectedSensorsAsByte();

        /**
         * Copy roc1Bytes, roc2Bytes and roc3Bytes into command from cell 1 (included)
         */
        for (int i = 1; i <= 3*NUM_BYTES_TO_STORE_NUM_SAMPLES; i++) {
            if (i <= NUM_BYTES_TO_STORE_NUM_SAMPLES) {
                command[i] = roc1BytesTrunc[i-1];
            } else if (i <= 2*NUM_BYTES_TO_STORE_NUM_SAMPLES) {
                command[i] = roc2BytesTrunc[i-1-NUM_BYTES_TO_STORE_NUM_SAMPLES];
            } else {
                command[i] = roc3BytesTrunc[i-1-2*NUM_BYTES_TO_STORE_NUM_SAMPLES];
            }
        }
        //Adding sensor and sampling rate selector byte
        command[3*NUM_BYTES_TO_STORE_NUM_SAMPLES+1]=getMuxSampleByte();
        // Needed for the Tag to consider the WRITE on address 0xFF
        command[COMMAND_SIZE-1] = (byte) 0x11; // anything but 0x00

        return command;
    }

    /**
     * Return the list of virtual sensors for which the MCU will send data.
     * Note: The order of the indices in the list matters: it is a convention agreed with the MCU
     * that data is sent in the following order:
     * Sensor1-Case1, Sensor2-Case1, ..., Sensor4-Case1, Sensor1-Case2, Sensor2-Case2, ...
     */
    public ArrayList<VirtualSensor> getVirtualSensorsToFill(Context context) {
        ArrayList<VirtualSensor> list = new ArrayList<>();

        if (mNumSamplesForRoc1 > 0) {
            if (mSensor1) {
                list.add(new Sensor1Case1(context, this));
            }
            if (mSensor2) {
                list.add(new Sensor2Case1(context, this));
            }
            if (mSensor3) {
                list.add(new Sensor3Case1(context, this));
            }
            if (mSensor4) {
                list.add(new Sensor4Case1(context, this));
            }
        }

        if (mNumSamplesForRoc2 > 0) {
            if (mSensor1) {
                list.add(new Sensor1Case2(context, this));
            }
            if (mSensor2) {
                list.add(new Sensor2Case2(context, this));
            }
            if (mSensor3) {
                list.add(new Sensor3Case2(context, this));}

            if (mSensor4) {
                list.add(new Sensor4Case2(context, this));}

        }

        if (mNumSamplesForRoc3 > 0) {
            if (mSensor1) {
                list.add(new Sensor1Case3(context, this));
            }
            if (mSensor2) {
                list.add(new Sensor2Case3(context, this));
            }
            if (mSensor3) {
                list.add(new Sensor3Case3(context, this));
            }
            if (mSensor4) {
                list.add(new Sensor4Case3(context, this));
            }
        }

        return list;
    }


    public int getNumSensorsSelected() {
        int numSensorsSelected = 0;
        if (mSensor1) {
            numSensorsSelected++;
        }
        if (mSensor2) {
            numSensorsSelected++;
        }
        if (mSensor3) {
            numSensorsSelected++;
        }
        if (mSensor4) {
            numSensorsSelected++;
        }
        return numSensorsSelected;
    }


    ///////////// Parcelable interface implementation /////////////

    protected PhoneMcuCommand(Parcel in) {
        boolean[] sensorsSelected = new boolean[MAX_NUM_SENSORS];
        in.readBooleanArray(sensorsSelected);
        mSensor1 = sensorsSelected[0];
        mSensor2 = sensorsSelected[1];
        mSensor3 = sensorsSelected[2];
        mSensor4 = sensorsSelected[3];

        mNumSamplesForRoc1 = in.readInt();
        mNumSamplesForRoc2 = in.readInt();
        mNumSamplesForRoc3 = in.readInt();
        mMuxSensor = in.readInt();
        mSampleRate = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBooleanArray(new boolean[] {mSensor1, mSensor2, mSensor3, mSensor4});

        dest.writeInt(mNumSamplesForRoc1);
        dest.writeInt(mNumSamplesForRoc2);
        dest.writeInt(mNumSamplesForRoc3);
        dest.writeInt(mMuxSensor);
        dest.writeInt(mSampleRate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PhoneMcuCommand> CREATOR = new Creator<PhoneMcuCommand>() {
        @Override
        public PhoneMcuCommand createFromParcel(Parcel in) {
            return new PhoneMcuCommand(in);
        }

        @Override
        public PhoneMcuCommand[] newArray(int size) {
            return new PhoneMcuCommand[size];
        }
    };
}