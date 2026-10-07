package com.example.capstone.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.capstone.data.local.AppDatabase
import com.example.capstone.data.local.entity.WrongAnswer
import kotlinx.coroutines.launch

enum class SortOption(val label: String) {
    LATEST("최신순"),
    OLDEST("오래된순"),
    MOST_WRONG("많이 틀린순"),
    ALPHABET("알파벳순")
}

fun sortWrongAnswers(items: List<WrongAnswer>, option: SortOption): List<WrongAnswer> = when (option) {
    SortOption.LATEST -> items.sortedByDescending { it.lastWrongAt }
    SortOption.OLDEST -> items.sortedBy { it.lastWrongAt }
    SortOption.MOST_WRONG -> items.sortedWith(compareByDescending<WrongAnswer> { it.wrongCount }.thenByDescending { it.lastWrongAt })
    SortOption.ALPHABET -> items.sortedBy { it.word.lowercase() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrongNoteScreen(navController: NavController, langCode: String,
                    modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = remember { AppDatabase.getDatabase(context).wrongAnswerDao() }

    // DB 를 관찰하므로 삭제·추가가 바로 화면에 반영된다
    val wrongList by dao.observeByLanguage(langCode).collectAsState(initial = emptyList())
    var selectedSort by remember { mutableStateOf(SortOption.LATEST) }
    var expanded by remember { mutableStateOf(false) }
    val sortedList = remember(wrongList, selectedSort) { sortWrongAnswers(wrongList, selectedSort) }

    Column(
        modifier = modifier
            .then(Modifier.padding(16.dp))
            .fillMaxSize()
    ) {
        Text("📝 오답노트", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        // 정렬 드롭다운
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedSort.label,
                onValueChange = {},
                readOnly = true,
                label = { Text("정렬 방식") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                SortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            selectedSort = option
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (sortedList.isEmpty()) {
            Text("아직 틀린 단어가 없어요.", style = MaterialTheme.typography.bodyMedium)
        }

        LazyColumn {
            items(sortedList, key = { it.id }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.word, style = MaterialTheme.typography.titleMedium)
                            Text(
                                item.meaning,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Text("${item.wrongCount}회", style = MaterialTheme.typography.labelLarge)
                        // id 로 하나만 삭제 (기존엔 모든 오답의 id 가 0 이라 하나를 지우면 전부 지워졌다)
                        IconButton(onClick = { scope.launch { dao.deleteById(item.id) } }) {
                            Icon(Icons.Default.Delete, contentDescription = "삭제")
                        }
                    }
                }
            }
        }
    }
}
