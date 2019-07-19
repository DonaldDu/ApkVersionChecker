package com.example.apkversionchecker

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.dhy.versionchecker.NewUpdateActivity
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
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
            checkVersion()
        }

        buttonMultTest.setOnClickListener {
            checkVersion()
            checkVersion()
            buttonCommit.postDelayed({ checkVersion() }, 500)
            buttonCommit.postDelayed({ checkVersion() }, 1000)
        }
    }

    private fun checkVersion() {
        val api = Observable.create<AppVersion> {
            Thread.sleep(1600)
            it.onNext(v)
            it.onComplete()
        }
        NewUpdateActivity.checkVersion(this, api, update)
    }
}