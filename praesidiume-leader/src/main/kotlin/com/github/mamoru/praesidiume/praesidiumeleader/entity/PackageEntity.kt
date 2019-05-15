package com.github.mamoru.praesidiume.praesidiumeleader.entity

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "package")
data class PackageEntity(
    @Id
    var id: UUID = UUID.randomUUID(),

    var name: String,

    @OneToMany(mappedBy = "packageEntity")
    var versions: List<PackageVersionDescriptionEntity>
)
