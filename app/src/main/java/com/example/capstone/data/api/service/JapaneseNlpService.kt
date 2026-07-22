package com.example.capstone.data.api.service

import com.example.capstone.data.api.model.ArticleAnalysis
import com.example.capstone.data.api.service.NlpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface JapaneseNlpService {

    @POST("analyze-japanese-news")
    suspend fun analyzeText(@Body request: NlpRequest): Response<ArticleAnalysis>
}
