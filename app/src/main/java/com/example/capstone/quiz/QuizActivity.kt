package com.example.capstone.quiz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.capstone.R
import com.example.capstone.data.local.AppDatabase
import com.example.capstone.data.local.entity.Vocabulary
import kotlinx.coroutines.launch

class QuizActivity : AppCompatActivity() {

    private lateinit var questionList: List<QuizQuestion>
    private var currentIndex = 0
    private var correctCount = 0

    private lateinit var textQuestionNumber: TextView
    private lateinit var textWord: TextView
    private lateinit var optionButtons: List<Button>

    private lateinit var langCode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        // 퀴즈 언어코드 전달받음
        langCode = intent.getStringExtra("langCode") ?: "en"

        // 이전 오답 초기화
        WrongNoteManager.clear()

        // 뷰 연결
        textQuestionNumber = findViewById(R.id.textQuestionNumber)
        textWord = findViewById(R.id.textWord)
        optionButtons = listOf(
            findViewById(R.id.btnOption1),
            findViewById(R.id.btnOption2),
            findViewById(R.id.btnOption3),
            findViewById(R.id.btnOption4)
        )

        // DB에서 단어 불러오고 퀴즈 생성
        lifecycleScope.launch {
            val dao = AppDatabase.getDatabase(applicationContext).vocabularyDao()
            val vocabList = dao.readLanguageVocabulary(langCode)
            questionList = QuizGenerator.generateQuizQuestions(vocabList, langCode)
            showQuestion()
        }
    }

    private fun showQuestion() {
        // 퀴즈 종료 시 결과 화면 이동
        if (currentIndex >= questionList.size) {
            val intent = Intent(this, QuizResultActivity::class.java)
            intent.putExtra("total", questionList.size)
            intent.putExtra("correct", correctCount)
            startActivity(intent)
            finish()
            return
        }

        // 현재 문제 표시
        val question = questionList[currentIndex]
        textQuestionNumber.text = "${currentIndex + 1} / ${questionList.size}"
        textWord.text = question.word

        for (i in 0 until 4) {
            val btn = optionButtons[i]
            btn.text = question.options[i]
            btn.setOnClickListener {
                val selected = question.options[i]

                if (selected == question.correctMeaning) {
                    correctCount++
                } else {
                    // 오답 저장
                    val vocab = Vocabulary(
                        word = question.word,
                        meaning = question.correctMeaning,
                        langCode = langCode,
                        pronunciation = "",
                        isFavorite = false
                    )
                    WrongNoteManager.addWrongAnswer(vocab)
                }

                currentIndex++
                showQuestion()
            }
        }
    }
}
