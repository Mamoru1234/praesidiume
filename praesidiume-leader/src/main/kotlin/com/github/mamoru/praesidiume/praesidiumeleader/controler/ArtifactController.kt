package com.github.mamoru.praesidiume.praesidiumeleader.controler

import com.fasterxml.jackson.databind.node.JsonNodeType
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.mamoru.praesidiume.praesidiumeleader.exception.ClientException
import com.github.mamoru.praesidiume.praesidiumeleader.service.NpmClientProvider
import com.github.mamoru.praesidiume.praesidiumeleader.service.NpmPackageBuilder
import com.github.mamoru.praesidiume.praesidiumeleader.service.NpmPackageParser
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/artifact")
class ArtifactController(
        private val npmClientProvider: NpmClientProvider,
        private val npmPackageParser: NpmPackageParser,
        private val npmPackageBuilder: NpmPackageBuilder
) {
    @GetMapping("/metadata/{packageName}")
    fun getPackageMetadata(
            @PathVariable("packageName") packageName: String
    ) {
        val npmClient = npmClientProvider.getClient()
        val body = npmClient.getPackageMeta(packageName)
                .execute().body()
        val rawMetadata = body ?: throw ClientException("Package metadata is null")
        if (rawMetadata.nodeType != JsonNodeType.OBJECT) {
            throw ClientException("Metadata should be object")
        }
        val metadata = rawMetadata as ObjectNode
        npmPackageParser.ensurePackage(metadata)
    }

    @GetMapping("/metadata/{packageName}/{packageVersion}")
    fun getMetadata(
            @RequestParam params: Map<String, String>,
            @PathVariable("packageName") packageName: String,
            @PathVariable("packageVersion") packageVersion: String
    ): ObjectNode? {
        val npmClient = npmClientProvider.getClient()
        val body = npmClient.getPackageVersionMeta(packageName, packageVersion)
                .execute().body()
        val rawMetadata = body ?: throw ClientException("Package metadata is null")
        if (rawMetadata.nodeType != JsonNodeType.OBJECT) {
            throw ClientException("Metadata should be object")
        }
        val metadata = rawMetadata as ObjectNode
        return npmPackageBuilder.buildPackage(metadata)
    }
}
