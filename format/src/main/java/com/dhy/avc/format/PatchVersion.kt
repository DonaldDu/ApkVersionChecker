package com.dhy.avc.format

class PatchVersion {
    companion object {
        private const val versionReg = "[\\d.@]+"

        //差分包文件名：{oldVersionName}_{oldVersionCode}v{newVersionName}_{newVersionCode}-{newApkMd5}
        //eg -> 1.0@258v1.1@300-54621B46C1664DB5BA7127D8F22AFF00.bsPatch.apk
        private val reg by lazy { "\\W?($versionReg)v($versionReg)-([\\da-zA-Z]{32})\\.([\\da-zA-Z]+)\\.apk".toRegex() }
        fun invalidFormat(patchFileUrl: String?): Boolean {
            if (patchFileUrl.isNullOrEmpty()) return true
            return reg.find(patchFileUrl) == null
        }

        fun isValidFormat(patchFileUrl: String?): Boolean {
            return !invalidFormat(patchFileUrl)
        }

        @JvmStatic
        fun format(v: PatchVersion, patchExt: String): String {
            return String.format("%s@%dv%s@%d-%s.$patchExt.apk", v.oldVersionName, v.oldVersion, v.newVersionName, v.newVersion, v.newApkMd5?.lowercase())
        }

        @JvmStatic
        fun parse(patchFileUrl: String): PatchVersion {
            return PatchVersion().apply {
                val m = reg.find(patchFileUrl)
                if (m != null) {
                    val ov = m.groupValues[1]
                    var nc = ov.split("@")
                    oldVersionName = nc.first()
                    oldVersion = nc.last().toInt()

                    val nv = m.groupValues[2]
                    nc = nv.split("@")
                    newVersionName = nc.first()
                    newVersion = nc.last().toInt()

                    newApkMd5 = m.groupValues[3]
                    val patchExt = m.groupValues[4]
                    fileName = format(this, patchExt)
                }
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