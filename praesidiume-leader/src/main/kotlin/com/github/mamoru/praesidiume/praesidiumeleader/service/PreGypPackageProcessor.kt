package com.github.mamoru.praesidiume.praesidiumeleader.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.github.mamoru.praesidiume.praesidiumeleader.entity.PackageVersionDescriptionEntity
import com.github.mamoru.praesidiume.praesidiumeleader.utils.getObjectNode
import com.github.mamoru.praesidiume.praesidiumeleader.utils.resolve
import org.apache.commons.compress.archivers.examples.Expander
import org.apache.commons.io.FileUtils
import org.rauschig.jarchivelib.ArchiveFormat
import org.rauschig.jarchivelib.ArchiverFactory
import org.rauschig.jarchivelib.CompressionType
import org.springframework.stereotype.Service
import java.io.File
import java.util.*
import javax.annotation.PostConstruct

@Service
class PreGypPackageProcessor(
        private val npmClientProvider: NpmClientProvider,
        private val preGypParser: PreGypParser
) {
    val tempStorageDir = File("/home/alexei/temp/dyplom_result_storage")
    val archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP)
    val mapper = jacksonObjectMapper()

    @PostConstruct
    fun init() {
        tempStorageDir.mkdirs()
    }
    fun processPackage(
            packageVersionDescriptionEntity: PackageVersionDescriptionEntity,
            metadata: ObjectNode,
            rawParams: ObjectNode,
            codeArchive: File): File {
        val sandboxDir = File(tempStorageDir, UUID.randomUUID().toString())
        val sourceDir = File(sandboxDir, "source")
        sourceDir.mkdirs()
        archiver.extract(codeArchive, sourceDir)
        println("Start processing in sandbox ${sandboxDir.absoluteFile}")
        val binary = getObjectNode(metadata, arrayOf("binary"))
        val params = generateParams(metadata, rawParams)
        println(binary)
        val tarUrl = preGypParser.buildTarUrl(binary, params)
        val modulePath = File(sandboxDir, "binary.tgz")
        npmClientProvider.downloadFile(modulePath, tarUrl)
        val packageDir = sourceDir.resolve("package")
        val binarySourceDir = preGypParser.getBinarySourceDir(binary, params, packageDir)
        archiver.extract(modulePath, binarySourceDir)
        val resultArchive = archiver.create("${UUID.randomUUID()}.tgz", tempStorageDir, sourceDir)
        FileUtils.deleteDirectory(sandboxDir)
        println("Result archive path: ${resultArchive.absolutePath}")
        return resultArchive
    }

    fun generateParams(metadata: ObjectNode, rawParams: ObjectNode): Map<String, String> {
        val result: MutableMap<String, String> = mapper.convertValue(rawParams, jacksonTypeRef<MutableMap<String, String>>())
        result["name"] = metadata.get("name").textValue()
        result["version"] = metadata.get("version").textValue()
        return result
    }
}
