package com.dhy.avc.format

import org.junit.Assert
import org.junit.Test

class PatchVersionTest {
    @Test
    fun testMe() {
        Assert.assertTrue(PatchVersion.invalidFormat(null))
        Assert.assertTrue(PatchVersion.invalidFormat(""))
        val md5 = "54621b46c1664db5ba7127d8f22aff00"
        Assert.assertTrue(PatchVersion.invalidFormat("http//:a.b.c/54621b46c1664db5ba7127d8f22aff00-258v300.bsPatch/apk"))//should end with ".apk"
        Assert.assertTrue(PatchVersion.invalidFormat("http//:a.b.c/54621b46c1664db5ba7127d8f22aff00-258v300.bsPatch.apk"))//need VersionName part

        val url = "http//:a.b.c/1.0v1.1-54621b46c1664db5ba7127d8f22aff00-258v300.bsPatch.apk"
        val v = PatchVersion(url)
        Assert.assertEquals("1.0v1.1-54621b46c1664db5ba7127d8f22aff00-258v300.bsPatch.apk", v.fileName)
        Assert.assertTrue(v.matchMd5(md5))
        Assert.assertTrue(v.matchMd5(md5.uppercase()))
        Assert.assertEquals(258, v.oldVersion)
        Assert.assertEquals(300, v.newVersion)
        Assert.assertEquals("1.0", v.oldVersionName)
        Assert.assertEquals("1.1", v.newVersionName)
    }
}