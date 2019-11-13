package com.dhy.apkpatch.bsdiff;

import com.google.gson.Gson;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigTest {
    @Test
    public void test() {
        String reg = new Config().apkFileNameReg;
        Pattern nameReg = Pattern.compile(reg);
        Matcher matcher = nameReg.matcher("PUCHE-debug_v3.2.1.1.208-c208_master-d695845.apk");
        if (matcher.find()) {
            System.out.println(matcher.group("name"));
            System.out.println(matcher.group("vc"));
            System.out.println(matcher.group("branch"));
        }
    }

    @Test
    public void showJson() {
        System.out.println(new Gson().toJson(new Config()));
    }
}
