package com.github.mamoru.praesidiume.praesidiumeleader.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.lang.RuntimeException

private fun getNode(node: JsonNode, path: Array<String>): JsonNode = path.fold(node) { cur, property ->
    return@fold cur.get(property) ?: throw RuntimeException("Cannot get by path: $path")
}

fun getArrayNode(node: JsonNode, path: Array<String>): ArrayNode {
    val targetNode = getNode(node, path)
    if (!targetNode.isArray) {
        throw RuntimeException("node: $path is not array")
    }
    return targetNode as ArrayNode
}

fun getObjectNode(node: JsonNode, path: Array<String>): ObjectNode {
    val targetNode = getNode(node, path)
    if (!targetNode.isObject) {
        throw RuntimeException("node: $path is not object")
    }
    return targetNode as ObjectNode
}
