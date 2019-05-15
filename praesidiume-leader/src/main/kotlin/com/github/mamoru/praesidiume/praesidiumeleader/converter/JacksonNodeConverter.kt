package com.github.mamoru.praesidiume.praesidiumeleader.converter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import javax.persistence.AttributeConverter

class JacksonNodeConverter: AttributeConverter<JsonNode, String> {
    private val mapper = jacksonObjectMapper()

    override fun convertToDatabaseColumn(attribute: JsonNode?): String {
        return mapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String?): JsonNode {
        return mapper.readTree(dbData)
    }
}
