package com.xsensio.nfcsensorcomm.model;

import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensor;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorDefinition;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class representing a calibration profile of a virtual sensor. A calibration profile has one
 * and only one virtual sensor which determines some static data, see
 * {@link VirtualSensor}. It is used to calibrate the sensors and estimate
 * the outputs (e.g. concentration, pH, ...) for a given readout (i.e. a given measured value
 * returned by the ADC of a sensor).
 */
public class CalibrationProfile {

    private static final String TAG = "CalibrationProfile";

    /** Name of the profile. It is used to store the profile on the file system */
    private String mProfileName;

    /** Associated virtual sensor definition */
    private VirtualSensorDefinition mVirtualSensorDefinition;

    /** List of reference readouts: each readout is mapped with an output*/
    private List<Double> mReferenceReadouts;

    /** String representation of the reference readouts: avoids the problem of formatting double back into String */
    private String mReferenceReadoutsAsString;

    /** List of reference outsputs: each output is mapped with a readout */
    private List<Double> mReferenceOutputs;

    /** String representation of the reference outputs: avoids the problem of formatting double back into String */
    private String mReferenceOutputsAsString;

    // Contains mapping Readout -> Output, sorted on Readouts
    private Map<Double, Double> mRefReadoutVsRefOutputMapping;

    // Contains mapping Output -> Readout, sorted on Output
    private Map<Double, Double> mRefOutputsVsRefReadoutsMapping;

    /** Store the maximum value of all the reference readouts */
    private double mMinReadout = Double.MAX_VALUE;

    /** Store the minimum value of all the reference readouts */
    private double mMaxReadout = Double.MIN_VALUE;

    /** Store the minimum value of all the reference outputs */
    private double mMinOutput = Double.MAX_VALUE;

    /** Store the maximum value of all the reference outputs */
    private double mMaxOutput = Double.MIN_VALUE;

    /** Defines the margin around the extrema on the plot */
    private static final double AXIS_MARGIN = 0.05;

    public CalibrationProfile(String profileName,
                              VirtualSensorDefinition virtualSensorDefinition,
                              List<Double> refReadouts,
                              String refReadoutsAsString,
                              List<Double> outputs,
                              String refOutputsAsString) {

        mProfileName = profileName;

        mVirtualSensorDefinition = virtualSensorDefinition;

        mReferenceReadoutsAsString = refReadoutsAsString;

        mReferenceOutputsAsString = refOutputsAsString;

        // TreeMap sorts on Key (i.e. on Readouts)
        mRefReadoutVsRefOutputMapping = new TreeMap<>();

        // TreeMap sorts on Key (i.e. on Outputs)
        mRefOutputsVsRefReadoutsMapping = new TreeMap<>();

        if (refReadouts != null && outputs != null) {
            if (refReadouts.size() == outputs.size()) {
                mReferenceReadouts = new ArrayList<>(refReadouts);
                mReferenceOutputs = new ArrayList<>(outputs);

                for (int i = 0; i < mReferenceReadouts.size(); i++) {
                    double output = mReferenceOutputs.get(i);
                    double readout = mReferenceReadouts.get(i);

                    if (readout < mMinReadout) {
                        mMinReadout = readout;
                    }

                    if (readout > mMaxReadout) {
                        mMaxReadout = readout;
                    }

                    if (output < mMinOutput) {
                        mMinOutput = output;
                    }

                    if (output > mMaxOutput) {
                        mMaxOutput = output;
                    }

                    // Store the mapping Readouts -> Outputs (sorted on Readouts)
                    mRefReadoutVsRefOutputMapping.put(readout, output);

                    // Store the mapping Readouts -> Outputs (sorted on Outputs)
                    mRefOutputsVsRefReadoutsMapping.put(output, readout);
                }

            } else {
                throw new IllegalArgumentException("Readouts and outputs lists must have the same size");
            }
        } else {
            mReferenceReadouts = new ArrayList<>();
            mReferenceOutputs = new ArrayList<>();
        }
    }

    public List<Double> getReferenceReadouts() {
        return new ArrayList<>(mReferenceReadouts);
    }

    public VirtualSensorDefinition getVirtualSensorDefinition() {
        return mVirtualSensorDefinition;
    }

    public List<Double> getReferenceOutputs() {
        return new ArrayList<>(mReferenceOutputs);
    }

    public String toString() {
        return getName();
    }

    public String getName() {
        return mProfileName;
    }

    public String getReadoutAsString() {
        return mReferenceReadoutsAsString;
    }

    public String getOutputsAsString() {
        return mReferenceOutputsAsString;
    }

    public double getMinReadout() {
        return mMinReadout *(1- AXIS_MARGIN);
    }

    public double getMaxReadout() {
        return mMaxReadout *(1+ AXIS_MARGIN);
    }

    public double getMinOutput() {
        return mMinOutput *(1- AXIS_MARGIN);
    }

    public double getMaxOutput() {
        return mMaxOutput *(1+ AXIS_MARGIN);
    }

    public TreeMap<Double, Double> getRefReadoutVsOutputMapping() {
        return new TreeMap<>(mRefReadoutVsRefOutputMapping);
    }

    public TreeMap<Double, Double> getOutputVsReadoutMapping() {
        return new TreeMap<>(mRefOutputsVsRefReadoutsMapping);
    }

    public List<DataPoint> getDataPoints() {
        List<DataPoint> dataPoints = new ArrayList<>();
        for (Map.Entry<Double, Double> entry : mRefOutputsVsRefReadoutsMapping.entrySet()) {
            dataPoints.add(new DataPoint(entry.getKey(), entry.getValue()));
        }
        return dataPoints;
    }

    public static CalibrationProfile createCalibrationProfile(String profileName,
                                                              VirtualSensorDefinition virtualSensorDefinition,
                                                              String refReadouts,
                                                              String refOutputs) {

        List<Double> refReadoutsList = new ArrayList<Double>();;
        List<Double> refOutputsList = new ArrayList<Double>();

        String refReadoutsAsString = "";
        String refOutputsAsString = "";

        if (refReadouts != null && refOutputs != null) {
            // Parse EditText content to get the values of readouts and outputs
            refReadoutsAsString = refReadouts.trim();
            refOutputsAsString = refOutputs.trim();

            /**
             * Update the profile with the new lists if they are valid, that is, if the content
             * of both EditText is made only of decimal values separated by spaces and if there are
             * the same amount of values in both EditText since
             * they go in pairs (ref readout, ref output).
             */
            String regex = "((-?[0-9]+\\.[0-9]+)|(-?[0-9]+))(\\s((-?[0-9]+\\.[0-9]+)|(-?[0-9]+)))*";
            boolean listsUpdated = false;
            if (refReadoutsAsString.matches(regex) && refOutputsAsString.matches(regex)) {

                String[] refReadoutsArray = refReadoutsAsString.split("\\s+");
                String[] refOutputsArray = refOutputsAsString.split("\\s+");

                /**
                 * Readouts and concentration values goes in pairs: there must be the same
                 * amount of each.
                 */
                if (refReadoutsArray.length == refOutputsArray.length) {
                    try {
                        for (int i = 0; i < refReadoutsArray.length; i++) {
                            refReadoutsList.add(Double.valueOf(refReadoutsArray[i]));
                            refOutputsList.add(Double.valueOf(refOutputsArray[i]));
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Create the calibration profile
        CalibrationProfile profile = new CalibrationProfile(profileName, virtualSensorDefinition, refReadoutsList, refReadoutsAsString, refOutputsList, refOutputsAsString);

        return profile;
    }
}