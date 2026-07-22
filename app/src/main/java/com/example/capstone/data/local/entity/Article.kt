package com.example.capstone.data.local.entity

import androidx.room.*
import java.time.LocalDateTime

@Entity(
    tableName = "article",
    foreignKeys = [
        ForeignKey(
            entity = Code::class,
            parentColumns = ["code"],
            childColumns = ["language_code"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["title", "content"], unique = true),
        Index(value = ["language_code"]),
        Index(value = ["is_favorite"]),
        Index(value = ["topic"])
    ]
)
data class Article(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "article_id")
    val articleId : Long = 0,

    @ColumnInfo(name = "language_code")
    val langCode : String,
    @ColumnInfo(name = "topic")
    val topic : String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "description")
    val description: String?,
    @ColumnInfo(name = "url")
    val url: String?,
    @ColumnInfo(name = "image")
    val image: String?,
    @ColumnInfo(name = "published_at")
    val publishedAt: LocalDateTime?,
    @ColumnInfo(name = "difficulty")
    val difficulty: String?,
    @ColumnInfo(name = "content")
    val content: String?,
    @ColumnInfo(name = "is_favorite")
    val isFavorite : Boolean
)