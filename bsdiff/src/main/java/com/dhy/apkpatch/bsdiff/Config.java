package com.dhy.apkpatch.bsdiff;

public class Config {
    public String newApkFolder = "D:\\ANETU3\\app\\build\\outputs\\apk\\release";
    public String oldApkFolder = "C:\\Users\\Donald\\Videos\\";

    /**
     * PUCHE-debug_v3.2.1.1.208-c208_master-d695845.apk
     */
    public String apkFileNameReg = "(?<name>[\\d\\w-]+)_(v[\\d.]+(-c(?<vc>\\d+)))_((?<branch>[\\d\\w]+)-[\\d\\w]+).apk$";
}
