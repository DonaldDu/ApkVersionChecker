package com.dhy.versionchecker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File


class ApkFileProvider : FileProvider()

/**
 * @param apkFile should be in folder 'filesDir/updateApk/'
 * @return installed or not
 * */
fun Activity.installApk(apkFile: File, INSTALL_PERMISS_CODE: Int): Boolean {
    if (!canRequestPackageInstalls(INSTALL_PERMISS_CODE)) return false
    val intent = Intent(Intent.ACTION_VIEW)
    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        FileProvider.getUriForFile(this, "$packageName.ApkFileProvider", apkFile)
    } else {
        Uri.fromFile(apkFile)
    }
    intent.setDataAndType(uri, "application/vnd.android.package-archive")
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
    return true
}

fun Activity.canRequestPackageInstalls(INSTALL_PERMISS_CODE: Int?): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val ok = packageManager.canRequestPackageInstalls()
        if (!ok && INSTALL_PERMISS_CODE != null) {
            val packageURI = Uri.parse("package:$packageName")
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI)
            startActivityForResult(intent, INSTALL_PERMISS_CODE)
        }
        ok
    } else {
        true
    }
}

fun Context.apkFolder(): File? {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
            File(filesDir, "updateApk")
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        }
        else -> {
            staticDir()
        }
    }
}

/**
 * externalCacheDir: sdCard/android/data/packageName/cache
 * staticDir: sdCard/android/static/packageName
 * */
fun Context.staticDir(): File? {
    val path = externalCacheDir?.absolutePath ?: return null
    val data = path.substring(0, path.indexOf(packageName))
    val android = File(data).parent
    return File(android, "static/${packageName}")
}