package com.github.mamoru.praesidiume.praesidiumeleader.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.mamoru.praesidiume.praesidiumeleader.entity.PackageVersionArtifactEntity
import com.github.mamoru.praesidiume.praesidiumeleader.entity.PackageVersionDescriptionEntity
import com.github.mamoru.praesidiume.praesidiumeleader.exception.ClientException
import com.github.mamoru.praesidiume.praesidiumeleader.utils.*
import org.apache.commons.io.IOUtils
import org.hibernate.Session
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.*
import javax.annotation.PostConstruct
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext


@Service
class NpmPackageBuilder(
        private val preGypPackageProcessor: PreGypPackageProcessor,
        private val npmClientProvider: NpmClientProvider
) {
    private final val tempStorageDir = File("/home/alexei/temp/dyplom_storage")
    private final val tempPackagesDir = tempStorageDir.resolve("packages")
    private final val tempContentDir = tempStorageDir.resolve("content")

    @PersistenceContext
    lateinit var entityManager: EntityManager

    @PostConstruct
    fun init() {
        tempPackagesDir.mkdirs()
        tempContentDir.mkdirs()
    }

    fun buildPackage(
            packageVersionDescriptionEntity: PackageVersionDescriptionEntity,
            params: ObjectNode
    ): PackageVersionArtifactEntity {
        println("Building package ${packageVersionDescriptionEntity.name} ${packageVersionDescriptionEntity.version}")
        val metadata = getPackageMetadata(packageVersionDescriptionEntity)
        if (packageVersionDescriptionEntity.parameters.isEmpty()) {
            return rebuildUsualPackage(packageVersionDescriptionEntity, params, metadata)
        }
        println("Considering as native package")
        val npmSource = downloadNpmSource(metadata)
        val resultSource = preGypPackageProcessor.processPackage(
                packageVersionDescriptionEntity, metadata, params, npmSource)
        println("Sources combined")
        return createArtifact(packageVersionDescriptionEntity, params, resultSource)
    }

    private fun getPackageMetadata(packageVersionDescriptionEntity: PackageVersionDescriptionEntity): ObjectNode {
        val response = npmClientProvider.getClient()
                .getPackageVersionMeta(packageVersionDescriptionEntity.name, packageVersionDescriptionEntity.version)
                .execute()
        if (!response.isSuccessful) {
            throw ClientException("cannot get package metadata")
        }
        val rawMetadata = response.body()
                ?: throw ClientException("Body in meta should be present")
        return rawMetadata as ObjectNode
    }

    fun rebuildUsualPackage(
            packageVersionDescriptionEntity: PackageVersionDescriptionEntity,
            params: ObjectNode,
            metadata: ObjectNode): PackageVersionArtifactEntity {
        val npmSource = downloadNpmSource(metadata)
        return createArtifact(packageVersionDescriptionEntity, params, npmSource)
    }

    fun createArtifact(
            packageVersionDescriptionEntity: PackageVersionDescriptionEntity,
            params: ObjectNode,
            content: File
    ): PackageVersionArtifactEntity {
        val session = entityManager.unwrap(Session::class.java)
        val blob = session.lobHelper.createBlob(FileInputStream(content), content.length())
        val digest = MessageDigest.getInstance("sha-512")
        digest.updateFromFile(content)
        val encodeToString = Base64.getEncoder().encodeToString(digest.digest())
        val integrity = "sha512-$encodeToString"
        return PackageVersionArtifactEntity(
                name = packageVersionDescriptionEntity.name,
                dependencies = packageVersionDescriptionEntity.dependencies,
                integrity = integrity,
                parameters = params,
                version = packageVersionDescriptionEntity.version,
                content = blob,
                packageVersionEntity = packageVersionDescriptionEntity
        )
    }

    @Transactional
    fun getPackageContent(packageVersionArtifactEntity: PackageVersionArtifactEntity): File {
        val packageName = "${packageVersionArtifactEntity.name}-${packageVersionArtifactEntity.version}.tgz"
        val content = tempContentDir.resolve(packageName)
        if (content.exists()) {
            return content
        }
        IOUtils.copyLarge(packageVersionArtifactEntity.content.binaryStream, FileOutputStream(content))
        return content
    }

    fun downloadNpmSource(metadata: ObjectNode): File {
        val dist = getObjectNode(metadata, arrayOf("dist"))
        val tarballUrl = dist.get("tarball").asText()
        val fileName = "${metadata.get("name").asText()}-${metadata.get("version").asText()}.tgz"
        val packageFile = tempPackagesDir.resolve(fileName)
        npmClientProvider.downloadFile(packageFile, tarballUrl)
        verifyIntegrity(dist, packageFile)
        return packageFile
    }

    fun verifyIntegrity(dist: ObjectNode, file: File) {
        if (dist.has("integrity")) {
            val sri = dist.get("integrity").asText()
            val (digest, verifyText) = parseSri(sri)
            digest.updateFromFile(file)
            val hexString = digest.toHexString()
            if (hexString != verifyText) {
                throw ClientException("Integrity verification failed expected: $verifyText actual: $hexString")
            }
            return
        }
        if (dist.has("shasum")) {
            val verifyText = dist.get("shasum").asText()
            val digest = MessageDigest.getInstance("sha-1")
            digest.updateFromFile(file)
            val hexString = digest.toHexString()
            if (hexString != verifyText) {
                throw ClientException("Integrity verification failed expected: $verifyText actual: $hexString")
            }
        }
        throw ClientException("Unknown dist integrity")
    }
}
