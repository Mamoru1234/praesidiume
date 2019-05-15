package com.github.mamoru.praesidiume.praesidiumeleader.dao

import com.github.mamoru.praesidiume.praesidiumeleader.entity.PackageEntity
import com.github.mamoru.praesidiume.praesidiumeleader.entity.PackageVersionDescriptionEntity
import org.springframework.data.repository.CrudRepository
import java.util.*

interface PackageEntityDao: CrudRepository<PackageEntity, UUID> {
    fun findByName(name: String): Optional<PackageEntity>
}

interface PackageVersionDescriptionDao: CrudRepository<PackageVersionDescriptionEntity, UUID> {
    fun findByNameAndVersion(name: String, version: String)
}
