package com.dhy.versionchecker

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest

/**
 * hex in lower case
 * */
fun File.md5(): String {
    return FileInputStream(this).md5()
}

/**
 * hex in lower case, auto close stream
 * */
fun InputStream.md5(): String {
    return digest(DigestAlgorithm.MD5.algorithm)
}

/**
 * hex in lower case, auto close stream
 * */
fun InputStream.digest(algorithm: String): String {
    return digestToBytes(algorithm).toHex()
}

/**
 * hex in lower case, auto close stream
 * */
fun InputStream.digestToBytes(algorithm: String, bufferSize: Int = 1024 * 4 * 4): ByteArray {
    use {
        val digest = MessageDigest.getInstance(algorithm)
        val buffer = ByteArray(bufferSize)
        while (true) {
            val size = read(buffer)
            if (size == -1) break
            digest.update(buffer, 0, size)
        }
        close()
        return digest.digest()
    }
}

/**
 * hex in lower case
 * */
fun ByteArray.toHex(): String {
    val hex = BigInteger(1, this).toString(16)
    return hex.padStart(size * 2, '0')
}

enum class DigestAlgorithm(val algorithm: String) {
    MD5("MD5"),
    SHA1("SHA-1"),
    SHA256("SHA-256")
}