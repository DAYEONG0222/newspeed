package com.example.capstone.data.model

data class SpanishNewsResponse(
    val articles: List<SpanishArticle>
)

data class SpanishArticle(
    val url: String,
    val score: Double,
    val level: String,
    val sentence_len: Double,
    val noun_ratio: Double,
    val verb_ratio: Double,
    val adj_ratio: Double,
    val conj_ratio: Double,
    val sentences_analyzed: Int
)

