package com.example.capstone.data.model

import com.google.gson.annotations.SerializedName

// =============================================================================================
// JSON 파싱용 데이터 클래스들

data class SpanishVocabularyData(
    @SerializedName("spanish_vocabulary")
    val spanishVocabulary: List<SpanishVocabularyItem>
)

data class SpanishVocabularyItem(
    @SerializedName("spanish")
    val spanish: String,
    @SerializedName("korean")
    val korean: String,
    @SerializedName("band")
    val band: Int? = null       // 1 쉬움 / 2 중간 / 3 어려움 (빈도 순위 기준)
)

data class JapaneseVocabularyData(
    @SerializedName("japanese_vocabulary")
    val japaneseVocabulary: List<JapaneseVocabularyItem>
)

data class JapaneseVocabularyItem(
    @SerializedName("japanese")
    val japanese: String,
    @SerializedName("korean")
    val korean: String,
    @SerializedName("band")
    val band: Int? = null       // 1 = N5·N4, 2 = N3, 3 = N2·N1
)

data class EnglishVocabularyData(
    @SerializedName("english_vocabulary")
    val englishVocabulary: List<EnglishVocabularyItem>
)

data class EnglishVocabularyItem(
    @SerializedName("english")
    val english: String,
    @SerializedName("korean")
    val korean: String,
    @SerializedName("rank")
    val rank: Int? = null,      // NGSL 빈도 순위 (작을수록 자주 쓰이는 쉬운 단어)
    @SerializedName("band")
    val band: Int? = null       // 1: ~1000위, 2: ~2000위, 3: 그 이후
)

// =============================================================================================
// 테스트용 데이터 클래스들

data class TestWord(
    val foreignWord: String,
    val koreanMeaning: String,
    val languageCode: String,
    val band: Int? = null       // 출제 구간 1 쉬움 / 2 중간 / 3 어려움
)

data class QuizQuestion(
    val word: TestWord,
    val questionType: QuestionType,
    val options: List<String>, // 4개의 선택지
    val correctAnswerIndex: Int
) {
    val questionText: String
        get() = when (questionType) {
            QuestionType.WORD_TO_MEANING -> word.foreignWord
            QuestionType.MEANING_TO_WORD -> word.koreanMeaning
        }

    val correctAnswer: String
        get() = options[correctAnswerIndex]
}

data class LevelTestSession(
    val languageCode: String,
    val questions: List<QuizQuestion>,
    val currentQuestionIndex: Int = 0,
    val userAnswers: MutableList<Int> = mutableListOf(),
    val startTime: Long = System.currentTimeMillis()
) {
    companion object {
        const val TOTAL_QUESTIONS = 10
    }

    val totalQuestions: Int get() = TOTAL_QUESTIONS
    val isCompleted: Boolean get() = currentQuestionIndex >= questions.size

    val correctAnswersCount: Int
        get() = userAnswers.zip(questions).count { (userAnswer, question) ->
            userAnswer == question.correctAnswerIndex
        }

    val accuracyPercentage: Double
        get() = if (userAnswers.isNotEmpty()) {
            (correctAnswersCount.toDouble() / userAnswers.size) * 100
        } else 0.0

    fun getCurrentQuestion(): QuizQuestion? = questions.getOrNull(currentQuestionIndex)

    fun submitAnswer(selectedIndex: Int): Boolean {
        val currentQuestion = getCurrentQuestion() ?: return false
        userAnswers.add(selectedIndex)
        return selectedIndex == currentQuestion.correctAnswerIndex
    }

    fun nextQuestion(): QuizQuestion? {
        return if (currentQuestionIndex < questions.size - 1) {
            questions[currentQuestionIndex + 1]
        } else null
    }

    fun getDeterminedLevel(): UserLevel {
        return when {
            accuracyPercentage >= 80.0 -> UserLevel.ADVANCED     // 8-10개
            accuracyPercentage >= 60.0 -> UserLevel.INTERMEDIATE // 6-7개
            else -> UserLevel.BEGINNER                           // 0-5개
        }
    }

    fun getElapsedTimeSeconds(): Long = (System.currentTimeMillis() - startTime) / 1000

    fun getCorrectAnswersText(): String = "$correctAnswersCount/$totalQuestions"

    fun getAccuracyText(): String = "${accuracyPercentage.toInt()}%"
}

// =============================================================================================
// 열거형들

enum class QuestionType {
    WORD_TO_MEANING, // 외국어 단어 → 한국어 의미
    MEANING_TO_WORD  // 한국어 의미 → 외국어 단어
}

enum class LanguageType(val displayName: String, val code: String) {
    SPANISH("스페인어", "es"),
    JAPANESE("일본어", "ja"),
    ENGLISH("영어", "en");

    companion object {
        fun fromCode(code: String): LanguageType? {
            return values().find { it.code == code }
        }
    }
}

enum class UserLevel(val displayName: String, val description: String) {
    BEGINNER("초급", "기초 단어를 익히는 단계입니다"),
    INTERMEDIATE("중급", "일상 회화가 가능한 단계입니다"),
    ADVANCED("고급", "유창한 의사소통이 가능한 단계입니다");

    companion object {
        fun fromAccuracy(accuracy: Double): UserLevel {
            return when {
                accuracy >= 80.0 -> ADVANCED
                accuracy >= 60.0 -> INTERMEDIATE
                else -> BEGINNER
            }
        }

        fun fromDisplayName(displayName: String): UserLevel? {
            return values().find { it.displayName == displayName }
        }
    }
}

enum class TestState {
    NOT_STARTED,    // 시작 전
    IN_PROGRESS,    // 진행 중
    COMPLETED       // 완료
}