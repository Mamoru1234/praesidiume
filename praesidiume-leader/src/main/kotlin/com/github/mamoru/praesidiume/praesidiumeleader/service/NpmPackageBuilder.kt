package com.github.mamoru.praesidiume.praesidiumeleader.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.mamoru.praesidiume.praesidiumeleader.exception.ClientException
import com.github.mamoru.praesidiume.praesidiumeleader.utils.getObjectNode
import com.github.mamoru.praesidiume.praesidiumeleader.utils.parseSri
import com.github.mamoru.praesidiume.praesidiumeleader.utils.toHexString
import com.github.mamoru.praesidiume.praesidiumeleader.utils.updateFromFile
import org.apache.commons.io.IOUtils
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest


@Service
class NpmPackageBuilder(
        private val npmPackageParser: NpmPackageParser,
        private val npmClientProvider: NpmClientProvider,
        private val preGypParser: PreGypParser
) {
    fun buildPackage(metadata: ObjectNode): ObjectNode {
        println("Building package")
        println(metadata)
        val hasScripts = npmPackageParser.hasClientScripts(metadata)
        if (!hasScripts) {
            return rebuildUsualPackage(metadata)
        }
        return metadata
    }

    fun rebuildUsualPackage(metadata: ObjectNode): ObjectNode {
        downloadNpmSource(metadata)
        return metadata
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
