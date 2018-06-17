package com.xsensio.nfcsensorcomm.mainactivity.sensorcomm;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.xsensio.nfcsensorcomm.files.FileManagerActivity;
import com.xsensio.nfcsensorcomm.mainactivity.CommContract;
import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.calibration.CalibrationActivity;
import com.xsensio.nfcsensorcomm.model.PhoneMcuCommand;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensor;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorCase1;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorCase2;
import com.xsensio.nfcsensorcomm.nfc.ExtendedModeComm;
import com.xsensio.nfcsensorcomm.sensorresult.case1.VirtualSensorResultCase1Activity;
import com.xsensio.nfcsensorcomm.sensorresult.case2.VirtualSensorResultCase2Activity;
import com.xsensio.nfcsensorcomm.sensorresult.case3.VirtualSensorResultCase3Activity;
import com.xsensio.nfcsensorcomm.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

public class SensorCommFragment extends Fragment implements SensorCommContract.View {

    private static final String TAG = "SensorCommFragment";

    private SensorCommContract.Presenter mPresenter;

    private Switch mExtendedModeSwitch;

    private VirtualSensorAdapter mSensorResultAdapter;
    public List<VirtualSensor> mVirtualSensorsRows;
    private ListView mSensorResultsListview;

    private CheckBox mSensor1Cb;
    private CheckBox mSensor2Cb;
    private CheckBox mSensor3Cb;
    private CheckBox mSensor4Cb;

    private Button mReadSensorsButton;

    /** Progress bar used while receiving for sensor data */
    private ProgressBar mProgressBar;

    private TextView mProgressBarValueTextview;

    /** Display operation status of the last "read sensors" instance */
    private TextView mOperationStatusTv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_sensor_comm, container, false);

        mExtendedModeSwitch = (Switch) view.findViewById(R.id.ext_mode_swt);
        mExtendedModeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch extendedModeSwitch = (Switch) v;
                boolean newStatus = extendedModeSwitch.isChecked();

                /**
                 *  We set the switch back to its old status. The writing of the tag config
                 *  will trigger a read config that will update the switch status.
                 *  This way, the switch has a correct status even in case of a writing failure
                 */
                extendedModeSwitch.setChecked(!newStatus);

                mPresenter.setExtendedMode(newStatus);
            }
        });

        mSensor1Cb = (CheckBox) view.findViewById(R.id.sensor_comm_sensor1_cb);
        mSensor2Cb = (CheckBox) view.findViewById(R.id.sensor_comm_sensor2_cb);
        mSensor3Cb = (CheckBox) view.findViewById(R.id.sensor_comm_sensor3_cb);
        mSensor4Cb = (CheckBox) view.findViewById(R.id.sensor_comm_sensor4_cb);

        mReadSensorsButton = (Button) view.findViewById(R.id.sensor_comm_read_btn);
        mReadSensorsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateSensorResult(new ArrayList<VirtualSensor>());

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
                int numSamplesReadoutsCase1 = Integer.valueOf(settings.getString("num_samples_roc1", getString(R.string.num_samples_roc1_def_val)));
                int numSamplesReadoutsCase2 = Integer.valueOf(settings.getString("num_samples_roc2", getString(R.string.num_samples_roc2_def_val)));
                int numSamplesReadoutsCase3 = Integer.valueOf(settings.getString("num_samples_roc3", getString(R.string.num_samples_roc3_def_val)));
                int sensorSelect = Integer.valueOf(settings.getString("sensor_select","10"));
                int sampleRate = Integer.valueOf(settings.getString("sampling_frequency","5"));

                PhoneMcuCommand command = new PhoneMcuCommand(
                        getActivity().getApplicationContext(),
                        mSensor1Cb.isChecked(),
                        mSensor2Cb.isChecked(),
                        mSensor3Cb.isChecked(),
                        mSensor4Cb.isChecked(),
                        numSamplesReadoutsCase1,
                        numSamplesReadoutsCase2,
                        numSamplesReadoutsCase3,
                        sensorSelect,
                        sampleRate
                );
                mPresenter.readSensors(command);

                mOperationStatusTv.setText("Sending read command ...");
                mProgressBar.setVisibility(View.GONE);
                mProgressBarValueTextview.setVisibility(View.GONE);

                mReadSensorsButton.setEnabled(false);
            }
        });

        // Build ListView containing the sensors returned by the reading process
        mSensorResultsListview = (ListView) view.findViewById(R.id.sensor_result_listview);
        mVirtualSensorsRows = new ArrayList<>();
        mSensorResultAdapter = new VirtualSensorAdapter(getActivity(), mVirtualSensorsRows, this);

        mSensorResultsListview.setAdapter(mSensorResultAdapter);
        mSensorResultsListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                VirtualSensor virtualSensor = (VirtualSensor) mSensorResultsListview.getItemAtPosition(position);

                Intent intent;
                if (virtualSensor instanceof VirtualSensorCase1) {
                    intent = new Intent(getActivity(), VirtualSensorResultCase1Activity.class);
                    intent.putExtra(VirtualSensorResultCase1Activity.EXTRA_VIRTUAL_SENSOR, (Parcelable) virtualSensor);
                } else if (virtualSensor instanceof VirtualSensorCase2) {
                    intent = new Intent(getActivity(), VirtualSensorResultCase2Activity.class);
                    intent.putExtra(VirtualSensorResultCase2Activity.EXTRA_VIRTUAL_SENSOR, (Parcelable) virtualSensor);
                } else { // VirtualSensorCase3
                    intent = new Intent(getActivity(), VirtualSensorResultCase3Activity.class);
                    intent.putExtra(VirtualSensorResultCase3Activity.EXTRA_VIRTUAL_SENSOR, (Parcelable) virtualSensor);
                }

                startActivity(intent);
            }
        });

        getActivity().invalidateOptionsMenu();

        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);
        mProgressBar.setMax(0);
        mProgressBar.setMax(100);
        mProgressBar.setProgress(0);

        mProgressBarValueTextview = (TextView) view.findViewById(R.id.progress_bar_value_tv);

        mOperationStatusTv = (TextView) view.findViewById(R.id.read_status_tv);

        return view;
    }

    @Override
    public void setPresenter(@NonNull CommContract.Presenter presenter) {
        mPresenter = checkNotNull((SensorCommContract.Presenter) presenter);
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void updateSensorResult(List<VirtualSensor> virtualSensors) {
        mVirtualSensorsRows = virtualSensors;
        mSensorResultAdapter = new VirtualSensorAdapter(getActivity(), mVirtualSensorsRows, this);
        mSensorResultsListview.setAdapter(mSensorResultAdapter);
        //TODO load saved data layer 0
        // Remove progress bar from display
        mProgressBar.setVisibility(View.GONE);
        mProgressBarValueTextview.setVisibility(View.GONE);
    }

    @Override
    public void updateExtendedMode(boolean isEnabled) {
        mExtendedModeSwitch.setChecked(isEnabled);
    }

    @Override
    public void updateSensorResultStatus(String operationStatus) {
        mOperationStatusTv.setText(operationStatus);
    }

    @Override
    public void updateReadSensorProgress(String taskDescription, int completionRatio) {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBarValueTextview.setVisibility(View.VISIBLE);
        mProgressBar.setProgress(completionRatio);
        mProgressBarValueTextview.setText(completionRatio + "% ");
        mOperationStatusTv.setText(taskDescription);
    }

    @Override
    public void setReadSensorsButtonEnabled(boolean enabled) {
        mReadSensorsButton.setEnabled(enabled);
    }

    public void onResume() {
        super.onResume();

        if (mPresenter != null) {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
            manager.registerReceiver((BroadcastReceiver) mPresenter, new IntentFilter(SensorCommIntentService.ACTION_READ_SENSORS));
            manager.registerReceiver((BroadcastReceiver) mPresenter, new IntentFilter(ExtendedModeComm.ACTION_EXT_MOD_READ_PROGRESS_FEEDBACK));
        }
    }

    public void onPause() {
        super.onPause();

        if (mPresenter != null) {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
            manager.unregisterReceiver((BroadcastReceiver) mPresenter);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_sensorcomm, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;

        switch (item.getItemId()) {
            case R.id.action_settings:
                intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_calibration:
                intent = new Intent(getActivity(), CalibrationActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_files:
                intent = new Intent(getActivity(), FileManagerActivity.class);
                intent.putExtra("Exist",mVirtualSensorsRows.size()>0);
                ArrayList<VirtualSensor> tmp=new ArrayList<>();
                tmp.addAll(mVirtualSensorsRows);
                intent.putParcelableArrayListExtra("sensors",tmp);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}