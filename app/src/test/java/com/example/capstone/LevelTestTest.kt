package com.example.capstone

import com.example.capstone.data.model.LevelTestSession
import com.example.capstone.data.model.TestWord
import com.example.capstone.data.model.UserLevel
import com.example.capstone.data.service.buildQuestions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LevelTestTest {

    private val words = (1..50).map { TestWord("w$it", if (it % 5 == 0) "같은뜻" else "뜻$it", "ja") }

    @Test
    fun questionsHaveFourDistinctOptionsWithValidAnswer() {
        val questions = buildQuestions(words, 10, Random(1))
        assertEquals(10, questions.size)
        for (q in questions) {
            assertEquals(4, q.options.distinct().size)
            assertTrue(q.correctAnswerIndex in 0..3)
            assertEquals(q.correctAnswer, q.options[q.correctAnswerIndex])
        }
    }

    @Test
    fun gradingUsesEachQuestionsOwnAnswer() {
        // 기존 submitAnswer() 는 항상 1번 문제와 비교했다. 답안을 순서대로 넣으면 문제별로 채점돼야 한다.
        val questions = buildQuestions(words, 10, Random(2))
        val session = LevelTestSession("ja", questions)
        questions.forEachIndexed { i, q ->
            session.userAnswers.add(if (i < 7) q.correctAnswerIndex else (q.correctAnswerIndex + 1) % 4)
        }
        assertEquals(7, session.correctAnswersCount)
        assertEquals(UserLevel.INTERMEDIATE, session.getDeterminedLevel())
    }
}
