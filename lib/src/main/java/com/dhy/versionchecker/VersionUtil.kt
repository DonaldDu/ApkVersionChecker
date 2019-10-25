package com.dhy.versionchecker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import com.dhy.xintent.XIntent
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.listener.DownloadListener2
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


object VersionUtil {
    private var disposable: Disposable? = null
    private var version: IVersion? = null
    private var setting: IUpdateSetting? = null
    private var networkReceiver: NetworkConnectChangedReceiver? = null
    @JvmStatic
    fun showVersion(context: Context, version: IVersion?, setting: IUpdateSetting? = null) {
        if (version?.isNew == true) {
            val intent = XIntent(context, NewUpdateActivity::class, version, setting)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    /**
     * should call this in launch activity and MainActivity
     * */
    @JvmStatic
    fun <V : IVersion> checkVersion(context: Context, api: Observable<V>, autoDownload: Boolean, setting: IUpdateSetting?) {
        checkVersion(api) {
            if (autoDownload) autoDownload(context, it, setting)
            else showVersion(context, it, setting)
        }
    }

    private fun <V : IVersion> checkVersion(api: Observable<V>, showVersion: (IVersion) -> Unit) {
        if (disposable?.isDisposed == false) return
        disposable = api.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                dispose()
                if (it.isNew) showVersion(it)
            }, {
                dispose()
            }, {
                dispose()
            })
    }

    private fun dispose() {
        disposable?.dispose()
        disposable = null
    }

    @JvmStatic
    fun autoDownload(context: Context, version: IVersion, setting: IUpdateSetting? = null) {
        this.version = version
        this.setting = setting
        registerNetworkReceiver(context)
        download(context, version, setting)
    }

    private fun download(context: Context, version: IVersion, setting: IUpdateSetting? = null, retryCount: Int = 3) {
        val updateApkFolder = apkFolder(context)
        val task = DownloadTask.Builder(version.url, updateApkFolder, version.apkFileName(context))
            .setPassIfAlreadyCompleted(setting?.passIfAlreadyDownloadCompleted() ?: true)
            .setWifiRequired(setting?.isWifiRequired() ?: true)
            .build()

        task.enqueue(object : DownloadListener2() {
            override fun taskStart(task: DownloadTask) {}

            override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?) {
                if (cause == EndCause.COMPLETED) {
                    unregisterNetworkReceiver(context)
                    task.file?.deleteOldApkVersions()
                    showVersion(context, version, setting)
                } else {
                    if (retryCount > 0) {
                        download(context, version, setting, retryCount - 1)
                    }
                }
            }
        })
    }

    private fun unregisterNetworkReceiver(context: Context) {
        if (networkReceiver != null) {
            context.unregisterReceiver(networkReceiver)
            networkReceiver = null
        }
    }

    private fun registerNetworkReceiver(context: Context) {
        if (networkReceiver == null) {
            networkReceiver = NetworkConnectChangedReceiver()
            val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            context.registerReceiver(networkReceiver, filter)
        }
    }

    private class NetworkConnectChangedReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (context.isNetworkConnected) {
                download(context, version!!, setting)
            }
        }
    }
}

val Context.isNetworkConnected: Boolean
    get() {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        return cm?.activeNetworkInfo?.isConnected == true
    }