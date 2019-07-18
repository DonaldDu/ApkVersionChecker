package com.dhy.versionchecker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

public interface IVersion extends Serializable {
    @NonNull
    String getAppName(@NonNull Context context);

    long getSize();

    @NonNull
    String getUrl();

    boolean isForceUpdate();

    @NonNull
    String getVersionName();

    int getVersionCode();

    boolean isNew();

    boolean passIfAlreadyDownloadCompleted();

    /**
     * android:maxLines="15"
     */
    @Nullable
    String getLog();
}
