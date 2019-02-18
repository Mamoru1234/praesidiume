package com.github.mamoru.praesidiume.praesidiumeleader.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.mamoru.praesidiume.praesidiumeleader.npm.NpmClient
import org.springframework.stereotype.Service
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

@Service
class NpmClientProvider {
    private val npmRegistryURl = "https://registry.npmjs.org/"
    private val builder = Retrofit.Builder()
            .baseUrl(npmRegistryURl)
            .addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()))

    fun getClient(): NpmClient {
        return builder.build().create(NpmClient::class.java)
    }
}
