package com.dhy.versionchecker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import com.dhy.xintent.XIntent
import com.dhy.xintent.readExtra
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import com.liulishuo.okdownload.core.listener.DownloadListener1
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.avc_activity_new_update.*
import java.io.File
import kotlin.system.exitProcess


class NewUpdateActivity : AppCompatActivity() {
    companion object {
        private var disposable: Disposable? = null
        @JvmStatic
        private val updateActivities: MutableList<NewUpdateActivity> = mutableListOf()

        @JvmStatic
        fun showNewVersion(activity: Activity, version: IVersion?, setting: IUpdateSetting? = null) {
            if (version?.isNew == true) XIntent.startActivity(activity, NewUpdateActivity::class, version, setting)
        }

        @JvmStatic
        fun <V : IVersion> checkVersion(activity: Activity, api: Observable<V>, setting: IUpdateSetting? = null) {
            if (disposable?.isDisposed == false) return
            disposable = api.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    dispose()
                    if (it.isNew) showNewVersion(activity, it, setting)
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
    }

    private lateinit var version: IVersion
    private lateinit var setting: IUpdateSetting
    private var context: Context
    private var startDate = 0L
    private var autoFinish = false

    init {
        startDate = System.currentTimeMillis()
        finishRepeat()
        context = this
    }

    private fun finishRepeat() {
        updateActivities.add(this)
        updateActivities.forEach {
            if (it.startDate < startDate) {
                it.autoFinish = true
                it.finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.avc_activity_new_update)
        version = readExtra()!!
        setting = readExtra() ?: object : IUpdateSetting {}

        tv_title.text = setting.getTitle(context, version)
        tv_msg.text = setting.getMessage(context, version)

        buttonCommit.setOnClickListener {
            it.isEnabled = false
            downloadApk()
        }
        setFinishOnTouchOutside(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        updateActivities.remove(this)
        if (version.isForceUpdate && !autoFinish) exitProcess(0)
    }

    private var retryCount = 0
    private fun downloadApk() {
        val updateApkFolder = File(filesDir, "updateApk")
        val apk = "$packageName-c${version.versionCode}.apk"
        val task = DownloadTask.Builder(version.url, updateApkFolder.absolutePath, apk)
            .setPassIfAlreadyCompleted(setting.passIfAlreadyDownloadCompleted())
            .setMinIntervalMillisCallbackProcess(100)
            .build()
        task.enqueue(object : DownloadListener1() {
            override fun taskStart(task: DownloadTask, model: Listener1Assist.Listener1Model) {
            }

            override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?, model: Listener1Assist.Listener1Model) {
                println("EndCause $cause")
                realCause?.printStackTrace()

                if (cause == EndCause.COMPLETED) {
                    deleteOldVersions(updateApkFolder, task.file!!)
                    finish()
                    installApk(context, task.file!!)
                } else {
                    if (retryCount < setting.maxRetryCount()) {
                        retryCount++
                        startRetry()
                    } else reset()
                }
            }

            override fun progress(task: DownloadTask, currentOffset: Long, totalLength: Long) {
                showProgress(currentOffset, totalLength)
            }

            override fun connected(task: DownloadTask, blockCount: Int, currentOffset: Long, totalLength: Long) {
            }

            override fun retry(task: DownloadTask, cause: ResumeFailedCause) {
            }
        })
    }

    private fun reset() {
        retryCount = 0
        buttonCommit.isEnabled = true
        buttonCommit.setText(R.string.avc_button_retry)
    }

    private var timer: CountDownTimer? = null
    private fun startRetry() {
        timer?.cancel()
        timer = object : CountDownTimer(15_000, 500) {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            override fun onFinish() {
                reset()
            }

            override fun onTick(millisUntilFinished: Long) {
                if (isNetworkAvailable()) {
                    cancel()
                    downloadApk()
                }
            }

            fun isNetworkAvailable(): Boolean {
                return cm.activeNetworkInfo?.isConnected == true
            }
        }
        timer!!.start()
    }

    private fun showProgress(currentOffset: Long, totalLength: Long) {
        buttonCommit.text = setting.getProgress(currentOffset, totalLength)
    }

    private fun deleteOldVersions(updateApkFolder: File, newApk: File) {
        val newFile = newApk.name
        updateApkFolder.listFiles().forEach {
            if (it.name != newFile) it.delete()
        }
    }
}

/**
 * @param apkFile should be in folder 'filesDir/updateApk/'
 * */
fun installApk(context: Context, apkFile: File) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        val contentUri = FileProvider.getUriForFile(context, context.packageName, apkFile)
        intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
    } else {
        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive")
    }
    context.startActivity(intent)
}