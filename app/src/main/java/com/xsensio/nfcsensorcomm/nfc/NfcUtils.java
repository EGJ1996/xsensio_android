package com.xsensio.nfcsensorcomm.nfc;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Parcelable;
import android.util.Log;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;


/**
 * Helper functions for NFC-related operations
 */
public final class NfcUtils {

    private static final String TAG = "NfcUtils";

    private NfcUtils() {}

    /**
     * Translate TNF of NDEF records into human-readable String
     * @param tnf
     * @return
     */
    public static String translateTnfToString(short tnf) {

        HashMap<Short, String> translationTable = new HashMap<Short, String>();
        translationTable.put(NdefRecord.TNF_ABSOLUTE_URI, "Absolute URI");
        translationTable.put(NdefRecord.TNF_EMPTY, "Empty");
        translationTable.put(NdefRecord.TNF_EXTERNAL_TYPE, "External type");
        translationTable.put(NdefRecord.TNF_MIME_MEDIA, "MIME media");
        translationTable.put(NdefRecord.TNF_UNCHANGED, "Unchanged");
        translationTable.put(NdefRecord.TNF_UNKNOWN, "Unknown");
        translationTable.put(NdefRecord.TNF_WELL_KNOWN, "Well known");

        return translationTable.get(tnf);
    }

    public static String translateRecordTypeToString(byte[] type) {

        HashMap<String, String> translationTable = new HashMap<String, String>();
        translationTable.put(new String(NdefRecord.RTD_ALTERNATIVE_CARRIER), "Alternative carrier");
        translationTable.put(new String(NdefRecord.RTD_HANDOVER_CARRIER), "Handover carrier");
        translationTable.put(new String(NdefRecord.RTD_HANDOVER_REQUEST), "Handover request");
        translationTable.put(new String(NdefRecord.RTD_HANDOVER_SELECT), "Handover select");
        translationTable.put(new String(NdefRecord.RTD_SMART_POSTER), "Smart poster");
        translationTable.put(new String(NdefRecord.RTD_TEXT), "Text");
        translationTable.put(new String(NdefRecord.RTD_URI), "URI");

        return translationTable.get(new String(type));
    }

    /**
     * Extract text-formattable content from a NDEF NFC Tag, if any. Only the content of the first
     * NDEF message is returned.
     * @param intent received by Android OS due to the NFC event
     * @return the text-formattable content of the FIRST NDEF message.
     */
    public static String getTextFormattableNdefMessageContent(Intent intent) {

        String action = intent.getAction();

        String tagContent = "";

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Ndef ndef = Ndef.get(tag);

            if (ndef != null) {

                Parcelable[] rawMessages =
                        intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

                if (rawMessages != null) {

                    NdefMessage[] messages = new NdefMessage[rawMessages.length];
                    Log.d(TAG, "There are " + rawMessages.length + " messages in the received intent.");

                    // Select first NDEF message of the Tag
                    NdefMessage firstNdefMessage = (NdefMessage) rawMessages[0];

                    // Extract the content if text-formattable
                    tagContent = getTextFormatableContentFromNdefMessage(firstNdefMessage);

                    // For info, output the metadata of all the NDEF messages contained in the Tag
                    for (int i = 0; i < rawMessages.length; i++) {
                        messages[i] = (NdefMessage) rawMessages[i];

                        Log.i(TAG, "-------- New NDEF Message --------");
                        Log.i(TAG, "Message " + i + " contains " + messages[i].getRecords().length + " records.");
                        for (NdefRecord record : messages[i].getRecords()) {

                            Log.i(TAG, "---- New record ----");

                            byte[] recordId = record.getId();
                            Log.i(TAG, "Record id: " + new String(recordId, Charset.forName("UTF-8")));

                            String tnfAsString = NfcUtils.translateTnfToString(record.getTnf());
                            Log.i(TAG, "Message TNF: " + tnfAsString);

                            String typeAsString = NfcUtils.translateRecordTypeToString(record.getType());
                            Log.i(TAG, "Record type: " + typeAsString);
                            if ("URI".equals(typeAsString)) {
                                Log.i(TAG, "URI: " + record.toUri().toString());
                            }
                            Log.i(TAG, "Record MIME type: " + record.toMimeType());

                            byte[] payload = record.getPayload();
                            if (Arrays.equals(record.getType(), NdefRecord.RTD_TEXT)) {
                                Log.i(TAG, "Message payload: " + new String(payload, Charset.forName("UTF-8")));
                            }
                            Log.i(TAG, "Message payload: " + payload.toString());

                            Log.i(TAG, "---- End record ----");
                        }
                    }
                    Log.i(TAG, "-------- End message --------");
                }
            }
        }

        return tagContent;
    }

    /**
     * Extract text-formatable (Plain text or URI) content from the provided NDEF message
     * @param message
     * @return
     */
    public static String getTextFormatableContentFromNdefMessage(NdefMessage message) {

        String tagContent = "";

        for (NdefRecord record : message.getRecords()) {

            String tnfAsString = NfcUtils.translateTnfToString(record.getTnf());

            if ("Well known".equals(tnfAsString)) {

                String typeAsString = NfcUtils.translateRecordTypeToString(record.getType());

                if ("Text".equals(typeAsString)) {
                    tagContent += new String(record.getPayload(), Charset.forName("UTF-8")).substring(3); // remove locale
                    tagContent += "\n";
                } else if ("URI".equals(typeAsString)) {
                    tagContent += record.toUri().toString();
                    tagContent += "\n";
                }
            }
        }
        return tagContent;
    }
}