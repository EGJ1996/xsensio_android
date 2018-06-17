package com.xsensio.nfcsensorcomm;

import android.support.annotation.NonNull;

/**
 * Base interface for Views, see Model-View-Presenter architecture pattern
 */
public interface BaseView<T> {
    void setPresenter(@NonNull T presenter);

    void showToast(String message);
}