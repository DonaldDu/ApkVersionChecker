package com.dhy.versionchecker

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import com.dhy.avc.format.PatchVersion
import com.dhy.bspatch.PatchUtils
import com.dhy.xintent.ActivityKiller
import com.dhy.xintent.XIntent
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.listener.DownloadListener2
import java.io.File


object VersionUtil {
    private var version: IVersion? = null
    private var setting: IUpdateSetting? = null
    private var networkReceiver: NetworkConnectChangedReceiver? = null

    @JvmStatic
    fun showVersion(activity: Activity, version: IVersion?, setting: IUpdateSetting? = null) {
        if (version?.isNew == true) {
            val intent = XIntent(activity, NewUpdateActivity::class, version, setting)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            if (activity.isFinishing) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        }
    }

    /**
     * should call this in launch activity and MainActivity
     * */
    @JvmStatic
    fun checkVersion(activity: Activity, api: RxWrapper<*>, autoDownload: Boolean, setting: IUpdateSetting?) {
        ActivityKiller.init(activity.application)
        api.call {
            if (autoDownload) autoDownload(activity, it, setting)
            else showVersion(activity, it, setting)
        }
    }

    @JvmStatic
    fun autoDownload(activity: Activity, version: IVersion, setting: IUpdateSetting? = null) {
        this.version = version
        this.setting = setting
        registerNetworkReceiver(activity)
        download(activity, version, setting)
    }

    private fun download(activity: Activity, version: IVersion, setting: IUpdateSetting? = null, retryCount: Int = 3) {
        val task = version.toDownloadTask(activity)
            .setPassIfAlreadyCompleted(setting?.passIfAlreadyDownloadCompleted() ?: true)
            .setWifiRequired(setting?.isWifiRequired() ?: true)
            .build()

        task.enqueue(object : DownloadListener2() {
            override fun taskStart(task: DownloadTask) {}

            override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?) {
                if (cause == EndCause.COMPLETED) {
                    unregisterNetworkReceiver(activity)

                    patchApk(activity, version, task.file!!, {
                        showVersion(activity, version, setting)
                    }, {
                        download(activity, version, setting, retryCount)
                    })
                } else {
                    if (retryCount > 0) {
                        download(activity, version, setting, retryCount - 1)
                    }
                }
            }
        })
    }

    private fun File.isPathFile(): Boolean {
        return !PatchVersion.invalidFormat(name)
    }

    internal fun patchApk(context: Context, version: IVersion, file: File, installApk: (File) -> Unit, retry: () -> Unit) {
        if (file.isPathFile()) {
            val oldApkPath = context.getInstalledApkInfo()!!.applicationInfo.sourceDir
            var newApk = File.createTempFile("bs_merge", ".apk")
            Thread {
                val patchOk = PatchUtils.patch(oldApkPath, newApk.absolutePath, file.absolutePath) == 0
                val md5Ok = patchOk && PatchVersion(version.patchUrl!!).matchMd5(newApk.md5())

                if (md5Ok) {
                    val finalApk = File(file.parentFile, version.apkFileName(context))
                    newApk.renameTo(finalApk)
                    newApk = finalApk
                    newApk.deleteOldApkVersions()
                    installApk(newApk)
                } else {
                    newApk.delete()
                    newApk.createNewFile()
                    retry()
                }
            }.start()
        } else {
            file.deleteOldApkVersions()
            installApk(file)
        }
    }

    private fun unregisterNetworkReceiver(context: Context) {
        if (networkReceiver != null) {
            context.unregisterReceiver(networkReceiver)
            networkReceiver = null
        }
    }

    private fun registerNetworkReceiver(activity: Activity) {
        if (networkReceiver == null) {
            networkReceiver = NetworkConnectChangedReceiver(activity)
            @Suppress("DEPRECATION")
            val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            activity.registerReceiver(networkReceiver, filter)
        }
    }

    private class NetworkConnectChangedReceiver(val activity: Activity) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (context.isNetworkConnected) {
                download(activity, version!!, setting)
            }
        }
    }
}