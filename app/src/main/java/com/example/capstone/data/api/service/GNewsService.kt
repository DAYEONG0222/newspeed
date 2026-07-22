package com.example.capstone.data.api.service

import com.example.capstone.data.api.model.GNewsResponse
import com.example.capstone.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * [GNewsService]
 * GNews API와 통신하기 위한 Retrofit 인터페이스
 * Retrofit은 이 인터페이스 정의를 기반으로 실제 HTTP 요청 코드를 생성
 */
interface GNewsService {

    /**
     * GNews 'top-headlines' 엔드포인트 요청
     * GNews에서 뉴스 헤드라인 목록을 가져옴
     *
     * @param lang   요청할 뉴스의 언어 코드 (예: "en", "ja", "zh")
     * @param topic  뉴스 주제 (예: "politics", "business", "science" 등)
     * @param token  GNews API 토큰 (인증 키)
     * @return GNewsResponse 객체 (뉴스 기사 리스트 포함)
     */
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("lang") lang: String,
        @Query("topic") topic: String,
        @Query("token") token: String,
        @Query("max") max: Int = 10
    ): GNewsResponse

    @GET("search")
    suspend fun searchJapaneseNews(
        @Query("q") keyword: String,
        @Query("lang") lang: String = "ja",
        @Query("max") max: Int = 10,
        @Query("token") token: String = BuildConfig.GNEWS_API_KEY
    ): GNewsResponse

}


