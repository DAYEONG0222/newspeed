package com.example.capstone.data.api.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * [TranslateRequest]
 * 번역 요청 시 전송할 데이터 모델
 * @property text 번역할 원문 텍스트
 * @property target_language 번역할 대상 언어 코드 (기본값: "ko" → 한국어)
 */
data class TranslateRequest(
    val text: String,
    val target_language: String = "ko"
)

/**
 * [TranslateResponse]
 * 번역 응답 결과를 담는 데이터 클래스
 * @property translated_text 번역된 결과 문자열
 */
data class TranslateResponse(
    val translated_text: String
)

/**
 * [TranslateService]
 * Flask 번역 서버와 통신하는 Retrofit 인터페이스
 */
interface TranslateService {

    /**
     * POST 요청으로 텍스트 번역 수행
     * @param request 번역할 원문과 대상 언어를 담은 요청 객체
     * @return 서버로부터 번역된 텍스트가 담긴 응답
     */
    @POST("translate")
    suspend fun translateText(@Body request: TranslateRequest): Response<TranslateResponse>
}
