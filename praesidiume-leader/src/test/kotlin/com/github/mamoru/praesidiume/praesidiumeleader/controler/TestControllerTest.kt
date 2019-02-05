package com.github.mamoru.praesidiume.praesidiumeleader.controler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.mamoru.praesidiume.praesidiumeleader.controler.api.TestApi
import com.github.mamoru.praesidiume.praesidiumeleader.dto.TestDto
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.*


internal class TestControllerTest {
    private val apiClient: TestApi by lazy {
        val retrofit = Retrofit.Builder()
                .baseUrl("http://localhost:8080")
                .addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()))
                .build()
        retrofit.create(TestApi::class.java)
    }
    @Test
    fun testFlow() {
        Assertions.assertThat(apiClient.getAll().execute().isSuccessful).isTrue()
        val name = "test flow ${UUID.randomUUID()}"
        val newEntityId = apiClient.create(TestDto(name)).execute().body()
        Assertions.assertThat(newEntityId).isNotNull()
        Assertions.assertThat(apiClient.getAll().execute().body())
                .contains(TestDto(name))
    }
}
