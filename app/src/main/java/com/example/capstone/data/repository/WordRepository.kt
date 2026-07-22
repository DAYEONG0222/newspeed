package com.example.capstone.data.repository

import com.example.capstone.data.local.dao.VocabularyDao
import com.example.capstone.data.local.entity.Vocabulary
import kotlinx.coroutines.flow.Flow

class WordRepository(private val vocabularyDao: VocabularyDao) {

    //  단어 삽입
    suspend fun insertVocabulary(
        word: String,
        langCode: String,
        meaning: String,
        pronunciation: String,
        isFavorite: Boolean
    ): Long {
        return vocabularyDao.insertVocabulary(word, langCode, meaning, pronunciation, isFavorite)
    }

    //  단어 전체 불러오기 (정렬 없이 원본 반환)
    suspend fun getAllWords(): List<Vocabulary> {
        return vocabularyDao.readAllVocabulary()
    }

    //  알파벳 오름차순 정렬 반환
    suspend fun getAllWordsAlphabetical(): List<Vocabulary> {
        return vocabularyDao.readAllVocabulary()
            .sortedBy { it.word.lowercase() }
    }

    //  단일 삭제
    suspend fun deleteWord(word: Vocabulary) {
        vocabularyDao.deleteWord(word)
    }

    //  복수 삭제
    suspend fun deleteWordsByIds(vocaIdList: List<Long>) {
        vocabularyDao.deleteVocabulary(vocaIdList)
    }
    fun getWordsByLanguage(language: String): Flow<List<Vocabulary>> {
        return vocabularyDao.getWordsByLanguage(language)
    }

}