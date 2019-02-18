package com.github.mamoru.praesidiume.praesidiumeleader.controler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.mamoru.praesidiume.praesidiumeleader.controler.api.ArtifactApi
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

class ArtifactControllerTest {
    private val apiClient: ArtifactApi by lazy {
        val retrofit = Retrofit.Builder()
                .baseUrl("http://localhost:8080")
                .addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()))
                .build()
        retrofit.create(ArtifactApi::class.java)
    }

    @Test
    fun getArtifactMetadata() {
        val response = apiClient.getMetadata("lodash", "0.1.0").execute()
        Assertions.assertThat(response.isSuccessful).isTrue()
        println(response.body()?.string())
    }
}
