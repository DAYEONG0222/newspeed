package com.example.capstone.data.service

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Transaction
import com.example.capstone.data.local.dao.UserLanguageGradeDao
import com.example.capstone.data.local.dao.UserLanguageGradeHistoryDao
import com.example.capstone.data.local.entity.UserLanguageGrade
import com.example.capstone.data.local.entity.UserLanguageGradeHistory
import com.example.capstone.data.model.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
class LevelTestService(
    private val userLanguageGradeDao: UserLanguageGradeDao,
    private val userLanguageGradeHistoryDao: UserLanguageGradeHistoryDao,
    private val context: Context,
    private val gson: Gson = Gson()
) {

    companion object {
        const val TOTAL_QUESTIONS = 10

        // Assets 파일 경로
        private const val SPANISH_FILE = "vocabulary/spanish.json"
        private const val JAPANESE_FILE = "vocabulary/japanese.json"
        private const val ENGLISH_FILE = "vocabulary/english.json"
    }

    // =============================================================================================
    // JSON 파일에서 단어 로드

    suspend fun loadVocabularyFromAssets(languageType: LanguageType): Result<List<TestWord>> {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = when (languageType) {
                    LanguageType.SPANISH -> SPANISH_FILE
                    LanguageType.JAPANESE -> JAPANESE_FILE
                    LanguageType.ENGLISH -> ENGLISH_FILE
                }

                val jsonString = context.assets.open(fileName)
                    .bufferedReader()
                    .use { it.readText() }

                val testWords = when (languageType) {
                    LanguageType.SPANISH -> {
                        val data = gson.fromJson(jsonString, SpanishVocabularyData::class.java)
                        data.spanishVocabulary.map { item ->
                            TestWord(
                                foreignWord = item.spanish,
                                koreanMeaning = item.korean,
                                languageCode = languageType.code
                            )
                        }
                    }
                    LanguageType.JAPANESE -> {
                        val data = gson.fromJson(jsonString, JapaneseVocabularyData::class.java)
                        data.japaneseVocabulary.map { item ->
                            TestWord(
                                foreignWord = item.japanese,
                                koreanMeaning = item.korean,
                                languageCode = languageType.code
                            )
                        }
                    }
                    LanguageType.ENGLISH -> {
                        val data = gson.fromJson(jsonString, EnglishVocabularyData::class.java)
                        data.englishVocabulary.map { item ->
                            TestWord(
                                foreignWord = item.english,
                                koreanMeaning = item.korean,
                                languageCode = languageType.code
                            )
                        }
                    }
                }

                Result.success(testWords)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // =============================================================================================
    // 레벨테스트 문제 생성 (10문제)

    suspend fun generateLevelTest(languageType: LanguageType): Result<LevelTestSession> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. 단어 로드
                val wordsResult = loadVocabularyFromAssets(languageType)
                if (wordsResult.isFailure) {
                    return@withContext Result.failure(wordsResult.exceptionOrNull()!!)
                }

                val allWords = wordsResult.getOrNull()!!

                // 2. 단어 수 검증
                if (allWords.size < TOTAL_QUESTIONS * 4) { // 선택지 생성을 위해 최소 40개 필요
                    return@withContext Result.failure(
                        Exception("단어 데이터가 부족합니다. (필요: ${TOTAL_QUESTIONS * 4}개, 보유: ${allWords.size}개)")
                    )
                }

                // 3. 랜덤 단어 선택 (10개)
                val selectedWords = allWords.shuffled().take(TOTAL_QUESTIONS)

                // 4. 퀴즈 문제 생성
                val questions = selectedWords.mapIndexed { index, word ->
                    val questionType = if (index % 2 == 0) {
                        QuestionType.WORD_TO_MEANING
                    } else {
                        QuestionType.MEANING_TO_WORD
                    }

                    generateQuizQuestion(word, questionType, allWords)
                }

                // 5. 테스트 세션 생성
                val session = LevelTestSession(
                    languageCode = languageType.code,
                    questions = questions
                )

                Result.success(session)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // =============================================================================================
    // 개별 퀴즈 문제 생성 (4지선다)

    private fun generateQuizQuestion(
        word: TestWord,
        questionType: QuestionType,
        allWords: List<TestWord>
    ): QuizQuestion {
        val options = when (questionType) {
            QuestionType.WORD_TO_MEANING -> {
                // 문제: 외국어 단어 → 정답: 한국어 의미
                val wrongAnswers = allWords
                    .filter { it.koreanMeaning != word.koreanMeaning }
                    .map { it.koreanMeaning }
                    .distinct()
                    .shuffled()
                    .take(3)

                (listOf(word.koreanMeaning) + wrongAnswers).shuffled()
            }
            QuestionType.MEANING_TO_WORD -> {
                // 문제: 한국어 의미 → 정답: 외국어 단어
                val wrongAnswers = allWords
                    .filter { it.foreignWord != word.foreignWord }
                    .map { it.foreignWord }
                    .distinct()
                    .shuffled()
                    .take(3)

                (listOf(word.foreignWord) + wrongAnswers).shuffled()
            }
        }

        val correctAnswer = when (questionType) {
            QuestionType.WORD_TO_MEANING -> word.koreanMeaning
            QuestionType.MEANING_TO_WORD -> word.foreignWord
        }

        val correctAnswerIndex = options.indexOf(correctAnswer)

        return QuizQuestion(
            word = word,
            questionType = questionType,
            options = options,
            correctAnswerIndex = correctAnswerIndex
        )
    }

    // =============================================================================================
    // 테스트 완료 및 결과 저장

    @RequiresApi(Build.VERSION_CODES.O)
    @Transaction
    suspend fun completeTest(session: LevelTestSession, userName: String): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                val accuracy = session.accuracyPercentage
                val grade = determineGrade(accuracy)
                val now = LocalDateTime.now()

                // 1. 테스트 히스토리 저장
                val history = UserLanguageGradeHistory(
                    name = userName,
                    langCode = session.languageCode,
                    grade = grade.displayName,
                    accuracyPercentage = accuracy,
                    updatedTime = now
                )

                val historyId = userLanguageGradeHistoryDao.insertGradeHistory(history)

                // 2. 현재 등급 업데이트
                val currentGrade = UserLanguageGrade(
                    name = userName,
                    langCode = session.languageCode,
                    grade = grade.displayName,
                    updatedTime = now
                )

                userLanguageGradeDao.insertOrUpdateGrade(currentGrade)

                Result.success(historyId)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // =============================================================================================
    // 등급 결정 (10문제 기준)

    private fun determineGrade(accuracyPercentage: Double): UserLevel {
        return when {
            accuracyPercentage >= 80.0 -> UserLevel.ADVANCED     // 8-10개 맞음
            accuracyPercentage >= 60.0 -> UserLevel.INTERMEDIATE // 6-7개 맞음
            else -> UserLevel.BEGINNER                           // 0-5개 맞음
        }
    }

    // =============================================================================================
    // 사용자 통계 조회

    suspend fun getUserStats(userName: String, langCode: String): Result<UserTestStats> {
        return withContext(Dispatchers.IO) {
            try {
                val currentGrade = userLanguageGradeDao.getUserLanguageGrade(userName, langCode)
                val overallStats = userLanguageGradeHistoryDao.getUserOverallStats(userName, langCode)
                val recentTests = userLanguageGradeHistoryDao.getRecentTestResults(userName, langCode, 5)

                val stats = UserTestStats(
                    currentGrade = currentGrade?.grade ?: "미측정",
                    totalTests = overallStats?.totalTests ?: 0,
                    overallAverage = overallStats?.overallAverage ?: 0.0,
                    bestScore = overallStats?.bestScore ?: 0.0,
                    recentTests = recentTests
                )

                Result.success(stats)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // =============================================================================================
    // 단어장 정보 조회

    suspend fun getVocabularyInfo(languageType: LanguageType): Result<VocabularyInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val wordsResult = loadVocabularyFromAssets(languageType)
                if (wordsResult.isFailure) {
                    return@withContext Result.failure(wordsResult.exceptionOrNull()!!)
                }

                val words = wordsResult.getOrNull()!!
                val info = VocabularyInfo(
                    languageType = languageType,
                    totalWords = words.size,
                    canGenerateTest = words.size >= TOTAL_QUESTIONS * 4
                )

                Result.success(info)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

// =============================================================================================
// 데이터 클래스들

data class UserTestStats(
    val currentGrade: String,
    val totalTests: Int,
    val overallAverage: Double,
    val bestScore: Double,
    val recentTests: List<UserLanguageGradeHistory>
)

data class VocabularyInfo(
    val languageType: LanguageType,
    val totalWords: Int,
    val canGenerateTest: Boolean
)