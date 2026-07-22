package com.example.capstone.data.model

/**
 * [ArticleCardItem]
 * GNews 기사들을 공통 타입으로 처리하기 위한 인터페이스
 */
interface ArticleCardItem {
    val title: String
    val description: String?
    val url: String
    val publishedAt: String?
    val difficulty: String?
}

/**
 * [GNewsArticle] - GNews에서 가져온 기사 구조
 */
data class GNewsArticle(
    override val title: String,
    override val description: String?,
    override val url: String,
    val image: String?,
    override val publishedAt: String,
    override val difficulty: String?,
    val content: String?
) : ArticleCardItem

