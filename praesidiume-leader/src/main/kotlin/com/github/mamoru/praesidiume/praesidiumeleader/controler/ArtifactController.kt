package com.github.mamoru.praesidiume.praesidiumeleader.controler

import com.github.mamoru.praesidiume.praesidiumeleader.dto.PackageDto
import com.github.mamoru.praesidiume.praesidiumeleader.dto.PackageVersionDto
import com.github.mamoru.praesidiume.praesidiumeleader.service.PackageArtifactService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/artifact")
class ArtifactController(
        private val artifactService: PackageArtifactService
) {
    @GetMapping("/metadata/{packageName}")
    fun getPackageMetadata(
            @PathVariable("packageName") packageName: String
    ): PackageDto {
        return artifactService.getPackageMeta(packageName)
    }

    @GetMapping("/metadata/{packageName}/{packageVersion}")
    fun getMetadata(
            @RequestParam params: Map<String, String>,
            @PathVariable("packageName") packageName: String,
            @PathVariable("packageVersion") packageVersion: String
    ): PackageVersionDto {
        return artifactService.getArtifact(params, packageName, packageVersion)
    }
}
