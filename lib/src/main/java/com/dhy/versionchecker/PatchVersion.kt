package com.dhy.versionchecker

internal class PatchVersion(patchFileUrl: String) {
    companion object {
        //差分包文件名：bsPatch_{MD5}_{旧代码号}v{新代码号}：54621B46C1664DB5BA7127D8F22AFF00_258v300.apk
        private val reg by lazy { "bsPatch_([0-9A-Z]{32})_([0-9]+)v([0-9]+)\\.apk".toRegex() }
        fun invalidFormat(patchFileUrl: String?): Boolean {
            if (patchFileUrl.isNullOrEmpty()) return true
            return reg.find(patchFileUrl) == null
        }
    }

    val md5: String
    val fileName: String
    val oldVersion: Int
    val newVersion: Int

    init {
        val m = reg.find(patchFileUrl)!!
        fileName = m.groupValues[0]
        md5 = m.groupValues[1]
        oldVersion = m.groupValues[2].toInt()
        newVersion = m.groupValues[3].toInt()
    }
}