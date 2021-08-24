package com.example.apkversionchecker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.dhy.versionchecker.*
import com.dhy.xintent.ActivityKiller
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.text.SimpleDateFormat

class MainActivity : BaseBackupApkFileActivity() {
    companion object {
        var versionDates: MutableList<String> = mutableListOf()
        var index = 0
    }

    private val INSTALL_PERMISS_CODE = 1
    private val v = AppVersion()
    private val update = UpdateSetting()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppVersion.forceUpdate = isForceUpdate.isChecked
        isForceUpdate.setOnCheckedChangeListener { _, isChecked ->
            AppVersion.forceUpdate = isChecked
        }

        UpdateSetting.pass = passIfAlreadyDownloadCompleted.isChecked
        passIfAlreadyDownloadCompleted.setOnCheckedChangeListener { _, isChecked ->
            UpdateSetting.pass = isChecked
        }

        buttonCommit.setOnClickListener {
            val wrapperApi = Rx3Wrapper { Observable.just(v) }
            VersionUtil.checkVersion(this, wrapperApi, false, update)
        }

        buttonMultTest.setOnClickListener {
            checkVersion(1500)
            checkVersion(2000)
            checkVersion(2500)
            checkVersion(3000)
        }
        buttonAutoDownload.setOnClickListener {
            val wrapperApi = Rx3Wrapper { getApi(100) }
            VersionUtil.checkVersion(this, wrapperApi, true, update)
        }
        btInstallTest.setOnClickListener {
            val path = getInstalledApkPath()!!
            apkFile = File(apkFolder(), "mine.apk")
            if (apkFile!!.exists()) apkFile!!.delete()
            File(path).copyTo(apkFile!!, true)
            installApk(apkFile!!, INSTALL_PERMISS_CODE)
        }
        tv.text = versionDates.joinToString("\n")
        Log.i("TAG", "staticDir " + staticDir())

        btDownladingNewPageTest.setOnClickListener {

        }

        btFinishAll.setOnClickListener {
            ActivityKiller.killAll()
        }
    }

    private fun getApi(delay: Long): Observable<AppVersion> {
        val f = SimpleDateFormat("yyyy-M-d HH:mm:ss SSS")
        return Observable.create {
            Thread.sleep(delay)
            val head = " startIndex: $index, version date："
            index++
            val v = AppVersion()
            v.msg = head + f.format(System.currentTimeMillis())
            versionDates.add(v.msg!!)
            it.onNext(v)
            it.onComplete()
        }
    }

    private fun checkVersion(delay: Long) {
        checkVersion(getApi(delay), update, Intent(this, MainActivity::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == INSTALL_PERMISS_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (apkFile != null) installApk(apkFile!!, INSTALL_PERMISS_CODE)
                else Toast.makeText(this, "apkFile is null", Toast.LENGTH_LONG).show()
            } else Toast.makeText(this, "install canceled ", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("CheckResult")
    private fun <V : IVersion> checkVersion(api: Observable<V>, setting: IUpdateSetting? = null, mainIntent: Intent) {
        api.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.isNew) VersionUtil.showVersion(this, it, setting)
                buttonCommit.postDelayed({
                    startActivity(mainIntent)
                }, 1500)
            }
    }
}