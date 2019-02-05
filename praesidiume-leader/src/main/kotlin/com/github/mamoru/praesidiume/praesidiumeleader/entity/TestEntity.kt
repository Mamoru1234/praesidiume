package com.github.mamoru.praesidiume.praesidiumeleader.entity

import org.hibernate.annotations.Type
import org.springframework.context.annotation.Lazy
import java.sql.Blob
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "test")
data class TestEntity(
        @Id
        @GeneratedValue
        @Type(type = "pg-uuid")
        var id: UUID? = null,
        var name: String,

        @Lob
        @Lazy
        var file: Blob
)
