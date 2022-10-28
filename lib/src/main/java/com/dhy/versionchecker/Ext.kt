package com.dhy.versionchecker

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import com.liulishuo.okdownload.DownloadTask
import java.io.File

fun Context.getInstalledApkPath(pn: String = packageName): String? {
    return getInstalledApkInfo(pn)?.applicationInfo?.sourceDir
}

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

internal fun IVersion.getApkFileSize(): Long {
    return if (isValidPatch()) patchSize else size
}

/**
 * 确保增量包是当前版本的，且是最新的（可以超前）
 * */
fun IVersion.isValidPatch(): Boolean {
    return if (PatchVersion.invalidFormat(patchUrl)) false
    else {
        val pv = PatchVersion(patchUrl!!)
        pv.newVersion == versionCode && pv.oldVersion == currentVersionCode
    }
}

internal fun IVersion.toDownloadTask(context: Context): DownloadTask.Builder {
    val updateApkFolder = context.apkFolder()!!.absolutePath
    val newApkName = apkFileName(context)
    val newApk = File(updateApkFolder, newApkName)

    return if (newApk.exists()) {
        DownloadTask.Builder(url, updateApkFolder, newApkName)
    } else {
        val pv = if (patchUrl.isNullOrEmpty()) null else PatchVersion(patchUrl!!)
        if (isValidPatch()) {
            DownloadTask.Builder(patchUrl!!, updateApkFolder, pv!!.name).fixConnectionError()
        } else {
            DownloadTask.Builder(url, updateApkFolder, newApkName).fixConnectionError()
        }
    }
}

private fun DownloadTask.Builder.fixConnectionError(): DownloadTask.Builder {
    if (Build.VERSION.SDK_INT >= 29) setConnectionCount(1)
    return this
}

internal val PackageInfo.currentVersionCode: Int
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

fun Activity.hasFilePermission(requestPermissions: Boolean): Boolean {
    val filePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
    } else {
        arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
    val ok = filePermissions.find {
        ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
    } == null
    if (!ok && requestPermissions) ActivityCompat.requestPermissions(this, filePermissions, 1)
    return ok
}

fun Context.getActivity(): Activity? {
    return if (this is ContextWrapper) {
        if (this is Activity) this
        else baseContext.getActivity()
    } else null
}