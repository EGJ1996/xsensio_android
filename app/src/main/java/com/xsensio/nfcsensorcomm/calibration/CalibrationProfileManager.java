package com.xsensio.nfcsensorcomm.calibration;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.Utils;
import com.xsensio.nfcsensorcomm.model.CalibrationProfile;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorDefinition;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorDefinitionCase1;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorDefinitionCase2;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorDefinitionCase3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class used to define helper functions to load/save/delete calibration profiles from the
 * file system
 */
public final class CalibrationProfileManager {

    private CalibrationProfileManager() {}

    private static final String TAG = "CalibrationProfileMgr";

    private static final String REF_READOUTS_LABEL = "ref_readouts";
    private static final String REF_OUTPUTS_LABEL = "ref_outputs";
    private static final String VIRTUAL_SENSOR_DEF_LABEL = "virtual_sensor_def";

    /**
     * Look for files in {@code profilesFolderPath} and load the calibration profiles found within
     * @param profilesFolderPath
     * @param targetVirtualSensorDefinition: returns only the profiles corresponding to the specified
     *                              virtual sensor, {@code null} returns ALL profiles
     * @return a List of calibration profiles
     * @throws IOException
     */
    public static List<CalibrationProfile> loadCalibrationProfilesFromFiles(String profilesFolderPath, VirtualSensorDefinition targetVirtualSensorDefinition) throws IOException {

        List<CalibrationProfile> calibrationProfiles = new ArrayList<CalibrationProfile>();

        if(Utils.isExternalStorageReadable()) {

            File calibrationProfilesFolder = new File(Environment.getExternalStorageDirectory(), profilesFolderPath);

            Log.d(TAG, "Profile folder:" + calibrationProfilesFolder.getAbsolutePath());

            // Get the list of all readable files in the folder at {@code profilesFolderPath}.
            File[] calibrationProfileFiles = calibrationProfilesFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    File file = new File(dir, name);
                    return file != null
                            && file.isFile()
                            && file.canRead();
                }
            });

            if (calibrationProfileFiles == null) {
                Log.i(TAG, "NO profile files to READ");
                return new ArrayList<CalibrationProfile>();
            }

            // Read all files and load all calibration profiles found
            for (File calibrationProfileFile : calibrationProfileFiles) {

                FileInputStream fis = new FileInputStream(calibrationProfileFile);
                try {
                    Log.i(TAG, "Read profile file: " + calibrationProfileFile.getAbsolutePath());

                    Toml toml = new Toml().read(fis);

                    // Read reference readouts
                    String refReadouts = toml.getString(REF_READOUTS_LABEL);

                    // Read virtual sensor definition
                    VirtualSensorDefinition virtualsensorDefinition = selectVirtualSensorDefinition(
                            toml.getString(VIRTUAL_SENSOR_DEF_LABEL, VirtualSensorDefinitionCase1.SENSOR_1.toString()));

                    // Read reference outputs
                    String refOutputs = toml.getString(REF_OUTPUTS_LABEL);

                    // Filter the profile on its virtual sensor definition
                    if (targetVirtualSensorDefinition == null || virtualsensorDefinition == targetVirtualSensorDefinition) {

                        CalibrationProfile profile = CalibrationProfile.createCalibrationProfile(
                                calibrationProfileFile.getName(),
                                virtualsensorDefinition,
                                refReadouts,
                                refOutputs);

                        calibrationProfiles.add(profile);
                    }

                } catch (Exception e) {
                    Log.d(TAG, "An error occured while reading " + calibrationProfileFile.getAbsolutePath() + ", the profile is not loaded (could be a formatting problem).");
                } finally {
                    if (fis != null) {
                        fis.close();
                    }
                }
            }

            return calibrationProfiles;
        } else {
            throw new IOException("External storage is not available");
        }
    }

    public static void saveCalibrationProfileInFile(Context context, CalibrationProfile profile) throws IOException {
        if(Utils.isExternalStorageWritable()) {

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            String profilesFolderPath = settings.getString("calibration_folder_path", context.getString(R.string.calibration_folder_path_def_val));

            File calibrationProfilesFolder = new File(Environment.getExternalStorageDirectory(), profilesFolderPath);

            Log.d(TAG, "Profile folder:" + calibrationProfilesFolder.getAbsolutePath());

            // Create folder if it does not exist
            if (!calibrationProfilesFolder.exists()) {
                // For some reason, the status is ALWAYS false...
                boolean status = calibrationProfilesFolder.mkdirs();
                Log.d(TAG, "Destination folder " + calibrationProfilesFolder + " was created ? " + status);
            } else {
                Log.d(TAG, "Destination folder " + calibrationProfilesFolder + " already exists");
            }

            File calibrationProfileFile = new File(calibrationProfilesFolder, profile.toString());

            Log.d(TAG, "Profile file path:" + calibrationProfileFile.getAbsolutePath());

            // Write profile
            TomlWriter tomlWriter = new TomlWriter();

            Map<String, Object> map = new HashMap<String, Object>();
            String refReadoutsAsString = profile.getReadoutAsString();
            map.put(REF_READOUTS_LABEL, refReadoutsAsString);

            map.put(VIRTUAL_SENSOR_DEF_LABEL, profile.getVirtualSensorDefinition());

            String refOutputsAsString = profile.getOutputsAsString();
            map.put(REF_OUTPUTS_LABEL, refOutputsAsString);

            // Write in its own file
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(calibrationProfileFile);
                tomlWriter.write(map, fos);
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }

        } else {
            throw new IOException("External storage is not writable");
        }
    }

    /**
     * Delete the file corresponding to the calibration profile provided.
     * @param profile - profile to delete
     * @return true if and only if the file has been removed
     */
    public static boolean deleteCalibrationProfileFile(Context context, CalibrationProfile profile) throws IOException {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String profilesFolderPath = settings.getString("calibration_folder_path", context.getString(R.string.calibration_folder_path_def_val));

        File calibrationProfilesFolder = new File(Environment.getExternalStorageDirectory(), profilesFolderPath);

        Log.d(TAG, "Profile folder:" + calibrationProfilesFolder.getAbsolutePath());

        File fileToDelete = new File(calibrationProfilesFolder, profile.getName());

        boolean deleteResult;
        if(Utils.isExternalStorageWritable()) {
            deleteResult = fileToDelete.delete();

        } else {
            throw new IOException("External storage is not writable");
        }

        return deleteResult;
    }

    private static VirtualSensorDefinition selectVirtualSensorDefinition(String name) {
        if (name.equals("SENSOR_1_CASE_1")) {
            return VirtualSensorDefinitionCase1.SENSOR_1;
        } else if (name.equals("SENSOR_1_CASE_2")) {
            return VirtualSensorDefinitionCase2.SENSOR_1;
        } else if (name.equals("SENSOR_1_CASE_3")) {
            return VirtualSensorDefinitionCase3.SENSOR_1;
        } else if (name.equals("SENSOR_2_CASE_1")) {
            return VirtualSensorDefinitionCase1.SENSOR_2;
        } else if (name.equals("SENSOR_2_CASE_2")) {
            return VirtualSensorDefinitionCase2.SENSOR_2;
        } else if (name.equals("SENSOR_2_CASE_3")) {
            return VirtualSensorDefinitionCase3.SENSOR_2;
        } else if (name.equals("SENSOR_3_CASE_1")) {
            return VirtualSensorDefinitionCase1.SENSOR_3;
        } else if (name.equals("SENSOR_3_CASE_2")) {
            return VirtualSensorDefinitionCase2.SENSOR_3;
        } else if (name.equals("SENSOR_3_CASE_3")) {
            return VirtualSensorDefinitionCase3.SENSOR_3;
        } else if (name.equals("SENSOR_4_CASE_1")) {
            return VirtualSensorDefinitionCase1.SENSOR_4;
        } else if (name.equals("SENSOR_4_CASE_2")) {
            return VirtualSensorDefinitionCase2.SENSOR_4;
        } else { // "SENSOR_4_CASE_3"
            return VirtualSensorDefinitionCase3.SENSOR_4;
        }
    }
}