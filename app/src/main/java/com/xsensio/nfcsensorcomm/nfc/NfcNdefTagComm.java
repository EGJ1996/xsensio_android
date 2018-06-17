package com.xsensio.nfcsensorcomm.nfc;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.Ndef;
import android.util.Log;

import com.xsensio.nfcsensorcomm.R;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Enables to read and write data in the NFC Tag EEPROM using some NDEF messages.
 */
public class NfcNdefTagComm {

    private static final String TAG = "NfcNdefTagComm";

    private NfcNdefTagComm() {}

    /**
     * Read the NDEF content of the NFC tag, if any
     * @param tag
     * @return
     * @throws IOException
     * @throws FormatException
     * @throws TagNotWritableException
     */
    public static NdefMessage readNdefTag(Tag tag) throws IOException, FormatException, TagNotWritableException {

        if (tag == null) {
            throw new TagLostException("Tag instance is null");
        }

        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            throw new FormatException("Tag is not NDEF-formatted");
        }

        NdefMessage ndefMessage = null;

        ndef.connect();

        if (ndef.isWritable()) {
            ndefMessage = ndef.getNdefMessage();
        } else {
            throw new TagNotWritableException("NDEF Tag is not writable");
        }

        try {
            if (ndef != null) {
                ndef.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ndefMessage;
    }

    /**
     * Write the provided NDEF message to the NFC tag
     * @param tag
     * @param message
     * @throws IOException
     * @throws FormatException
     * @throws TagNotWritableException
     */
    public static void writeNdefTag(Tag tag, NdefMessage message) throws IOException, FormatException, TagNotWritableException {

        if (tag == null) {
            throw new TagLostException("Tag instance is null");
        }

        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            throw new FormatException("Tag is not NDEF-formatted");
        }

        if (message == null) {
            throw new FormatException("NDEF message provided is null.");
        }

        ndef.connect();

        if (ndef.isWritable()) {
            ndef.writeNdefMessage(message);
        } else {
            throw new TagNotWritableException("NDEF Tag is not writable");
        }

        try {
            if (ndef != null) {
                ndef.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}