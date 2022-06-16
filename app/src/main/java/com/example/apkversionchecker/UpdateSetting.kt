package com.example.apkversionchecker

import com.dhy.versionchecker.IUpdateSetting

class UpdateSetting : IUpdateSetting {
    companion object {
        var pass = false
    }

    override fun mdLinkTextColorRes(): Int = R.color.colorPrimary
    override fun passIfAlreadyDownloadCompleted() = pass
}