package com.dhy.versionchecker

import android.content.Context
import androidx.annotation.ColorRes
import java.io.Serializable

interface IUpdateSetting : Serializable {
    fun getTitle(context: Context, version: IVersion): CharSequence {
        val appName = getAppName(context)
        val fileSize = version.getApkFileSize()
        return if (fileSize > 0) {
            val min = 1024 * 10
            if (fileSize < min) {
                val size = fileSize / 1024f
                String.format("发现新版本：%s v%s（%.2fKB）", appName, version.versionName, size)
            } else {
                val size = formatSizeInMB(fileSize)
                String.format("发现新版本：%s v%s（%.2fMB）", appName, version.versionName, size)//保留2位小数，10KB以下显示为0.00MB
            }
        } else {
            String.format("发现新版本：%s v%s", appName, version.versionName)
        }
    }

    fun getMessage(context: Context, version: IVersion): CharSequence {
        val colorRes = mdLinkTextColorRes()
        val msg = version.log ?: ""
        return if (colorRes == null) msg
        else supportMdLink(context, msg, colorRes)
    }

    @ColorRes
    fun mdLinkTextColorRes(): Int? = R.color.xintent_theme
    fun getProgress(currentOffset: Long, totalLength: Long): String {
        val percent = currentOffset * 100f / totalLength
        return String.format("%.2f%%", percent)
    }

    private fun formatSizeInMB(size: Long): Float {
        return size.toFloat() / 1024 / 1024//kb/mb
    }

    fun maxRetryCount(): Int = 3

    fun passIfAlreadyDownloadCompleted(): Boolean = true
    fun isWifiRequired(): Boolean = false

    fun getAppName(context: Context): String {
        return context.applicationInfo.loadLabel(context.packageManager).toString()
    }

    fun getMaxPatchMemory(): Long = 1024L * 1024 * 50//MB
}