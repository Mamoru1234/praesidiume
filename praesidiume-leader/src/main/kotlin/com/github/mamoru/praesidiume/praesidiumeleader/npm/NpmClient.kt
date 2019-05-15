package com.github.mamoru.praesidiume.praesidiumeleader.npm

import com.fasterxml.jackson.databind.JsonNode
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface NpmClient {
    @GET("/{packageName}")
    fun getPackageMeta(
            @Path("packageName") packageName: String
    ): Call<JsonNode>

    @GET("/{packageName}/{packageVersion}")
    fun getPackageVersionMeta(
            @Path("packageName") packageName: String,
            @Path("packageVersion") packageVersion: String
    ): Call<JsonNode>

    @GET
    fun downloadArchive(@Url archivePath: String): Call<ResponseBody>
}
