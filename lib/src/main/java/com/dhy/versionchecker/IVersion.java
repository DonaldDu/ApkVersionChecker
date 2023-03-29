package com.dhy.versionchecker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

public interface IVersion extends Serializable {
    long getSize();

    @NonNull
    String getUrl();

    @Nullable
    String getPatchUrl();

    void setPatchUrl(@Nullable String url);

    long getPatchSize();

    void setPatchSize(@NonNull Long size);

    boolean isForceUpdate();

    @NonNull
    String getVersionName();

    int getNewVersionCode();

    int getOldVersionCode();

    String getOldVersionName();

    boolean isNew();

    /**
     * android:maxLines="15"
     */
    @Nullable
    String getLog();
}
