package com.xsensio.nfcsensorcomm.nfc;

import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.NfcA;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.xsensio.nfcsensorcomm.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Enables Extended mode communication between the Phone and the MCU.
 *
 * Note: The user of this class must instantiate it, call the {@link #connect()} function
 * prior to reading of writing data and should call {@link #close()} when all communications
 * have been done to release resources. These functions are not called automatically because
 * it has been noticed that if {@link #close()} is called after writing data to the tag buffer
 * (for the MCU) and before the MCU has read the data, the buffer is cleared and thus the MCU
 * read only zeros.
 *
 */
public final class ExtendedModeComm {

    private static final String TAG = "ExtendedModeComm";

    private static final int NUM_USEFUL_BYTE_PER_READ = 12;
    public static final int NUM_BLOCKS_TO_WRITE = 4;
    public static final int NUM_BYTE_PER_BLOCK = 4;
    public static final int TOTAL_NUM_BYTES = NUM_BLOCKS_TO_WRITE * NUM_BYTE_PER_BLOCK;

    public static final String ACTION_EXT_MOD_READ_PROGRESS_FEEDBACK = "com.xsensio.nfcsensorcomm.mainactivity.sensorcomm.action.EXT_MOD_READ_PROGRESS_FEEDBACK";
    public static final String EXTRA_EXT_MOD_TASK_COMPLETION_RATIO = "com.xsensio.nfcsensorcomm.mainactivity.sensorcomm.extra.COMPLETION_RATIO";
    public static final String EXTRA_EXT_MOD_TASK_DESCRIPTION = "com.xsensio.nfcsensorcomm.mainactivity.sensorcomm.extra.TASK_DESCRIPTION";

    /**
     *  Address of the first block to read: the NFC tag returns the following 3 blocks
     *  as well, that is, 4 blocks in total, starting from address specified.
     *  In Extended mode, the address of the first block if 0xFC.
     */
    public static final byte FIRST_BLOCK_ADDRESS_FOR_READ = (byte) 0xFC;

    // Addresses of blocks to write on. Write on the whole buffer in Extended mode.
    public static final byte[] BLOCK_ADDRESSES_FOR_WRITE = new byte[] {(byte) 0xfc, (byte) 0xfd, (byte) 0xfe, (byte) 0xff};

    private final NfcA mNfcA;

    /** Used to send feedback on the progress */
    private final Context mContext;

    public ExtendedModeComm(Tag tag, Context context) throws TagLostException {

        mContext = context;

        if (tag != null) {
            mNfcA = NfcA.get(tag);
        } else {
            throw new TagLostException();
        }
    }

    /**
     * Establishes connection with NFC Tag
     * @throws IOException
     */
    public void connect() throws IOException {
        mNfcA.connect();
    }

    /**
     * Close connection with NFC Tag and release resources
     * @throws IOException
     */
    public void close() throws IOException {
        mNfcA.close();
    }

    /**
     * Check if there is fresh data to consume
     * @return true if there is fresh data to consume, false otherwise
     */
    public boolean checkForData() throws MaxNumRetriesReachedException, IOException {
        if (mNfcA.isConnected()) {

                byte[] currentReceivedBytes = Utils.createByteArrayWithzeros(NUM_BYTE_PER_BLOCK);

                currentReceivedBytes = mNfcA.transceive(new byte[]{
                        (byte) 0x30,  // READ
                        FIRST_BLOCK_ADDRESS_FOR_READ
                });

                // Extract last two bits of last byte.received
                String lastTwoBitsOfFlagByte = Utils.byteToHexString((byte) (currentReceivedBytes[15] & 0x03));

                // If the last two bits of the last received byte are 01, fresh data have been received
                return "01".equals(lastTwoBitsOfFlagByte);
        } else {
            throw new TagLostException("Not connected to tag !");
        }
    }

    /**
     * Read a specified amount of bytes in at most numRetries or fails.
     * Note: One must first call connect() to establish the communication with the NFC tag before
     * calling this function. One should call close() when NFC communication is no more needed.
     *
     * @param numByteToRead number of bytes to read coming from the MCU
     * @param numRetries    throw ... exception when reaches 0.
     * @return a List of Byte objects
     */
    public List<Byte> read(int numByteToRead, int numRetries, String taskDescription) throws MaxNumRetriesReachedException, IOException {

        List<Byte> allReceivedBytes = new ArrayList<>();

        if (mNfcA.isConnected()) {

            while (allReceivedBytes.size() < numByteToRead) {

                byte[] currentReceivedBytes = Utils.createByteArrayWithzeros(NUM_BYTE_PER_BLOCK);

                /**
                 * Last two bits of last byte.
                 * Should be initialized at a value different than 01
                 */
                String lastTwoBitsOfFlagByte = "00";

                // While the last two bits of the last received byte are NOT 01, keep reading
                while (!"01".equals(lastTwoBitsOfFlagByte)) {

                    if (numRetries != 0) {
                        currentReceivedBytes = mNfcA.transceive(new byte[]{
                                (byte) 0x30,  // READ
                                FIRST_BLOCK_ADDRESS_FOR_READ
                        });

                        //Log.d(TAG, "Received bytes: " + Utils.bytesToHexString(currentReceivedBytes));

                        // Extract last two bits of last byte.received
                        lastTwoBitsOfFlagByte = Utils.byteToHexString((byte) (currentReceivedBytes[15] & 0x03));
                        //Log.d(TAG, "Last two bits of Flag byte: " + lastTwoBitsOfFlagByte);

                        numRetries--;
                    } else {
                        throw new MaxNumRetriesReachedException("Maximum number of retries reached !");
                    }
                }

                /**
                 *  Read at most NUM_USEFUL_BYTE_PER_READ bytes (i.e. 12 bytes) since in total we have NUM_BYTES_PER_RESULT which might not be a multiple of 12
                 * (--> the first 12 bytes come from the MCU, the next 4 come from the tag)
                 */
                int numIterations = Math.min(numByteToRead - allReceivedBytes.size(), NUM_USEFUL_BYTE_PER_READ);
                for (int i = 0; i < numIterations; i++) {
                    allReceivedBytes.add(currentReceivedBytes[i]);
                }

                // Notify the BroadcastReceiver of the progress...
                int completionRatio = (int)((double) allReceivedBytes.size() / (double) numByteToRead * 100);
                LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
                Intent intent = new Intent(ACTION_EXT_MOD_READ_PROGRESS_FEEDBACK);
                intent.putExtra(EXTRA_EXT_MOD_TASK_DESCRIPTION, taskDescription);
                intent.putExtra(EXTRA_EXT_MOD_TASK_COMPLETION_RATIO, completionRatio);
                manager.sendBroadcast(intent);

                /**
                 * If actual data have been received, we send write
                 * WRITE on block FFh to indicate to the MCU that it can fill it with new data
                 */
                byte pageToWrite = (byte) 0xff;
                byte[] resultOfWrite = mNfcA.transceive(new byte[]{
                        (byte) 0xA2,  // WRITE
                        pageToWrite,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
                });
                //Log.d(TAG, "Result of WRITE: " + Utils.bytesToHexString(resultOfWrite));
            }
        } else {
            throw new TagLostException("Not connected to tag !");
        }

        return allReceivedBytes;
    }

    /**
     * Write 16 bytes of data per call
     * @param data byte-array containing 16 bytes
     * @throws IOException
     */
    public void write(byte[] data) throws IOException {
        if (data.length < TOTAL_NUM_BYTES) {
            throw new IllegalArgumentException("Data must contain " + TOTAL_NUM_BYTES + " bytes per write operation.");
        }

        if (mNfcA.isConnected()) {

            // Write 4 blocks of 4 bytes each
            for (int i = 0; i < NUM_BLOCKS_TO_WRITE; i++) {
                /*Log.d(TAG, "Write: "
                        + Utils.byteToHexString(data[4*i]) + " "
                        + Utils.byteToHexString(data[4*i+1]) + " "
                        + Utils.byteToHexString(data[4*i+2]) + " "
                        + Utils.byteToHexString(data[4*i+3])
                        + " at address " + Utils.byteToHexString(BLOCK_ADDRESSES_FOR_WRITE[i]));
                */
                byte[] resultOfWrite = mNfcA.transceive(new byte[] {
                        (byte)0xA2,  // WRITE
                        BLOCK_ADDRESSES_FOR_WRITE[i],
                        data[4*i], data[4*i+1], data[4*i+2], data[4*i+3]
                });
                Log.d(TAG, "Result of WRITE: " + Utils.bytesToHexString(resultOfWrite));
            }
        } else {
            throw new TagLostException("Not connected to tag !");
        }
    }
}