package com.xsensio.nfcsensorcomm.mainactivity;

import android.content.Intent;

import com.xsensio.nfcsensorcomm.BasePresenter;
import com.xsensio.nfcsensorcomm.BaseView;

/**
 * Created by Michael Heiniger on 14.07.17.
 */

public interface MainActivityContract {
    interface View extends BaseView<Presenter> {

    }

    interface Presenter extends BasePresenter {
        void processReceivedIntent(Intent intent);
    }
}
