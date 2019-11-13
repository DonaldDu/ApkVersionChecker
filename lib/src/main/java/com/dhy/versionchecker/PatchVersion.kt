package com.dhy.versionchecker

internal class PatchVersion(patchFileUrl: String) {
    val md5: String
    val name: String
    val oldVersion: Int
    val newVersion: Int

    init {
        val start = patchFileUrl.lastIndexOf("/")
        name = patchFileUrl.substring(start + 1)

        //差分包文件名：{MD5}_{旧代码号}v{新代码号}：54621B46C1664DB5BA7127D8F22AFF00_258v300.patch.apk
        md5 = name.split("_").first()
        val version = "_(\\d+)v(\\d+)".toRegex()
        val oldNewVersion = version.find(name)
        if (oldNewVersion != null) {
            oldVersion = oldNewVersion.groups[1]!!.value.toInt()
            newVersion = oldNewVersion.groups[2]!!.value.toInt()
        } else {
            oldVersion = 0
            newVersion = 0
        }
    }
}