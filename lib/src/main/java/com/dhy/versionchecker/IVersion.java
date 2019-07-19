package com.dhy.versionchecker;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

public interface IVersion extends Serializable {
    long getSize();

    @NonNull
    String getUrl();

    boolean isForceUpdate();

    @NonNull
    String getVersionName();

    int getVersionCode();

    boolean isNew();

    /**
     * android:maxLines="15"
     */
    @Nullable
    String getLog();
}
