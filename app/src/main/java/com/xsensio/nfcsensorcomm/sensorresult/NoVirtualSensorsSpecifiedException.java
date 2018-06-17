package com.xsensio.nfcsensorcomm.sensorresult;

import com.xsensio.nfcsensorcomm.nfc.NfcCommException;

/**
 * Created by Michael Heiniger on 31.07.17.
 */

public class NoVirtualSensorsSpecifiedException extends NfcCommException {

    public NoVirtualSensorsSpecifiedException(String message) {
        super(message);
    }
}
