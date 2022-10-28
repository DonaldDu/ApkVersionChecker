package com.dhy.versionchecker

import org.junit.Assert
import org.junit.Test
import java.io.File

class MD5UtilTest {
    @Test
    fun testMd5() {
        val tmp = File.createTempFile("tmp", "md5").apply { deleteOnExit() }
        tmp.writeBytes("123".toByteArray())
        Assert.assertEquals("202cb962ac59075b964b07152d234b70", tmp.md5())
    }
}