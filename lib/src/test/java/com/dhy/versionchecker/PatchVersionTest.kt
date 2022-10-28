package com.dhy.versionchecker

import org.junit.Assert
import org.junit.Test

class PatchVersionTest {
    @Test
    fun testMe() {
        Assert.assertTrue(PatchVersion.invalidFormat(null))
        Assert.assertTrue(PatchVersion.invalidFormat(""))
        Assert.assertTrue(PatchVersion.invalidFormat("http//:a.b.c/54621B46C1664DB5BA7127D8F22AFF00_258v300.bsPatch/apk"))

        //差分包文件名：{MD5}_{旧代码号}v{新代码号}：54621B46C1664DB5BA7127D8F22AFF00_258v300.bsPatch.apk
        val url = "http//:a.b.c/54621B46C1664DB5BA7127D8F22AFF00_258v300.bsPatch.apk"
        val v = PatchVersion(url)
        Assert.assertEquals("54621B46C1664DB5BA7127D8F22AFF00_258v300.bsPatch.apk", v.name)
        Assert.assertEquals(258, v.oldVersion)
        Assert.assertEquals(300, v.newVersion)
    }
}