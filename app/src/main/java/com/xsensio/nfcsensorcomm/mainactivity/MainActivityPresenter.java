package com.xsensio.nfcsensorcomm.mainactivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.util.Log;

import com.xsensio.nfcsensorcomm.OperationStatus;
import com.xsensio.nfcsensorcomm.mainactivity.tagconfiguration.NfcTagConfigurationIntentService;
import com.xsensio.nfcsensorcomm.model.NfcTagConfiguration;
import com.xsensio.nfcsensorcomm.nfc.NfcUtils;

import java.util.ArrayList;
import java.util.List;

import static com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm.PhoneTagCommIntentService.EXTRA_NFC_COMM_STATUS;

/**
 * Created by Michael Heiniger on 14.07.17.
 */

public class MainActivityPresenter extends BroadcastReceiver implements MainActivityContract.Presenter {

    private static final String TAG = "MainActivityPresenter";

    private final MainActivityContract.View mView;
    private final List<CommContract.Presenter> mCommContractPresenters;

    private Tag mTag;

    public MainActivityPresenter(MainActivityContract.View view, List<CommContract.Presenter> presenters) {
        mView = view;
        mCommContractPresenters = new ArrayList<CommContract.Presenter>(presenters);
    }

    @Override
    public void processReceivedIntent(Intent intent) {

        String action = intent.getAction();

        Log.i(TAG, "New intent received, action: " + action + " type: " + intent.getType());

        // If a NFC Tag is detected
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {

            mView.showToast("Tag detected !");
            Global.nfc_set = true;
            mTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            // Extract Text-formattable (i.e. plain-text or URL) Ndef message from the tag, if any.
            NfcUtils.getTextFormattableNdefMessageContent(intent);

            /** Read the tag configuration and notify all relevant fragments (see {@link #onReceive}) */
            NfcTagConfigurationIntentService.startActionReadTagConfiguration(((Activity) mView).getApplicationContext(), mTag);
        }
    }

    @Override
    public void showToast(String message) {
        mView.showToast(message);
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        Log.d(TAG, "BroadcastReceiver: intent received, action: " + action);

        // Extract event status
        OperationStatus status = (OperationStatus) intent.getSerializableExtra(EXTRA_NFC_COMM_STATUS);
        if (status != null && status != OperationStatus.READ_TAG_CONFIGURATION_SUCCESS) {
            showToast(status.toUserFriendlyString());
        }

        if (NfcTagConfigurationIntentService.ACTION_READ_TAG_CONFIGURATION.equals(action)) {

            // Extract relevant data from intent
            String tagContent = intent.getStringExtra(NfcTagConfigurationIntentService.EXTRA_TAG_CONTENT);
            NfcTagConfiguration config = intent.getParcelableExtra(NfcTagConfigurationIntentService.EXTRA_TAG_CONFIGURATION);

            if (config != null) {
                Log.i(TAG, config.toString());
            }

            // Forward NFC tag detection event and data to fragments presenters
            for (CommContract.Presenter presenter : mCommContractPresenters) {
                presenter.nfcTagDetected(mTag, config);
            }

        } else if (NfcTagConfigurationIntentService.ACTION_WRITE_TAG_CONFIGURATION.equals(action)) {

            // Extract relevant data from intent
            /**
             * Trigger a new configuration read to confirm the state of the configuration and
             * notify all other fragments
             */
            NfcTagConfigurationIntentService.startActionReadTagConfiguration(((Activity) mView).getApplicationContext(), mTag);

        } else {
            Log.d(TAG, "Intent received: action unknown");
        }
    }
}