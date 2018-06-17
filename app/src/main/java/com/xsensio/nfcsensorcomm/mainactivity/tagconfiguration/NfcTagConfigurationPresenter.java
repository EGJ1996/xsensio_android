package com.xsensio.nfcsensorcomm.mainactivity.tagconfiguration;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.util.Log;

import com.xsensio.nfcsensorcomm.model.NfcTagConfiguration;


/**
 * Created by Michael Heiniger on 25.07.17.
 */

public class NfcTagConfigurationPresenter extends BroadcastReceiver implements NfcTagConfigurationContract.Presenter{

    private static final String TAG = "NfcTagConfigPresenter";

    private NfcTagConfigurationContract.View mView;
    private Activity mActivity;

    private Tag mTag;
    private NfcTagConfiguration mTagConfiguration;

    public NfcTagConfigurationPresenter(Activity activity, NfcTagConfigurationContract.View view) {
        mView = view;
        mActivity = activity;
    }

    @Override
    public void showToast(String message) {
        mView.showToast(message);
    }

    @Override
    public void readTagConfiguration() {
        NfcTagConfigurationIntentService.startActionReadTagConfiguration(mActivity, mTag);
    }

    @Override
    public void writeTagConfiguration(byte powerMode, byte voltage, byte outputResistance, byte extendedMode) {

        if (mTagConfiguration != null) {
            NfcTagConfiguration newConfig = new NfcTagConfiguration(mTagConfiguration, powerMode, voltage, outputResistance, extendedMode);

            byte[] bytes = newConfig.getConfigAsMemoryBlocks().get(NfcTagConfiguration.POS_BLOCK_7F).getContentAsBytes();

            NfcTagConfigurationIntentService.startActionWriteConfiguration(mActivity, mTag, newConfig);

            mTagConfiguration = newConfig;
        } else {
            showToast("Tag configuration must be read first.");
        }
    }

    @Override
    public void nfcTagDetected(Tag tag, NfcTagConfiguration config) {

        Log.d(TAG, "nfcTagDetected !!");
        mTag = tag;
        mTagConfiguration = config;

        if (mTagConfiguration != null) {
            mView.setExtendedMode(mTagConfiguration.isExtendedModeEnabled());
            mView.setPowerMode(mTagConfiguration.getPowerModeAsByte());
            mView.setVoltage(mTagConfiguration.getVoltageValueAsByte());
            mView.setOutputResistance(mTagConfiguration.getOutputResistanceAsByte());
            mView.setMemoryBlockConfiguration(mTagConfiguration.getConfigAsMemoryBlocks());
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        /*String action = intent.getAction();

        Log.d(TAG, "BroadcastReceiver: intent received, action: " + action);

        // Extract event status
        OperationStatus status = (OperationStatus) intent.getSerializableExtra(EXTRA_NFC_COMM_STATUS);
        if (status != null) {
            showToast(status.getVirtualSensorName());
        }*/
    }
}