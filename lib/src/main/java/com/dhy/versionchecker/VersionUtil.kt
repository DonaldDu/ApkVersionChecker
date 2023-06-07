package com.dhy.versionchecker

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import com.dhy.avc.format.PatchVersion
import com.dhy.xintent.ActivityKiller
import com.dhy.xintent.XIntent
import com.github.sisong.ApkPatch
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
            val intent = XIntent(activity, NewUpdateActivity::class, version, setting.finalValue)
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
        val finalValue = setting.finalValue
        ActivityKiller.init(activity.application)
        api.call {
            if (autoDownload) autoDownload(activity, it, finalValue)
            else showVersion(activity, it, finalValue)
        }
    }

    @JvmStatic
    fun autoDownload(activity: Activity, version: IVersion, setting: IUpdateSetting? = null) {
        this.version = version
        this.setting = setting.finalValue
        registerNetworkReceiver(activity)
        download(activity, version, this.setting!!)
    }

    private fun download(activity: Activity, version: IVersion, setting: IUpdateSetting, retryCount: Int = 3) {
        val task = version.toDownloadTask(activity)
            .setPassIfAlreadyCompleted(setting.passIfAlreadyDownloadCompleted())
            .setWifiRequired(setting.isWifiRequired())
            .build()

        task.enqueue(object : DownloadListener2() {
            override fun taskStart(task: DownloadTask) {}

            override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?) {
                if (cause == EndCause.COMPLETED) {
                    unregisterNetworkReceiver(activity)

                    patchApk(activity, setting.getMaxPatchMemory(), version, task.file!!, {
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

    internal fun patchApk(context: Context, maxMemory: Long, version: IVersion, file: File, installApk: (File) -> Unit, retry: () -> Unit) {
        if (file.isPathFile()) {
            patchApkInNewThread(context, maxMemory, version, file, installApk, retry)
        } else {
            file.deleteOldApkVersions()
            installApk(file)
        }
    }

    private fun patchApkInNewThread(context: Context, maxMemory: Long, version: IVersion, patch: File, installApk: (File) -> Unit, retry: () -> Unit) {
        val oldApkPath = context.getInstalledApkInfo()!!.applicationInfo.sourceDir
        var newApk = File.createTempFile("bs_merge", ".apk")
        Thread {
            val tmp = File(context.cacheDir, "tmp_${System.currentTimeMillis()}")

            val patchOk = ApkPatch.patch(oldApkPath, patch.absolutePath, newApk.absolutePath, maxMemory, tmp.absolutePath, 3) == 0
            val md5Ok = patchOk && PatchVersion.parse(version.patchUrl!!).matchMd5(newApk.md5())
            tmp.deleteRecursively()
            if (md5Ok) {
                val finalApk = File(patch.parentFile, version.apkFileName(context))
                newApk.renameTo(finalApk)
                newApk = finalApk
                newApk.deleteOldApkVersions()
                installApk(newApk)
            } else {
                version.patchUrl = null//合成后补丁校验不匹配，则清空删除补丁链接，使用完整包下载。
                newApk.delete()
                newApk.createNewFile()
                retry()
            }
        }.start()
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
                download(activity, version!!, setting.finalValue)
            }
        }
    }

    private val IUpdateSetting?.finalValue: IUpdateSetting
        get() {
            return this ?: object : IUpdateSetting {}
        }
}