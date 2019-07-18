package com.example.apkversionchecker

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.dhy.versionchecker.NewUpdateActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val v = AppVersion()
        buttonCommit.setOnClickListener {
            NewUpdateActivity.showNewVersion(this, v)
            NewUpdateActivity.showNewVersion(this, v)
            NewUpdateActivity.showNewVersion(this, v)
        }
        isForceUpdate.setOnCheckedChangeListener { _, isChecked ->
            AppVersion.forceUpdate = isChecked
        }
        passIfAlreadyDownloadCompleted.setOnCheckedChangeListener { _, isChecked ->
            AppVersion.pass = isChecked
        }
    }
}
