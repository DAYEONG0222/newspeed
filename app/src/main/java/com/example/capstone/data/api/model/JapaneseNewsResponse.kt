package com.example.capstone.data.api.model

data class ArticleAnalysis(
    val score: Double,
    val level: String,
    val sentence_len: Double,
    val kanji_ratio: Double,
    val wago_ratio: Double,
    val verb_ratio: Double,
    val particle_ratio: Double,
    val sentences_analyzed: Int,
    val tokens: List<String>? = null
)

data class ArticleResult(
    val original: String,
    val description: String?,
    val url: String,
    val publishedAt: String?,
    val analysis: ArticleAnalysis
)

data class AnalysisResult(
    val level: String
)


data class JapaneseNewsResponse(
    val keyword: String,
    val results: List<ArticleResult>
)

