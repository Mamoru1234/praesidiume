package com.github.mamoru.praesidiume.praesidiumeleader.controler.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ArtifactApi {
    @GET("/artifact/metadata/{packageName}/{packageVersion}")
    fun getMetadata(
            @Path("packageName") packageName: String,
            @Path("packageVersion") packageVersion: String
    ): Call<ResponseBody>
}
