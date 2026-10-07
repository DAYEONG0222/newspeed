package com.example.capstone.data.api

import com.example.capstone.BuildConfig
import com.example.capstone.data.api.service.ArticleBatchService
import com.example.capstone.data.api.service.DatamuseService
import com.example.capstone.data.api.service.ExtractService
import com.example.capstone.data.api.service.FlaskThesaurusService
import com.example.capstone.data.api.service.JapaneseNlpService
import com.example.capstone.data.api.service.NlpService
import com.example.capstone.data.api.service.SpanishNlpApiService
import com.example.capstone.data.api.service.TokenizeJaService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * [RetrofitClient]
 * 통합 Flask 서버(newspeed_server, 포트 5000) 하나와 통신한다.
 * 기존에는 기능마다 포트가 달라(5000/6000/6100/6201/7001) Retrofit 인스턴스를 각각 만들었다.
 */
object RetrofitClient {

    private val FLASK_SERVER_IP =
        if (android.os.Build.FINGERPRINT.contains("generic")) "10.0.2.2" else BuildConfig.FLASK_HOST
    val FLASK_BASE_URL = "http://$FLASK_SERVER_IP:5000/"

    // 네트워크 타임아웃이 설정된 OkHttpClient (배치 분석은 기사 여러 개를 처리하므로 여유 있게)
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val flask: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(FLASK_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /** 기사 URL 목록을 한 번에 추출·분석 (/articles/analyze) */
    val articleBatchService: ArticleBatchService by lazy { flask.create(ArticleBatchService::class.java) }

    val nlpService: NlpService by lazy { flask.create(NlpService::class.java) }
    val tokenizeJaService: TokenizeJaService by lazy { flask.create(TokenizeJaService::class.java) }
    val extractService: ExtractService by lazy { flask.create(ExtractService::class.java) }
    val flaskService: FlaskThesaurusService by lazy { flask.create(FlaskThesaurusService::class.java) }
    val japaneseNlpService: JapaneseNlpService by lazy { flask.create(JapaneseNlpService::class.java) }
    val spanishNlpService: SpanishNlpApiService by lazy { flask.create(SpanishNlpApiService::class.java) }

    val datamuseService: DatamuseService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.datamuse.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DatamuseService::class.java)
    }
}
