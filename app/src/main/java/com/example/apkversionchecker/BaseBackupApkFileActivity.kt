package com.example.apkversionchecker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dhy.xintent.XIntent
import com.dhy.xintent.putSerializableExtra
import com.dhy.xintent.readExtra
import java.io.File

abstract class BaseBackupApkFileActivity : AppCompatActivity() {
    protected var apkFile: File? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            apkFile = XIntent.with(savedInstanceState).readExtra()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        XIntent.with(outState).putSerializableExtra(apkFile)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        apkFile = XIntent.with(savedInstanceState).readExtra()
    }
}