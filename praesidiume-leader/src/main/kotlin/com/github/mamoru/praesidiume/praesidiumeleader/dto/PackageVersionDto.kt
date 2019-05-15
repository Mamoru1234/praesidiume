package com.github.mamoru.praesidiume.praesidiumeleader.dto

import com.fasterxml.jackson.databind.node.ObjectNode

data class PackageVersionDto(
        val name: String,
        val version: String,
        val integrity: String,
        val parameters: ObjectNode,
        val dependencies: ObjectNode
)
