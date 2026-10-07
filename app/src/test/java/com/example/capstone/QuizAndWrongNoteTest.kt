package com.example.capstone

import com.example.capstone.data.local.entity.Vocabulary
import com.example.capstone.data.local.entity.WrongAnswer
import com.example.capstone.quiz.QuizGenerator
import com.example.capstone.screen.SortOption
import com.example.capstone.screen.sortWrongAnswers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizAndWrongNoteTest {

    private fun vocab(word: String, meaning: String) =
        Vocabulary(word = word, langCode = "es", meaning = meaning, pronunciation = "", isFavorite = false)

    @Test
    fun quizOptionsAreFourDistinctMeaningsIncludingAnswer() {
        // 뜻이 겹치는 단어가 섞여 있어도 보기가 중복되면 안 된다
        val words = (1..12).map { vocab("w$it", if (it % 2 == 0) "같은뜻" else "뜻$it") }
        val questions = QuizGenerator.generateQuizQuestions(words, "es")
        assertTrue(questions.isNotEmpty())
        for (q in questions) {
            assertEquals(4, q.options.size)
            assertEquals(4, q.options.distinct().size)
            assertTrue(q.correctMeaning in q.options)
        }
    }

    @Test
    fun wrongNoteSortsByRealTimestampsAndCounts() {
        val items = listOf(
            WrongAnswer(id = 1, word = "b", meaning = "", langCode = "es", wrongCount = 1, lastWrongAt = 100),
            WrongAnswer(id = 2, word = "a", meaning = "", langCode = "es", wrongCount = 3, lastWrongAt = 300),
            WrongAnswer(id = 3, word = "c", meaning = "", langCode = "es", wrongCount = 2, lastWrongAt = 200),
        )
        assertEquals(listOf(2L, 3L, 1L), sortWrongAnswers(items, SortOption.LATEST).map { it.id })
        assertEquals(listOf(1L, 3L, 2L), sortWrongAnswers(items, SortOption.OLDEST).map { it.id })
        assertEquals(listOf(2L, 3L, 1L), sortWrongAnswers(items, SortOption.MOST_WRONG).map { it.id })
        assertEquals(listOf("a", "b", "c"), sortWrongAnswers(items, SortOption.ALPHABET).map { it.word })
    }
}
