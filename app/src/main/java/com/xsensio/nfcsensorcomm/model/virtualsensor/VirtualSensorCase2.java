package com.xsensio.nfcsensorcomm.model.virtualsensor;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.model.CalibrationProfile;
import com.xsensio.nfcsensorcomm.model.DataPoint;
import com.xsensio.nfcsensorcomm.model.DataUtils;
import com.xsensio.nfcsensorcomm.model.FunctionEstimator;
import com.xsensio.nfcsensorcomm.model.LinearFunctionEstimator;
import com.xsensio.nfcsensorcomm.model.PhoneMcuCommand;
import com.xsensio.nfcsensorcomm.model.SignalProcessor;
import com.xsensio.nfcsensorcomm.model.TimescaleEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by michael on 05.09.17.
 */

public abstract class VirtualSensorCase2 implements VirtualSensor {

    private static final String TAG = "VirtualSensorCase2";

    /** Command that initiated the sensor reading */
    private PhoneMcuCommand mCommand;

    /** Duration (milliseconds) of the read data process */
    private long mReadDataDuration;

    /** List of readouts coming from the sensor */
    private List<Byte> mReadoutsAsBytes;

    /** Total number of bytes to receive */
    private int mNumBytesToReceive;

    /** Number of bytes sent by the sensor for each measured sample */
    private int mNumBytesPerSample;

    protected VirtualSensorCase2(Context context, PhoneMcuCommand command) {

        mCommand = command;

        // Load settings
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        mNumBytesPerSample = Integer.valueOf(settings.getString("num_bytes_per_sample", context.getString(R.string.num_bytes_per_sample_def_val)));
        int numSamplesToReceive = Integer.valueOf(settings.getString("num_samples_roc2", context.getString(R.string.num_samples_roc2_def_val)));
        mNumBytesToReceive = numSamplesToReceive * mNumBytesPerSample;

        mReadoutsAsBytes = new ArrayList<>();
    }

    public void saveReadoutBytesReceived(List<Byte> readouts) {
        mReadoutsAsBytes = new ArrayList<>(readouts);
    }

    @Override
    public int getNumBytesToReceive() {
        return mNumBytesToReceive;
    }

    /**
     * Arbitrary function applied on the mapped data.
     * @param mappedData
     * @return
     */
    protected abstract Double applyFunctionOnMappedData(Double mappedData);

    public long getReadDataDuration() {
        return mReadDataDuration;
    }

    public void setReadDataDuration(long duration) {
        mReadDataDuration = duration;
    }

    ///////////// Parcelable interface implementation /////////////

    protected VirtualSensorCase2(Parcel in) {
        mCommand = in.readParcelable(PhoneMcuCommand.class.getClassLoader());
        mReadDataDuration = in.readLong();
        mNumBytesPerSample = in.readInt();
        int numReadouts = in.readInt();

        // Read Readouts list
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

        // Write Readouts list
        byte[] tmp = new byte[mReadoutsAsBytes.size()];
        for (int i = 0; i < mReadoutsAsBytes.size(); i++) {
            tmp[i] = mReadoutsAsBytes.get(i);
        }
        dest.writeByteArray(tmp);
    }

    ///////////// Data Container /////////////

    public DataContainer getDataContainer(Context context, CalibrationProfile profile) {
        return new DataContainer(context, profile);
    }

    public class DataContainer {

        private List<Double> mSamples;
        private List<DataPoint> mDerivativesDp;
        private List<DataPoint> mMappedDataDp;

        private double mValueRef;

        private int mEffectiveSamplingFrequency;

        protected double mDeadbandSize;

        private TimescaleEnum mTimescale;

        private double mAverageDerivative;

        private double mAverageMappedData;

        public int[] samplingFrequencies={380,724,1297,2168,3271,6706,13368,27593,56306,83892,112359,169491,1};

        public double freqMCU=0;

        private void setFreqMCU(int currentFreqIdx,int fromMCU){
            if(currentFreqIdx>11){
                freqMCU=samplingFrequencies[fromMCU];
            } else {
                freqMCU=SignalProcessor.getRightSampleRate(mSamples,currentFreqIdx);
            }
            mEffectiveSamplingFrequency=(int)freqMCU/mCommand.getNumSensorsSelected();
        }
        private DataContainer(Context context, CalibrationProfile profile) {

            // Load settings
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            int numBitsPerSample = Integer.valueOf(settings.getString("num_bits_per_sample_roc2", context.getString(R.string.num_bits_per_sample_roc2_def_val)));
            mValueRef = Float.valueOf(settings.getString("value_ref_roc2", context.getString(R.string.value_ref_roc2_def_val)));
            int overallSamplingFrequency = samplingFrequencies[Integer.valueOf(settings.getString("sampling_frequency", context.getString(R.string.sampling_frequency_def_val)))];
            int numSamplesForDerivative = Integer.valueOf(settings.getString("num_samples_for_derivative", context.getString(R.string.num_samples_for_derivative_def_val)));
            mDeadbandSize = Double.valueOf(settings.getString("deadband_size", context.getString(R.string.deadband_size_def_val)));
            mTimescale = TimescaleEnum.valueOf(settings.getString("graph_time_scale", context.getString(R.string.graph_time_scale_def_val)));

            // Compute effective sampling frequency
            mEffectiveSamplingFrequency = overallSamplingFrequency/mCommand.getNumSensorsSelected();
            // Group bytes to obtain samples as decimal numbers
            int fromMCU=mReadoutsAsBytes.get(mReadoutsAsBytes.size()-1)>>4;
            mReadoutsAsBytes=mReadoutsAsBytes.subList(0,mReadoutsAsBytes.size()-2);
            mSamples = DataUtils.bytesToDecimals(mReadoutsAsBytes, mNumBytesPerSample, numBitsPerSample, mValueRef);

            setFreqMCU(Integer.valueOf(settings.getString("sampling_frequency", context.getString(R.string.sampling_frequency_def_val))),fromMCU);

            // Compute derivatives over the samples
            double samplingPeriod = 1/(double) mEffectiveSamplingFrequency;
            //List<Double> derivatives = DataUtils.computeDerivatives(mSamples, numSamplesForDerivative, samplingPeriod);
            List<Double> derivatives = SignalProcessor.derivative(mSamples,samplingPeriod);

            List<DataPoint> derivativeDatapoints = formDataVsTimeDataPoints(derivatives, mTimescale);

//            List<DataPoint> negativeDeadbandDerivativeDp = getNegativeDeadbandDerivativeDataPoints(derivativeDatapoints);
//
//            List<DataPoint> positiveDeadbandDerivativeDp = getPositiveDeadbandDerivativesDataPoints(derivativeDatapoints);
//
//            double negativeDeadbandDerivativeAverage = DataPoint.computeAverageOverY(negativeDeadbandDerivativeDp);
//
//            double positiveDeadbandDerivativeAverage = DataPoint.computeAverageOverY(positiveDeadbandDerivativeDp);

            mAverageDerivative=SignalProcessor.mean(derivatives);
            mDerivativesDp=new ArrayList<>(derivativeDatapoints);
//            if (Math.abs(negativeDeadbandDerivativeAverage) < Math.abs(positiveDeadbandDerivativeAverage)) {
//                mAverageDerivative = negativeDeadbandDerivativeAverage;
//                mDerivativesDp = new ArrayList<>(negativeDeadbandDerivativeDp);
//            } else {
//                mAverageDerivative = positiveDeadbandDerivativeAverage;
//                mDerivativesDp = new ArrayList<>(positiveDeadbandDerivativeDp);
//            }

            // Map derivatives to concentration/pH/...
            mMappedDataDp = computeMappedData(profile, mDerivativesDp);

            // Compute the average of the mapped data
            mAverageMappedData = DataPoint.computeAverageOverY(mMappedDataDp);
        }

        private List<DataPoint> getNegativeDeadbandDerivativeDataPoints(List<DataPoint> derivativesDp) {

            // Compute the average of negative derivatives
            double sum = 0;
            int count = 0;
            for (DataPoint dp : derivativesDp) {
                if (dp.getY() < 0) {
                    count++;
                    sum += dp.getY();
                }
            }
            double negativeDerivativesAverage = sum / count;
            Log.d(TAG, "negativeDerivativesAverage = " + negativeDerivativesAverage);

            // Keep derivatives in the range (negativeDerivativesAverage-mDeadbandSize; negativeDerivativesAverage)
            List<DataPoint> derivativeDataPointsToKeep = new ArrayList<>();
            for (DataPoint dp : derivativesDp) {
                if (dp.getY() > (negativeDerivativesAverage-mDeadbandSize) && dp.getY() < negativeDerivativesAverage) {
                    derivativeDataPointsToKeep.add(dp);
                }
            }
            return derivativeDataPointsToKeep;
        }

        private List<DataPoint> getPositiveDeadbandDerivativesDataPoints(List<DataPoint> derivativesDp) {

            // Compute the average of positive derivatives
            double sum = 0;
            double count = 0;
            for (DataPoint dp: derivativesDp) {
                if (dp.getY() > 0) {
                    count++;
                    sum += dp.getY();
                }
            }
            double positiveDerivativesAverage = sum / count;
            Log.d(TAG, "positiveDerivativesAverage = " + positiveDerivativesAverage);

            // Keep derivatives in the range (positiveDerivativesAverage; positiveDerivativesAverage+mDeadbandSize)
            List<DataPoint> derivativeDataPointsToKeep = new ArrayList<>();
            for (DataPoint dp : derivativesDp) {
                if (dp.getY() > positiveDerivativesAverage && dp.getY() < (positiveDerivativesAverage+mDeadbandSize)) {
                    derivativeDataPointsToKeep.add(dp);
                }
            }

            return derivativeDataPointsToKeep;
        }

        private List<DataPoint> computeMappedData(CalibrationProfile profile, List<DataPoint> valuesToMap) {

            if (profile == null) {
                return new ArrayList<>();
            }

            List<DataPoint> mappedDataPoints = new ArrayList<>();

            FunctionEstimator functionEstimator = new LinearFunctionEstimator(profile.getRefReadoutVsOutputMapping());
            for (DataPoint dp : valuesToMap) {
                // Apply mapping
                double mappedValue = functionEstimator.getEstimate(dp.getY());

                // Apply sensor-dependent function on every mapped data
                mappedValue = applyFunctionOnMappedData(mappedValue);

                mappedDataPoints.add(new DataPoint(dp.getX(), mappedValue));
            }

            return mappedDataPoints;
        }

        private List<DataPoint> formDataVsTimeDataPoints(List<Double> abscissaData, TimescaleEnum timescale) {
            List<DataPoint> dataPoints = new ArrayList<>();

            List<Double> timeAxisPoints = DataUtils.getTimeAxisPoints(abscissaData.size(), timescale, mEffectiveSamplingFrequency);

            for (int i = 0; i < abscissaData.size(); i++) {
                dataPoints.add(new DataPoint(timeAxisPoints.get(i), abscissaData.get(i)));
            }
            return dataPoints;
        }

        public List<DataPoint> getSamplesVsTime() {
            return formDataVsTimeDataPoints(mSamples, mTimescale);
        }

        public List<DataPoint> getDerivativesVsTime() {
            return mDerivativesDp;
        }

        public List<DataPoint> getMappedDataVsTime() {
            return mMappedDataDp;
        }

        public double getAverageDerivative() { return mAverageDerivative; }

        public double getAverageMappedData() { return mAverageMappedData; }

        public double getValueRef() {
            return mValueRef;
        }

        public TimescaleEnum getTimescale() {
            return mTimescale;
        }

    }
}