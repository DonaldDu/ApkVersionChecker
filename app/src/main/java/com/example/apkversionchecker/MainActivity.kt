package com.example.apkversionchecker

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.dhy.versionchecker.IUpdateSetting
import com.dhy.versionchecker.NewUpdateActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        isForceUpdate.setOnCheckedChangeListener { _, isChecked ->
            AppVersion.forceUpdate = isChecked
        }
        passIfAlreadyDownloadCompleted.setOnCheckedChangeListener { _, isChecked ->
            UpdateSetting.pass = isChecked
        }

        val v = AppVersion()
        val update = UpdateSetting()
        buttonCommit.setOnClickListener {
            NewUpdateActivity.showNewVersion(this, v, update)
        }

        buttonMultTest.setOnClickListener {
            NewUpdateActivity.showNewVersion(this, v, update)
            buttonCommit.postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
            }, 500)
            buttonCommit.postDelayed({
                NewUpdateActivity.showNewVersion(this, v, update)
            }, 1000)
        }
    }
}

class UpdateSetting : IUpdateSetting {
    companion object {
        var pass = false
    }

    override fun passIfAlreadyDownloadCompleted() = pass
}