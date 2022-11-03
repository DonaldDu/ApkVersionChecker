package com.dhy.avc.format

enum class FileType(private val offset: Int) {
    RAW_APK(0),
    APK_BINARY_PATCH(1000),
    DATA_FILE(2000),
    HOTFIX_FULL_PATCH(3000);

    fun getType(appType: Int): Int {
        return offset + appType
    }
}