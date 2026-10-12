package com.example.capstone.data.service

import android.content.Context
import com.example.capstone.data.model.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 어휘 레벨 테스트 (10문항 4지선다).
 * 기존 버전은 DB 에 등록되지 않은 등급 테이블 DAO 를 생성자로 받아서 어디서도 쓸 수 없었다.
 * 문제 생성만 남기고, 결과 레벨은 UserLevelStore 에 저장한다.
 */
class LevelTestService(
    private val context: Context,
    private val gson: Gson = Gson()
) {

    companion object {
        const val TOTAL_QUESTIONS = 10

        // Assets 파일 경로. 영어는 NGSL 1.2 (CC BY-SA 4.0) 에 빈도 순위가 붙어 있다.
        private const val SPANISH_FILE = "vocabulary/spanish.json"
        private const val JAPANESE_FILE = "vocabulary/japanese.json"
        private const val ENGLISH_FILE = "vocabulary/english.json"

        fun supports(languageType: LanguageType): Boolean = true
    }

    // =============================================================================================
    // JSON 파일에서 단어 로드

    suspend fun loadVocabularyFromAssets(languageType: LanguageType): Result<List<TestWord>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                when (languageType) {
                    LanguageType.SPANISH -> {
                        val json = context.assets.open(SPANISH_FILE).bufferedReader().use { it.readText() }
                        gson.fromJson(json, SpanishVocabularyData::class.java).spanishVocabulary
                            // 형식이 다른 항목이 섞여 있으면 Gson 이 null 을 넣으므로 걸러낸다
                            .mapNotNull { item ->
                                val word: String? = item.spanish
                                val meaning: String? = item.korean
                                if (word.isNullOrBlank() || meaning.isNullOrBlank()) null
                                else TestWord(word, meaning, languageType.code)
                            }
                    }
                    LanguageType.JAPANESE -> {
                        val json = context.assets.open(JAPANESE_FILE).bufferedReader().use { it.readText() }
                        gson.fromJson(json, JapaneseVocabularyData::class.java).japaneseVocabulary
                            .mapNotNull { item ->
                                val word: String? = item.japanese
                                val meaning: String? = item.korean
                                if (word.isNullOrBlank() || meaning.isNullOrBlank()) null
                                else TestWord(word, meaning, languageType.code)
                            }
                    }
                    LanguageType.ENGLISH -> {
                        val json = context.assets.open(ENGLISH_FILE).bufferedReader().use { it.readText() }
                        gson.fromJson(json, EnglishVocabularyData::class.java).englishVocabulary
                            .mapNotNull { item ->
                                val word: String? = item.english
                                val meaning: String? = item.korean
                                if (word.isNullOrBlank() || meaning.isNullOrBlank()) null
                                else TestWord(word, meaning, languageType.code, item.rank)
                            }
                    }
                }
            }
        }
    }

    // =============================================================================================
    // 레벨테스트 문제 생성 (10문제)

    suspend fun generateLevelTest(languageType: LanguageType): Result<LevelTestSession> {
        val allWords = loadVocabularyFromAssets(languageType).getOrElse { return Result.failure(it) }
        if (allWords.size < TOTAL_QUESTIONS * 4) { // 선택지 생성을 위해 최소 40개 필요
            return Result.failure(
                IllegalStateException("단어 데이터가 부족합니다. (필요: ${TOTAL_QUESTIONS * 4}개, 보유: ${allWords.size}개)")
            )
        }
        val questions = buildQuestions(allWords, TOTAL_QUESTIONS)
        return Result.success(LevelTestSession(languageCode = languageType.code, questions = questions))
    }
}

/** 빈도 구간: 101~1000위 / 1001~2000위 / 2001위~ 에서 3:4:3 비율로 뽑는다 */
private val RANK_BANDS = listOf(0..1000, 1001..2000, 2001..Int.MAX_VALUE)
private val BAND_SHARE = listOf(0.3, 0.4, 0.3)

/**
 * 출제할 단어 고르기.
 * 빈도 순위가 있으면(영어) 쉬운·중간·어려운 구간에서 골고루, 쉬운 것부터 순서대로 낸다.
 * 순위가 없으면(일본어·스페인어 목록) 기존처럼 무작위.
 */
fun pickWords(allWords: List<TestWord>, count: Int, random: kotlin.random.Random = kotlin.random.Random): List<TestWord> {
    if (allWords.isEmpty() || allWords.any { it.rank == null }) return allWords.shuffled(random).take(count)
    val picked = mutableListOf<TestWord>()
    RANK_BANDS.forEachIndexed { i, band ->
        val n = if (i == RANK_BANDS.lastIndex) count - picked.size else Math.round(count * BAND_SHARE[i]).toInt()
        picked += allWords.filter { it.rank!! in band }.shuffled(random).take(n)
    }
    if (picked.size < count) {  // 어떤 구간 단어가 모자라면 나머지에서 채움
        picked += (allWords - picked.toSet()).shuffled(random).take(count - picked.size)
    }
    return picked.sortedBy { it.rank }
}

/** 단어 목록에서 문제 생성 (짝수 번은 단어→뜻, 홀수 번은 뜻→단어). 테스트하기 쉽도록 분리 */
fun buildQuestions(allWords: List<TestWord>, count: Int, random: kotlin.random.Random = kotlin.random.Random): List<QuizQuestion> {
    return pickWords(allWords, count, random).mapIndexed { index, word ->
        val type = if (index % 2 == 0) QuestionType.WORD_TO_MEANING else QuestionType.MEANING_TO_WORD
        val answer = if (type == QuestionType.WORD_TO_MEANING) word.koreanMeaning else word.foreignWord
        val wrong = allWords
            .map { if (type == QuestionType.WORD_TO_MEANING) it.koreanMeaning else it.foreignWord }
            .filter { it != answer }
            .distinct()
            .shuffled(random)
            .take(3)
        val options = (wrong + answer).shuffled(random)
        QuizQuestion(word = word, questionType = type, options = options, correctAnswerIndex = options.indexOf(answer))
    }
}
