package com.example.apkversionchecker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.dhy.versionchecker.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.apache.commons.io.FileUtils
import java.io.File
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {
    companion object {
        var versionDates: MutableList<String> = mutableListOf()
        var index = 0
    }

    private val v = AppVersion()
    private val update = UpdateSetting()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        isForceUpdate.setOnCheckedChangeListener { _, isChecked ->
            AppVersion.forceUpdate = isChecked
        }
        passIfAlreadyDownloadCompleted.setOnCheckedChangeListener { _, isChecked ->
            UpdateSetting.pass = isChecked
        }

        buttonCommit.setOnClickListener {
            VersionUtil.checkVersion(this, Observable.just(v), false, update)
        }

        buttonMultTest.setOnClickListener {
            checkVersion(1500)
            checkVersion(2000)
            checkVersion(2500)
            checkVersion(3000)
        }
        buttonAutoDownload.setOnClickListener {
            VersionUtil.checkVersion(this, getApi(100), true, update)
        }
        btInstallTest.setOnClickListener {
            if (hasFilePermission(true)) {
                val path = getInstalledApkPath()!!
                val apk = File(apkFolder(), "mine.apk")
                FileUtils.copyFile(File(path), apk)
                installApk(apk)
            }
        }
        tv.text = versionDates.joinToString("\n")
        Log.i("TAG", "staticDir " + staticDir())

        btDownladingNewPageTest.setOnClickListener {

        }
    }

    private fun getApi(delay: Long): Observable<AppVersion> {
        val f = SimpleDateFormat("yyyy-M-d HH:mm:ss SSS")
        return Observable.create<AppVersion> {
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

    @SuppressLint("CheckResult")
    private fun <V : IVersion> checkVersion(api: Observable<V>, setting: IUpdateSetting? = null, mainIntent: Intent) {
        api.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.isNew && index == 1) VersionUtil.showVersion(this, it, setting)
                buttonCommit.postDelayed({
                    startActivity(mainIntent)
                }, 1500)
            }
    }
}