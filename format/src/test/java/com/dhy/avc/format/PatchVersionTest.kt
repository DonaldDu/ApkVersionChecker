package com.dhy.avc.format

import org.junit.Assert
import org.junit.Test

class PatchVersionTest {
    @Test
    fun testMe() {
        Assert.assertTrue(PatchVersion.invalidFormat(null))
        Assert.assertTrue(PatchVersion.invalidFormat(""))
        val md5 = "54621b46c1664db5ba7127d8f22aff00"
        Assert.assertTrue(PatchVersion.invalidFormat("http//:a.b.c/$md5-258v300.bsPatch/apk"))

        val url = "http//:a.b.c/$md5-258v300.bsPatch.apk"
        val v = PatchVersion(url)
        Assert.assertEquals("$md5-258v300.bsPatch.apk", v.fileName)
        Assert.assertTrue(v.matchMd5(md5))
        Assert.assertTrue(v.matchMd5(md5.uppercase()))
        Assert.assertEquals(258, v.oldVersion)
        Assert.assertEquals(300, v.newVersion)
    }
}