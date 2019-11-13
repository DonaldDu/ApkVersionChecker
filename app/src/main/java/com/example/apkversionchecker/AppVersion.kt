package com.example.apkversionchecker

import com.dhy.versionchecker.IVersion

class AppVersion : IVersion {
    override fun getPatchUrl(): String? {
        return "http://www.wwvas.com/apk/32cfe55d85a28dca0c15689c7dddb4a3-20v208.patch.apk"
    }

    companion object {
        var forceUpdate = false
    }

    override fun getVersionCode() = 0

    override fun getSize() = 76753135L

    override fun getFullUrl(): String {
        return "http://www.wwvas.com/apk/PUCHE-debug_v3.2.1.1.208-c208_master-d695845.apk"
    }

    override fun isForceUpdate() = forceUpdate

    override fun getVersionName() = "1.0.0"

    override fun isNew() = true
    var msg: String? = null
    override fun getLog(): String {
        return msg ?: (1..9).toList().joinToString("\n") {
            "$it. 修复第${it}个bug"
        }
    }
}