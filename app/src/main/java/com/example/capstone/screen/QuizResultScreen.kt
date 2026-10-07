package com.example.capstone.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * 퀴즈 결과 화면.
 * 기존 QuizScreen 은 "quiz_result/..." 로 이동했지만 NavGraph 에 해당 경로가 없어서
 * 마지막 문제를 풀면 앱이 종료됐다.
 */
@Composable
fun QuizResultScreen(
    navController: NavController,
    langCode: String,
    total: Int,
    correct: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .then(Modifier.padding(24.dp))
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("퀴즈 결과", style = MaterialTheme.typography.headlineSmall)
        Text("$correct / $total 정답", style = MaterialTheme.typography.displaySmall)
        if (correct < total) {
            Text(
                "틀린 ${total - correct}개 단어는 오답노트에 저장됐어요.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Button(
            onClick = {
                navController.navigate("wrong_note/$langCode") {
                    popUpTo("quiz_result/{langCode}/{total}/{correct}") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("오답노트 보기") }
        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) { Text("단어장으로") }
    }
}
