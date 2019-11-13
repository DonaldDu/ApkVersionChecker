package com.dhy.versionchecker;

/**
 * 类说明： 	APK Patch工具类
 *
 * @author Cundong
 * @version 1.0
 */
public class PatchUtils {
    static {
        System.loadLibrary("ApkPatchLibrary");
    }

    /**
     * native方法 使用路径为oldApkPath的apk与路径为patchPath的补丁包，合成新的apk，并存储于newApkPath
     * <p>
     * 返回：0，说明操作成功
     *
     * @param oldApkPath 示例:/sdcard/old.apk
     * @param newApkPath 示例:/sdcard/new.apk
     * @param patchPath  示例:/sdcard/xx.patch
     */
    public static native int patch(String oldApkPath, String newApkPath, String patchPath);

    public static native int patchTest(String oldApkPath, String newApkPath, String patchPath);
}