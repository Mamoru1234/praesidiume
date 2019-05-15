package com.github.mamoru.praesidiume.praesidiumeleader.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.mamoru.praesidiume.praesidiumeleader.exception.ClientException
import com.github.mamoru.praesidiume.praesidiumeleader.npm.NpmClient
import org.apache.commons.io.IOUtils
import org.springframework.stereotype.Service
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.File
import java.io.FileOutputStream

@Service
class NpmClientProvider {
    private val npmRegistryURl = "https://registry.npmjs.org/"
    private val builder = Retrofit.Builder()
            .baseUrl(npmRegistryURl)
            .addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()))

    fun getClient(): NpmClient {
        return builder.build().create(NpmClient::class.java)
    }

    fun downloadFile(packageFile: File, fileUrl: String) {
        if (packageFile.exists()) return
        val response = getClient().downloadArchive(fileUrl).execute()
        if (!response.isSuccessful) {
            throw ClientException("Cannot download $fileUrl")
        }
        IOUtils.copyLarge(response.body()!!.byteStream(), FileOutputStream(packageFile))
    }
}
