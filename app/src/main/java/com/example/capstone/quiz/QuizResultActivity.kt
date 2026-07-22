package com.example.capstone.quiz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone.R
import com.example.capstone.MainActivity

class QuizResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_result)

        // 전달받은 퀴즈 결과 값 (전체 문제 수, 정답 수)
        val total = intent.getIntExtra("total", 0)
        val correct = intent.getIntExtra("correct", 0)

        // 결과 텍스트 표시
        val scoreText = findViewById<TextView>(R.id.textScore)
        scoreText.text = "$correct / $total 정답"

        // 홈으로 돌아가기 버튼 클릭 시 MainActivity로 이동
        val btnBack = findViewById<Button>(R.id.btnBackToHome)
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }
}
