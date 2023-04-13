package com.dhy.avc.format

object FullApk {
    /**
     * {appShortName}-v{versionName}_c{versionCode}_{gitVersion}-{MD5}.apk, eg: "ECT-v1.0_c1_fs465-202cb962ac59075b964b07152d234b70.apk"
     */
    @JvmStatic
    fun formatName(appShortName: String, versionName: String, versionCode: Int, gitVersion: String, apkMd5: String): String {
        return String.format("%s-v%s_c%d_%s-%s.apk", appShortName, versionName, versionCode, gitVersion, apkMd5)
    }
}