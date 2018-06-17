package com.xsensio.nfcsensorcomm.mainactivity.tagconfiguration;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.os.Parcelable;

import com.xsensio.nfcsensorcomm.OperationStatus;
import com.xsensio.nfcsensorcomm.model.MemoryBlock;
import com.xsensio.nfcsensorcomm.model.NfcTagConfiguration;
import com.xsensio.nfcsensorcomm.nfc.NfcATagComm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm.PhoneTagCommIntentService.EXTRA_NFC_COMM_STATUS;


public class NfcTagConfigurationIntentService extends IntentService {

    private static final String TAG = "MainActivityIS";

    public static final String ACTION_READ_TAG_CONFIGURATION = "com.xsensio.nfcsensorcomm.comm.action.READ_TAG_CONFIGURATION";
    public static final String ACTION_WRITE_TAG_CONFIGURATION = "com.xsensio.nfcsensorcomm.comm.action.WRITE_TAG_CONFIGURATION";

    public static final String EXTRA_TAG = "com.xsensio.nfcsensorcomm.comm.extra.TAG";
    public static final String EXTRA_TAG_CONTENT = "com.xsensio.nfcsensorcomm.comm.extra.TAG_CONTENT";
    public static final String EXTRA_TAG_CONFIGURATION = "com.xsensio.nfcsensorcomm.comm.extra.TAG_CONFIGURATION";

    public NfcTagConfigurationIntentService() {
        super("NfcTagConfigurationIntentService");
    }

    public static void startActionReadTagConfiguration(Context context, Tag tag) {
        Intent intent = new Intent(context, NfcTagConfigurationIntentService.class);
        intent.setAction(ACTION_READ_TAG_CONFIGURATION);
        intent.putExtra(EXTRA_TAG, tag);
        context.startService(intent);
    }

    public static void startActionWriteConfiguration(Context context, Tag tag, NfcTagConfiguration config) {
        Intent intent = new Intent(context, NfcTagConfigurationIntentService.class);
        intent.setAction(ACTION_WRITE_TAG_CONFIGURATION);
        intent.putExtra(EXTRA_TAG, tag);
        intent.putExtra(EXTRA_TAG_CONFIGURATION, (Parcelable) config);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_READ_TAG_CONFIGURATION.equals(action)) {
                final Tag tag = intent.getParcelableExtra(EXTRA_TAG);
                handleActionReadTagConfiguration(tag);
            } else if (ACTION_WRITE_TAG_CONFIGURATION.equals(action)) {
                final Tag tag = intent.getParcelableExtra(EXTRA_TAG);
                final NfcTagConfiguration config = intent.getParcelableExtra(EXTRA_TAG_CONFIGURATION);
                handleActionWriteTagConfiguration(tag, config);
            }
        }
    }

    private void handleActionReadTagConfiguration(Tag tag) {

        // Read blocks 7C, 7D, 7E, 7F
        Byte blockAddress = (byte) 0x7C;

        NfcTagConfiguration config = null;
        OperationStatus status = null;
        try {
            List<MemoryBlock> memoryBlocks = NfcATagComm.read(tag, new MemoryBlock(blockAddress, MemoryBlock.UNDEFINED_CONTENT));
            config = new NfcTagConfiguration(memoryBlocks);

            status = OperationStatus.READ_TAG_CONFIGURATION_SUCCESS;
        } catch (TagLostException e) {
            e.printStackTrace();
            status = OperationStatus.TAG_LOST;
        } catch (IOException e) {
            e.printStackTrace();
            status = OperationStatus.COMM_FAILURE;
        }

        Intent intent = new Intent(ACTION_READ_TAG_CONFIGURATION);
        intent.putExtra(EXTRA_TAG_CONFIGURATION, (Parcelable) config);
        intent.putExtra(EXTRA_NFC_COMM_STATUS, status);
        sendBroadcast(intent);
    }

    private void handleActionWriteTagConfiguration(Tag tag, NfcTagConfiguration config) {

        OperationStatus status;
        if (config != null) {
            List<MemoryBlock> configAsMemoryBlock = config.getConfigAsMemoryBlocks();

            /**
             * Remove the block at address 7C: Block 7C can only be written through NFC in
             * "authenticated" mode block. That would require some extra code.
             *
             * Exchange writing order of blocks 7F and 7D, that is, block 7D is written AFTER block
             * 7F. Indeed, to be able to write block 7D, the bit b3 of byte IC_CFG2 (second byte
             * of block 7F) must be 1. A security measure has been introduce in
             * NfcATagComm.writeMultipleBlocks() such that this bit is ALWAYS 1. This way, by simply
             * writing block, 7F BEFORE block 7D, we are sure that we are ALLOWED to write block 7D.
             * See AMS AS3955 datasheet for more details.
             */
            List<MemoryBlock> configAsMemoryBlockReordered = new ArrayList<>();
            configAsMemoryBlockReordered.add(configAsMemoryBlock.get(NfcTagConfiguration.POS_BLOCK_7E));
            configAsMemoryBlockReordered.add(configAsMemoryBlock.get(NfcTagConfiguration.POS_BLOCK_7F));
            configAsMemoryBlockReordered.add(configAsMemoryBlock.get(NfcTagConfiguration.POS_BLOCK_7D));

            try {
                NfcATagComm.writeMultipleBlocks(tag, configAsMemoryBlockReordered);
                status = OperationStatus.WRITE_TAG_CONFIGURATION_SUCCESS;
            } catch (TagLostException e) {
                e.printStackTrace();
                status = OperationStatus.TAG_LOST;
            } catch (IOException e) {
                e.printStackTrace();
                status = OperationStatus.COMM_FAILURE;
            }
        } else {
            status = OperationStatus.TAG_CONFIGURATION_EMPTY;
        }

        Intent intent = new Intent(ACTION_WRITE_TAG_CONFIGURATION);
        intent.putExtra(EXTRA_NFC_COMM_STATUS, status);
        sendBroadcast(intent);
    }
}