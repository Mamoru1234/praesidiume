package com.github.mamoru.praesidiume.praesidiumeleader.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.mamoru.praesidiume.praesidiumeleader.exception.ClientException
import com.github.mamoru.praesidiume.praesidiumeleader.utils.getObjectNode
import org.springframework.stereotype.Service

val CLIENT_SIDE_SCRIPTS = listOf(
        "install",
        "postinstall",
        "preinstall",
        "prepublish",
        "prepare",
        "uninstall"
)

@Service
class NpmPackageParser {
    fun hasClientScripts(rawMetadata: JsonNode): Boolean {
        if (rawMetadata.nodeType != JsonNodeType.OBJECT) {
            throw ClientException("Metadata should be object")
        }
        val metadata = rawMetadata as ObjectNode
        if (!metadata.has("scripts")) {
            return false
        }
        val scriptsNode = getObjectNode(metadata, arrayOf("scripts"))
        return CLIENT_SIDE_SCRIPTS.any { scriptsNode.has(it) }
    }
    fun isPreBuildSupported(rawMetadata: JsonNode): Boolean {
        if (rawMetadata.nodeType != JsonNodeType.OBJECT) {
            throw ClientException("Metadata should be object")
        }
        val metadata = rawMetadata as ObjectNode
        if (!metadata.has("binary")) {
            return false
        }
        val scripts = getObjectNode(metadata, arrayOf("scripts"))
        if (!scripts.get("install").textValue().contains("node-pre-gyp")) {
            return false
        }
        val binary = getObjectNode(metadata, arrayOf("binary"))
        return binary.has("module_name") && binary.has("module_path") && binary.has("host")
    }
}
