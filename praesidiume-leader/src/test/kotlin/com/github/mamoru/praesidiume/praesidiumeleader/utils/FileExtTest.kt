package com.github.mamoru.praesidiume.praesidiumeleader.utils

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

class FileExtTest {
    @Test
    fun isChildOfFalse() {
        val base = File("/home/alexei/temp")
        val another = base.resolve("../test")
        Assertions.assertThat(another.isChildOf(base)).isFalse()
    }
    @Test
    fun isChildOfTrue() {
        val base = File("/home/alexei/temp")
        val another = base.resolve("./test/../test")
        Assertions.assertThat(another.isChildOf(base)).isTrue()
    }
}
