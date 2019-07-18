package com.example.apkversionchecker

import android.content.Context
import com.dhy.versionchecker.IVersion

class AppVersion : IVersion {
    override fun passIfAlreadyDownloadCompleted() = pass

    companion object {
        var forceUpdate = false
        var pass = false
    }

    override fun getVersionCode() = 0

    override fun getAppName(context: Context): String {
        return context.getString(R.string.app_name)
    }

    override fun getSize() = 76753135L

    override fun getUrl(): String {
        return "http://p.gdown.baidu.com/67aa8bfc05f2264ffd4f764fa27f5c46b1be3374fb5c806139ec2ba35a1080526f1ab37348686d745241fb6b677a0a8afd1c644211e33159dcf03cc3bded49075b7ad65738c6a7bf9e2dd53a207ec408b119e0badbb505fa0cc91f2e4ba868bd01cca4d0a175cc82ae846498400dc3341538926fee90cd007f41ae4ee098f1b9f2d9856fc2a617ce135bd7526722336a1afded02e8d64ca58fb68c580be2b3220559e5770e130c82c9f1e4ea5211dcbac082ad73f03962854107df5cd3c8bea1"
    }

    override fun isForceUpdate() = forceUpdate

    override fun getVersionName() = "1.0.0"

    override fun isNew() = true

    override fun getLog() = "1. 修复一些bug\n2. 修复一些bug"
}