package com.xsensio.nfcsensorcomm.model.virtualsensor;

import com.xsensio.nfcsensorcomm.model.PlotMetadata;

public enum VirtualSensorDefinitionCase1 implements VirtualSensorDefinition {
    SENSOR_1("Sensor 1",
            new PlotMetadata("Voltage VS pH", "pH", "[-]", "Voltage", "[V]", false),
            new PlotMetadata("pH VS time", "time", "", "pH", "[-]", false)),
    SENSOR_2("Sensor 2",
            new PlotMetadata("Voltage VS pH", "pH", "[-]", "Voltage", "[V]", false),
            new PlotMetadata("pH VS time", "time", "", "pH", "[-]", false)),
    SENSOR_3("Sensor 3",
            new PlotMetadata("Voltage VS pH", "pH", "[-]", "Voltage", "[V]", false),
            new PlotMetadata("pH VS time", "time", "", "pH", "[-]", false)),
    SENSOR_4("Sensor 4",
            new PlotMetadata("Voltage VS pH", "pH", "[-]", "Voltage", "[V]", false),
            new PlotMetadata("pH VS time", "time", "", "pH", "[-]", false));

    /** Name of the Readout case */
    private static final String CASE_NAME = "Case 1";

    /** Name of the virtual sensor */
    private String mSensorName;

    private PlotMetadata mCalibrationPlotMetadata;

    private PlotMetadata mMappedDataPlotMetadata;

    VirtualSensorDefinitionCase1(String sensorName, PlotMetadata calibrationPlotMetadata, PlotMetadata mappedDataPlotMetadata) {
        mSensorName = sensorName;
        mCalibrationPlotMetadata = calibrationPlotMetadata;
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

    public PlotMetadata getMappedDataPlotMetadata() {
        return mMappedDataPlotMetadata;
    }

    public String toUserFriendlyString() {
        return mSensorName + ", " + CASE_NAME;
    }

    public String toString() {
        return super.toString() + "_CASE_1";
    }
}
