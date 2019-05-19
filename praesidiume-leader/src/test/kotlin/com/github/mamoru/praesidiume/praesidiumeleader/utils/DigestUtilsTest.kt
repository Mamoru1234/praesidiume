package com.github.mamoru.praesidiume.praesidiumeleader.utils

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File
import java.security.MessageDigest

class DigestUtilsTest {
    @Test
    fun testSha1Digest() {
        val file = File("/home/alexei/temp/dyplom_storage/lodash-4.17.11.tgz")
        val messageDigest = MessageDigest.getInstance("sha1")
        messageDigest.updateFromFile(file)
        Assertions.assertThat(messageDigest.toHexString()).isEqualTo("b39ea6229ef607ecd89e2c8df12536891cac9b8d")
    }

    @Test
    fun testSha512Digest() {
        val file = File("/home/alexei/temp/dyplom_storage/lodash-4.17.11.tgz")
        val messageDigest = MessageDigest.getInstance("sha-512")
        messageDigest.updateFromFile(file)
        Assertions.assertThat(messageDigest.toHexString())
                .isEqualTo("7102a1f22828e5052167b960dfc0d8580c4cbe3480286d00f3019256298fd3b4885042b650ef9aad244a6d1656b5e94cb4de55d07930879af23ada3f4ac85822")
    }

    @Test
    fun testParseSri() {
        val sri = "sha512-cQKh8igo5QUhZ7lg38DYWAxMvjSAKG0A8wGSVimP07SIUEK2UO+arSRKbRZWtelMtN5V0Hkwh5ryOto/SshYIg=="
        val (digest, verifyText) = parseSri(sri)
        Assertions.assertThat(verifyText)
                .isEqualTo("7102a1f22828e5052167b960dfc0d8580c4cbe3480286d00f3019256298fd3b4885042b650ef9aad244a6d1656b5e94cb4de55d07930879af23ada3f4ac85822")
        val file = File("/home/alexei/temp/dyplom_storage/lodash-4.17.11.tgz")
        digest.updateFromFile(file)
        Assertions.assertThat(digest.toHexString())
                .isEqualTo(verifyText)
    }
}
