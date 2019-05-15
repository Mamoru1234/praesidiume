package com.github.mamoru.praesidiume.praesidiumeleader.entity

import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "package")
data class PackageEntity(
    @Id
    var id: UUID = UUID.randomUUID(),

    var name: String,

    @OneToMany
    var versions: Set<PackageVersionDescriptionEntity>
)
