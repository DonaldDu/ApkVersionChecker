package com.example.apkversionchecker

import com.dhy.versionchecker.IUpdateSetting

class UpdateSetting : IUpdateSetting {
    companion object {
        var pass = false
    }

    override fun passIfAlreadyDownloadCompleted() = pass
}