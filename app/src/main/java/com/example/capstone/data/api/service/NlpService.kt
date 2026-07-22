package com.example.capstone.data.api.service

import com.example.capstone.data.api.model.NlpResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response
import java.io.Serializable

/**
 * [NlpRequest]
 * Flask 서버로 NLP 분석 요청을 보낼 때 사용하는 요청 모델
 * @param text 분석 대상 텍스트 (또는 URL 문자열)
 */
data class NlpRequest(val text: String)

/**
 * [NlpEntity]
 * Google Cloud NLP 분석 결과로 추출된 엔터티 정보
 *
 * @property name     엔터티 이름 (예: "Apple")
 * @property type     엔터티 유형 (예: ORGANIZATION, PERSON 등)
 * @property salience 중요도 점수 (0.0 ~ 1.0)
 * @property metadata 부가 정보 (예: 위키피디아 링크 등)
 */
data class NlpEntity(
    val name: String,
    val type: String,
    val salience: Double,
    val metadata: Map<String, String>? = null
) : Serializable



/**
 * [NlpService]
 * Flask 서버의 /analyze 엔드포인트와 연결된 Retrofit 인터페이스
 */
interface NlpService {

    /**
     * 기사 텍스트를 기반으로 NLP 분석 요청
     * @param request 분석할 텍스트를 담은 NlpRequest 객체
     * @return 분석 결과를 담은 NlpResponse (엔터티 + 난이도 등)
     */
    @POST("/analyze")
    suspend fun analyzeText(@Body request: NlpRequest): Response<NlpResponse>
}


