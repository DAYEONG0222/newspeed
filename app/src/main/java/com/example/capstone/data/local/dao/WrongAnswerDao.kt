package com.example.capstone.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.capstone.data.local.entity.WrongAnswer
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WrongAnswerDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertIgnore(item: WrongAnswer): Long

    @Query(
        """UPDATE wrong_answer
           SET wrong_count = wrong_count + 1, last_wrong_at = :now, meaning = :meaning
           WHERE word = :word AND language_code = :langCode"""
    )
    abstract suspend fun increment(word: String, langCode: String, meaning: String, now: Long): Int

    /** 처음 틀리면 추가, 이미 있으면 횟수 +1 (한 트랜잭션) */
    @Transaction
    open suspend fun recordWrong(word: String, meaning: String, langCode: String, now: Long = System.currentTimeMillis()) {
        val inserted = insertIgnore(
            WrongAnswer(word = word, meaning = meaning, langCode = langCode, wrongCount = 1, lastWrongAt = now)
        )
        if (inserted == -1L) increment(word, langCode, meaning, now)
    }

    @Query("SELECT * FROM wrong_answer WHERE language_code = :langCode ORDER BY last_wrong_at DESC")
    abstract fun observeByLanguage(langCode: String): Flow<List<WrongAnswer>>

    @Query("DELETE FROM wrong_answer WHERE wrong_answer_id = :id")
    abstract suspend fun deleteById(id: Long): Int
}
