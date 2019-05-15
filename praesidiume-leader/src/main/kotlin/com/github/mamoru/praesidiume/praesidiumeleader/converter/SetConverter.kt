package com.github.mamoru.praesidiume.praesidiumeleader.converter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import javax.persistence.AttributeConverter

class SetConverter: AttributeConverter<Set<String>, String> {
    private val mapper = jacksonObjectMapper()

    override fun convertToDatabaseColumn(attribute: Set<String>?): String {
        return mapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String?): Set<String> {
        return mapper.readValue(dbData, object: TypeReference<Set<String>>() {})
    }
}
