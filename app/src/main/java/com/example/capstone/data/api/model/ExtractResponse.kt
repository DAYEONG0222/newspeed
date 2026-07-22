package com.example.capstone.data.api.model

/**
 * [ExtractResponse]
 * Flask 서버의 /extract 응답 데이터를 담는 데이터 클래스
 * @property url  - 분석 요청한 기사 URL
 * @property text - 해당 기사에서 추출된 본문 텍스트
 */
data class ExtractResponse(
    val url: String,
    val text: String
)
