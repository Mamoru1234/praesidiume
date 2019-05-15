package com.github.mamoru.praesidiume.praesidiumeleader.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.mamoru.praesidiume.praesidiumeleader.entity.PackageVersionArtifactEntity
import com.github.mamoru.praesidiume.praesidiumeleader.entity.PackageVersionDescriptionEntity
import com.github.mamoru.praesidiume.praesidiumeleader.exception.ClientException
import com.github.mamoru.praesidiume.praesidiumeleader.utils.getObjectNode
import com.github.mamoru.praesidiume.praesidiumeleader.utils.parseSri
import com.github.mamoru.praesidiume.praesidiumeleader.utils.toHexString
import com.github.mamoru.praesidiume.praesidiumeleader.utils.updateFromFile
import org.apache.commons.io.IOUtils
import org.hibernate.Session
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext


@Service
class NpmPackageBuilder(
        private val npmClientProvider: NpmClientProvider
) {
    @PersistenceContext
    lateinit var entityManager: EntityManager

    fun buildPackage(metadata: ObjectNode): ObjectNode {
        println("Building package")
        println(metadata)
        return metadata
    }

    fun buildPackage(
            packageVersionDescriptionEntity: PackageVersionDescriptionEntity,
            params: ObjectNode
    ): PackageVersionArtifactEntity {
        println("Building package ${packageVersionDescriptionEntity.name} ${packageVersionDescriptionEntity.version}")
        println(packageVersionDescriptionEntity.name)
        val response = npmClientProvider.getClient()
                .getPackageVersionMeta(packageVersionDescriptionEntity.name, packageVersionDescriptionEntity.version)
                .execute()
        if (!response.isSuccessful) {
            throw ClientException("cannot get package metadata")
        }
        val rawMetadata = response.body()
                ?: throw ClientException("Body in meta should be present")
        return rebuildUsualPackage(packageVersionDescriptionEntity, params, rawMetadata as ObjectNode)
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

    fun downloadNpmSource(metadata: ObjectNode): File {
        val dist = getObjectNode(metadata, arrayOf("dist"))
        val tarballUrl = dist.get("tarball").asText()
        val fileName = "${metadata.get("name").asText()}-${metadata.get("version").asText()}.tgz"
        val fileStorePath = "/home/alexei/temp/dyplom_storage"
        val packageFile = File("$fileStorePath/$fileName")
        if (!packageFile.exists()) {
            val response = npmClientProvider.getClient().downloadArchive(tarballUrl).execute()
            if (!response.isSuccessful) {
                throw ClientException("Cannot download $tarballUrl")
            }
            IOUtils.copyLarge(response.body()!!.byteStream(), FileOutputStream(packageFile))
        }
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
