package com.example.apkversionchecker

import android.app.Activity
import com.dhy.avc.format.FileType
import com.dhy.avc.format.PatchVersion
import com.dhy.versionchecker.Rx3Wrapper
import com.dhy.versionchecker.VersionUtil
import com.dhy.versionchecker.isNew
import com.example.apkversionchecker.data.AppVersion
import com.example.apkversionchecker.data.ResponseW
import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit


interface VersionApi {
    @GET("admin/versioninfo/get")
    fun checkVersion(@Query("packagename") packageName: String, @Query("versiontype") versionType: Int): Observable<ResponseW<AppVersion?>>
}

object Apis {
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(APK_UPDATE_URL)
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: VersionApi by lazy {
        retrofit.create(VersionApi::class.java)
    }

    private val APK_UPDATE_URL: String by lazy {
        try {
            val f = BuildConfig::class.java.getDeclaredField("APK_UPDATE_URL").apply { isAccessible = true }
            f.get(null) as String
        } catch (e: Exception) {
            "http://www.avc.com"
        }
    }
}

private val APK_TYPE_CODE: Int by lazy {
    try {
        val f = BuildConfig::class.java.getDeclaredField("APK_TYPE_CODE").apply { isAccessible = true }
        f.get(null) as Int
    } catch (e: Exception) {
        0
    }
}

fun Activity.checkAppVersion() {
    val api = Rx3Wrapper { checkVersion() }
    VersionUtil.checkVersion(this, api, false, null)
}

private fun checkVersion(): Observable<AppVersion> {
    return Apis.api.checkVersion(BuildConfig.APPLICATION_ID, APK_TYPE_CODE)
        .map {
            it.data ?: AppVersion()
        }.flatMap {
            if (it.isNew) {
                checkPatchVersion(it)
            } else {
                Observable.just(it)
            }
        }
}

private fun checkPatchVersion(fullApkUpdate: AppVersion): Observable<AppVersion> {
    val patchType = FileType.APK_BINARY_PATCH.getType(APK_TYPE_CODE)
    return Apis.api.checkVersion(BuildConfig.APPLICATION_ID, patchType).map {
        val patchUrl = it.data?.url
        if (PatchVersion.isValidFormat(patchUrl)) {
            val patchVersion = PatchVersion(patchUrl!!)
            if (patchVersion.isNew(fullApkUpdate)) {
                val patch = it.data!!
                fullApkUpdate.patchUrl = patchUrl
                fullApkUpdate.patchSize = patch.size
            }
        }
        fullApkUpdate
    }
}