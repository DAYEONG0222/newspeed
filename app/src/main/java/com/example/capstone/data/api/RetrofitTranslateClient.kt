package com.example.capstone.data.api

import com.example.capstone.data.api.service.TranslateService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * [RetrofitTranslateClient]
 * 번역 Flask 서버와 통신하기 위한 Retrofit 클라이언트
 * - 서버 주소: http://10.0.2.2:7000/
 * - 엔드포인트: TranslateService 인터페이스에 정의됨
 */
object RetrofitTranslateClient {

    /**
     * - 번역 요청용 서비스 객체
     * - 최초 접근 시 Retrofit 인스턴스를 생성
     * - 응답 데이터는 Gson으로 JSON 파싱
     */

    private val FLASK_SERVER_IP = if (android.os.Build.FINGERPRINT.contains("generic")) "10.0.2.2" else "192.168.213.33"

    val translateService: TranslateService by lazy {
        Retrofit.Builder()
            .baseUrl("http://$FLASK_SERVER_IP:7000/") // 로컬 Flask 서버 (에뮬레이터 기준)
            .addConverterFactory(GsonConverterFactory.create()) // JSON ↔ 객체 자동 변환
            .build()
            .create(TranslateService::class.java) // 번역용 Retrofit 인터페이스와 연결
    }
}
