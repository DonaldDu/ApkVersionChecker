package com.example.apkversionchecker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.dhy.versionchecker.IUpdateSetting
import com.dhy.versionchecker.IVersion
import com.dhy.versionchecker.NewUpdateActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
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
            NewUpdateActivity.checkVersion(this, Observable.just(v), update)
        }

        buttonMultTest.setOnClickListener {
            checkVersion(1500)
            checkVersion(2000)
            checkVersion(2500)
            checkVersion(3000)
        }
        tv.text = versionDates.joinToString("\n")
    }

    private fun checkVersion(delay: Long) {
        val f = SimpleDateFormat("yyyy-M-d HH:mm:ss SSS")
        val api = Observable.create<AppVersion> {
            Thread.sleep(delay)
            val head = " startIndex: $index, version date："
            index++
            val v = AppVersion()
            v.msg = head + f.format(System.currentTimeMillis())
            versionDates.add(v.msg!!)
            it.onNext(v)
            it.onComplete()
        }
        checkVersion(this, api, update, Intent(this, MainActivity::class.java))
    }

    @SuppressLint("CheckResult")
    private fun <V : IVersion> checkVersion(activity: Activity, api: Observable<V>, setting: IUpdateSetting? = null, mainIntent: Intent) {
        api.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startActivity(mainIntent)
                if (it.isNew) NewUpdateActivity.showVersion(activity, it, setting)
            }
    }
}