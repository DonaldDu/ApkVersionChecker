package com.dhy.apkpatch.bsdiff;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class DiffApk {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        System.out.println("开始生成差分包，请等待...");

        Config config = new Config();
        PatchMaterial material = PatchMaterial.getPatchMaterial(config);

        File patchFile = getPatchFile(material);
        String msg = excuteCMDCommand(getDiffCMD(material.oldApk.file, material.newApk.file, patchFile));
        System.out.println(msg);

        long end = System.currentTimeMillis();
        System.out.println("生成差分包成功：" + patchFile.getAbsolutePath() + "，耗时：" + (end - start) / 1000 + "秒");
    }

    private static File getPatchFile(PatchMaterial material) {
        File newApk = material.newApk.file;
        String md5 = SignUtils.getMd5ByFile(newApk);
        int ov = material.oldApk.vc;
        int nv = material.newApk.vc;
        String patchFileName = formatPatchFileName(md5, ov, nv);
        return new File(material.oldApk.file.getParent(), patchFileName);
    }

    private static String getDiffCMD(File oldApk, File newApk, File patchFile) {
        return String.format("bsdiff %s %s %s ", oldApk.getAbsolutePath(), newApk.getAbsolutePath(), patchFile.getAbsolutePath());
    }

    private static String formatPatchFileName(String md5, int oldVersion, int newVersion) {
        return String.format("%s_%dv%d.patch.apk", md5, oldVersion, newVersion);
    }

    private static String excuteCMDCommand(String cmdCommand) {
        StringBuilder stringBuilder = new StringBuilder();
        Process process;
        try {
            process = Runtime.getRuntime().exec(cmdCommand);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
