package com.example.capstone.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 퀴즈 오답 기록.
 * 기존에는 메모리 싱글톤(WrongNoteManager)에 담아서 앱을 다시 켜거나 퀴즈를 새로 시작하면 사라졌다.
 * (word, language_code) 를 유니크로 두고 같은 단어를 또 틀리면 횟수와 시각만 갱신한다.
 */
@Entity(
    tableName = "wrong_answer",
    indices = [Index(value = ["word", "language_code"], unique = true)]
)
data class WrongAnswer(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "wrong_answer_id")
    val id: Long = 0,

    @ColumnInfo(name = "word")
    val word: String,
    @ColumnInfo(name = "meaning")
    val meaning: String,
    @ColumnInfo(name = "language_code")
    val langCode: String,
    @ColumnInfo(name = "wrong_count")
    val wrongCount: Int,
    @ColumnInfo(name = "last_wrong_at")
    val lastWrongAt: Long       // epoch millis
)
