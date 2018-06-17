package com.xsensio.nfcsensorcomm.mainactivity.phonemcucomm;

import com.xsensio.nfcsensorcomm.mainactivity.CommContract;

/**
 * Created by Michael Heiniger on 13.07.17.
 */

public interface PhoneMcuCommContract {

    interface View extends CommContract.View {
        void setReceivedData(byte[] data);

        /**
         * Presenter notify the View that the Extended mode has been updated
         * @param isChecked
         */
        void updateExtendedMode(boolean isChecked);
    }

    interface Presenter extends CommContract.Presenter {

        /**
         * View asks the Presenter to read new data from the MCU
         */
        void readData();

        /**
         * View asks the Presenter to write new data to the MCU
         * @param data to write to the MCU
         */
        void writeData(byte[] data);

        /**
         * View asks the Presenter to change the state of the Extended mode on the NFC Tag
         * @param isChecked
         */
        void setExtendedMode(boolean isChecked);
    }
}