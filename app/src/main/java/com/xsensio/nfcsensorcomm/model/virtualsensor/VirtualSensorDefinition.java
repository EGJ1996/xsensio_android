package com.xsensio.nfcsensorcomm.model.virtualsensor;

import com.xsensio.nfcsensorcomm.model.PlotMetadata;

/**
 * Created by Michael Heiniger on 13.09.17.
 */

public interface VirtualSensorDefinition {

    String getSensorName();

    String getCaseName();

    PlotMetadata getCalibrationPlotMetadata();

    String toUserFriendlyString();

    String toString();
}
