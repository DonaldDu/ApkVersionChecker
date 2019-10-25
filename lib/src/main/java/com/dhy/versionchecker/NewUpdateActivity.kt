package com.dhy.versionchecker

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import com.dhy.xintent.readExtra
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import com.liulishuo.okdownload.core.listener.DownloadListener1
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist
import kotlinx.android.synthetic.main.avc_activity_new_update.*
import kotlin.system.exitProcess


class NewUpdateActivity : AppCompatActivity() {
    companion object {
        private val updateActivities: MutableList<NewUpdateActivity> = mutableListOf()
    }

    private lateinit var context: Context
    private lateinit var version: IVersion
    private lateinit var setting: IUpdateSetting

    private var autoFinish = false
    private val startDate: Long = System.currentTimeMillis()

    private fun finishRepeat() {
        updateActivities.add(this)
        updateActivities.forEach {
            if (it.startDate < startDate) {
                it.autoFinish = true
                it.finish()
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        context = this
        finishRepeat()
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

        if (!autoFinish) initReshow()
    }

    private val lifecycleCallbacks: ActivityLifecycleCallbacks2 = object : ActivityLifecycleCallbacks2 {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (activity !is NewUpdateActivity) {
                application.unregisterActivityLifecycleCallbacks(this)
                VersionUtil.showVersion(context, version, setting)
            }
        }
    }

    private fun initReshow() {
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
    }

    override fun onDestroy() {
        super.onDestroy()
        application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
        timer?.cancel()
        updateActivities.remove(this)
        if (version.isForceUpdate && !autoFinish) {
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(0)
        }
    }

    private var retryCount = 0
    private fun downloadApk() {
        val updateApkFolder = apkFolder(this)
        val apk = version.apkFileName(this)
        val task = DownloadTask.Builder(version.url, updateApkFolder, apk)
            .setPassIfAlreadyCompleted(setting.passIfAlreadyDownloadCompleted())
            .setMinIntervalMillisCallbackProcess(100)
            .build()
        task.enqueue(object : DownloadListener1() {
            override fun taskStart(task: DownloadTask, model: Listener1Assist.Listener1Model) {}

            override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?, model: Listener1Assist.Listener1Model) {
                println("EndCause $cause")
                realCause?.printStackTrace()

                if (cause == EndCause.COMPLETED) {
                    task.file!!.deleteOldApkVersions()
                    installApk(context, task.file!!)
                    finish()
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

            override fun connected(task: DownloadTask, blockCount: Int, currentOffset: Long, totalLength: Long) {}

            override fun retry(task: DownloadTask, cause: ResumeFailedCause) {}
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
}