package com.example.capstone.data.api

import com.example.capstone.data.api.service.ExtractService
import com.example.capstone.data.api.service.GNewsService
import com.example.capstone.data.api.service.NlpService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    // OkHttpClient 설정: 타임아웃 30초로 지정
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // GNews API용 Retrofit 서비스
    val api: GNewsService by lazy {
        Retrofit.Builder()
            .baseUrl("https://gnews.io/api/v4/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GNewsService::class.java)
    }


    // 기사 본문 추출 API
    val extractService: ExtractService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5000/")  // 본문 추출 Flask 서버
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExtractService::class.java)
    }

    // NLP 분석 API
    val nlpService: NlpService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5000/")  // NLP Flask 서버
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NlpService::class.java)
    }

}

