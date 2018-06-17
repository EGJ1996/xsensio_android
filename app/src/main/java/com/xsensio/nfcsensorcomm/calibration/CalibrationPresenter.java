package com.xsensio.nfcsensorcomm.calibration;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.model.CalibrationProfile;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorDefinition;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorDefinitionCase1;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorDefinitionCase2;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorDefinitionCase3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CalibrationPresenter implements CalibrationContract.Presenter {

    private static final String TAG = "CalibrationPresenter";

    /*
     * Request codes used when check the permissions to use the file system for calibration
     * profile read/write operations
     */
    private static final int LOAD_PROFILES_REQUEST_CODE = 1;
    private static final int SAVE_PROFILE_REQUEST_CODE = 2;
    private static final int DELETE_PROFILE_REQUEST_CODE = 3;

    private Activity mActivity;
    private CalibrationContract.View mView;

    private List<CalibrationProfile> mProfiles;

    public CalibrationPresenter(Activity activity, CalibrationContract.View view) {
        mActivity = activity;
        mView = view;

        mProfiles = new ArrayList<>();
    }

    @Override
    public void showToast(String message) {
        mView.showToast(message);
    }


    @Override
    public void loadProfiles() {
        if (mActivity.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"Permission is granted");
            try {

                // Load from the Settings the path of the folder containing the calibration profiles
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
                String profilesFolderPath = settings.getString("calibration_folder_path", mActivity.getString(R.string.calibration_folder_path_def_val));

                // Clear the list of currently loaded profiles
                mProfiles.clear();

                // Load ALL profiles
                mProfiles = CalibrationProfileManager.loadCalibrationProfilesFromFiles(profilesFolderPath, null);

                if (mProfiles == null || mProfiles.size() == 0) {
                    showToast("There is no profile file to read.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                showToast("Error while reading profile files.");
            }
        } else {
            Log.d(TAG,"Permission is revoked");
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, LOAD_PROFILES_REQUEST_CODE);
        }

        // Update calibration profile spinner with the newly loaded profiles
        mView.updateProfiles(new ArrayList<CalibrationProfile>(mProfiles), 0);
    }

    @Override
    public void updateSelectedProfile() {
        int position = mView.getSelectedProfilePosition();

        if (position < 0 || position >= mProfiles.size()) {
            showToast("No profile to update, create one first.");
            return;
        }

        CalibrationProfile profileToUpdate = mProfiles.get(position);

        CalibrationProfile updatedProfile = createCalibrationProfile(
                profileToUpdate.getName(),
                mView.getReferenceReadouts(),
                mView.getReferenceOutputs()
        );

        // Update profile to save in the list of profiles
        mProfiles.set(position, updatedProfile);

        // Update list of profiles in the View
        mView.updateProfiles(mProfiles, position);
    }

    @Override
    public void saveSelectedProfile() {

        int position = mView.getSelectedProfilePosition();

        if (position < 0 || position >= mProfiles.size()) {
            showToast("No profile to save, create one first.");
            return;
        }

        updateSelectedProfile();

        CalibrationProfile profileToSave = mProfiles.get(position);

        if (mActivity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"Permission is granted");
            try {
                // Save the calibration profile on the file system
                CalibrationProfileManager.saveCalibrationProfileInFile(mActivity, profileToSave);
            } catch (IOException e) {
                e.printStackTrace();
                showToast("Error while saving the profile.");
            }
            showToast("Profile " + profileToSave.toString() + " saved.");
        } else {
            Log.d(TAG,"Permission is revoked");
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, SAVE_PROFILE_REQUEST_CODE);
        }


    }

    @Override
    public void createNewProfile(String name) {

        CalibrationProfile profile = createCalibrationProfile(
                name,
                null,
                null
        );

        mProfiles.add(profile);

        mView.updateProfiles(mProfiles, mProfiles.size()-1);
    }

    @Override
    public void deleteSelectedProfile() {

        final int position = mView.getSelectedProfilePosition();

        if (mActivity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"Permission is granted");

            final CalibrationProfile profileToDelete = mProfiles.get(position);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mActivity);
            dialogBuilder.setTitle("Do you really want to delete the profile " + profileToDelete.getName() + "?");
            dialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                try {
                    // NOTE: result returned is always "false" even if the profile file was deleted successfully
                    boolean result = CalibrationProfileManager.deleteCalibrationProfileFile(mActivity, profileToDelete);

                    mProfiles.remove(position);

                    mView.updateProfiles(mProfiles, position-1);

                    showToast(mActivity.getString(R.string.calibration_profile_successfully_removed));
                } catch (IOException e) {
                    e.printStackTrace();
                    showToast(mActivity.getString(R.string.calibration_profile_not_removed));
                }
                }
            });
            dialogBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                // Nothing to do
                }
            });

            dialogBuilder.create().show();
        } else {
            Log.d(TAG,"Permission is revoked");
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, DELETE_PROFILE_REQUEST_CODE);
        }
    }

    @Override
    public List<String> getCalibrationProfileNames() {
        List<String> profileNames = new ArrayList<>();
        for (CalibrationProfile profile : mProfiles) {
            profileNames.add(profile.getName());
        }
        return profileNames;
    }

    /**
     * Create calibration profile object (Class {@link CalibrationProfile})
     * from provided data
     */
    private CalibrationProfile createCalibrationProfile(String profileName, String refReadouts, String refOutputs) {

        VirtualSensorDefinition virtualSensorDefinition = getSelectedVirtualSensorDefinition();

        // Create the calibration profile
        CalibrationProfile profile = CalibrationProfile.createCalibrationProfile(profileName, virtualSensorDefinition, refReadouts, refOutputs);

        return profile;
    }

    @Override
    public VirtualSensorDefinition getSelectedVirtualSensorDefinition() {

        int sensorId = mView.getSensorId();
        int readoutCaseId = mView.getReadoutCaseId();

        if (sensorId == 1) {
            if (readoutCaseId == 1) {
                return VirtualSensorDefinitionCase1.SENSOR_1;
            } else if (readoutCaseId == 2) {
                return VirtualSensorDefinitionCase2.SENSOR_1;
            } else { // readoutCaseId == 3
                return VirtualSensorDefinitionCase3.SENSOR_1;
            }
        } else if (sensorId == 2) {
            if (readoutCaseId == 1) {
                return VirtualSensorDefinitionCase1.SENSOR_2;
            } else if (readoutCaseId == 2) {
                return VirtualSensorDefinitionCase2.SENSOR_2;
            } else { // readoutCaseId == 3
                return VirtualSensorDefinitionCase3.SENSOR_2;
            }
        } else if (sensorId == 3) {
            if (readoutCaseId == 1) {
                return VirtualSensorDefinitionCase1.SENSOR_3;
            } else if (readoutCaseId == 2) {
                return VirtualSensorDefinitionCase2.SENSOR_3;
            } else { // readoutCaseId == 3
                return VirtualSensorDefinitionCase3.SENSOR_3;
            }
        } else { // sensorId == 4
            if (readoutCaseId == 1) {
                return VirtualSensorDefinitionCase1.SENSOR_4;
            } else if (readoutCaseId == 2) {
                return VirtualSensorDefinitionCase2.SENSOR_4;
            } else { // readoutCaseId == 3
                return VirtualSensorDefinitionCase3.SENSOR_4;
            }
        }
    }
}