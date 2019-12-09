package com.dhy.versionchecker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.v4.content.FileProvider
import java.io.File

class ApkFileProvider : FileProvider()

/**
 * @param apkFile should be in folder 'filesDir/updateApk/'
 * */
fun Context.installApk(apkFile: File) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
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