package com.example.capstone.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.capstone.data.local.entity.Vocabulary
import com.example.capstone.viewmodel.WordViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordBookScreen(
    navController: NavHostController,
    wordViewModel: WordViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    // 퀴즈·오답노트에서 돌아와도 고른 언어 탭이 유지되도록 rememberSaveable 사용
    var selectedLanguage by rememberSaveable { mutableStateOf("en") }
    var sortOption by rememberSaveable { mutableStateOf("최신순") }
    var currentWords by remember { mutableStateOf<List<Vocabulary>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedLanguage, sortOption) {
        isLoading = true
        currentWords = emptyList()
        wordViewModel.getWordsByLanguage(selectedLanguage).collectLatest { words ->
            currentWords = if (selectedLanguage == "en" && sortOption == "알파벳순") {
                words.sortedBy { it.word.lowercase() }
            } else {
                words.sortedByDescending { it.vocaId }
            }
            isLoading = false
        }
    }

    Column( modifier = modifier
        .then(Modifier.fillMaxSize())) {
        Text(
            text = when (selectedLanguage) {
                "en" -> "📘 영어 단어장"
                "ja" -> "📙 일본어 단어장"
                "es" -> "📗 스페인어 단어장"
                else -> "📘 단어장"
            },
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        // 퀴즈/오답노트 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { navController.navigate("quiz/$selectedLanguage") },
                modifier = Modifier.weight(1f),
                enabled = currentWords.size >= 10
            ) {
                Text("퀴즈 시작하기")
            }
            Button(
                onClick = { navController.navigate("wrong_note/$selectedLanguage") },
                modifier = Modifier.weight(1f)
            ) {
                Text("오답노트 보기")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 영어일 경우만 정렬 옵션 제공
        if (selectedLanguage == "en") {
            var expanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier
                .padding(start = 16.dp, bottom = 4.dp)
                .wrapContentSize(Alignment.TopStart)) {
                TextButton(onClick = { expanded = true }) {
                    Text("정렬: $sortOption")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text("최신순") },
                        onClick = {
                            sortOption = "최신순"
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("알파벳순") },
                        onClick = {
                            sortOption = "알파벳순"
                            expanded = false
                        }
                    )
                }
            }
        }

        // 단어 리스트
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                key(selectedLanguage) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(currentWords, key = { it.vocaId }) { word ->
                            ListItem(
                                headlineContent = { Text(word.word) },
                                supportingContent = { Text(word.meaning) },
                                trailingContent = {
                                    IconButton(onClick = { wordViewModel.deleteWord(word) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "삭제")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // 언어 선택 탭
        NavigationBar {
            NavigationBarItem(
                selected = selectedLanguage == "en",
                onClick = {
                    selectedLanguage = "en"
                    sortOption = "최신순"
                },
                icon = { Icon(Icons.Default.Language, contentDescription = "영어") },
                label = { Text("영어") }
            )
            NavigationBarItem(
                selected = selectedLanguage == "ja",
                onClick = {
                    selectedLanguage = "ja"
                    sortOption = "최신순"
                },
                icon = { Icon(Icons.Default.Language, contentDescription = "일본어") },
                label = { Text("일본어") }
            )
            NavigationBarItem(
                selected = selectedLanguage == "es",
                onClick = {
                    selectedLanguage = "es"
                    sortOption = "최신순"
                },
                icon = { Icon(Icons.Default.Language, contentDescription = "스페인어") },
                label = { Text("스페인어") }
            )
        }
    }
}


