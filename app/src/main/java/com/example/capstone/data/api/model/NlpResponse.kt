package com.example.capstone.data.api.model

import com.example.capstone.data.api.service.NlpEntity

/**
 * [NlpResponse]
 * Flask 서버로부터 반환되는 분석 응답 데이터
 *
 * @property difficulty 분석된 텍스트의 난이도 (초급/중급/고급 등)
 * @property entities   추출된 엔터티 리스트
 * @property text       분석에 사용된 원문 텍스트 (선택적)
 */
data class NlpResponse(
    val difficulty: String?,
    val entities: List<NlpEntity>,
    val text: String = ""
)