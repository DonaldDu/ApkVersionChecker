package com.dhy.apkpatch.bsdiff

import java.io.File
import java.util.regex.Pattern

class PatchMaterial {
    lateinit var newApk: ApkFile
    lateinit var oldApk: ApkFile

    val isValid: Boolean
        get() = newApk.name == oldApk.name && newApk.branch == oldApk.branch

    companion object {
        @JvmStatic
        fun getPatchMaterial(config: Config): PatchMaterial {
            val nameReg = Pattern.compile(config.apkFileNameReg)
            var newApk = getApk(nameReg, config.oldApkFolder)
            var oldApk = getApk(nameReg, config.newApkFolder)
            requireNotNull(newApk) { "newApk not found" }
            requireNotNull(oldApk) { "oldApk not found" }
            if (newApk.vc < oldApk.vc) {
                val temp: ApkFile = newApk
                newApk = oldApk
                oldApk = temp
            }
            val patchFile = PatchMaterial()
            patchFile.newApk = newApk
            patchFile.oldApk = oldApk
            return patchFile
        }

        private fun getApk(nameReg: Pattern, folder: String): ApkFile? {
            val file = File(folder)
            return if (file.list() == null) null
            else file.list()!!.map { name: String ->
                val matcher = nameReg.matcher(name)
                if (matcher.find()) {
                    val apkFile = ApkFile()
                    apkFile.name = matcher.group("name")
                    apkFile.vc = matcher.group("vc").toInt()
                    apkFile.branch = matcher.group("branch")
                    apkFile.file = File(file, name)
                    return@map apkFile
                } else return@map null
            }.filterNotNull().maxBy { it.vc }
        }
    }
}