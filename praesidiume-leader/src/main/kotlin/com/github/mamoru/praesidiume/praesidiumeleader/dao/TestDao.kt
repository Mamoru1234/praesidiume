package com.github.mamoru.praesidiume.praesidiumeleader.dao

import com.github.mamoru.praesidiume.praesidiumeleader.entity.TestEntity
import org.springframework.data.repository.CrudRepository
import java.util.*

interface TestDao: CrudRepository<TestEntity, UUID>
