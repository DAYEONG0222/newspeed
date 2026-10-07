package com.example.capstone.screen

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.capstone.data.local.AppDatabase
import com.example.capstone.quiz.QuizGenerator
import com.example.capstone.quiz.QuizQuestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun QuizScreen(navController: NavController, langCode: String = "en",
               modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val wrongAnswerDao = remember { AppDatabase.getDatabase(context).wrongAnswerDao() }
    var questionList by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(0) }
    var correctCount by remember { mutableStateOf(0) }

    // 퀴즈 데이터 로딩 (이전 오답 기록은 지우지 않고 누적)
    LaunchedEffect(Unit) {
        questionList = loadQuestions(context, langCode)
    }

    // 데이터가 아직 준비되지 않았을 때
    if (questionList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // 퀴즈가 끝났을 경우: 컴포지션 중 직접 navigate 하지 않고 한 번만 이동
    if (currentIndex >= questionList.size) {
        LaunchedEffect(Unit) {
            navController.navigate("quiz_result/$langCode/${questionList.size}/$correctCount") {
                popUpTo("quiz/{langCode}") { inclusive = true }
            }
        }
        return
    }

    // 현재 퀴즈 표시
    val current = questionList[currentIndex]

    Column(
        modifier = modifier
            .then(Modifier.padding(16.dp))
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(text = "${currentIndex + 1} / ${questionList.size}", style = MaterialTheme.typography.titleMedium)
        Text(text = current.word, style = MaterialTheme.typography.headlineLarge)

        current.options.forEach { option ->
            Button(
                onClick = {
                    if (option == current.correctMeaning) {
                        correctCount++
                    } else {
                        // 오답 저장 (DB, 같은 단어면 틀린 횟수 +1)
                        scope.launch {
                            wrongAnswerDao.recordWrong(current.word, current.correctMeaning, langCode)
                        }
                    }
                    currentIndex++
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(option)
            }
        }
    }
}

// 비동기 퀴즈 문제 생성
private suspend fun loadQuestions(context: Context, langCode: String): List<QuizQuestion> {
    return withContext(Dispatchers.IO) {
        val dao = AppDatabase.getDatabase(context).vocabularyDao()
        val vocabList = dao.readLanguageVocabulary(langCode)
        QuizGenerator.generateQuizQuestions(vocabList, langCode)
    }
}
