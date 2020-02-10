package com.dhy.versionchecker;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

public interface IVersion extends Serializable {
    long getSize();

    @NonNull
    String getUrl();

    @Nullable
    String getPatchUrl();

    void setPatchUrl(@Nullable String url);

    long getPatchSize();

    void setPatchSize(long size);

    boolean isForceUpdate();

    @NonNull
    String getVersionName();

    int getVersionCode();

    int getCurrentVersionCode();

    boolean isNew();

    /**
     * android:maxLines="15"
     */
    @Nullable
    String getLog();
}
