package com.dhy.versionchecker

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dhy.avc.format.PatchVersion
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PatchVersionTest {
    @Test
    fun testMe() {
        Assert.assertTrue(PatchVersion.invalidFormat(null))
        Assert.assertTrue(PatchVersion.invalidFormat(""))
        val md5 = "54621b46c1664db5ba7127d8f22aff00"
        Assert.assertTrue("should end with '.apk' ", PatchVersion.invalidFormat("http//:a.b.c/1.0@258v1.1@300-54621b46c1664db5ba7127d8f22aff00.bsPatch/apk"))
        Assert.assertTrue("need VersionName part", PatchVersion.invalidFormat("http//:a.b.c/54621b46c1664db5ba7127d8f22aff00.bsPatch.apk"))

        val url = "http//:a.b.c/1.0@258v1.1@300-54621b46c1664db5ba7127d8f22aff00.bsPatch.apk"
        val v = PatchVersion.parse(url)
        Assert.assertEquals("1.0@258v1.1@300-54621b46c1664db5ba7127d8f22aff00.bsPatch.apk", v.fileName)
        Assert.assertTrue(v.matchMd5(md5))
        Assert.assertTrue(v.matchMd5(md5.uppercase()))
        Assert.assertEquals(258, v.oldVersion)
        Assert.assertEquals(300, v.newVersion)
        Assert.assertEquals("1.0", v.oldVersionName)
        Assert.assertEquals("1.1", v.newVersionName)
    }
}