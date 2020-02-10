package com.example.apkversionchecker

import com.dhy.versionchecker.IVersion

class AppVersion : IVersion {
    override fun getPatchSize(): Long {
        return size
    }

    override fun getPatchUrl(): String? {
        return null
    }

    override fun setPatchSize(size: Long) {

    }

    companion object {
        var forceUpdate = false
    }

    override fun getVersionCode() = 0
    override fun setPatchUrl(url: String?) {

    }

    override fun getSize() = 4882533L

    override fun getUrl(): String {
        return "http://www.wwvas.com/apk/ApkVersionChecker.apk"
    }

    override fun getCurrentVersionCode(): Int {
        return BuildConfig.VERSION_CODE
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