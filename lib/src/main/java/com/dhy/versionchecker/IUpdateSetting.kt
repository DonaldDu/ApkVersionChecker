package com.dhy.versionchecker

import android.content.Context
import java.io.Serializable

interface IUpdateSetting : Serializable {
    fun getTitle(context: Context, version: IVersion): String {
        val appName = getAppName(context)
        val fileSize = version.getApkFileSize()
        return if (fileSize > 0) {
            val size = formatSizeInMB(fileSize)
            String.format("发现新版本：%s v%s（%.2fMB）", appName, version.versionName, size)
        } else {
            String.format("发现新版本：%s v%s", appName, version.versionName)
        }
    }

    fun getMessage(context: Context, version: IVersion): String {
        return version.log ?: ""
    }

    fun getProgress(currentOffset: Long, totalLength: Long): String {
        val percent = currentOffset * 100f / totalLength
        return String.format("%.2f%%", percent)
    }

    private fun formatSizeInMB(size: Long): Float {
        return size.toFloat() / 1024 / 1024//byte/kb/mb
    }

    fun maxRetryCount(): Int = 3

    fun passIfAlreadyDownloadCompleted(): Boolean = true
    fun isWifiRequired(): Boolean = true

    fun getAppName(context: Context): String {
        return context.applicationInfo.loadLabel(context.packageManager).toString()
    }
}