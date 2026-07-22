package com.example.capstone.data.api.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.capstone.BuildConfig

/**
 * [GNewsApiService]
 * GNews API와 통신하기 위한 Retrofit 클라이언트
 * 싱글톤 패턴으로 정의되어 앱 전체에서 재사용됨
 */
object GNewsApiService {
    // GNews API 기본 URL
    private const val BASE_URL = "https://gnews.io/api/v4/"

    /**
     * GNewsService 인터페이스 구현체
     * Retrofit이 이 객체를 통해 실제 네트워크 요청 코드를 생성
     */
    val api: GNewsService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // News API base URL 설정
            .addConverterFactory(GsonConverterFactory.create()) // JSON → 객체 변환기 (Gson)
            .build()
            .create(GNewsService::class.java) // GNewsService 인터페이스와 연결
    }

    /**
     * - API 키 반환 프로퍼티
     * - `build.gradle`의 BuildConfig에 정의된 키를 가져옴
     * - 키를 코드에 직접 쓰지 않고 안전하게 숨기기 위해 사용
     */
    val apiKey: String
        get() = BuildConfig.GNEWS_API_KEY
}
