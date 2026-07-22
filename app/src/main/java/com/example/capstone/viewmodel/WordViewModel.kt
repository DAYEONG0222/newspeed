package com.example.capstone.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstone.data.local.AppDatabase
import com.example.capstone.data.local.entity.Vocabulary
import com.example.capstone.data.repository.WordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WordViewModel(application: Application) : AndroidViewModel(application) {

    // 정렬 옵션 정의
    enum class SortOption {
        ALPHABET_ASC
    }

    // Repository 객체 초기화
    private val repository: WordRepository

    init {
        val dao = AppDatabase.getDatabase(application).vocabularyDao()
        repository = WordRepository(dao)
    }

    /**
     * 단어 저장 함수
     */
    fun saveWord(
        word: String,
        langCode: String,
        meaning: String,
        pronunciation: String = "",
        isFavorite: Boolean = false
    ) {
        viewModelScope.launch {
            val result = repository.insertVocabulary(word, langCode, meaning, pronunciation, isFavorite)
            if (result > 0) {
                Log.d("WordViewModel", "✅ 단어 저장 성공: $word")
            } else {
                Log.d("WordViewModel", "⚠️ 단어 이미 존재함: $word")
            }
        }
    }

    /**
     * 단일 단어 삭제 (Entity 기반)
     */
    fun deleteWord(word: Vocabulary) {
        viewModelScope.launch {
            repository.deleteWord(word)
            Log.d("WordViewModel", "🗑 단어 삭제: ${word.word}")
        }
    }

    /**
     * 복수 단어 삭제 (vocaId 기준)
     */
    fun deleteWordsByIds(vocaIdList: List<Long>) {
        viewModelScope.launch {
            repository.deleteWordsByIds(vocaIdList)
            Log.d("WordViewModel", "🗑 복수 삭제: $vocaIdList")
        }
    }

    /**
     * 전체 단어 불러오기 (정렬 없음)
     */
    suspend fun getAllWords(): List<Vocabulary> {
        return withContext(Dispatchers.IO) {
            repository.getAllWords()
        }
    }

    /**
     * 언어별 단어 조회 (Flow)
     */
    fun getWordsByLanguage(language: String): Flow<List<Vocabulary>> {
        return repository.getWordsByLanguage(language)
    }

    /**
     * 정렬 기준으로 단어 정렬
     */
    suspend fun getWordsSortedBy(option: SortOption): List<Vocabulary> {
        return withContext(Dispatchers.IO) {
            val list = repository.getAllWords()
            when (option) {
                SortOption.ALPHABET_ASC -> list.sortedBy { it.word.lowercase() }
            }
        }
    }
}
