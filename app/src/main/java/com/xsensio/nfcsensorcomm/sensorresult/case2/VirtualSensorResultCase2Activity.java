package com.xsensio.nfcsensorcomm.sensorresult.case2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.xy.XYPlot;
import com.google.gson.Gson;
import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.XYPlotWrapper;
import com.xsensio.nfcsensorcomm.calibration.CalibrationActivity;
import com.xsensio.nfcsensorcomm.model.CalibrationProfile;
import com.xsensio.nfcsensorcomm.model.DataPoint;
import com.xsensio.nfcsensorcomm.model.DataUtils;
import com.xsensio.nfcsensorcomm.model.PlotMetadata;
import com.xsensio.nfcsensorcomm.model.SignalProcessor;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorCase2;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorDefinitionCase2;
import com.xsensio.nfcsensorcomm.settings.SettingsActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class VirtualSensorResultCase2Activity extends AppCompatActivity implements VirtualSensorResultCase2Contract.View {

    private static final String TAG = "VSResultCase2Activity";

    public static final String EXTRA_VIRTUAL_SENSOR = "com.xsensio.nfcsensorcomm.mainactivity.sensorcomm.extra.VIRTUAL_SENSOR";

    private Toolbar mToolbar;

    private VirtualSensorResultCase2Contract.Presenter mPresenter;

    private TextView mAverageDerivativeTv;

    private TextView mAverageMappedDataTv;

    private XYPlot mSamplesVsTimePlot;
    private XYPlot mDerivativesVsTimePlot;
    private XYPlot mMappedDataVsTimePlot;

    private ArrayAdapter mCalibrationProfileAdapter;
    private Spinner mCalibrationProfilesSpinner;

    private TextView mIdealFrequency;
    private Button mSetFrequency;
    private int idealFreq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_virtual_sensor_result_case2);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        // Build Presenter of MainActivity (which is the corresponding View)
        mPresenter = new VirtualSensorResultCase2Presenter(this);
        mPresenter.handleIntent(getIntent());

        mAverageDerivativeTv = (TextView) findViewById(R.id.average_derivative_tv);
        mAverageMappedDataTv = (TextView) findViewById(R.id.average_mapped_data_tv);

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

        mSamplesVsTimePlot = (XYPlot) findViewById(R.id.readouts_vs_time_plot);
        mDerivativesVsTimePlot = (XYPlot) findViewById(R.id.derivatives_vs_time_plot);
        mMappedDataVsTimePlot = (XYPlot) findViewById(R.id.mapped_data_vs_time_plot);

        mIdealFrequency=(TextView) findViewById(R.id.ideal_sample_rate);
        mSetFrequency=(Button) findViewById(R.id.set_sample_rate);
        mSetFrequency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor=settings.edit();
                int verifiedFrequency= idealFreq; //TODO verify frequency
                String tmp=settings.getString("sampling_frequency","SEXYINEES");
                editor.putString("sampling_frequency",Integer.toString(verifiedFrequency));
                editor.commit();
            }
        });
        updateGui();

        // Set the size of the linear layouts containing the plots such that each layout
        // takes a whole screen (a scrollview allows to scroll from one to the other)
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int screenHeight = size.y;
        int toolbarHeight = getToolBarHeight();
        int statusbarHeight = getStatusBarHeight();

        LinearLayout frame1Layout = (LinearLayout) findViewById(R.id.frame1_layout);
        ViewGroup.LayoutParams paramsFrame1 = frame1Layout.getLayoutParams();
        paramsFrame1.height = screenHeight - toolbarHeight - statusbarHeight;

        LinearLayout frame2Layout = (LinearLayout) findViewById(R.id.frame2_layout);
        ViewGroup.LayoutParams paramsFrame2 = frame2Layout.getLayoutParams();
        paramsFrame2.height = screenHeight - toolbarHeight - statusbarHeight;

        LinearLayout frame3Layout = (LinearLayout) findViewById(R.id.frame3_layout);
        ViewGroup.LayoutParams paramsFrame3 = frame3Layout.getLayoutParams();
        paramsFrame3.height = screenHeight - toolbarHeight - statusbarHeight;
    }

    public void updateGui() {

        VirtualSensorCase2 virtualSensor = mPresenter.getVirtualSensor();

        CalibrationProfile profile = getSelectedCalibrationProfile();

        VirtualSensorCase2.DataContainer dataContainer = virtualSensor.getDataContainer(getApplicationContext(), profile);
        //savetheshit(dataContainer);
        final VirtualSensorDefinitionCase2 virtualSensorDefinition = (VirtualSensorDefinitionCase2) virtualSensor.getVirtualSensorDefinition();

        mIdealFrequency.setText(Double.toString(SignalProcessor.samplingFrequencies[dataContainer.idealSampleRate]/1000)+"KHz");
        idealFreq=dataContainer.idealSampleRate;
        // Render plots
        renderSamplesVsTimePlot(virtualSensorDefinition, dataContainer);
        renderDerivativesVsTimePlot(virtualSensorDefinition, dataContainer);
        //Todo 3: in order to display temperature in sensor 3, set the following if condition to "sensor 3", by junrui
        //Todo 4: in order to display concentration in sensor 3, there is still steps missing causing crash of app
        if(dataContainer.sensorName=="Sensor 3"){
            mMappedDataVsTimePlot.setVisibility(View.INVISIBLE);
        } else {
            renderMappedDataVsTimePlot(virtualSensorDefinition, dataContainer);
        }
    }


    private void refreshCalibrationProfileSpinner() {
        // Load calibration profiles
        List<CalibrationProfile> profiles = mPresenter.refreshCalibrationProfiles();

        mCalibrationProfileAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, profiles.toArray());
        mCalibrationProfileAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCalibrationProfilesSpinner.setAdapter(mCalibrationProfileAdapter);
    }

    private void renderSamplesVsTimePlot(final VirtualSensorDefinitionCase2 virtualSensorDefinition,
                                         VirtualSensorCase2.DataContainer dataContainer) {


        Log.i(TAG, "Render samples VS time plot for " + virtualSensorDefinition.toUserFriendlyString());

        final PlotMetadata plotMetadata = virtualSensorDefinition.getSamplesPlotMetadata();
        plotMetadata.setXAxisUnitLabel("[" + dataContainer.getTimescale().getAbbreviation() + "]");

        List<DataPoint> datapoints = dataContainer.getSamplesVsTime();

        new XYPlotWrapper(mSamplesVsTimePlot, plotMetadata, datapoints);
    }

    private void renderDerivativesVsTimePlot(final VirtualSensorDefinitionCase2 virtualSensorDefinition,
                                             VirtualSensorCase2.DataContainer dataContainer) {

        Log.i(TAG, "Render derivatives VS time plot for " + virtualSensorDefinition.toUserFriendlyString());

        // Load Settings
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        double manualYValueAxis = Double.valueOf(settings.getString("manual_y_axis_plot_2_case2", getString(R.string.manual_y_axis_plot_2_case2_def_val)));

        List<DataPoint> datapoints = dataContainer.getDerivativesVsTime();
        double averageYValue = DataPoint.computeAverageOverY(datapoints);

        final PlotMetadata plotMetadata = virtualSensorDefinition.getDerivativesPlotMetadata();
        plotMetadata.setXAxisUnitLabel("[" + dataContainer.getTimescale().getAbbreviation() + "]");
//        plotMetadata.setCenterForYAxisManual(true);
//        plotMetadata.setMinValueYAxis(averageYValue/Math.pow(10, manualYValueAxis));
//        plotMetadata.setMaxValueYAxis(averageYValue*Math.pow(10, manualYValueAxis));

        new XYPlotWrapper(mDerivativesVsTimePlot, plotMetadata, datapoints);

        NumberFormat formatter = new DecimalFormat("00.##E0");
        String averageDerivativeAsString = formatter.format(dataContainer.getAverageDerivative());
        mAverageDerivativeTv.setText("Average " + plotMetadata.getYAxisLabel() + ": " + averageDerivativeAsString+virtualSensorDefinition.getDerivativesPlotMetadata().getYAxisUnitLabel());
    }

    private void renderMappedDataVsTimePlot(final VirtualSensorDefinitionCase2 virtualSensorDefinition,
                                            VirtualSensorCase2.DataContainer dataContainer) {

        Log.i(TAG, "Render mapped data VS time plot for " + virtualSensorDefinition.toUserFriendlyString());

        // Load Settings
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        double manualYAxis = Double.valueOf(settings.getString("manual_y_axis_plot_3_case2", getString(R.string.manual_y_axis_plot_3_case2_def_val)));
        if(manualYAxis<2){ manualYAxis=2;}
        List<DataPoint> datapoints = dataContainer.getMappedDataVsTime();

        boolean areYValuesNegative = true;
        if (datapoints.size() >= 1) {
            areYValuesNegative = datapoints.get(0).getY() < 0;
        }
        
        double averageYValue = 0.0;
        if (areYValuesNegative) {
            List<Double> logValues = new ArrayList<>();
            for (DataPoint dp : datapoints) {
                logValues.add(-Math.log10(Math.abs(dp.getY())));
            }
            averageYValue = SignalProcessor.mean(logValues,false);
        } else {
            List<Double> logValues = new ArrayList<>();
            for (DataPoint dp : datapoints) {
                logValues.add(Math.log10(dp.getY()));
            }
            averageYValue = SignalProcessor.mean(logValues,false);
        }

        final PlotMetadata plotMetadata = virtualSensorDefinition.getMappedDataPlotMetadata();
        plotMetadata.setXAxisUnitLabel("[" + dataContainer.getTimescale().getAbbreviation() + "]");
        plotMetadata.setCenterForYAxisManual(true);
        plotMetadata.setMinValueYAxis(averageYValue-manualYAxis);
        plotMetadata.setMaxValueYAxis(averageYValue+manualYAxis);

        new XYPlotWrapper(mMappedDataVsTimePlot, plotMetadata, datapoints);

        String averageMappedDataAsString = String.format ("%.5f", Math.pow(10,dataContainer.getAverageMappedData()));
        mAverageMappedDataTv.setText("Average " + plotMetadata.getYAxisLabel() + ": " + averageMappedDataAsString+"(mol/l)");
    }

    private CalibrationProfile getSelectedCalibrationProfile() {
        return (CalibrationProfile) mCalibrationProfilesSpinner.getSelectedItem();
    }

    private int getToolBarHeight() {
        TypedValue tv = new TypedValue();

        int actionBarHeight = 0;
        if (getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void setPresenter(Object presenter) {
        mPresenter = (VirtualSensorResultCase2Contract.Presenter) presenter;
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