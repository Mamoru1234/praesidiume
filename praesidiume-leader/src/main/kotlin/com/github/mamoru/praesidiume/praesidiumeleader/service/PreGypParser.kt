package com.github.mamoru.praesidiume.praesidiumeleader.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.mamoru.praesidiume.praesidiumeleader.exception.ClientException
import com.github.mamoru.praesidiume.praesidiumeleader.utils.isChildOf
import com.github.mamoru.praesidiume.praesidiumeleader.utils.resolve
import org.springframework.stereotype.Service
import java.io.File
import java.lang.IllegalArgumentException
import java.net.URI

val regex: Regex = Regex("\\{([a-x_]+)}")

val versioningVars = listOf("remote_path", "module_path", "package_name")

fun fixSlash(path: String): String = if (!path.endsWith("/")) { "$path/" } else { path }

@Service
class PreGypParser {
    fun getRequiredProperties(binary: ObjectNode): List<String> {
        return versioningVars
                .mapNotNull {
                    if (!binary.has(it)) {
                        return@mapNotNull null
                    }
                    return@mapNotNull regex.findAll(binary.get(it).textValue())
                            .map { it.groupValues[1] }
                            .toList()
                }
                .flatten()
                .distinct()
    }

    fun evalPart(part: String, properties: Map<String, String>): String = regex.replace(part) {
        val key = it.groupValues[1]
        return@replace properties[key]
                ?: throw ClientException("$key not found")
    }

    fun processOrDefault(binary: ObjectNode, key: String, properties: Map<String, String>, value: String): String {
        return if (binary.has(key)) {
            evalPart(binary[key].textValue(), properties)
        } else {
            ""
        }
    }

    fun buildTarUrl(binary: ObjectNode, properties: Map<String, String>): String {
        val remotePath = processOrDefault(binary, "remote_path", properties, "")
        val packageName = if (binary.has("package_name")) {
            binary.get("package_name").textValue()
        } else {
            "{module_name}-v{version}-{node_abi}-{platform}-{arch}.tar.gz"
        }
        return URI(fixSlash(binary.get("host").textValue()))
                .resolve(fixSlash(remotePath))
                .resolve(evalPart(packageName, properties))
                .toString()
    }

    fun getBinarySourceDir(binary: ObjectNode, properties: Map<String, String>, packageDir: File): File {
        var modulePath = evalPart(binary.get("module_path").asText(), properties)
        if (modulePath.isBlank()) {
            throw IllegalArgumentException("module path is blank")
        }
        if (!modulePath.endsWith("/")) {
            modulePath += "/"
        }
        modulePath += ".."
        val binarySourceDir = packageDir.resolve(modulePath)
        if (!binarySourceDir.isChildOf(packageDir)) {
            throw IllegalArgumentException("binary source out of package")
        }
        if (!binarySourceDir.mkdirs()) {
            throw IllegalArgumentException("Cannot create binary source dir: ${binarySourceDir.absolutePath}")
        }
        return binarySourceDir
    }
}
