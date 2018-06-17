package com.xsensio.nfcsensorcomm.nfc;

/**
 * Exception representing the event that the NFC Tag is NOT writable using the App
 */
public class TagNotWritableException extends NfcCommException {

    public TagNotWritableException(String message) {
        super(message);
    }

}
