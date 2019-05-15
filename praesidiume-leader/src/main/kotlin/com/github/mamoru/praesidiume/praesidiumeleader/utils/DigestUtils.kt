package com.github.mamoru.praesidiume.praesidiumeleader.utils

import org.apache.commons.io.IOUtils
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.lang.IllegalArgumentException
import java.security.MessageDigest
import java.util.*

fun MessageDigest.updateFromFile(file: File) {
    BufferedInputStream(FileInputStream(file)).use { stream ->
        val buffer = ByteArray(4096)
        var read: Int
        do {
            read = IOUtils.read(stream, buffer)
            this.update(buffer, 0, read)
        } while (read == buffer.size)
    }
}

fun parseSri(sri: String): Pair<MessageDigest, String> {
    val (algorithm, encodedVerification) = sri.split("-")
    val digest = when (algorithm) {
        "sha1" -> MessageDigest.getInstance("sha-1")
        "sha512" -> MessageDigest.getInstance("sha-512")
        else -> throw IllegalArgumentException("unknown sri algorithm")
    }
    return digest to Base64.getDecoder().decode(encodedVerification).toHexString()
}

fun ByteArray.toHexString(): String {
    return Formatter().use { formatter ->
        for (b in this) {
            formatter.format("%02x", b)
        }
        return@use formatter.toString()
    }
}

fun MessageDigest.toHexString(): String {
    return this.digest().toHexString()
}
