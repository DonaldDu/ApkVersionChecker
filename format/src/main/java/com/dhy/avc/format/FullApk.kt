package com.dhy.avc.format

object FullApk {
    /**
     * {appShortName}_v{versionName}_c{versionCode}_{MD5}.apk, eg: "ECT_v1.0_c1-202cb962ac59075b964b07152d234b70.apk"
     */
    @JvmStatic
    fun formatName(appShortName: String, versionName: String, versionCode: Int, apkMd5: String): String {
        return String.format("%s_v%s_c%d-%s.apk", appShortName, versionName, versionCode, apkMd5)
    }
}