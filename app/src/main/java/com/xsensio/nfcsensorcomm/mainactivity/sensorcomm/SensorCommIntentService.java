package com.xsensio.nfcsensorcomm.mainactivity.sensorcomm;



import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.xsensio.nfcsensorcomm.OperationStatus;
import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.Utils;
import com.xsensio.nfcsensorcomm.mainactivity.Global;
import com.xsensio.nfcsensorcomm.model.PhoneMcuCommand;
import com.xsensio.nfcsensorcomm.model.virtualsensor.Sensor1Case2;
import com.xsensio.nfcsensorcomm.model.virtualsensor.Sensor3Case2;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensor;
import com.xsensio.nfcsensorcomm.nfc.ExtendedModeComm;
import com.xsensio.nfcsensorcomm.nfc.MaxNumRetriesReachedException;
import com.xsensio.nfcsensorcomm.sensorresult.NoVirtualSensorsSpecifiedException;
import com.xsensio.nfcsensorcomm.sensorresult.case1.VirtualSensorResultCase1Activity;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm.PhoneTagCommIntentService.EXTRA_NFC_COMM_STATUS;


public class SensorCommIntentService extends IntentService {

    private static final String TAG = "SensorCommIntentService";

    public static final String ACTION_READ_SENSORS = "com.xsensio.nfcsensorcomm.mainactivity.sensorcomm.action.READ_SENSORS";

    public static final String EXTRA_TAG = "com.xsensio.nfcsensorcomm.mainactivity.sensorcomm.extra.TAG";
    public static final String EXTRA_SENSOR_RESULTS = "com.xsensio.nfcsensorcomm.mainactivity.sensorcomm.extra.SENSOR_RESULTS";
    public static final String EXTRA_COMMAND = "com.xsensio.nfcsensorcomm.mainactivity.sensorcomm.extra.COMMAND";

    public SensorCommIntentService() {
        super("SensorCommIntentService");
    }

    public static void startActionReadSensors(Context context, Tag tag, PhoneMcuCommand command) {
        Intent intent = new Intent(context, SensorCommIntentService.class);
        intent.setAction(ACTION_READ_SENSORS);
        intent.putExtra(EXTRA_TAG, tag);
        intent.putExtra(EXTRA_COMMAND, (Parcelable) command);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_READ_SENSORS.equals(action)) {
                final Tag tag = intent.getParcelableExtra(EXTRA_TAG);
                final PhoneMcuCommand command = intent.getParcelableExtra(EXTRA_COMMAND);
                handleActionReadSensors(tag, command);
            }
        }
    }

    private void handleActionReadSensors(Tag tag, PhoneMcuCommand command) {


        boolean devMode = false;

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        int maxNumRetries = Integer.valueOf(settings.getString("sensor_reading_max_num_retries", getString(R.string.sensor_reading_max_num_retries_def_val)));
        //TODO read query that is sending
        Log.i(TAG, "SEND query for sensor data: " + Utils.bytesToHexString(command.getCommandAsBytes()));
        OperationStatus status;

        ExtendedModeComm comm = null;

        // Load virtual sensors for which data is expected
        ArrayList<VirtualSensor> virtualSensors = command.getVirtualSensorsToFill(this);

        try {

            if (virtualSensors == null || virtualSensors.size() == 0) {
                throw new NoVirtualSensorsSpecifiedException("No virtual sensors specified !");
            }

            if (!devMode) {
                comm = new ExtendedModeComm(tag, this);

                // Connect to the tag
                comm.connect();

                // Send command to MCU
                comm.write(command.getCommandAsBytes());

                // Wait for fresh data so that the timer of each virtual sensor are in the same initial conditions
                int numRetries = maxNumRetries;
                while (!comm.checkForData()) {
                    // Wait for fresh data
                    numRetries--;
                    if (numRetries == 0) {
                        throw new MaxNumRetriesReachedException("Maximum number of retries reached, still no fresh data");
                    }
                }
            }

            /**
             * Read data sent in response to the command from
             * the MCU until all data is received
             */


            Global.global_sensors = virtualSensors;

            for (VirtualSensor virtualSensor: virtualSensors) { // For each virtual sensor

                String taskDescription = "Receiving data for " + virtualSensor.getVirtualSensorDefinition().toUserFriendlyString();

                int numBytesToReceive = virtualSensor.getNumBytesToReceive();

                Log.i(TAG, "Expecting data for " + virtualSensor.getVirtualSensorDefinition().toUserFriendlyString() + " ...");
                Log.i(TAG, "Number of bytes expected is " + numBytesToReceive);

                Log.d("Tag", "Expecting data for " + virtualSensor.getVirtualSensorDefinition().toUserFriendlyString() + " ...");
                Log.d("Tag", "Number of bytes expected is " + numBytesToReceive);

                List<Byte> bytesReceived = new ArrayList<>();

                Calendar cal = Calendar.getInstance();
                long beginningTimestamp = cal.getTimeInMillis();

                if (!devMode) {
                    // Read results
                    //Todo: This is were bytes are received
                    bytesReceived = comm.read(numBytesToReceive+2, maxNumRetries, taskDescription);
//                    Log.d("Tag","Bytes Received = "+bytesReceived+"\n");
//                    Log.d("Tag","Size of received bytes = "+bytesReceived.size()+"\n");
                } else {
                    String filename = virtualSensor.getVirtualSensorDefinition().toString();
                   /*  Byte[] byteArray = new Byte[mReadoutsAsBytes.size()];
                    mReadoutsAsBytes.toArray(byteArray);

                    FileOutputStream fileOuputStream = null;
                    try {
                        File file = new File(Environment.getExternalStorageDirectory(), "NFCSensorComm/" + filename);
                        fileOuputStream = new FileOutputStream(file);
                        fileOuputStream.write(Utils.bytesWrapperToBytesPrim(byteArray));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            fileOuputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    */

                    File file = new File(Environment.getExternalStorageDirectory(), "NFCSensorComm/data/" + filename);

                    FileInputStream fis = null;

                    try {
                        byte[] bytes = FileUtils.readFileToByteArray(file);

                        for (int i = 0; i < bytes.length; i++) {
                            bytesReceived.add(bytes[i]);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                cal = Calendar.getInstance();
                long endTimestamp = cal.getTimeInMillis();

                long duration = endTimestamp-beginningTimestamp;

                // Save duration of the data reading process
                virtualSensor.setReadDataDuration(duration);

                Log.i(TAG, "Bytes received from MCU (in " + duration + " milliseconds): " + Utils.bytesToHexString(Utils.bytesListToArray(bytesReceived)));
                Log.d("Tag", "Bytes received from MCU (in " + duration + " milliseconds): " + Utils.bytesToHexString(Utils.bytesListToArray(bytesReceived)));

                // Save bytes received into virtual sensor
                virtualSensor.saveReadoutBytesReceived(bytesReceived);
            }

            status = OperationStatus.READ_SENSORS_SUCCESS;

        } catch (TagLostException e) {
            Log.d("Tag","Tag Lost exception\n");
            e.printStackTrace();
            status = OperationStatus.TAG_LOST;
            Global.nfc_set = false;
        } catch (IOException e) {
            e.printStackTrace();
            status = OperationStatus.COMM_FAILURE;
            Global.nfc_set = false;
        } catch (NoVirtualSensorsSpecifiedException e) {
            e.printStackTrace();
            status = OperationStatus.NO_VIRTUAL_SENSORS_SPECIFIED;
        } catch (MaxNumRetriesReachedException e) {
            e.printStackTrace();
            status = OperationStatus.MAX_NUM_RETRIES_REACHED;
        } finally {
            if (!devMode) {
                if (comm != null) {
                    try {
                        comm.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);

        Intent intent = new Intent(ACTION_READ_SENSORS);
        intent.putParcelableArrayListExtra(VirtualSensorResultCase1Activity.EXTRA_VIRTUAL_SENSOR, virtualSensors);
        intent.putExtra(SensorCommIntentService.EXTRA_COMMAND, (Parcelable) command);
        intent.putExtra(EXTRA_NFC_COMM_STATUS, status);
        manager.sendBroadcast(intent);
    }
}