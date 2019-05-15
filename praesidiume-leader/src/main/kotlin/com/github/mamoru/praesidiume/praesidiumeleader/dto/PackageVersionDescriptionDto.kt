package com.github.mamoru.praesidiume.praesidiumeleader.dto

import com.fasterxml.jackson.databind.node.ObjectNode

data class PackageVersionDescriptionDto(
        val name: String,
        val version: String,
        val parameters: Set<String>,
        val dependencies: ObjectNode
)
