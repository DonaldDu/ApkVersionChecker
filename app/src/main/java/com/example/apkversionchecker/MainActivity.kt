package com.example.apkversionchecker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
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

    private val INSTALL_PERMISS_CODE = 1
    private lateinit var apkFile: File
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
            val path = getInstalledApkPath()!!
            apkFile = File(apkFolder(), "mine.apk")
            FileUtils.copyFile(File(path), apkFile)
            installApk(apkFile, INSTALL_PERMISS_CODE)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == INSTALL_PERMISS_CODE) {
            if (resultCode == Activity.RESULT_OK) installApk(apkFile, INSTALL_PERMISS_CODE)
            else Toast.makeText(this, "install canceled ", Toast.LENGTH_LONG).show()
        }
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