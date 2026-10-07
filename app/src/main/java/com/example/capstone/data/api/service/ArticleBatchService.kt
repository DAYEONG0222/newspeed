package com.example.capstone.data.api.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class ArticleBatchRequest(
    val lang: String,
    val urls: List<String>,
    val category: String = "default"
)

/** 기사 하나의 결과. 실패하면 error 만 채워진다. */
data class ArticleBatchItem(
    val url: String,
    val text: String? = null,
    val level: String? = null,
    val error: String? = null,
    val cached: Boolean = false
)

data class ArticleBatchResponse(
    val lang: String,
    val results: List<ArticleBatchItem>
)

interface ArticleBatchService {
    @POST("articles/analyze")
    suspend fun analyze(@Body request: ArticleBatchRequest): Response<ArticleBatchResponse>
}
