package com.example.capstone.data.local.entity

import androidx.room.*

@Entity(
    tableName = "vocabulary",
    foreignKeys = [
        ForeignKey(
            entity = Code::class,
            parentColumns = ["code"],
            childColumns = ["language_code"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["word", "language_code"], unique = true),  // (언어-단어) 중복값 불허
        Index(value = ["language_code"]),                        // 언어별 검색 최적화
        Index(value = ["is_favorite"]),                          // 즐겨찾기 필터링 최적화
        Index(value = ["word"])                                  // 단어 검색 최적화
    ]
)
data class Vocabulary(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "vocabulary_id")
    val vocaId : Long = 0 ,  // 순서 숫자

    @ColumnInfo(name = "word")
    val word: String,   // 단어
    @ColumnInfo(name = "language_code")
    val langCode : String,  // 언어
    @ColumnInfo(name = "meaning")
    val meaning : String,    // 의미
    @ColumnInfo(name = "pronunciation")
    val pronunciation: String,  // 발음 기호
    @ColumnInfo(name = "is_favorite")
    val isFavorite : Boolean    // 즐겨찾기
)
