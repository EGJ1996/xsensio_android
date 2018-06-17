package com.xsensio.nfcsensorcomm.mainactivity.phonemcucomm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.util.Log;

import com.xsensio.nfcsensorcomm.OperationStatus;
import com.xsensio.nfcsensorcomm.model.NfcTagConfiguration;
import com.xsensio.nfcsensorcomm.mainactivity.tagconfiguration.NfcTagConfigurationIntentService;

import static com.xsensio.nfcsensorcomm.mainactivity.phonemcucomm.PhoneMcuCommIntentService.*;
import static com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm.PhoneTagCommIntentService.EXTRA_NFC_COMM_STATUS;


/**
 * Created by Michael Heiniger on 13.07.17.
 */

public class PhoneMcuCommPresenter extends BroadcastReceiver implements PhoneMcuCommContract.Presenter {

    private static final String TAG = "PhoneMcuCommPresenter";

    private Activity mActivity;
    private PhoneMcuCommContract.View mView;

    private Tag mTag;

    // Data attributes used by the View
    private NfcTagConfiguration mTagConfiguration;

    public PhoneMcuCommPresenter(Activity activity, PhoneMcuCommContract.View view) {
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
            // Inform View of incoming pushed data
            mView.updateExtendedMode(mTagConfiguration.isExtendedModeEnabled());
        }
    }

    @Override
    public void readData() {
        PhoneMcuCommIntentService.startActionRead(mActivity.getApplicationContext(), mTag);
    }

    @Override
    public void writeData(byte[] data) {
        PhoneMcuCommIntentService.startActionWrite(mActivity.getApplicationContext(), mTag, data);
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
        String action = intent.getAction();

        Log.d(TAG, "BroadcastReceiver: intent received, action: " + action);

        // Extract event status
        OperationStatus status = (OperationStatus) intent.getSerializableExtra(EXTRA_NFC_COMM_STATUS);
        if (status != null) {
            showToast(status.toUserFriendlyString());
        }

        if (ACTION_READ.equals(action) && OperationStatus.READ_MCU_SUCCESS == status) {

            // Extract relevant data from intent
            byte[] dataReceivedFromMcu = intent.getByteArrayExtra(EXTRA_DATA);

            // Notify View
            mView.setReceivedData(dataReceivedFromMcu);
        } else {
            Log.d(TAG, "Intent received: action unknown");
        }
    }
}