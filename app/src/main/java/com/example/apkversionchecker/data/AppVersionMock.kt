package com.example.apkversionchecker.data

import com.example.apkversionchecker.BuildConfig

class AppVersionMock : AppVersion() {
    companion object {
        var forceUpdate = false
    }

    override fun getPatchSize(): Long {
        return size
    }

    override fun getPatchUrl(): String? {
        return null
    }

    override fun setPatchSize(size: Long) {
    }

    override fun getNewVersionCode() = 0
    override fun setPatchUrl(url: String?) {
    }

    override fun getSize() = 4882533L

    override fun getUrl(): String {
        return "http://apk.wwvas.com:10000/apk/com.example.apkversionchecker-v1.0_c20_t0.apk"
    }

    override fun getOldVersionCode(): Int {
        return BuildConfig.VERSION_CODE
    }

    override fun isForceUpdate() = forceUpdate

    override fun getVersionName() = "1.0.0"

    override fun isNew() = true
    private val logMsg: String
        get() {
            return (1..8).toList().joinToString("\n") { "$it. 修复第${it}个bug" } +
                    "\n[点击查看详情](https://juejin.cn/user/817692382075102/posts)"
        }
    var msg: String? = null
    override fun getLog(): String {
        return msg ?: logMsg
    }
}