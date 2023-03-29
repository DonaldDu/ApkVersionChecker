package com.dhy.avc.format

class PatchVersion {
    companion object {
        private const val keyMd5 = "Md5"
        private const val keyOldVersion = "ov"
        private const val keyNewVersion = "nv"

        private const val keyOldVersionName = "ovn"
        private const val keyNewVersionName = "nvn"

        //差分包文件名：{oldVersionName}v{newVersionName}-{newApkMd5}-{oldVersionCode}v{newVersionCode}：1.0v1.1-54621B46C1664DB5BA7127D8F22AFF00-258v300.bsPatch.apk
        private val reg by lazy { "[^\\da-zA-Z]?(?<$keyOldVersionName>[\\d.]+)v(?<$keyNewVersionName>[\\d.]+)-(?<$keyMd5>[\\da-zA-Z]{32})-(?<$keyOldVersion>\\d+)v(?<$keyNewVersion>\\d+)\\.bsPatch\\.apk".toRegex() }
        fun invalidFormat(patchFileUrl: String?): Boolean {
            if (patchFileUrl.isNullOrEmpty()) return true
            return reg.find(patchFileUrl) == null
        }

        fun isValidFormat(patchFileUrl: String?): Boolean {
            return !invalidFormat(patchFileUrl)
        }

        @JvmStatic
        fun format(v: PatchVersion): String {
            return String.format("%sv%s-%s-%dv%d.bsPatch.apk", v.oldVersionName, v.newVersionName, v.newApkMd5?.lowercase(), v.oldVersion, v.newVersion)
        }

        @JvmStatic
        fun parse(patchFileUrl: String): PatchVersion {
            return PatchVersion().apply {
                val m = reg.find(patchFileUrl)
                newApkMd5 = m?.groups?.get(keyMd5)?.value
                oldVersion = m?.groups?.get(keyOldVersion)?.value?.toInt()
                newVersion = m?.groups?.get(keyNewVersion)?.value?.toInt()

                oldVersionName = m?.groups?.get(keyOldVersionName)?.value
                newVersionName = m?.groups?.get(keyNewVersionName)?.value

                fileName = format(this)
            }
        }
    }

    var newApkMd5: String? = null

    var oldVersion: Int? = null
    var newVersion: Int? = null
    var oldVersionName: String? = null
    var newVersionName: String? = null

    var fileName: String? = null

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