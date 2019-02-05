package com.github.mamoru.praesidiume.praesidiumeleader.controler.api

import com.github.mamoru.praesidiume.praesidiumeleader.dto.TestDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.*

interface TestApi {
    @GET("/test/")
    fun getAll(): Call<List<TestDto>>

    @POST("/test/create")
    fun create(@Body body: TestDto): Call<UUID>
}
