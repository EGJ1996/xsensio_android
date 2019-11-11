package com.xsensio.nfcsensorcomm.model.virtualsensor;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.preference.PreferenceManager;
import android.support.v4.content.res.TypedArrayUtils;
import android.util.Log;

import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.model.CalibrationProfile;
import com.xsensio.nfcsensorcomm.model.DataPoint;
import com.xsensio.nfcsensorcomm.model.DataUtils;
import com.xsensio.nfcsensorcomm.model.FunctionEstimator;
import com.xsensio.nfcsensorcomm.model.LinearFunctionEstimator;
import com.xsensio.nfcsensorcomm.model.PhoneMcuCommand;
import com.xsensio.nfcsensorcomm.model.TimescaleEnum;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michael on 05.09.17.
 */

public abstract class VirtualSensorCase1 implements VirtualSensor {

    private static final String TAG = "VirtualSensorCase1";

    /** Command that initiated the sensor reading */
    private PhoneMcuCommand mCommand;

    /** Duration (milliseconds) of the read data process */
    private long mReadDataDuration;

    /** List of readouts coming from the sensor */
    protected List<Byte> mReadoutsAsBytes;

    /** Total number of bytes to receive */
    private int mNumBytesToReceive;

    /** Number of bytes sent by the sensor for each measured sample */
    private int mNumBytesPerSample;

    protected VirtualSensorCase1(Context context, PhoneMcuCommand command) {

        mCommand = command;

        // Load Settings
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        mNumBytesPerSample = Integer.valueOf(settings.getString("num_bytes_per_sample", context.getString(R.string.num_bytes_per_sample_def_val)));
        int numSamplesToReceive = Integer.valueOf(settings.getString("num_samples_roc1", context.getString(R.string.num_samples_roc1_def_val)));
        mNumBytesToReceive = numSamplesToReceive * mNumBytesPerSample;

        mReadoutsAsBytes = new ArrayList<>();
    }

    @Override
    public void saveReadoutBytesReceived(List<Byte> readoutBytes) {
        mReadoutsAsBytes = readoutBytes;
    }

    public List<Byte> getReadoutBytes(){
        return mReadoutsAsBytes;
    }

    @Override
    public int getNumBytesToReceive() {
        return mNumBytesToReceive;
    }

    protected abstract Double applyFunctionOnMappedData(Double mappedData);

    ///////////// Parcelable interface implementation /////////////

    protected VirtualSensorCase1(Parcel in) {
        mCommand = in.readParcelable(PhoneMcuCommand.class.getClassLoader());
        mReadDataDuration = in.readLong();
        mNumBytesPerSample = in.readInt();
        int numReadouts = in.readInt();

        byte[] tmp = new byte[numReadouts];
        in.readByteArray(tmp);

        mReadoutsAsBytes = new ArrayList<>();
        for (int i = 0; i < tmp.length; i++) {
            mReadoutsAsBytes.add(tmp[i]);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mCommand, flags);
        dest.writeLong(mReadDataDuration);
        dest.writeInt(mNumBytesPerSample);
        dest.writeInt(mReadoutsAsBytes.size());

        byte[] tmp = new byte[mReadoutsAsBytes.size()];
        for (int i = 0; i < mReadoutsAsBytes.size(); i++) {
            tmp[i] = mReadoutsAsBytes.get(i);
        }
        dest.writeByteArray(tmp);
    }

    public long getReadDataDuration() {
        return mReadDataDuration;
    }

    public void setReadDataDuration(long duration) {
        mReadDataDuration = duration;
    }

    ///////////// Data Container /////////////

    public DataContainer getDataContainer(Context context, CalibrationProfile profile) {
        return new DataContainer(context, profile);
    }

    public class DataContainer {

        private List<Double> mSamples;
        private List<Double> mMappedData;

        private TimescaleEnum mTimescale;

        /** Sampling frequency used by the ADC to sample the analog signal produced by the sensor */
        private int mEffectiveSamplingFrequency;

        private DataContainer(Context context, CalibrationProfile profile) {

            // Load settings
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            int numBitsPerSample = Integer.valueOf(settings.getString("num_bits_per_sample_roc1", context.getString(R.string.num_bits_per_sample_roc1_def_val)));
            double valueRef = Float.valueOf(settings.getString("value_ref_roc1", context.getString(R.string.value_ref_roc1_def_val)));
            int overallSamplingFrequency = Integer.valueOf(settings.getString("sampling_frequency", context.getString(R.string.sampling_frequency_def_val)));
            mTimescale = TimescaleEnum.valueOf(settings.getString("graph_time_scale", context.getString(R.string.graph_time_scale_def_val)));

            // Compute effective sampling frequency
            mEffectiveSamplingFrequency = overallSamplingFrequency/mCommand.getNumSensorsSelected();
            // Group bytes to obtain samples as decimal numbers

            mSamples = DataUtils.bytesToDecimals(mReadoutsAsBytes, mNumBytesPerSample, numBitsPerSample, valueRef);
            // Map integer to concentration/pH/...
            mMappedData = computeMappedData(profile);
        }



        private List<Double> computeMappedData(CalibrationProfile profile) {
            if (profile == null) {
                return new ArrayList<>();
            }

            FunctionEstimator functionEstimator = new LinearFunctionEstimator(profile.getRefReadoutVsOutputMapping());
            List<Double> mappedData = functionEstimator.getEstimates(mSamples);

            // Apply sensor-dependent function on every mapped data
            for (int i = 0; i < mappedData.size(); i++) {
                mappedData.set(i, applyFunctionOnMappedData(mappedData.get(i)));
            }

            return mappedData;
        }


        private List<DataPoint> formDataVsTimeDataPoints(List<Double> abscissaData, TimescaleEnum timescale) {
            List<DataPoint> dataPoints = new ArrayList<>();

            List<Double> timeAxisPoints = DataUtils.getTimeAxisPoints(abscissaData.size(), timescale, mEffectiveSamplingFrequency);

            for (int i = 0; i < abscissaData.size(); i++) {
                dataPoints.add(new DataPoint(timeAxisPoints.get(i), abscissaData.get(i)));
            }
            return dataPoints;
        }

        public List<DataPoint> getMappedDataVsTime( ) {
            return formDataVsTimeDataPoints(mMappedData, mTimescale);
        }

        public TimescaleEnum getTimescale() {
            return mTimescale;
        }
    }
}
