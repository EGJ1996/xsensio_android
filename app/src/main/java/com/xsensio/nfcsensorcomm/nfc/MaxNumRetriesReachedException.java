package com.xsensio.nfcsensorcomm.nfc;


/**
 * Exception representing the event that a certain number of retries of NFC communication
 * has been reached and that it is unusual.
 */
public class MaxNumRetriesReachedException extends NfcCommException {
    public MaxNumRetriesReachedException(String message) {
        super(message);
    }
}
