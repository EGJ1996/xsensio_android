package com.xsensio.nfcsensorcomm.mainactivity.sensorcomm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.util.Log;

import com.xsensio.nfcsensorcomm.model.PhoneMcuCommand;
import com.xsensio.nfcsensorcomm.OperationStatus;
import com.xsensio.nfcsensorcomm.model.NfcTagConfiguration;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensor;
import com.xsensio.nfcsensorcomm.sensorresult.case1.VirtualSensorResultCase1Activity;
import com.xsensio.nfcsensorcomm.mainactivity.tagconfiguration.NfcTagConfigurationIntentService;

import java.util.List;

import static com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm.PhoneTagCommIntentService.EXTRA_NFC_COMM_STATUS;
import static com.xsensio.nfcsensorcomm.mainactivity.sensorcomm.SensorCommIntentService.ACTION_READ_SENSORS;
import static com.xsensio.nfcsensorcomm.nfc.ExtendedModeComm.ACTION_EXT_MOD_READ_PROGRESS_FEEDBACK;
import static com.xsensio.nfcsensorcomm.nfc.ExtendedModeComm.EXTRA_EXT_MOD_TASK_COMPLETION_RATIO;
import static com.xsensio.nfcsensorcomm.nfc.ExtendedModeComm.EXTRA_EXT_MOD_TASK_DESCRIPTION;


/**
 * Created by Michael Heiniger on 13.07.17.
 */

public class SensorCommPresenter extends BroadcastReceiver implements SensorCommContract.Presenter {

    private static final String TAG = "SensorCommPresenter";

    private Activity mActivity;
    private SensorCommContract.View mView;

    private Tag mTag;
    private NfcTagConfiguration mTagConfiguration;

    public SensorCommPresenter(Activity activity, SensorCommContract.View view) {
        mActivity = activity;
        mView = view;
    }

    @Override
    public void showToast(String message) {
        mView.showToast(message);
    }

    @Override
    public void nfcTagDetected(Tag tag, NfcTagConfiguration config) {

        Log.d(TAG, "nfcTagDetected !!");
        mTag = tag;
        mTagConfiguration = config;

        if (mTagConfiguration != null) {
            mView.updateExtendedMode(mTagConfiguration.isExtendedModeEnabled());
        }
    }

    @Override
    public void readSensors(PhoneMcuCommand command) {
         SensorCommIntentService.startActionReadSensors(mActivity.getApplicationContext(), mTag, command);
    }

    @Override
    public void setExtendedMode(boolean isChecked) {
        if (mTagConfiguration == null) {
            showToast("No tag is detected, Extended mode cannot be enabled or disabled.");
            return;
        }

        NfcTagConfiguration newConfig = mTagConfiguration.setExtendedMode(isChecked);
        NfcTagConfigurationIntentService.startActionWriteConfiguration(mActivity.getApplicationContext(), mTag, newConfig);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //TODO on data Recieve updating the progress bar
        String action = intent.getAction();

        //Log.d(TAG, "BroadcastReceiver: intent received, action: " + action);

        // Extract event status
        OperationStatus status = (OperationStatus) intent.getSerializableExtra(EXTRA_NFC_COMM_STATUS);
        if (status != null) {
            showToast(status.toUserFriendlyString());
        }

        if (ACTION_READ_SENSORS.equals(action)) {

            // Extract relevant data from intent
            PhoneMcuCommand phoneMcuCommand = intent.getParcelableExtra(SensorCommIntentService.EXTRA_COMMAND);
            List<VirtualSensor> virtualSensors = intent.getParcelableArrayListExtra(VirtualSensorResultCase1Activity.EXTRA_VIRTUAL_SENSOR);

            // Notify View
            if (status == OperationStatus.READ_SENSORS_SUCCESS) {
                mView.updateSensorResult(virtualSensors);
            }
//            mView.updateSensorResultStatus(status.toUserFriendlyString());

            mView.setReadSensorsButtonEnabled(true);

        } else if (ACTION_EXT_MOD_READ_PROGRESS_FEEDBACK.equals(action)) {
            String taskDescription = intent.getStringExtra(EXTRA_EXT_MOD_TASK_DESCRIPTION);
            int completionRatio = intent.getIntExtra(EXTRA_EXT_MOD_TASK_COMPLETION_RATIO, 0);

            //Log.d(TAG, "Task description: " + taskDescription + ", task completion ratio: " + completionRatio);

            // Notify View
            mView.updateReadSensorProgress(taskDescription, completionRatio);

        } else {
            Log.d(TAG, "Intent received: action unknown");
        }
    }
}
