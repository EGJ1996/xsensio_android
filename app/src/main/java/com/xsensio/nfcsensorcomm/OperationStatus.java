package com.xsensio.nfcsensorcomm;


/**
 * Defines status with user-friendly description for operations running in
 * background (IntentServices)
 */
public enum OperationStatus {
    READ_TAG_CONFIGURATION_SUCCESS("Tag configuration successfully read"),
    WRITE_TAG_CONFIGURATION_SUCCESS("Tag configuration successfully written"),
    TAG_CONFIGURATION_EMPTY("Tag configuration empty"),
    TAG_NOT_NDEF_WRITABLE("The tag cannot be written with a NDEF message"),
    READ_MEMORY_BLOCK_SUCCESS("Memory block successfully read (NFC-A)"),
    WRITE_MEMORY_BLOCK_SUCCESS("Memory block successfully written "),
    READ_MCU_SUCCESS("Data from MCU successfully read"),
    WRITE_MCU_SUCCESS("Data successfully sent to tag"),
    NO_VIRTUAL_SENSORS_SPECIFIED("No virtual sensors are specified."),
    READ_SENSORS_SUCCESS("Data from sensors successfully read"),
    READ_NDEF_SUCCESS("Tag successfully read (NDEF)"),
    WRITE_NDEF_SUCCESS("Tag successfully written (NDEF)"),
    TAG_LOST("Tag is out of reach"),
    FORMAT_ERROR ("NDEF message format error"),
    COMM_FAILURE("IO error"),
    MAX_NUM_RETRIES_REACHED("The maximum number of retries has been reached."),
    UNKNOWN_FAILURE("Unknown failure");

    private String mUserFriendlyStatus = "";

    OperationStatus(String userFriendStatus){
        mUserFriendlyStatus = userFriendStatus;
    }

    public String toUserFriendlyString(){
        return mUserFriendlyStatus;
    }

}