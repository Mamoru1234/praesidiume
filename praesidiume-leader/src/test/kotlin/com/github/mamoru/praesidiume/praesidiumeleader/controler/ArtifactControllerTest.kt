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
    fun getPackageMeta() {
        val response = apiClient.getPackage("lodash").execute()
        Assertions.assertThat(response.isSuccessful).isTrue()
        println(response.body()!!.string())
    }

    @Test
    fun getBinaryPackageMeta() {
        val response = apiClient.getPackage("libxmljs").execute()
        Assertions.assertThat(response.isSuccessful).isTrue()
        println(response.body()!!.string())
    }

    @Test
    fun getArtifactMetadata() {
        val response = apiClient.getMetadata("lodash", "4.17.11").execute()
        Assertions.assertThat(response.isSuccessful).isTrue()
        println(response.body()?.string())
    }

    @Test
    fun getPreBuildCase() {
        val queryParams = mapOf(
                "node_abi" to "node-v64",
                "platform" to "linux",
                "arch" to "x64"
        )
        val response = apiClient.getMetadata("libxmljs", "0.19.5", queryParams).execute()
        Assertions.assertThat(response.isSuccessful).isTrue()
        val content = apiClient.getPackageContent("libxmljs", "0.19.5", queryParams).execute()
        Assertions.assertThat(content.isSuccessful).isTrue()
    }
}
