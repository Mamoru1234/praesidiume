package com.github.mamoru.praesidiume.praesidiumeleader.controler

import com.fasterxml.jackson.databind.JsonNode
import com.github.mamoru.praesidiume.praesidiumeleader.service.NpmClientProvider
import com.github.mamoru.praesidiume.praesidiumeleader.service.NpmPackageParser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.RuntimeException

@RestController
@RequestMapping("/artifact")
class ArtifactController(
        private val npmClientProvider: NpmClientProvider,
        private val npmPackageParser: NpmPackageParser
) {
    @GetMapping("/metadata/{packageName}/{packageVersion}")
    fun getMetadata(
            @PathVariable("packageName") packageName: String,
            @PathVariable("packageVersion") packageVersion: String
    ): JsonNode? {
        val npmClient = npmClientProvider.getClient()
        val metadata = npmClient.getPackageMeta(packageName, packageVersion)
                .execute().body() ?: throw RuntimeException("Package metadata is null")
        val hasScripts = npmPackageParser.hasClientScripts(metadata)
        println(hasScripts)
        return npmClient.getPackageMeta(packageName, packageVersion).execute().body()
    }
}
