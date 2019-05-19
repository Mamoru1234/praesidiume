package com.github.mamoru.praesidiume.praesidiumeleader.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.mamoru.praesidiume.praesidiumeleader.dao.PackageEntityDao
import com.github.mamoru.praesidiume.praesidiumeleader.dao.PackageVersionDescriptionDao
import com.github.mamoru.praesidiume.praesidiumeleader.entity.PackageEntity
import com.github.mamoru.praesidiume.praesidiumeleader.entity.PackageVersionDescriptionEntity
import com.github.mamoru.praesidiume.praesidiumeleader.exception.ClientException
import com.github.mamoru.praesidiume.praesidiumeleader.utils.getObjectNode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

val CLIENT_SIDE_SCRIPTS = listOf(
        "install",
        "postinstall",
        "preinstall",
//       Some packages has wrong usages of this scripts
//        "prepublish",
//        "prepare",
        "uninstall"
)

val DEPENDECY_FIELDS = listOf("dependencies", "devDependencies")

@Service
class NpmPackageParser(
        private val packageEntityDao: PackageEntityDao,
        private val preGypParser: PreGypParser,
        private val packageVersionDescriptionDao: PackageVersionDescriptionDao
) {
    private val mapper: ObjectMapper = jacksonObjectMapper()

    @Transactional
    fun ensurePackage(metadata: ObjectNode): PackageEntity {
        println("Ensuring structure for package")
        println(metadata)
        val name = metadata.get("name").asText()
        val packageEntity = PackageEntity(name = name, versions = emptyList())
        packageEntityDao.save(packageEntity)
        val versions = getObjectNode(metadata, arrayOf("versions"))
        val versionsEntities = versions.fields().asSequence().mapNotNull {
            versionMeta ->
            val version = versionMeta.key
            val dependencies = mapper.createObjectNode()
            DEPENDECY_FIELDS.forEach {
                if (!versionMeta.value.has(it)){
                    return@forEach
                }
                val jsonNode = versionMeta.value.get(it) as ObjectNode
                if (jsonNode.fieldNames().asSequence().toList().isEmpty()) {
                    return@forEach
                }
                dependencies.set(it, jsonNode)
            }
            val parameters = getPackageParameters(versionMeta.value)
                    ?: return@mapNotNull null
            return@mapNotNull PackageVersionDescriptionEntity(
                    name = packageEntity.name,
                    version = version,
                    parameters = parameters,
                    dependencies = dependencies,
                    packageEntity = packageEntity,
                    artifacts = emptyList()
            )
        }
        val versionsSet = versionsEntities.toList()
        packageVersionDescriptionDao.saveAll(versionsSet)
        packageEntity.versions = versionsSet
        return packageEntity
    }
    fun hasClientScripts(metadata: ObjectNode): Boolean {
        if (!metadata.has("scripts")) {
            return false
        }
        val scriptsNode = getObjectNode(metadata, arrayOf("scripts"))
        return CLIENT_SIDE_SCRIPTS.any { scriptsNode.has(it) }
    }
    fun getPackageParameters(rawMetadata: JsonNode): Set<String>? {
        if (rawMetadata.nodeType != JsonNodeType.OBJECT) {
            throw ClientException("Metadata should be object")
        }
        val metadata = rawMetadata as ObjectNode
        if (!hasClientScripts(metadata)) {
            return emptySet()
        }
        if (!isPreBuildSupported(rawMetadata)) {
            println("Prebuild is not supported ${metadata.get("name")} ${metadata.get("version")}")
            return null
        }
        val binary = metadata.get("binary") as ObjectNode
        return preGypParser.getRequiredProperties(binary).toSet()
    }
    fun hasClientScripts(rawMetadata: JsonNode): Boolean {
        if (rawMetadata.nodeType != JsonNodeType.OBJECT) {
            throw ClientException("Metadata should be object")
        }
        return hasClientScripts(rawMetadata as ObjectNode)
    }
    fun isPreBuildSupported(rawMetadata: JsonNode): Boolean {
        if (rawMetadata.nodeType != JsonNodeType.OBJECT) {
            throw ClientException("Metadata should be object")
        }
        val metadata = rawMetadata as ObjectNode
        if (!metadata.has("binary")) {
            return false
        }
        val scripts = getObjectNode(metadata, arrayOf("scripts"))
        if (!scripts.get("install").textValue().contains("node-pre-gyp")) {
            return false
        }
        val binary = getObjectNode(metadata, arrayOf("binary"))
        return binary.has("module_name") && binary.has("module_path") && binary.has("host")
    }
}
