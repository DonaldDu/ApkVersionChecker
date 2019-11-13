package com.dhy.apkpatch.bsdiff;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatchMaterial {
    ApkFile newApk;
    ApkFile oldApk;

    public boolean isValid() {
        return newApk.name.equals(oldApk.name) && newApk.branch.equals(oldApk.branch);
    }

    static PatchMaterial getPatchMaterial(Config config) {
        Pattern nameReg = Pattern.compile(config.apkFileNameReg);
        ApkFile newApk = getApk(nameReg, config.oldApkFolder);
        ApkFile oldApk = getApk(nameReg, config.newApkFolder);
        if (newApk == null) throw new IllegalArgumentException("newApk not found");
        if (oldApk == null) throw new IllegalArgumentException("oldApk not found");
        if (newApk.vc < oldApk.vc) {
            ApkFile temp = newApk;
            newApk = oldApk;
            oldApk = temp;
        }
        PatchMaterial patchFile = new PatchMaterial();
        patchFile.newApk = newApk;
        patchFile.oldApk = oldApk;
        return patchFile;
    }

    private static ApkFile getApk(Pattern nameReg, String folder) {
        File file = new File(folder);
        if (file.list() == null) return null;
        return Arrays.stream(Objects.requireNonNull(file.list())).map(name -> {
            Matcher matcher = nameReg.matcher(name);
            if (matcher.find()) {
                ApkFile apkFile = new ApkFile();
                apkFile.name = matcher.group("name");
                apkFile.vc = Integer.parseInt(matcher.group("vc"));
                apkFile.branch = matcher.group("branch");
                apkFile.file = new File(file, name);
                return apkFile;
            } else return null;
        }).filter(Objects::nonNull).findFirst().get();
    }
}
