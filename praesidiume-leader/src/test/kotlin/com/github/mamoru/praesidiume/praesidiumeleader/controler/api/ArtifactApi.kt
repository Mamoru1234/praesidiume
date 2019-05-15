package com.github.mamoru.praesidiume.praesidiumeleader.controler.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface ArtifactApi {
    @GET("/artifact/metadata/{packageName}")
    fun getPackage(
            @Path("packageName") packageName: String
    ): Call<ResponseBody>
    @GET("/artifact/metadata/{packageName}/{packageVersion}")
    fun getMetadata(
            @Path("packageName") packageName: String,
            @Path("packageVersion") packageVersion: String,
            @QueryMap params: Map<String, String> = emptyMap()
    ): Call<ResponseBody>

    @GET("/artifact/download/{packageName}/{packageVersion}")
    fun getPackageContent(
            @Path("packageName") packageName: String,
            @Path("packageVersion") packageVersion: String,
            @QueryMap params: Map<String, String> = emptyMap()
    ): Call<ResponseBody>
}
