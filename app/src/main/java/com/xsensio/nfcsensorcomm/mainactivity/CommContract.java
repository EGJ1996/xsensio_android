package com.xsensio.nfcsensorcomm.mainactivity;

import android.nfc.Tag;

import com.xsensio.nfcsensorcomm.BasePresenter;
import com.xsensio.nfcsensorcomm.BaseView;
import com.xsensio.nfcsensorcomm.model.NfcTagConfiguration;

/**
 * Interface for common functions for MainActivity Fragments.
 */
public interface CommContract {

    interface View extends BaseView<Presenter> {

    }

    interface Presenter extends BasePresenter {

        /**
         * Callback implemented by the MainActivity Fragments and called when NFC Tag is detected
         * by Android
         * @param tag
         * @param config
         */
        void nfcTagDetected(Tag tag, NfcTagConfiguration config);
    }
}
