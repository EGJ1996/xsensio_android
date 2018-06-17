package com.xsensio.nfcsensorcomm.sensorresult.case1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.calibration.CalibrationProfileManager;
import com.xsensio.nfcsensorcomm.model.CalibrationProfile;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensor;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorCase1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VirtualSensorResultCase1Presenter implements VirtualSensorResultCase1Contract.Presenter {

    private VirtualSensorResultCase1Contract.View mView;
    private Activity mActivity;

    public VirtualSensorResultCase1Presenter(VirtualSensorResultCase1Contract.View view) {
        mView = view;
        mActivity = (Activity) view;
    }

    private VirtualSensorCase1 mVirtualSensor;

    @Override
    public void showToast(String message) {
        mView.showToast(message);
    }

    @Override
    public void handleIntent(Intent intent) {
        mVirtualSensor = intent.getParcelableExtra(VirtualSensorResultCase1Activity.EXTRA_VIRTUAL_SENSOR);
    }

    @Override
    public List<CalibrationProfile> refreshCalibrationProfiles() {

        List<CalibrationProfile> profiles = new ArrayList<>();

        try {

            // Load from the Settings the path of the folder containing the calibration profiles
            Context context = mActivity.getApplicationContext();
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            String profilesFolderPath = settings.getString("calibration_folder_path", context.getString(R.string.calibration_folder_path_def_val));

            // Load all available calibration profiles
            profiles = CalibrationProfileManager.loadCalibrationProfilesFromFiles(profilesFolderPath, mVirtualSensor.getVirtualSensorDefinition());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return profiles;
    }

    @Override
    public VirtualSensorCase1 getVirtualSensor() {
        return mVirtualSensor;
    }
}
