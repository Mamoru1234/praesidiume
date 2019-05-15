package com.github.mamoru.praesidiume.praesidiumeleader.entity

import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.mamoru.praesidiume.praesidiumeleader.converter.JacksonNodeConverter
import org.springframework.context.annotation.Lazy
import java.sql.Blob
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "package_version_artifact")
data class PackageVersionArtifactEntity(
        @Id
        var id: UUID = UUID.randomUUID(),

        var name: String,

        var version: String,

        var integrity: String,

        @Convert(converter = JacksonNodeConverter::class)
        var parameters: ObjectNode,

        @Convert(converter = JacksonNodeConverter::class)
        var dependencies: ObjectNode,

        @Lob
        @Lazy
        var content: Blob,

        @ManyToOne
        @JoinColumn(name = "fk_package_version")
        var packageVersionEntity: PackageVersionDescriptionEntity
)
