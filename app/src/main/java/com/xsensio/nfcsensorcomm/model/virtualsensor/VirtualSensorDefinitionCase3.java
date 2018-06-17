package com.xsensio.nfcsensorcomm.model.virtualsensor;

import com.xsensio.nfcsensorcomm.model.PlotMetadata;

public enum VirtualSensorDefinitionCase3 implements VirtualSensorDefinition {
    SENSOR_1("Sensor 1",
            new PlotMetadata("# pulses vs concentration", "concentration", "[mol/l]", "# pulses", "[-]", false),
            new PlotMetadata("concentration VS time", "time", "", "concentration", "[10^(-pH)](mol/l)", true)),
    SENSOR_2("Sensor 2",
            new PlotMetadata("# pulses vs concentration", "concentration", "[mol/l]", "# pulses", "[-]", false),
            new PlotMetadata("concentration VS time", "time", "", "concentration", "[10^(-pH)](mol/l)", true)),
    SENSOR_3("Sensor 3",
            new PlotMetadata("# pulses vs concentration", "concentration", "[mol/l]", "# pulses", "[-]", false),
            new PlotMetadata("concentration VS time", "time", "", "concentration", "[10^(-pH)](mol/l)", true)),
    SENSOR_4("Sensor 4",
            new PlotMetadata("# pulses vs concentration", "concentration", "[mol/l]", "# pulses", "[-]", false),
            new PlotMetadata("concentration VS time", "time", "", "concentration", "[10^(-pH)](mol/l)", true));

    /** Name of the Readout case */
    private static final String CASE_NAME = "Case 3";

    /** Name of the virtual sensor */
    private String mSensorName;

    private PlotMetadata mCalibrationPlotMetadata;

    private PlotMetadata mMappedDataPlotMetadata;

    VirtualSensorDefinitionCase3(String sensorName, PlotMetadata calibrationPlotMetadata, PlotMetadata mappedDataPlotMetadata) {
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
        return super.toString() + "_CASE_3";
    }
}
