package com.github.mamoru.praesidiume.praesidiumeleader.dto

import com.github.mamoru.praesidiume.praesidiumeleader.entity.TestEntity

data class TestDto(
        val name: String
) {
    companion object {
        fun fromEntity(entity: TestEntity): TestDto {
            return TestDto(
                    name = entity.name
            )
        }
    }
}
