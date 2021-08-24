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


class ApkFileProvider : FileProvider() {
    companion object {
        fun getFile(context: Context): File {
            return File(context.filesDir, "updateApk")
        }

        fun getUri(context: Context, file: File): Uri {
            return getUriForFile(context, "${context.packageName}.ApkFileProvider", file)
        }
    }
}

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
        ApkFileProvider.getUri(this, apkFile)
    } else {
        Uri.fromFile(apkFile)//经测试：7.0以下不能用FileProvider
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
            ApkFileProvider.getFile(this)
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
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