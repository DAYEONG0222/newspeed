package com.example.capstone.data.api.service

import com.example.capstone.data.api.model.ExtractResponse
import com.example.capstone.data.api.model.NlpResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

/**
 * [ExtractService]
 * Flask 서버와 통신하기 위한 Retrofit 인터페이스
 * - /analyze: 기사 본문에 대한 NLP 엔터티 분석 요청
 * - /extract: 기사 URL로부터 본문 텍스트 추출 요청
 */
interface ExtractService {

    /**
     * 기사 본문 NLP 분석 요청
     * @param requestBody - 분석할 텍스트를 포함한 Map (예: {"text": "기사 내용"})
     * @return 분석 결과(NlpResponse)를 담은 Retrofit Response 객체
     */
    @POST("analyze")
    suspend fun analyzeText(@Body requestBody: Map<String, String>): Response<NlpResponse>

    /**
     * 기사 본문 추출 요청
     * @param urlMap - URL 정보를 담은 Map (예: {"url": "https://example.com"})
     * @return 추출된 기사 본문(ExtractResponse)을 담은 Retrofit Response 객체
     */
    @POST("extract")
    suspend fun extractArticle(@Body urlMap: Map<String, String>): Response<ExtractResponse>
}

