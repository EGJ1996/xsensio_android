package com.xsensio.nfcsensorcomm.sensorresult.case2;

import android.content.Intent;

import com.xsensio.nfcsensorcomm.BasePresenter;
import com.xsensio.nfcsensorcomm.BaseView;
import com.xsensio.nfcsensorcomm.model.CalibrationProfile;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensor;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorCase2;

import java.util.List;

/**
 * Created by Michael Heiniger on 19.07.17.
 */

public interface VirtualSensorResultCase2Contract {

    interface View extends BaseView {
    }

    interface Presenter extends BasePresenter {
        void handleIntent(Intent intent);

        List<CalibrationProfile> refreshCalibrationProfiles();

        VirtualSensorCase2 getVirtualSensor();
    }
}
