package com.github.mamoru.praesidiume.praesidiumeleader.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.mamoru.praesidiume.praesidiumeleader.dao.PackageEntityDao
import com.github.mamoru.praesidiume.praesidiumeleader.dao.PackageVersionArtifactDao
import com.github.mamoru.praesidiume.praesidiumeleader.dao.PackageVersionDescriptionDao
import com.github.mamoru.praesidiume.praesidiumeleader.dto.PackageDto
import com.github.mamoru.praesidiume.praesidiumeleader.dto.PackageVersionDescriptionDto
import com.github.mamoru.praesidiume.praesidiumeleader.dto.PackageVersionDto
import com.github.mamoru.praesidiume.praesidiumeleader.entity.PackageEntity
import com.github.mamoru.praesidiume.praesidiumeleader.entity.PackageVersionArtifactEntity
import com.github.mamoru.praesidiume.praesidiumeleader.entity.PackageVersionDescriptionEntity
import com.github.mamoru.praesidiume.praesidiumeleader.exception.ClientException
import org.springframework.stereotype.Service
import java.io.File

@Service
class PackageArtifactService(
        private val npmClientProvider: NpmClientProvider,
        private val npmPackageBuilder: NpmPackageBuilder,
        private val packageVersionArtifactDao: PackageVersionArtifactDao,
        private val packageVersionDescriptionDao: PackageVersionDescriptionDao,
        private val packageEntityDao: PackageEntityDao,
        private val npmPackageParser: NpmPackageParser
) {
    private val mapper: ObjectMapper = jacksonObjectMapper()
    fun getPackageMeta(packageName: String): PackageDto {
        val packageEntityOpt = packageEntityDao.findByName(packageName)
        if (packageEntityOpt.isPresent) {
            return convertPackageToDto(packageEntityOpt.get())
        }
        val npmClient = npmClientProvider.getClient()
        val body = npmClient.getPackageMeta(packageName)
                .execute().body()
        val rawMetadata = body ?: throw ClientException("Package metadata is null")
        if (rawMetadata.nodeType != JsonNodeType.OBJECT) {
            throw ClientException("Metadata should be object")
        }
        val metadata = rawMetadata as ObjectNode
        val packageEntity = npmPackageParser.ensurePackage(metadata)
        return convertPackageToDto(packageEntity)
    }

    fun getArtifact(
            params: Map<String, String>,
            packageName: String,
            packageVersion: String
    ): PackageVersionDto {
        val parametersNode = mapper.valueToTree<ObjectNode>(params)
        val artifactOpt = packageVersionArtifactDao.findByNameAndVersionAndParameters(
                name = packageName,
                version = packageVersion,
                parameters = parametersNode
        )
        if (artifactOpt.isPresent) {
            return convertArtifactToDto(artifactOpt.get())
        }
        val packageVersionDescriptionOpt = packageVersionDescriptionDao.findByNameAndVersion(packageName, packageVersion)
        if (packageVersionDescriptionOpt.isEmpty) {
            throw ClientException("Description for $packageName $packageVersion not found")
        }
        val packageArtifact = npmPackageBuilder.buildPackage(packageVersionDescriptionOpt.get(), parametersNode)
        packageVersionArtifactDao.save(packageArtifact)
        return convertArtifactToDto(packageArtifact)
    }

    fun getArtifactContent(
            params: Map<String, String>,
            packageName: String,
            packageVersion: String
    ): File {
        val parametersNode = mapper.valueToTree<ObjectNode>(params)
        val artifact = packageVersionArtifactDao.findByNameAndVersionAndParameters(
                name = packageName,
                version = packageVersion,
                parameters = parametersNode
        ).orElseThrow {
            ClientException("No artifact found")
        }
        return npmPackageBuilder.getPackageContent(artifact)
    }

    fun convertPackageToDto(packageEntity: PackageEntity): PackageDto {
        return PackageDto(
                name = packageEntity.name,
                versions = packageEntity.versions.map { convertPackageDescriptionToDto(it) }
        )
    }

    fun convertArtifactToDto(artifactEntity: PackageVersionArtifactEntity): PackageVersionDto {
        return PackageVersionDto(
                name = artifactEntity.name,
                version = artifactEntity.version,
                parameters = artifactEntity.parameters,
                dependencies = artifactEntity.dependencies,
                integrity = artifactEntity.integrity
        )
    }

    fun convertPackageDescriptionToDto(packageVersionDescriptionEntity: PackageVersionDescriptionEntity): PackageVersionDescriptionDto {
        return PackageVersionDescriptionDto(
                name = packageVersionDescriptionEntity.name,
                version = packageVersionDescriptionEntity.version,
                dependencies = packageVersionDescriptionEntity.dependencies,
                parameters = packageVersionDescriptionEntity.parameters
        )
    }
}
