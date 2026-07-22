package com.example.capstone.data.api

import com.example.capstone.data.model.SpanishNewsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SpanishNlpService {
    @GET("spanish-news-analyze")
    fun getAnalyzedSpanishNews(
        @Query("category") category: String
    ): Call<SpanishNewsResponse>
}
