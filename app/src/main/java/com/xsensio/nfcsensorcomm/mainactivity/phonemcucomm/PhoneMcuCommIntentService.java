package com.xsensio.nfcsensorcomm.mainactivity.phonemcucomm;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.nfc.Tag;
import android.nfc.TagLostException;

import com.xsensio.nfcsensorcomm.OperationStatus;
import com.xsensio.nfcsensorcomm.Utils;
import com.xsensio.nfcsensorcomm.nfc.ExtendedModeComm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm.PhoneTagCommIntentService.EXTRA_NFC_COMM_STATUS;


public class PhoneMcuCommIntentService extends IntentService {

    public static final String ACTION_READ = "com.xsensio.nfcsensorcomm.mainactivity.phonemcucomm.action.READ";
    public static final String ACTION_WRITE = "com.xsensio.nfcsensorcomm.mainactivity.phonemcucomm.action.WRITE";

    public static final String EXTRA_TAG = "com.xsensio.nfcsensorcomm.mainactivity.phonemcucomm.extra.TAG";
    public static final String EXTRA_DATA = "com.xsensio.nfcsensorcomm.mainactivity.phonemcucomm.extra.DATA";

    public PhoneMcuCommIntentService() {
        super("PhoneMcuCommIntentService");
    }

    public static void startActionRead(Context context, Tag tag) {
        Intent intent = new Intent(context, PhoneMcuCommIntentService.class);
        intent.setAction(ACTION_READ);
        intent.putExtra(EXTRA_TAG, tag);
        context.startService(intent);
    }

    public static void startActionWrite(Context context, Tag tag, byte[] data) {
        Intent intent = new Intent(context, PhoneMcuCommIntentService.class);
        intent.setAction(ACTION_WRITE);
        intent.putExtra(EXTRA_TAG, tag);
        intent.putExtra(EXTRA_DATA, data);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_READ.equals(action)) {
                final Tag tag = intent.getParcelableExtra(EXTRA_TAG);
                handleActionRead(tag);
            } else if (ACTION_WRITE.equals(action)) {
                final Tag tag = intent.getParcelableExtra(EXTRA_TAG);
                final byte[] data = intent.getByteArrayExtra(EXTRA_DATA);
                handleActionWrite(tag, data);
            }
        }
    }

    private void handleActionRead(Tag tag) {

        OperationStatus status = null;

        ExtendedModeComm comm = null;

        List<Byte> bytesReadList = new ArrayList<>();

        try {

            comm = new ExtendedModeComm(tag, this);

            comm.connect();
            int maxNumRetries = 200; //TODO TO PUT INTO SETTINGS
            bytesReadList = comm.read(ExtendedModeComm.TOTAL_NUM_BYTES, maxNumRetries, "");

            status = OperationStatus.READ_MCU_SUCCESS;

        } catch (TagLostException e) {
            e.printStackTrace();
            status = OperationStatus.TAG_LOST;
        } catch (IOException e) {
            e.printStackTrace();
            status = OperationStatus.COMM_FAILURE;
        } catch (Exception e) {
            e.printStackTrace();
            status = OperationStatus.UNKNOWN_FAILURE;
        } finally {
            if (comm != null) {
                try {
                    comm.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Intent intent = new Intent(ACTION_READ);
        intent.putExtra(EXTRA_NFC_COMM_STATUS, status);
        intent.putExtra(EXTRA_DATA, Utils.bytesListToArray(bytesReadList));
        sendBroadcast(intent);
    }

    private void handleActionWrite(Tag tag, byte[] data) {

        OperationStatus status = null;

        ExtendedModeComm comm = null;

        try {

            comm = new ExtendedModeComm(tag, this);

            comm.connect();

            comm.write(data);

            status = OperationStatus.WRITE_MCU_SUCCESS;

        } catch (TagLostException e) {
            e.printStackTrace();
            status = OperationStatus.TAG_LOST;
        } catch (IOException e) {
            e.printStackTrace();
            status = OperationStatus.COMM_FAILURE;
        } catch (Exception e) {
            e.printStackTrace();
            status = OperationStatus.UNKNOWN_FAILURE;
        } finally {
            if (comm != null) {
                try {
                    comm.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Intent intent = new Intent(ACTION_WRITE);
        intent.putExtra(EXTRA_NFC_COMM_STATUS, status);
        sendBroadcast(intent);
    }
}