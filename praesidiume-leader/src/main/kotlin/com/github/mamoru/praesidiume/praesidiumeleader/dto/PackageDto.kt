package com.github.mamoru.praesidiume.praesidiumeleader.dto

data class PackageDto (
        val name: String,
        val versions: List<PackageVersionDescriptionDto>
)
