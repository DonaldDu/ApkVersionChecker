package com.dhy.versionchecker

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.text.TextUtils
import com.liulishuo.okdownload.DownloadTask
import java.io.File

fun Context.getInstalledApkInfo(pn: String = packageName): PackageInfo? {
    if (TextUtils.isEmpty(pn)) return null
    return try {
        packageManager.getPackageInfo(pn, 0)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        null
    }
}

val Context.isNetworkConnected: Boolean
    get() {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        return cm?.activeNetworkInfo?.isConnected == true
    }

internal fun IVersion.apkFileName(context: Context): String {
    return "${context.packageName}_c${versionCode}.apk"
}

internal fun IVersion.toDownloadTask(context: Context): DownloadTask.Builder {
    val updateApkFolder = apkFolder(context)
    val newApkName = apkFileName(context)
    val newApk = File(updateApkFolder, newApkName)

    return if (newApk.exists()) {
        DownloadTask.Builder(fullUrl, updateApkFolder, newApkName)
    } else {
        val info = context.getInstalledApkInfo()
        val pv = if (patchUrl.isNullOrEmpty()) null else PatchVersion(patchUrl!!)
        if (pv != null && pv.oldVersion == info!!.currentVersionCode) {
            DownloadTask.Builder(patchUrl!!, updateApkFolder, pv.name)
        } else {
            DownloadTask.Builder(fullUrl, updateApkFolder, newApkName)
        }
    }
}

private val PackageInfo.currentVersionCode: Int
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            longVersionCode.toInt()
        } else versionCode
    }

internal fun File.deleteOldApkVersions() {
    val updateApkFolder = parentFile
    val newFile = name
    updateApkFolder.listFiles().forEach {
        if (it.name != newFile) it.delete()
    }
}