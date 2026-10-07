package com.example.capstone.quiz

import com.example.capstone.data.local.entity.Vocabulary

data class QuizQuestion(
    val word: String,              // 문제로 출제할 단어
    val correctMeaning: String,   // 정답 뜻
    val options: List<String>     // 4지선다 보기
)

object QuizGenerator {

    /**
     * 단어장 목록에서 주어진 언어 코드에 해당하는 단어들로 퀴즈 문제 리스트 생성
     * - 단어 개수 구간에 따라 문제 수 자동 조정
     * - 각 문제는 정답 1개 + 오답 3개 보기로 구성
     */
    fun generateQuizQuestions(vocabList: List<Vocabulary>, langCode: String): List<QuizQuestion> {
        // 해당 언어의 단어만 필터링
        val filtered = vocabList.filter { it.langCode == langCode }

        // 단어가 10개 미만이면 퀴즈 생성 불가
        if (filtered.size < 10) return emptyList()

        // 단어 수 구간별 문제 수 설정
        val questionCount = when (filtered.size) {
            in 10..19 -> 10
            in 20..39 -> 15
            in 40..59 -> 20
            in 60..99 -> 25
            else -> 30
        }

        val quizList = mutableListOf<QuizQuestion>()
        val shuffled = filtered.shuffled()

        for (entry in shuffled) {
            val correct = entry.meaning
            // 뜻이 같은 단어가 여러 개면 보기가 겹칠 수 있어 뜻 기준으로 중복 제거
            val others = filtered.map { it.meaning }.filter { it != correct }.distinct().shuffled().take(3)
            if (others.size < 3) continue   // 보기 4개를 만들 수 없는 단어는 출제하지 않음
            val options = (others + correct).shuffled()

            quizList.add(
                QuizQuestion(
                    word = entry.word,
                    correctMeaning = correct,
                    options = options
                )
            )

            if (quizList.size == questionCount) break
        }

        return quizList
    }
}


