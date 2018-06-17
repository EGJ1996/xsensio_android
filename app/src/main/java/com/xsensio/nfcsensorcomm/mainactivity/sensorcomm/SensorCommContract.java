package com.xsensio.nfcsensorcomm.mainactivity.sensorcomm;

import com.xsensio.nfcsensorcomm.mainactivity.CommContract;
import com.xsensio.nfcsensorcomm.model.PhoneMcuCommand;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensor;

import java.util.List;

/**
 * Created by Michael Heiniger on 13.07.17.
 */

public interface SensorCommContract {

    interface View extends CommContract.View {

        void updateSensorResult(List<VirtualSensor> sensorResults);

        /**
         * Presenter notify the View that the Extended mode has been updated
         * @param isChecked
         */
        void updateExtendedMode(boolean isChecked);

        void updateSensorResultStatus(String operationStatus);

        void updateReadSensorProgress(String taskDescription, int completionRatio);

        void setReadSensorsButtonEnabled(boolean enabled);
    }

    interface Presenter extends CommContract.Presenter {
        void readSensors(PhoneMcuCommand command);

        /**
         * View asks the Presenter to change the state of the Extended mode on the NFC Tag
         * @param isChecked
         */
        void setExtendedMode(boolean isChecked);

    }
}