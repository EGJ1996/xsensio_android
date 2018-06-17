package com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.util.Log;

import com.xsensio.nfcsensorcomm.model.MemoryBlock;
import com.xsensio.nfcsensorcomm.OperationStatus;
import com.xsensio.nfcsensorcomm.model.NfcTagConfiguration;
import com.xsensio.nfcsensorcomm.nfc.NfcUtils;

import java.util.List;
import java.util.Locale;

import static com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm.PhoneTagCommIntentService.*;

/**
 * Created by Michael Heiniger on 12.07.17.
 */

public class PhoneTagCommPresenter extends BroadcastReceiver implements PhoneTagCommContract.Presenter {

    private static final String TAG = "PhoneTagCommPresenter";

    private Activity mActivity;
    private PhoneTagCommContract.View mView;

    private Tag mTag;

    public PhoneTagCommPresenter(Activity activity, PhoneTagCommContract.View view) {
        mActivity = activity;
        mView = view;
    }

    @Override
    public void nfcTagDetected(Tag tag, NfcTagConfiguration config) {

        Log.d(TAG, "nfcTagDetected !!");
        mTag = tag;
    }

    @Override
    public void readMemoryBlock(String blockAddressAsString) {
        try {
            byte blockAddress = Integer.valueOf(blockAddressAsString.trim(), 16).byteValue();
            PhoneTagCommIntentService.startActionReadMemoryBlock(mActivity.getApplicationContext(), mTag, new MemoryBlock(blockAddress, MemoryBlock.UNDEFINED_CONTENT));
        } catch (IllegalArgumentException e) {
            mView.showToast("Block address cannot be empty !");
        }
    }

    @Override
    public void readAllMemoryBlocks() {
        PhoneTagCommIntentService.startActionReadAllMemoryBlocks(mActivity.getApplicationContext(), mTag);
    }

    @Override
    public void writeMemoryBlock(String blockAddressAsString, String blockContentAsString) {
        try {
            PhoneTagCommIntentService.startActionWriteMemoryBlock(mActivity.getApplicationContext(), mTag, new MemoryBlock(blockAddressAsString, blockContentAsString));
        } catch (IllegalArgumentException e) {
            mView.showToast("Block address cannot be empty !");
        }
    }

    @Override
    public void resetNfcTag() {
        PhoneTagCommIntentService.startActionResetTag(mActivity.getApplicationContext(), mTag);
    }

    @Override
    public void readNdefTag() {
        PhoneTagCommIntentService.startActionReadNdefTag(mActivity.getApplicationContext(), mTag);
    }

    @Override
    public void writeNdefMessage(String text) {
        NdefMessage ndefMessage = new NdefMessage(NdefRecord.createTextRecord(Locale.US.getLanguage(), text));
        PhoneTagCommIntentService.startActionWriteNdefTag(mActivity.getApplicationContext(), mTag, ndefMessage);
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
        if (status != null) {
            showToast(status.toUserFriendlyString());
        }

        if (ACTION_READ_MEMORY_BLOCK.equals(action) && OperationStatus.READ_MEMORY_BLOCK_SUCCESS == status) {

            // Extract relevant data from intent
            MemoryBlock memoryBlock = intent.getParcelableExtra(EXTRA_MEMORY_BLOCK);

            // Notify View
            mView.updateMemoryBlock(memoryBlock);

        } else if (ACTION_READ_ALL_MEMORY_BLOCKS.equals(action) && OperationStatus.READ_MEMORY_BLOCK_SUCCESS == status) {

            // Extract relevant data from intent
            List<MemoryBlock> memoryBlocks = intent.getParcelableArrayListExtra(EXTRA_MEMORY_BLOCKS);

            if (memoryBlocks != null) {
                // Notify View
                for (MemoryBlock block : memoryBlocks) {
                    mView.updateMemoryBlock(block);
                }
            }

        } else if (ACTION_WRITE_MEMORY_BLOCK.equals(action)) {

            // Extract relevant data from intent
            MemoryBlock memoryBlock = intent.getParcelableExtra(EXTRA_MEMORY_BLOCK);

        } else if (ACTION_READ_NDEF_TAG.equals(action) && OperationStatus.READ_NDEF_SUCCESS ==  status) {

            // Extract relevant data from intent
            NdefMessage message = intent.getParcelableExtra(EXTRA_NDEF_RESPONSE);

            String tagContent = NfcUtils.getTextFormatableContentFromNdefMessage(message);

            // Notify View
            mView.updateNdefMessage(tagContent);

        } else {
            Log.d(TAG, "Intent received: action unknown");
        }
    }
}
