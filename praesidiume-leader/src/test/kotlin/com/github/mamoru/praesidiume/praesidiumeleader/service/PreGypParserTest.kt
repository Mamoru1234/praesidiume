package com.github.mamoru.praesidiume.praesidiumeleader.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class PreGypParserTest {
    @Test
    fun getRequiredResources() {
        val binary = """
            {
                "module_name": "xmljs",
                "module_path": "./build/Release/",
                "host": "https://github.com",
                "remote_path": "./libxmljs/libxmljs/releases/download/v{version}/",
                "package_name": "{node_abi}-{platform}-{arch}.tar.gz"
            }
        """.trim()
        val parser = PreGypParser()
        val result = parser.getRequiredProperties(jacksonObjectMapper().readTree(binary) as ObjectNode)
        Assertions.assertThat(result).containsAll(listOf("version", "arch", "platform", "node_abi"))
    }

    @Test
    fun getBuildUrl() {
        val binary = """
            {
                "module_name": "xmljs",
                "module_path": "./build/Release/",
                "host": "https://github.com",
                "remote_path": "./libxmljs/libxmljs/releases/download/v{version}/",
                "package_name": "{node_abi}-{platform}-{arch}.tar.gz"
            }
        """.trim()
        val parser = PreGypParser()
        val objectNode = jacksonObjectMapper().readTree(binary) as ObjectNode
        val result = parser.buildTarUrl(objectNode, mapOf(
                "version" to "0.19.5",
                "platform" to "linux",
                "arch" to "x64",
                "node_abi" to "node-v64"
        ))
        Assertions.assertThat(result)
                .isEqualTo("https://github.com/libxmljs/libxmljs/releases/download/v0.19.5/node-v64-linux-x64.tar.gz")
    }
}
