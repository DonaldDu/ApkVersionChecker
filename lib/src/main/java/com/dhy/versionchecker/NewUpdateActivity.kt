package com.dhy.versionchecker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.text.method.LinkMovementMethod
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.dhy.xintent.ActivityKiller
import com.dhy.xintent.Waterfall
import com.dhy.xintent.XIntent
import com.dhy.xintent.putSerializableExtra
import com.dhy.xintent.readExtra
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import com.liulishuo.okdownload.core.listener.DownloadListener1
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist
import kotlinx.android.synthetic.main.avc_activity_new_update.buttonCommit
import kotlinx.android.synthetic.main.avc_activity_new_update.tv_msg
import kotlinx.android.synthetic.main.avc_activity_new_update.tv_title
import java.io.File
import kotlin.system.exitProcess


class NewUpdateActivity : AppCompatActivity() {
    companion object {
        /**
         * 已进入系统安装界面，安装中。。。
         * */
        private var installing = false
    }

    private var apkFile: File? = null
    private val TAG = "NewUpdate"
    private val showLog = false
    private val INSTALL_PERMISS_CODE = 1
    private lateinit var context: Context
    private lateinit var version: IVersion
    private lateinit var setting: IUpdateSetting
    private val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val v: IVersion? = readExtra()
        if (v != null) version = v
        else {
            if (showLog) Log.e(TAG, "NewUpdateActivity onCreate: version can't be null")
            return finish()
        }
        setting = readExtra() ?: object : IUpdateSetting {}
        if (installing) {
            if (showLog) Log.i(TAG, "onCreate: cancel restart NewUpdateActivity when installing")
            return finish()
        }
        if (savedInstanceState != null) restoreInstanceState(savedInstanceState)
        context = this
        setContentView(R.layout.avc_activity_new_update)
        tv_title.text = setting.getTitle(context, version)

        tv_msg.text = setting.getMessage(context, version)
        tv_msg.isClickable = true
        tv_msg.movementMethod = LinkMovementMethod.getInstance()

        buttonCommit.setOnClickListener {
            checkDownloadApk()
        }
        setFinishOnTouchOutside(false)
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        XIntent.with(outState).putSerializableExtra(apkFile, installing)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restoreInstanceState(savedInstanceState)
    }

    private fun restoreInstanceState(savedInstanceState: Bundle) {
        XIntent.with(savedInstanceState).apply {
            apkFile = readExtra()
            installing = readExtra() ?: false
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (!version.isForceUpdate) {
            unregisterActivityLifecycleCallbacks()
        }
    }

    private fun checkDownloadApk() {
        Waterfall.flow {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> next()
                else -> {
                    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                        if (hasFilePermission(true)) next()
                        else downloadApkWithBrowser()
                    } else {
                        downloadApkWithBrowser()
                    }
                }
            }
        }.flow {
            buttonCommit.isEnabled = false
            downloadApk()
            if (hasNoProgressData()) showProgress(0, 100)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (hasFilePermission(false)) checkDownloadApk()
    }

    private fun downloadApkWithBrowser() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(version.url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private val lifecycleCallbacks: ActivityLifecycleCallbacks2 = object : ActivityLifecycleCallbacks2 {
        override fun onActivityResumed(activity: Activity) {
            if (showLog) Log.i(TAG, "onActivityResumed: ${activity.javaClass.name}")
            if (activity !is NewUpdateActivity) {
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({
                    VersionUtil.showVersion(activity, version, setting)
                }, 500)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        task?.cancel()
        timer?.cancel()
        unregisterActivityLifecycleCallbacks()
        if (version.isForceUpdate) {
            ActivityKiller.killAll()
            Process.killProcess(Process.myPid())
            exitProcess(0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == INSTALL_PERMISS_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                handler.removeCallbacksAndMessages(null)
                if (showLog) Log.i(TAG, "onActivityResult end installApk")
                startInstallApk(apkFile)
            } else reset(true)
        }
    }

    private var retryCount = 0
    private var task: DownloadTask? = null
    private fun downloadApk() {
        task = version.toDownloadTask(context)
            .setPassIfAlreadyCompleted(setting.passIfAlreadyDownloadCompleted())
            .setMinIntervalMillisCallbackProcess(100)
            .build()

        task!!.enqueue(object : DownloadListener1() {
            override fun taskStart(task: DownloadTask, model: Listener1Assist.Listener1Model) {}

            override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?, model: Listener1Assist.Listener1Model) {
                println("EndCause $cause")
                realCause?.printStackTrace()
                if (cause == EndCause.COMPLETED) {
                    VersionUtil.patchApk(context, setting.getMaxPatchMemory(), version, task.file!!, {
                        apkFile = it

                        if (showLog) Log.i(TAG, "downloadApk end installApk")
                        startInstallApk(it)
                    }, {
                        startRetry()//合成补丁检验未通过后，返回调用此方法时，需要在UI线程调用。
                    })
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

    private fun startInstallApk(file: File?) {
        if (file != null && !isFinishing) {
            installApk(file)
            installing = true
            unregisterActivityLifecycleCallbacks()
            finish()
        }
    }

    private fun unregisterActivityLifecycleCallbacks() {
        handler.removeCallbacksAndMessages(null)
        application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
    }

    private fun reset(toInstall: Boolean = false) {
        retryCount = 0
        buttonCommit.isEnabled = true
        val action = if (toInstall) R.string.avc_button_install else R.string.avc_button_retry
        buttonCommit.setText(action)
    }

    private var timer: CountDownTimer? = null
    private fun startRetry() {
        runOnUiThread {
            timer?.cancel()
            timer = MyCountDownTimer()
            timer!!.start()
        }
    }

    private inner class MyCountDownTimer : CountDownTimer(15_000, 500) {
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

    private fun hasNoProgressData(): Boolean {
        return buttonCommit.tag == null
    }

    /**
     * 下载完成后一直显示99.99%，以便给增量更新合成新件提供时间。如果不是增量包，就会关闭进度框和当前页，然后跳转到新页面。
     * */
    private fun showProgress(currentOffset: Long, totalLength: Long) {
        val offset = if (currentOffset >= totalLength) (totalLength * 0.9999f).toLong() else currentOffset
        buttonCommit.text = setting.getProgress(offset, totalLength)
        buttonCommit.tag = true//got progress data flag
    }
}