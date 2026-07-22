package com.example.capstone.data.local.dao

import androidx.room.*
import com.example.capstone.data.local.entity.Article
import java.time.LocalDateTime

@Dao
interface ArticleDao {

    // =============================================================================================
    // 기사 INSERT

    // 기사 추가(중복 방지)
    @Query(
        """INSERT OR IGNORE INTO article (language_code, topic, title, description, url, image, published_at, difficulty, content, is_favorite) VALUES (:langCode, :topic, :title, :description, :url, :image, :publishedAt, :difficulty, :content, :isFavorite)"""
    )
    suspend fun insertArticle(
        langCode: String,
        topic: String,
        title: String,
        description: String,
        url: String,
        image: String,
        publishedAt: LocalDateTime,
        difficulty: String,
        content: String,
        isFavorite: Boolean
    ) : Long

    // =============================================================================================
    // 기사 READ

    // 전체 기사
    @Query("""SELECT * FROM article""")
    suspend fun readAllArticle() : List<Article>

    // 언어별 기사
    @Query("""SELECT * FROM article WHERE language_code = :langCode""")
    suspend fun readLanguageArticle(langCode: String): List<Article>

    // 주제별 기사
    @Query("""SELECT * FROM article WHERE topic = :topic""")
    suspend fun readTopicArticle(topic: String): List<Article>

    // =============================================================================================
    // 기사 UPDATE

    // 즐겨찾기 상태 업데이트
    @Query("UPDATE article SET is_favorite = :isFavorite WHERE article_id = :articleId")
    suspend fun updateFavoriteStatus(isFavorite: Boolean, articleId: Long): Int

    // =============================================================================================
    // 기사 DELETE

    // 단일 삭제
    @Query("DELETE FROM article WHERE article_id = :articleId")
    suspend fun deleteArticle(articleId: Long) : Int

    //  복수 삭제
    @Query("DELETE FROM article WHERE article_id IN (:articleIdList)")
    suspend fun deleteArticle(articleIdList: List<Long>) : Int
}