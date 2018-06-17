package com.xsensio.nfcsensorcomm.nfc;

/**
 * Base Exception for all NFC-Communication-related exceptions
 */
public class NfcCommException extends Exception {

    public NfcCommException(String message) {
        super(message);
    }
}
