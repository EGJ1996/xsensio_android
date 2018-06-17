package com.xsensio.nfcsensorcomm.calibration;

import com.xsensio.nfcsensorcomm.BasePresenter;
import com.xsensio.nfcsensorcomm.BaseView;
import com.xsensio.nfcsensorcomm.model.CalibrationProfile;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorDefinition;

import java.util.List;

/**
 * Created by Michael Heiniger on 05.08.17.
 */

public interface CalibrationContract {

    interface View extends BaseView {
        void updateProfiles(List<CalibrationProfile> profiles, int position);

        int getSelectedProfilePosition();

        int getSensorId();

        int getReadoutCaseId();

        String getReferenceReadouts();

        String getReferenceOutputs();
    }

    interface Presenter extends BasePresenter {
        void loadProfiles();

        void updateSelectedProfile();

        void saveSelectedProfile();

        void createNewProfile(String name);

        void deleteSelectedProfile();

        VirtualSensorDefinition getSelectedVirtualSensorDefinition();

        List<String> getCalibrationProfileNames();
    }
}
