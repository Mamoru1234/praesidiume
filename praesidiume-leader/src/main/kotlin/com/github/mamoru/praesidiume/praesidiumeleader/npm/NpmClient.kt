package com.github.mamoru.praesidiume.praesidiumeleader.npm

import com.fasterxml.jackson.databind.JsonNode
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface NpmClient {
    @GET("/{packageName}/{packageVersion}")
    fun getPackageMeta(
            @Path("packageName") packageName: String,
            @Path("packageVersion") packageVersion: String
    ): Call<JsonNode>
}
