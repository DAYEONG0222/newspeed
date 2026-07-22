package com.example.capstone.data.api.service

import com.example.capstone.data.api.model.TokenizeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenizeJaService {
    @POST("/tokenize-ja")
    suspend fun tokenize(@Body body: Map<String, String>): Response<TokenizeResponse>
}