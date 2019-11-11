package com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.os.Parcelable;
import android.util.Log;

import com.xsensio.nfcsensorcomm.OperationStatus;
import com.xsensio.nfcsensorcomm.mainactivity.Global;
import com.xsensio.nfcsensorcomm.model.Memory;
import com.xsensio.nfcsensorcomm.model.MemoryBlock;
import com.xsensio.nfcsensorcomm.nfc.NfcATagComm;
import com.xsensio.nfcsensorcomm.nfc.NfcNdefTagComm;
import com.xsensio.nfcsensorcomm.nfc.TagNotWritableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhoneTagCommIntentService extends IntentService {

    public static final String ACTION_READ_MEMORY_BLOCK = "com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm.action.READ_MEMORY_BLOCK";
    public static final String ACTION_WRITE_MEMORY_BLOCK = "com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm.action.WRITE_MEMORY_BLOCK";
    public static final String ACTION_RESET_TAG = "com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm.action.RESET_TAG";
    public static final String ACTION_READ_NDEF_TAG = "com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm.action.READ_NDEF_TAG";
    public static final String ACTION_WRITE_NDEF_TAG = "com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm.action.WRITE_NDEF_TAG";
    public static final String ACTION_READ_ALL_MEMORY_BLOCKS = "com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm.action.READ_ALL_MEMORY_BLOCKS";

    public static final String EXTRA_NFC_TAG = "com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm.extra.NFC_TAG";
    public static final String EXTRA_MEMORY_BLOCK = "com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm.extra.MEMORY_BLOCK";
    public static final String EXTRA_MEMORY_BLOCKS = "com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm.extra.MEMORY_BLOCKS";
    public static final String EXTRA_NDEF_MESSAGE = "com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm.extra.NDEF_MESSAGE";

    public static final String EXTRA_NDEF_RESPONSE = "com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm.extra.NDEF_RESPONSE";
    public static final String EXTRA_NFC_COMM_STATUS = "com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm.extra.NFC_COMM_STATUS";


    public PhoneTagCommIntentService() {
        super("PhoneTagCommIntentService");
    }

    public static void startActionReadMemoryBlock(Context context, Tag tag, MemoryBlock memorBlock) {
        Intent intent = new Intent(context, PhoneTagCommIntentService.class);
        intent.setAction(ACTION_READ_MEMORY_BLOCK);
        intent.putExtra(EXTRA_NFC_TAG, tag);
        intent.putExtra(EXTRA_MEMORY_BLOCK, (Parcelable) memorBlock);
        context.startService(intent);
    }

    public static void startActionReadAllMemoryBlocks(Context context, Tag tag) {
        Intent intent = new Intent(context, PhoneTagCommIntentService.class);
        intent.setAction(ACTION_READ_ALL_MEMORY_BLOCKS);
        intent.putExtra(EXTRA_NFC_TAG, tag);
        context.startService(intent);
    }

    public static void startActionWriteMemoryBlock(Context context, Tag tag, MemoryBlock memorBlock) {
        Intent intent = new Intent(context, PhoneTagCommIntentService.class);
        intent.setAction(ACTION_WRITE_MEMORY_BLOCK);
        intent.putExtra(EXTRA_NFC_TAG, tag);
        intent.putExtra(EXTRA_MEMORY_BLOCK, (Parcelable) memorBlock);
        context.startService(intent);
    }

    public static void startActionResetTag(Context context, Tag tag) {
        Intent intent = new Intent(context, PhoneTagCommIntentService.class);
        intent.setAction(ACTION_RESET_TAG);
        intent.putExtra(EXTRA_NFC_TAG, tag);
        context.startService(intent);
    }

    public static void startActionReadNdefTag(Context context, Tag tag) {
        Intent intent = new Intent(context, PhoneTagCommIntentService.class);
        intent.setAction(ACTION_READ_NDEF_TAG);
        intent.putExtra(EXTRA_NFC_TAG, tag);
        context.startService(intent);
    }

    public static void startActionWriteNdefTag(Context context, Tag tag, NdefMessage ndefMessage) {
        Intent intent = new Intent(context, PhoneTagCommIntentService.class);
        intent.setAction(ACTION_WRITE_NDEF_TAG);
        intent.putExtra(EXTRA_NFC_TAG, tag);
        intent.putExtra(EXTRA_NDEF_MESSAGE, ndefMessage);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_READ_MEMORY_BLOCK.equals(action)) {
                final Tag tag = (Tag) intent.getParcelableExtra(EXTRA_NFC_TAG);
                final MemoryBlock block = intent.getParcelableExtra(EXTRA_MEMORY_BLOCK);
                handleActionReadMemoryBlock(tag, block);
            } else if (ACTION_READ_ALL_MEMORY_BLOCKS.equals(action)) {
                final Tag tag = (Tag) intent.getParcelableExtra(EXTRA_NFC_TAG);
                handleActionReadAllMemoryBlocks(tag);
            } else if (ACTION_WRITE_MEMORY_BLOCK.equals(action)) {
                final Tag tag = (Tag) intent.getParcelableExtra(EXTRA_NFC_TAG);
                final MemoryBlock memoryBlock = intent.getParcelableExtra(EXTRA_MEMORY_BLOCK);
                handleActionWriteMemoryBlock(tag, memoryBlock);
            } else if (ACTION_RESET_TAG.equals(action)) {
                final Tag tag = (Tag) intent.getParcelableExtra(EXTRA_NFC_TAG);
                handleActionResetTagMemoryBlock(tag);
            } else if (ACTION_READ_NDEF_TAG.equals(action)) {
                final Tag tag = (Tag) intent.getParcelableExtra(EXTRA_NFC_TAG);
                handleActionReadNdefTag(tag);
            } else if (ACTION_WRITE_NDEF_TAG.equals(action)) {
                final Tag tag = (Tag) intent.getParcelableExtra(EXTRA_NFC_TAG);
                final NdefMessage ndefMessage = (NdefMessage) intent.getParcelableExtra(EXTRA_NDEF_MESSAGE);
                handleActionWriteNdefTag(tag, ndefMessage);
            }
        }
    }

    private void handleActionReadMemoryBlock(Tag tag, MemoryBlock memorBlock) {

        MemoryBlock firstBlock = null;
        OperationStatus status;
        try {
            List<MemoryBlock> memoryBlocks = NfcATagComm.read(tag, memorBlock);
            status = OperationStatus.READ_MEMORY_BLOCK_SUCCESS;

            /** We want the data of only ONE block (i.e. the first 4 bytes returned) but the NFC-A
             * protocol returns 4 blocks (starting at the specified block address), so we need to
             * truncate the received bytes and keep only the first 4.
             */
            if (memoryBlocks.size() >= 1) {
                firstBlock = memoryBlocks.get(0);
            }
        } catch (TagLostException e) {
            Log.d("Tag","Tag Lost exception\n");

            e.printStackTrace();
            status = OperationStatus.TAG_LOST;
            Global.nfc_set = false;
        } catch (IOException e) {
            e.printStackTrace();
            status = OperationStatus.COMM_FAILURE;
            Global.nfc_set = false;
        }

        Intent intent = new Intent(ACTION_READ_MEMORY_BLOCK);
        intent.putExtra(EXTRA_MEMORY_BLOCK, (Parcelable) firstBlock);
        intent.putExtra(EXTRA_NFC_COMM_STATUS, status);
        sendBroadcast(intent);
    }

    private void handleActionReadAllMemoryBlocks(Tag tag) {

        ArrayList<MemoryBlock> memoryBlocks = null; //TODO Add IO status

        OperationStatus status;
        try {
            memoryBlocks = NfcATagComm.readMultipleBlocks(tag, Memory.getAllMemoryAddresses());
            status = OperationStatus.READ_MEMORY_BLOCK_SUCCESS;
        } catch (TagLostException e) {
            Log.d("Tag","Tag Lost exception\n");
            e.printStackTrace();
            status = OperationStatus.TAG_LOST;
            Global.nfc_set = false;
        } catch (IOException e) {
            e.printStackTrace();
            status = OperationStatus.COMM_FAILURE;
            Global.nfc_set = false;
        }

        Intent intent = new Intent(ACTION_READ_ALL_MEMORY_BLOCKS);
        intent.putParcelableArrayListExtra(EXTRA_MEMORY_BLOCKS, memoryBlocks);
        intent.putExtra(EXTRA_NFC_COMM_STATUS, status);

        sendBroadcast(intent);
    }

    private void handleActionWriteMemoryBlock(Tag tag, MemoryBlock memoryBlock) {

        OperationStatus status;
        try {
            NfcATagComm.writeOneBlock(tag, memoryBlock);
            status = OperationStatus.WRITE_MEMORY_BLOCK_SUCCESS;
        } catch (TagLostException e) {
            Log.d("Tag","Tag Lost exception\n");

            e.printStackTrace();
            status = OperationStatus.TAG_LOST;
            Global.nfc_set = false;
        } catch (IOException e) {
            e.printStackTrace();
            status = OperationStatus.COMM_FAILURE;
            Global.nfc_set = false;
        }

        Intent intent = new Intent(ACTION_WRITE_MEMORY_BLOCK);
        intent.putExtra(EXTRA_MEMORY_BLOCK, (Parcelable) memoryBlock);
        intent.putExtra(EXTRA_NFC_COMM_STATUS, status);
        sendBroadcast(intent);
    }

    private void handleActionResetTagMemoryBlock(Tag tag) {

        List<MemoryBlock> memoryBlocksToWrite = Memory.getStableMemoryConfiguration();

        OperationStatus status;
        try {
            NfcATagComm.writeMultipleBlocks(tag, memoryBlocksToWrite);
            status = OperationStatus.WRITE_MEMORY_BLOCK_SUCCESS;
        } catch (TagLostException e) {
            Log.d("Tag","Tag Lost exception\n");
            e.printStackTrace();
            status = OperationStatus.TAG_LOST;
            Global.nfc_set = false;
        } catch (IOException e) {
            e.printStackTrace();
            status = OperationStatus.COMM_FAILURE;
            Global.nfc_set = false;
        }

        Intent intent = new Intent(ACTION_RESET_TAG);
        intent.putExtra(EXTRA_NFC_COMM_STATUS, status);
        sendBroadcast(intent);
    }

    private void handleActionReadNdefTag(Tag tag) {

        NdefMessage ndefMessage = null;

        OperationStatus status;
        try {
            ndefMessage = NfcNdefTagComm.readNdefTag(tag);
            status = OperationStatus.READ_NDEF_SUCCESS;
        } catch (TagLostException e) {
            Log.d("Tag","Tag Lost exception\n");
            e.printStackTrace();
            status = OperationStatus.TAG_LOST;
            Global.nfc_set = false;
        } catch (IOException e) {
            e.printStackTrace();
            status = OperationStatus.COMM_FAILURE;
            Global.nfc_set = false;
        } catch (FormatException e) {
            e.printStackTrace();
            status = OperationStatus.FORMAT_ERROR;
        } catch (TagNotWritableException e) {
            e.printStackTrace();
            status = OperationStatus.TAG_NOT_NDEF_WRITABLE;
        }

        Intent intent = new Intent(ACTION_READ_NDEF_TAG);
        intent.putExtra(EXTRA_NDEF_RESPONSE, ndefMessage);
        intent.putExtra(EXTRA_NFC_COMM_STATUS, status);
        sendBroadcast(intent);
    }

    private void handleActionWriteNdefTag(Tag tag, NdefMessage ndefMessage) {

        OperationStatus status;
        try {
            NfcNdefTagComm.writeNdefTag(tag, ndefMessage);
            status = OperationStatus.WRITE_NDEF_SUCCESS;
        } catch (TagLostException e) {
            Log.d("Tag","Tag Lost exception\n");
            e.printStackTrace();
            status = OperationStatus.TAG_LOST;
            Global.nfc_set = false;
        } catch (IOException e) {
            e.printStackTrace();
            status = OperationStatus.COMM_FAILURE;
            Global.nfc_set = false;
        } catch (FormatException e) {
            e.printStackTrace();
            status = OperationStatus.FORMAT_ERROR;
        } catch (TagNotWritableException e) {
            e.printStackTrace();
            status = OperationStatus.TAG_NOT_NDEF_WRITABLE;
        }

        Intent intent = new Intent(ACTION_WRITE_NDEF_TAG);
        intent.putExtra(EXTRA_NFC_COMM_STATUS, status);
        sendBroadcast(intent);
    }
}