package com.github.mamoru.praesidiume.praesidiumeleader.controler

import com.github.mamoru.praesidiume.praesidiumeleader.dao.TestDao
import com.github.mamoru.praesidiume.praesidiumeleader.dto.TestDto
import com.github.mamoru.praesidiume.praesidiumeleader.entity.TestEntity
import org.hibernate.Session
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@RestController
@RequestMapping("/test/")
class TestController(
        val testDao: TestDao
) {
    @PersistenceContext
    var entityManager: EntityManager? = null

    @GetMapping
    fun testGet() = testDao.findAll().map { TestDto.fromEntity(it) }

    @PostMapping("/create")
    fun testCreate(@RequestBody body: TestDto): UUID? {
        val sample = "gewgwe".toByteArray()
        val session = entityManager!!.unwrap(Session::class.java)
        val entity = TestEntity(
                name = body.name,
                file = session.lobHelper.createBlob(sample)
        )
        testDao.save(entity)
        return entity.id
    }
}
