package com.example.capstone.data.api.service

import com.example.capstone.data.api.model.ArticleAnalysis
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SpanishNlpApiService {
    @POST("/analyze-es")
    suspend fun analyzeText(@Body request: NlpRequest): Response<ArticleAnalysis>
}
