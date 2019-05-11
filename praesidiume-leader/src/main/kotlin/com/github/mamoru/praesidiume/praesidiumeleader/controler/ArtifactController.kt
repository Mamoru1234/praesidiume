package com.github.mamoru.praesidiume.praesidiumeleader.controler

import com.fasterxml.jackson.databind.JsonNode
import com.github.mamoru.praesidiume.praesidiumeleader.exception.ClientException
import com.github.mamoru.praesidiume.praesidiumeleader.service.NpmClientProvider
import com.github.mamoru.praesidiume.praesidiumeleader.service.NpmPackageParser
import com.github.mamoru.praesidiume.praesidiumeleader.service.PreGypParser
import com.github.mamoru.praesidiume.praesidiumeleader.utils.getObjectNode
import org.springframework.web.bind.annotation.*

val metadataProps = listOf("name", "version")

@RestController
@RequestMapping("/artifact")
class ArtifactController(
        private val npmClientProvider: NpmClientProvider,
        private val npmPackageParser: NpmPackageParser,
        private val preGypParser: PreGypParser
) {
    @GetMapping("/metadata/{packageName}/{packageVersion}")
    fun getMetadata(
            @RequestParam params: Map<String, String>,
            @PathVariable("packageName") packageName: String,
            @PathVariable("packageVersion") packageVersion: String
    ): JsonNode? {
        val npmClient = npmClientProvider.getClient()
        val body = npmClient.getPackageMeta(packageName, packageVersion)
                .execute().body()
        val metadata = body ?: throw ClientException("Package metadata is null")
        val hasScripts = npmPackageParser.hasClientScripts(metadata)
        println("Scripts: $hasScripts")
        val prebuild = npmPackageParser.isPreBuildSupported(metadata)
        println("Is prebuild supported: $prebuild")
        if (!prebuild) {
            return metadata
        }
        val binary = getObjectNode(metadata, arrayOf("binary"))
        val requiredProps = preGypParser.getRequiredProperties(binary) - metadataProps
        println("props: $requiredProps")
        val hasAllProps = requiredProps.all { params.containsKey(it) }
        println("Has all props: $hasAllProps")
        if (!hasAllProps) {
            return metadata
        }
        val resultParams = params + getMetadataProps(metadata)
        println("Result url: ${preGypParser.buildTarUrl(binary, resultParams)}")
        return metadata
    }

    private fun getMetadataProps(metadata: JsonNode): Map<String, String> {
        return mapOf(
            "name" to metadata.get("name").textValue(),
            "version" to metadata.get("version").textValue()
        )
    }
}
