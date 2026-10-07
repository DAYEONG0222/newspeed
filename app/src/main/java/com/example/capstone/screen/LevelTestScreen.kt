package com.example.capstone.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.capstone.data.local.UserLevelStore
import com.example.capstone.data.model.LanguageType
import com.example.capstone.data.model.LevelTestSession
import com.example.capstone.data.service.LevelTestService

/**
 * 어휘 레벨 테스트 (10문항). 정답률 80% 이상 고급, 60% 이상 중급, 그 아래 초급.
 * 결과를 저장하면 뉴스 화면의 My Level / Other Level 이 이 레벨 기준으로 나뉜다.
 */
@Composable
fun LevelTestScreen(navController: NavController, langCode: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val language = LanguageType.fromCode(langCode) ?: LanguageType.JAPANESE
    var session by remember { mutableStateOf<LevelTestSession?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var index by remember { mutableStateOf(0) }

    LaunchedEffect(langCode) {
        LevelTestService(context).generateLevelTest(language)
            .onSuccess { session = it }
            .onFailure { error = it.message }
    }

    Column(
        modifier = modifier.then(Modifier.padding(16.dp)).fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("${language.displayName} 레벨 테스트", style = MaterialTheme.typography.headlineSmall)
        val current = session
        when {
            error != null -> Text(error ?: "", color = MaterialTheme.colorScheme.error)
            current == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            index >= current.questions.size -> {
                val level = current.getDeterminedLevel()
                Text("정답 ${current.getCorrectAnswersText()} (${current.getAccuracyText()})",
                    style = MaterialTheme.typography.titleLarge)
                Text("추천 레벨: ${level.displayName}", style = MaterialTheme.typography.displaySmall)
                Text(level.description, style = MaterialTheme.typography.bodyMedium)
                Button(
                    onClick = {
                        UserLevelStore.set(langCode, level)
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("이 레벨로 설정") }
                OutlinedButton(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                    Text("저장하지 않고 나가기")
                }
            }
            else -> {
                val question = current.questions[index]
                Text("${index + 1} / ${current.questions.size}", style = MaterialTheme.typography.titleMedium)
                Text(question.questionText, style = MaterialTheme.typography.headlineLarge)
                question.options.forEachIndexed { optionIndex, option ->
                    Button(
                        onClick = {
                            // LevelTestSession.submitAnswer() 는 currentQuestionIndex 가 val 이라 항상
                            // 1번 문제와 비교한다. 답안 목록에 순서대로 넣으면 채점(zip)은 올바르게 된다.
                            current.userAnswers.add(optionIndex)
                            index++
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(option) }
                }
            }
        }
    }
}
