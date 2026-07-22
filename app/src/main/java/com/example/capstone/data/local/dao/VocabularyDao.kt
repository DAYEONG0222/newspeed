package com.example.capstone.data.local.dao

import androidx.room.*
import com.example.capstone.data.local.entity.Vocabulary
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {

    // =============================================================================================
    // 단어 INSERT

    // 단어 추가 (중복 방지)
    @Query(
        """INSERT OR IGNORE INTO vocabulary 
           (word, language_code, meaning, pronunciation, is_favorite) 
           VALUES (:word, :langCode, :meaning, :pronunciation, :isFavorite)"""
    )
    suspend fun insertVocabulary(
        word: String,
        langCode: String,
        meaning: String,
        pronunciation: String,
        isFavorite: Boolean
    ): Long

    // =============================================================================================
    // 단어 READ

    // 전체 단어 불러오기
    @Query("SELECT * FROM vocabulary")
    suspend fun readAllVocabulary(): List<Vocabulary>

    // 언어별 단어 불러오기
    @Query("SELECT * FROM vocabulary WHERE language_code = :langCode")
    suspend fun readLanguageVocabulary(langCode: String): List<Vocabulary>

    // 언어별 단어 스트리밍 (Flow) – 하단 언어 탭용
    @Query("SELECT * FROM vocabulary WHERE language_code = :language ORDER BY vocabulary_id DESC")
    fun getWordsByLanguage(language: String): Flow<List<Vocabulary>>

    // =============================================================================================
    // 단어 UPDATE

    // 즐겨찾기 상태 업데이트
    @Query("UPDATE vocabulary SET is_favorite = :isFavorite WHERE vocabulary_id = :vocaId")
    suspend fun updateFavoriteStatus(isFavorite: Boolean, vocaId: Long): Int

    // =============================================================================================
    // 단어 DELETE

    //  단일 삭제 - 엔티티 기반
    @Delete
    suspend fun deleteWord(word: Vocabulary)

    //  단일 삭제 - PK 기반
    @Query("DELETE FROM vocabulary WHERE vocabulary_id = :vocaId")
    suspend fun deleteVocabulary(vocaId: Long) : Int

    //  복수 삭제
    @Query("DELETE FROM vocabulary WHERE vocabulary_id IN (:vocaIdList)")
    suspend fun deleteVocabulary(vocaIdList: List<Long>) : Int
}
