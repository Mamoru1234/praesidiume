package com.github.mamoru.praesidiume.praesidiumeleader.entity

import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.mamoru.praesidiume.praesidiumeleader.converter.SetConverter
import com.github.mamoru.praesidiume.praesidiumeleader.converter.JacksonNodeConverter
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "package_version_description")
data class PackageVersionDescriptionEntity(
    @Id
    var id: UUID = UUID.randomUUID(),

    var name: String,

    var version: String,

    @Convert(converter = SetConverter::class)
    var parameters: Set<String>,

    @Convert(converter = JacksonNodeConverter::class)
    var dependencies: ObjectNode,

    @ManyToOne
    @JoinColumn(name = "fk_package", nullable = false)
    var packageEntity: PackageEntity,

    @OneToMany(mappedBy = "packageVersionEntity", fetch = FetchType.LAZY)
    var artifacts: List<PackageVersionArtifactEntity>
)
