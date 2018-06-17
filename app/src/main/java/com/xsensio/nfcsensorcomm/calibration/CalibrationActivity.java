package com.xsensio.nfcsensorcomm.calibration;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.ui.Anchor;
import com.androidplot.ui.HorizontalPositioning;
import com.androidplot.ui.PositionMetrics;
import com.androidplot.ui.VerticalPositioning;
import com.androidplot.ui.widget.TextLabelWidget;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.ScalingXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.XYPlotWrapper;
import com.xsensio.nfcsensorcomm.files.FileManagerActivity;
import com.xsensio.nfcsensorcomm.model.CalibrationProfile;
import com.xsensio.nfcsensorcomm.model.DataPoint;
import com.xsensio.nfcsensorcomm.model.DataPointSeries;
import com.xsensio.nfcsensorcomm.model.PlotMetadata;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorDefinition;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorDefinitionCase1;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorDefinitionCase2;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorDefinitionCase3;
import com.xsensio.nfcsensorcomm.settings.SettingsActivity;

import java.util.List;

/**
 * Activity used to create, edit and delete sensor calibration profiles.
 */
public class CalibrationActivity extends AppCompatActivity implements CalibrationContract.View, NewCalibrationProfileDialogFragment.NewCalibrationProfileDialogListener {

    private static final String TAG = "CalibrationActivity";

    /*
     * Request codes used when check the permissions to use the file system for calibration
     * profile read/write operations
     */
    private static final int LOAD_PROFILES_REQUEST_CODE = 1;
    private static final int SAVE_PROFILE_REQUEST_CODE = 2;
    private static final int DELETE_PROFILE_REQUEST_CODE = 3;

    private Toolbar mToolbar;

    private CalibrationPresenter mPresenter;

    /** Spinner used by the user to select a calibration profile */
    private Spinner mCalibrationProfilesSpinner;

    // RadioButtons used by the user to assign the selected calibration profile to a sensor
    private RadioButton mSensor1RadioButton;
    private RadioButton mSensor2RadioButton;
    private RadioButton mSensor3RadioButton;
    private RadioButton mSensor4RadioButton;

    // RadioButtons used by the user to assign the selected calibration profile to a case
    private RadioButton mRoC1RadioButton;
    private RadioButton mRoC2RadioButton;
    private RadioButton mRoC3RadioButton;

    /** EditText used by the user to enter reference readouts. Each reference readout goes in pair
     * with a reference output in {@code mRefOutputsEdt}*/
    private EditText mRefReadoutEdt;

    /** Texview describing the reference outputs EditText */
    private TextView mRefOutputTextView;

    /** EditText used by the user to enter reference outputs. Each reference output goes in pair
     * with a reference readout in {@code mRefReadoutEdt} */
    private EditText mRefOutputsEdt;

    /** Object modeling the calibration graph {@link #renderCalibrationGraph} */
    private XYPlot mPlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        // Add toolbar to the activity
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        // Calibration profiles Spinner (drop-down list)
        mCalibrationProfilesSpinner = (Spinner) findViewById(R.id.calibration_profiles_sp);
        mCalibrationProfilesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateGuiForSelectedProfile();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing to do
            }
        });

        // Sensors RadioButtons
        mSensor1RadioButton = (RadioButton) findViewById(R.id.sensor1_radio);
        mSensor1RadioButton.setChecked(true);
        mSensor1RadioButton.setOnClickListener(new RadioButtonOnClickListener());
        mSensor2RadioButton = (RadioButton) findViewById(R.id.sensor2_radio);
        mSensor2RadioButton.setOnClickListener(new RadioButtonOnClickListener());
        mSensor3RadioButton = (RadioButton) findViewById(R.id.sensor3_radio);
        mSensor3RadioButton.setOnClickListener(new RadioButtonOnClickListener());
        mSensor4RadioButton = (RadioButton) findViewById(R.id.sensor4_radio);
        mSensor4RadioButton.setOnClickListener(new RadioButtonOnClickListener());

        // Readout Cases RadioButton
        mRoC1RadioButton = (RadioButton) findViewById(R.id.calibration_roc1_radio);
        mRoC1RadioButton.setChecked(true);
        mRoC1RadioButton.setOnClickListener(new RadioButtonOnClickListener());
        mRoC2RadioButton = (RadioButton) findViewById(R.id.calibration_roc2_radio);
        mRoC2RadioButton.setOnClickListener(new RadioButtonOnClickListener());
        mRoC3RadioButton = (RadioButton) findViewById(R.id.calibration_roc3_radio);
        mRoC3RadioButton.setOnClickListener(new RadioButtonOnClickListener());

        // Reference outputs EditText
        mRefOutputsEdt = (EditText) findViewById(R.id.calibration_ref_outputs_edt);

        // Reference outputs description TextView
        mRefOutputTextView = (TextView) findViewById(R.id.calibration_ref_output_tv);

        // Reference readouts EditText
        mRefReadoutEdt = (EditText) findViewById(R.id.calibration_readout_edt);

        // Plot of the selected calibration profile
        mPlot = (XYPlot) findViewById(R.id.calibration_graph);

        mPresenter = new CalibrationPresenter(this, this);

        // Load ALL calibration profiles
        mPresenter.loadProfiles();

        updateGuiForSelectedProfile();
    }

    /**
     * Update user-interface for selected calibration profile
     */
    private void updateGuiForSelectedProfile() {

        CalibrationProfile selectedProfile = getSelectedCalibrationProfile();

        if (selectedProfile != null) {

            VirtualSensorDefinition virtualSensorDefinition = selectedProfile.getVirtualSensorDefinition();

            // Update radiobuttons
            updateRadioButtonsFromVirtualSensor(virtualSensorDefinition);

            // Update Reference output TextView (Concentration, pH, ...)
            PlotMetadata calibrationPlotMetadata = virtualSensorDefinition.getCalibrationPlotMetadata();
            mRefOutputTextView.setText("Ref. " + calibrationPlotMetadata.getXAxisLabel());

            // Update edittexts
            mRefReadoutEdt.setText(selectedProfile.getReadoutAsString());
            mRefOutputsEdt.setText(selectedProfile.getOutputsAsString());

            // Update graph
            renderCalibrationGraph();
        }
    }

    /**
     * Plot the graph of the reference points (ref readout, ref output)
     */
    private void renderCalibrationGraph() {

        CalibrationProfile profile = getSelectedCalibrationProfile();

        Log.i(TAG, "Render graph");

        if (profile == null || profile.getDataPoints().size() == 0) {
            return;
        }

        // Get the virtual sensor definition of the calibration profile
        VirtualSensorDefinition virtualSensorDefinition = profile.getVirtualSensorDefinition();

        PlotMetadata plotMetadata = virtualSensorDefinition.getCalibrationPlotMetadata();

        // Load data points representing the graph to plot: (ref readout, ref output)
        List<DataPoint> dataPoints = profile.getDataPoints();

        new XYPlotWrapper(mPlot, plotMetadata, dataPoints);
    }

    /**
     * Update the state of the RadioButtons according to the provided virtual sensor definition
     * @param virtualSensorDefinition
     */
    private void updateRadioButtonsFromVirtualSensor(VirtualSensorDefinition virtualSensorDefinition) {

        if (virtualSensorDefinition == VirtualSensorDefinitionCase1.SENSOR_1) {
            mSensor1RadioButton.setChecked(true);
            mRoC1RadioButton.setChecked(true);
        } else if (virtualSensorDefinition == VirtualSensorDefinitionCase2.SENSOR_1) {
            mSensor1RadioButton.setChecked(true);
            mRoC2RadioButton.setChecked(true);
        } else if (virtualSensorDefinition == VirtualSensorDefinitionCase3.SENSOR_1) {
            mSensor1RadioButton.setChecked(true);
            mRoC3RadioButton.setChecked(true);
        } else if (virtualSensorDefinition == VirtualSensorDefinitionCase1.SENSOR_2) {
            mSensor2RadioButton.setChecked(true);
            mRoC1RadioButton.setChecked(true);
        } else if (virtualSensorDefinition == VirtualSensorDefinitionCase2.SENSOR_2) {
            mSensor2RadioButton.setChecked(true);
            mRoC2RadioButton.setChecked(true);
        } else if (virtualSensorDefinition == VirtualSensorDefinitionCase3.SENSOR_2) {
            mSensor2RadioButton.setChecked(true);
            mRoC3RadioButton.setChecked(true);
        } else if (virtualSensorDefinition == VirtualSensorDefinitionCase1.SENSOR_3) {
            mSensor3RadioButton.setChecked(true);
            mRoC1RadioButton.setChecked(true);
        } else if (virtualSensorDefinition == VirtualSensorDefinitionCase2.SENSOR_3) {
            mSensor3RadioButton.setChecked(true);
            mRoC2RadioButton.setChecked(true);
        } else if (virtualSensorDefinition == VirtualSensorDefinitionCase3.SENSOR_3) {
            mSensor3RadioButton.setChecked(true);
            mRoC3RadioButton.setChecked(true);
        } else if (virtualSensorDefinition == VirtualSensorDefinitionCase1.SENSOR_4) {
            mSensor4RadioButton.setChecked(true);
            mRoC1RadioButton.setChecked(true);
        } else if (virtualSensorDefinition == VirtualSensorDefinitionCase2.SENSOR_4) {
            mSensor4RadioButton.setChecked(true);
            mRoC2RadioButton.setChecked(true);
        } else { // SENSOR_4_CASE_3
            mSensor4RadioButton.setChecked(true);
            mRoC3RadioButton.setChecked(true);
        }
    }

    /**
     * Get the calibration profile selected in the Spinner
     * @return the selected calibration profile
     */
    private CalibrationProfile getSelectedCalibrationProfile() {
        return (CalibrationProfile) mCalibrationProfilesSpinner.getSelectedItem();
    }

    /**
     * Listener used to define operations done on RadioButtons click
     */
    private class RadioButtonOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {

            // Update graph
            renderCalibrationGraph();
        }
    }

    @Override
    public void updateProfiles(List<CalibrationProfile> profiles, int position) {
        ArrayAdapter calibrationProfileAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, profiles.toArray());
        calibrationProfileAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCalibrationProfilesSpinner.setAdapter(calibrationProfileAdapter);

        mCalibrationProfilesSpinner.setSelection(position);
    }

    @Override
    public int getSelectedProfilePosition() {
        return mCalibrationProfilesSpinner.getSelectedItemPosition();
    }

    @Override
    public int getSensorId() {
        if (mSensor1RadioButton.isChecked()) {
            return 1;
        } else if (mSensor2RadioButton.isChecked()) {
            return 2;
        } else if (mSensor3RadioButton.isChecked()) {
            return 3;
        } else { // Sensor 4 is selected
            return 4;
        }
    }

    @Override
    public int getReadoutCaseId() {
        if (mRoC1RadioButton.isChecked()) {
            return 1;
        } else if (mRoC2RadioButton.isChecked()) {
            return 2;
        } else { // Readout case 3 is selected
            return 3;
        }
    }

    @Override
    public String getReferenceReadouts() {
        return mRefReadoutEdt.getText().toString();
    }

    @Override
    public String getReferenceOutputs() {
        return mRefOutputsEdt.getText().toString();
    }

    /**
     * Callback executed when a new calibration profile is created
     * @param profileName
     */
    @Override
    public void onDialogPositiveClick(String profileName) {
        mPresenter.createNewProfile(profileName);
    }

    /**
     * Callback executed following the file system permission check: this is needed since
     * calibration profiles are stored in files. One first needs to check if the app has
     * permission to read/write files on the phone. The answer from Android is available in this
     * callback.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch(requestCode) {
                case LOAD_PROFILES_REQUEST_CODE:
                    // Load ALL profiles
                    mPresenter.loadProfiles();
                    return;
                case SAVE_PROFILE_REQUEST_CODE:
                    mPresenter.saveSelectedProfile();
                    return;
                case DELETE_PROFILE_REQUEST_CODE:
                    mPresenter.deleteSelectedProfile();
                    return;
                default:
                    // Nothing
                    return;
            }
        }
    }

    @Override
    public void setPresenter(@NonNull Object presenter) {
        // Not needed
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    ////////////////////// TOOLBAR MENU //////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_calibration, menu);
        return true;
    }

    /**
     * Define operations of toolbar menu
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;

        switch (item.getItemId()) {
            case R.id.action_update_profile:
                mPresenter.updateSelectedProfile();
                return true;
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);

                return true;
            case R.id.action_add_profile:
                FragmentManager fm = getFragmentManager();

                List<String> profileNames = mPresenter.getCalibrationProfileNames();
                NewCalibrationProfileDialogFragment newFragment = NewCalibrationProfileDialogFragment.newInstance("Some Title", profileNames);
                newFragment.show(fm, "fragment_new_profile");

                return true;
            case R.id.action_save_profile:
                mPresenter.saveSelectedProfile();
                return true;
            case R.id.action_delete_profile:
                mPresenter.deleteSelectedProfile();
                return true;
            case R.id.action_files:
                intent = new Intent(this, FileManagerActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
