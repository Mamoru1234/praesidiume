package com.github.mamoru.praesidiume.praesidiumeleader.utils

import org.apache.commons.io.FilenameUtils
import java.io.File
import java.lang.IllegalArgumentException

fun File.isChildOf(base: File): Boolean {
    if (!base.isDirectory) {
        throw IllegalArgumentException("Base dir wrong type")
    }
    return this.absolutePath.startsWith(base.absolutePath)
}

fun File.resolve(path: String): File {
    val base = this.absolutePath
    val newName = FilenameUtils.concat(base, path)
    return File(newName)
}
