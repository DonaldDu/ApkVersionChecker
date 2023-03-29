package com.example.apkversionchecker.data;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dhy.versionchecker.IVersion;
import com.example.apkversionchecker.BuildConfig;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AppVersion implements Serializable, IVersion {
    @SerializedName("packagename")
    public String packageName;
    @SerializedName("versioncode")
    private int versionCode;

    @SerializedName("versiontype")
    private String type;
    @SerializedName("versionname")
    private String versionName;
    @SerializedName("forceupdate")
    private int forceUpdate;

    @Override
    public boolean isForceUpdate() {
        return forceUpdate == 1;
    }

    @Override
    public long getSize() {
        return size;
    }

    @NonNull
    @Override
    public String getUrl() {
        return url;
    }

    public String patchUrl;

    @Nullable
    @Override
    public String getPatchUrl() {
        return patchUrl;
    }

    @Override
    public void setPatchUrl(@Nullable String url) {
        patchUrl = url;
    }

    public long patchSize;

    @Override
    public long getPatchSize() {
        return patchSize;
    }

    @Override
    public void setPatchSize(@NonNull Long size) {
        patchSize = size;
    }

    @NonNull
    public String getVersionName() {
        if (versionName != null) {
            return versionName;
        } else return "";
    }

    @Override
    public int getNewVersionCode() {
        return versionCode;
    }

    @Override
    public int getOldVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    @Override
    public String getOldVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public boolean isNew() {
        return BuildConfig.VERSION_CODE < versionCode;
    }

    @Override
    public String getLog() {
        return TextUtils.isEmpty(message) ? "" : message;
    }

    private String url;
    /**
     * 软件大小，单位 byte
     */
    @SerializedName("packagesize")
    public long size;
    private String message;
}
