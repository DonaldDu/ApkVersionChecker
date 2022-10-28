package com.dhy.versionchecker

import org.junit.Assert
import org.junit.Test

class PatchVersionTest {
    @Test
    fun testMe() {
        Assert.assertTrue(PatchVersion.invalidFormat(null))
        Assert.assertTrue(PatchVersion.invalidFormat(""))
        Assert.assertTrue(PatchVersion.invalidFormat("http//:a.b.c/bsPatch_54621B46C1664DB5BA7127D8F22AFF00_258v300/apk"))

        //差分包文件名：bsPatch_{MD5}_{旧代码号}v{新代码号}：54621B46C1664DB5BA7127D8F22AFF00_258v300.apk
        val url = "http//:a.b.c/bsPatch_54621B46C1664DB5BA7127D8F22AFF00_258v300.apk"
        val v = PatchVersion(url)
        Assert.assertEquals("bsPatch_54621B46C1664DB5BA7127D8F22AFF00_258v300.apk", v.fileName)
        Assert.assertEquals(258, v.oldVersion)
        Assert.assertEquals(300, v.newVersion)
    }
}