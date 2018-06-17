package com.xsensio.nfcsensorcomm.sensorresult.case3;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
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
import android.widget.Spinner;
import android.widget.Toast;

import com.androidplot.Plot;
import com.androidplot.ui.Anchor;
import com.androidplot.ui.HorizontalPositioning;
import com.androidplot.ui.PositionMetrics;
import com.androidplot.ui.VerticalPositioning;
import com.androidplot.ui.widget.TextLabelWidget;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.ScalingXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.XYPlotWrapper;
import com.xsensio.nfcsensorcomm.calibration.CalibrationActivity;
import com.xsensio.nfcsensorcomm.model.CalibrationProfile;
import com.xsensio.nfcsensorcomm.model.DataPoint;
import com.xsensio.nfcsensorcomm.model.DataPointSeries;
import com.xsensio.nfcsensorcomm.model.GraphData;
import com.xsensio.nfcsensorcomm.model.PlotMetadata;
import com.xsensio.nfcsensorcomm.model.TimescaleEnum;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorCase3;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorDefinitionCase3;
import com.xsensio.nfcsensorcomm.settings.SettingsActivity;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.List;

public class VirtualSensorResultCase3Activity extends AppCompatActivity implements VirtualSensorResultCase3Contract.View {

    private static final String TAG = "VSResultCase3Activity";

    public static final String EXTRA_VIRTUAL_SENSOR = "com.xsensio.nfcsensorcomm.mainactivity.sensorcomm.extra.VIRTUAL_SENSOR";

    private Toolbar mToolbar;

    private VirtualSensorResultCase3Contract.Presenter mPresenter;

    private ArrayAdapter mCalibrationProfileAdapter;
    private Spinner mCalibrationProfilesSpinner;

    private XYPlot mMappedDataVsTimePlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_virtual_sensor_result_case3);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        // Build Presenter of MainActivity (which is the corresponding View)
        mPresenter = new VirtualSensorResultCase3Presenter(this);
        mPresenter.handleIntent(getIntent());

        // Setup spinner
        mCalibrationProfilesSpinner = (Spinner) findViewById(R.id.sensor_result_calibration_profiles_sp);
        mCalibrationProfilesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateGui();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        refreshCalibrationProfileSpinner();

        mMappedDataVsTimePlot = (XYPlot) findViewById(R.id.mapped_data_vs_time_plot);

        updateGui();
    }

    public void updateGui() {

        VirtualSensorCase3 virtualSensor = mPresenter.getVirtualSensor();

        CalibrationProfile profile = getSelectedCalibrationProfile();

        VirtualSensorCase3.DataContainer dataContainer = virtualSensor.getDataContainer(getApplicationContext(), profile);

        final VirtualSensorDefinitionCase3 virtualSensorDefinition = (VirtualSensorDefinitionCase3) virtualSensor.getVirtualSensorDefinition();

        // Read time scale in settings
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        TimescaleEnum timescale = TimescaleEnum.valueOf(settings.getString("graph_time_scale", getString(R.string.graph_time_scale_def_val)));

        // Render plots
        renderMappedDataVsTimePlot(virtualSensorDefinition, dataContainer, timescale);
    }

    private void refreshCalibrationProfileSpinner() {
        // Load calibration profiles
        List<CalibrationProfile> profiles = mPresenter.refreshCalibrationProfiles();

        mCalibrationProfileAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, profiles.toArray());
        mCalibrationProfileAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCalibrationProfilesSpinner.setAdapter(mCalibrationProfileAdapter);
    }

    private void renderMappedDataVsTimePlot(VirtualSensorDefinitionCase3 virtualSensorDefinition,
                                            VirtualSensorCase3.DataContainer dataContainer,
                                            TimescaleEnum timescale) {

        Log.i(TAG, "Render graph for " + virtualSensorDefinition.toUserFriendlyString());

        final PlotMetadata plotMetadata = virtualSensorDefinition.getMappedDataPlotMetadata();
        plotMetadata.setXAxisUnitLabel(dataContainer.getTimescale().getAbbreviation());

        List<DataPoint> datapoints = dataContainer.getMappedDataVsTime();

        new XYPlotWrapper(mMappedDataVsTimePlot, plotMetadata, datapoints);
    }

    private CalibrationProfile getSelectedCalibrationProfile() {
        return (CalibrationProfile) mCalibrationProfilesSpinner.getSelectedItem();
    }

    @Override
    public void setPresenter(Object presenter) {
        mPresenter = (VirtualSensorResultCase3Contract.Presenter) presenter;
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sensor_result, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_update_profile:
                refreshCalibrationProfileSpinner();
                updateGui();
                return true;
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_calibration:
                intent = new Intent(this, CalibrationActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}