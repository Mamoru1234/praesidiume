package com.github.mamoru.praesidiume.praesidiumeleader.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.mamoru.praesidiume.praesidiumeleader.utils.getObjectNode
import org.springframework.stereotype.Service

val CLIENT_SIDE_SCRIPTS = listOf(
        "install",
        "preinstall",
        "uninstall"
)

@Service
class NpmPackageParser {
    fun hasClientScripts(rawMetadata: JsonNode): Boolean {
        if (rawMetadata.nodeType != JsonNodeType.OBJECT) {
            throw RuntimeException("Metadata should be object")
        }
        val metadata = rawMetadata as ObjectNode
        if (!metadata.has("scripts")) {
            return false
        }
        val scriptsNode = getObjectNode(metadata, arrayOf("scripts"))
        return CLIENT_SIDE_SCRIPTS.any { scriptsNode.has(it) }
    }
}
