package com.xsensio.nfcsensorcomm.model.virtualsensor;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.preference.PreferenceManager;
import android.util.Log;

import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.model.CalibrationProfile;
import com.xsensio.nfcsensorcomm.model.DataPoint;
import com.xsensio.nfcsensorcomm.model.DataUtils;
import com.xsensio.nfcsensorcomm.model.FunctionEstimator;
import com.xsensio.nfcsensorcomm.model.LinearFunctionEstimator;
import com.xsensio.nfcsensorcomm.model.PhoneMcuCommand;
import com.xsensio.nfcsensorcomm.model.TimescaleEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by michael on 05.09.17.
 */

public abstract class VirtualSensorCase3 implements VirtualSensor {

    private static final String TAG = "VirtualSensorCase3";

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

    protected VirtualSensorCase3(Context context, PhoneMcuCommand command) {

        mCommand = command;

        // Load settings
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        mNumBytesPerSample = Integer.valueOf(settings.getString("num_bytes_per_sample", context.getString(R.string.num_bytes_per_sample_def_val)));
        int numSamplesToReceive = Integer.valueOf(settings.getString("num_samples_roc3", context.getString(R.string.num_samples_roc3_def_val)));
        mNumBytesToReceive = numSamplesToReceive * mNumBytesPerSample;

        mReadoutsAsBytes = new ArrayList<>();
    }

    @Override
    public void saveReadoutBytesReceived(List<Byte> readoutBytes) {
        mReadoutsAsBytes = readoutBytes;
    }

    @Override
    public int getNumBytesToReceive() {
        return mNumBytesToReceive;
    }

    protected abstract Double applyFunctionOnMappedData(Double mappedData);

    ///////////// Parcelable interface implementation /////////////

    protected VirtualSensorCase3(Parcel in) {
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
        return new DataContainer(context, mReadoutsAsBytes, mNumBytesPerSample, profile);
    }

    public class DataContainer {

        private List<Double> mSamples;
        private List<Double> mMappedData;

        private TimescaleEnum mTimescale;

        /** Sampling frequency used by the ADC to sample the analog signal produced by the sensor */
        private int mEffectiveSamplingFrequency;

        private DataContainer(Context context, List<Byte> readoutsAsBytes, int numBytesPerSample, CalibrationProfile profile) {

            // Load settings
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            int numBitsPerSample = Integer.valueOf(settings.getString("num_bits_per_sample_roc3", context.getString(R.string.num_bits_per_sample_roc3_def_val)));
            int overallSamplingFrequency = Integer.valueOf(settings.getString("sampling_frequency", context.getString(R.string.sampling_frequency_def_val)));
            mTimescale = TimescaleEnum.valueOf(settings.getString("graph_time_scale", context.getString(R.string.graph_time_scale_def_val)));

            // Compute effective sampling frequency
            mEffectiveSamplingFrequency = overallSamplingFrequency/mCommand.getNumSensorsSelected();

            // Group bytes to obtain samples as decimal numbers
            List<Integer> samplesAsIntegers = DataUtils.bytesToInteger(readoutsAsBytes, numBytesPerSample, numBitsPerSample);

            mSamples = new ArrayList<>();
            for (Integer sample : samplesAsIntegers) {
                mSamples.add((double) sample);
            }

            // Map integer to concentration/pH/...
            mMappedData = computeMappedData(profile);
        }

        private List<Double> computeMappedData(CalibrationProfile profile) {

            if (profile == null) {
                return new ArrayList<>();
            }

            FunctionEstimator functionEstimator = new LinearFunctionEstimator(profile.getRefReadoutVsOutputMapping());
            List<Double> mappedData = functionEstimator.getEstimates(mSamples);
            Log.d(TAG, "There are " + mappedData.size() + " mapped data.");

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

        public List<DataPoint> getMappedDataVsTime() {
            return formDataVsTimeDataPoints(mMappedData, mTimescale);
        }

        public TimescaleEnum getTimescale() {
            return mTimescale;
        }
    }
}
