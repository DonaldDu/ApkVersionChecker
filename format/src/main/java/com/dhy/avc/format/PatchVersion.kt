package com.dhy.avc.format

class PatchVersion(patchFileUrl: String) {
    companion object {
        private const val keyMd5 = "Md5"
        private const val keyOldVersion = "oldVersion"
        private const val keyNewVersion = "newVersion"

        //差分包文件名：{newApkMd5}-{oldVersionCode}v{newVersionCode}：54621B46C1664DB5BA7127D8F22AFF00-258v300.bsPatch.apk
        private val reg by lazy { "[^0-9a-zA-Z]?(?<$keyMd5>[0-9a-zA-Z]{32})-(?<$keyOldVersion>[0-9]+)v(?<$keyNewVersion>[0-9]+)\\.bsPatch\\.apk".toRegex() }
        fun invalidFormat(patchFileUrl: String?): Boolean {
            if (patchFileUrl.isNullOrEmpty()) return true
            return reg.find(patchFileUrl) == null
        }

        fun isValidFormat(patchFileUrl: String?): Boolean {
            return !invalidFormat(patchFileUrl)
        }

        @JvmStatic
        fun format(newApkMd5: String, oldVersion: Int, newVersion: Int): String {
            return String.format("%s-%dv%d.bsPatch.apk", newApkMd5.lowercase(), oldVersion, newVersion)
        }
    }

    val newApkMd5: String
    val fileName: String
    val oldVersion: Int
    val newVersion: Int

    init {
        val m = reg.find(patchFileUrl)!!
        newApkMd5 = m.groups[keyMd5]!!.value
        oldVersion = m.groups[keyOldVersion]!!.value.toInt()
        newVersion = m.groups[keyNewVersion]!!.value.toInt()
        fileName = format(newApkMd5, oldVersion, newVersion)
    }

    constructor(newApkMd5: String, oldVersion: Int, newVersion: Int) : this(format(newApkMd5, oldVersion, newVersion))

    fun matchMd5(newApkMd5: String): Boolean {
        return newApkMd5.equals(this.newApkMd5, ignoreCase = true)
    }

    override fun hashCode(): Int {
        return fileName.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other?.hashCode() == hashCode()
    }
}