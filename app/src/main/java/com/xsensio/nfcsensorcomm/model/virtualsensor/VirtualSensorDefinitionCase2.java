package com.xsensio.nfcsensorcomm.model.virtualsensor;

import com.xsensio.nfcsensorcomm.model.PlotMetadata;

public enum VirtualSensorDefinitionCase2 implements VirtualSensorDefinition {
    /*
    Note: when the label of an axis is a time, the unit label should be empty since it is automatically
    added later (this is needed because the user can define in the settings if the time is in
    seconds, milliseconds or microseconds).
     */
    SENSOR_1("Sensor 1",
            new PlotMetadata("Voltage VS concentration", "concentration", "[mol/l]", "Voltage", "[V]", false),
            new PlotMetadata("Voltage VS time", "time", "", "Voltage", "[V]", false),
            new PlotMetadata("dv/dt VS time", "time", "", "dv/dt", "nA", false),
            new PlotMetadata("concentration VS time", "time", "", "concentration", "[10^(-pH)](mol/l)", true)),
    SENSOR_2("Sensor 2",
            new PlotMetadata("Voltage VS concentration", "concentration", "[mol/l]", "Voltage", "[V]", false),
            new PlotMetadata("Voltage VS time", "time", "", "Voltage", "[V]", false),
            new PlotMetadata("dv/dt VS time", "time", "", "dv/dt", "nA", false),
            new PlotMetadata("concentration VS time", "time", "", "concentration", "[10^(-pH)](mol/l)", true)),
    SENSOR_3("Sensor 3",
            new PlotMetadata("Voltage vs concentration", "Voltage", "[V]", "concentration", "[mol/l]", false),
            new PlotMetadata("Voltage VS time", "time", "", "Voltage", "[V]", false),
            new PlotMetadata("Temperature VS time", "time", "", "Temperature", "C", false),
            new PlotMetadata("concentration VS time", "time", "", "concentration", "[10^(-pH)](mol/l)", true)),
    SENSOR_4("Sensor 4",
            new PlotMetadata("Voltage vs concentration", "concentration", "[mol/l]", "Voltage", "[V]", false),
            new PlotMetadata("Voltage VS time", "time", "", "Voltage", "[V]", false),
            new PlotMetadata("dv/dt VS time", "time", "", "dv/dt", "", false),
            new PlotMetadata("concentration VS time", "time", "", "concentration", "[10^(-pH)](mol/l)", true));

    /** Name of the Readout case */
    private static final String CASE_NAME = "Case 2";

    /** Name of the virtual sensor */
    private String mSensorName;

    private PlotMetadata mCalibrationPlotMetadata;

    private PlotMetadata mSamplesPlotMetadata;

    private PlotMetadata mDerivativesPlotMetadata;

    private PlotMetadata mMappedDataPlotMetadata;

    VirtualSensorDefinitionCase2(String sensorName,
                                 PlotMetadata calibrationPlotMetadata,
                                 PlotMetadata samplesPlotMetadata,
                                 PlotMetadata derivativesPlotMetadata,
                                 PlotMetadata mappedDataPlotMetadata) {
        mSensorName = sensorName;
        mCalibrationPlotMetadata = calibrationPlotMetadata;
        mSamplesPlotMetadata = samplesPlotMetadata;
        mDerivativesPlotMetadata = derivativesPlotMetadata;
        mMappedDataPlotMetadata = mappedDataPlotMetadata;
    }

    public String getSensorName() {
        return mSensorName;
    }

    public String getCaseName() {
        return CASE_NAME;
    }

    @Override
    public PlotMetadata getCalibrationPlotMetadata() {
        return mCalibrationPlotMetadata;
    }

    public PlotMetadata getSamplesPlotMetadata() {
        return mSamplesPlotMetadata;
    }

    public PlotMetadata getDerivativesPlotMetadata() {
        return mDerivativesPlotMetadata;
    }

    public PlotMetadata getMappedDataPlotMetadata() {
        return mMappedDataPlotMetadata;
    }

    public String toUserFriendlyString() {
        return mSensorName + ", " + CASE_NAME;
    }

    public String toString() {
        return super.toString() + "_CASE_2";
    }
}
