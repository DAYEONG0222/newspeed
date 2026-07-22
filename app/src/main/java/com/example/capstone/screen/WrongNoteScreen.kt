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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.capstone.data.local.entity.Vocabulary
import com.example.capstone.quiz.SortOption
import com.example.capstone.quiz.WrongNoteManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrongNoteScreen(navController: NavController, langCode: String,
                    modifier: Modifier = Modifier) {
    var rawWrongList by remember { mutableStateOf(listOf<Vocabulary>()) }
    var sortedList by remember { mutableStateOf(listOf<Vocabulary>()) }

    val sortOptions = SortOption.values().toList()
    var selectedSort by remember { mutableStateOf(SortOption.LATEST) }
    var expanded by remember { mutableStateOf(false) }

    fun applySort() {
        sortedList = when (selectedSort) {
            SortOption.LATEST -> rawWrongList.sortedByDescending { it.vocaId }
            SortOption.OLDEST -> rawWrongList.sortedBy { it.vocaId }
            SortOption.ALPHABET -> rawWrongList.sortedBy { it.word.lowercase() }
        }
    }

    LaunchedEffect(Unit) {
        rawWrongList = WrongNoteManager.getByLang(langCode)
        applySort()
    }

    LaunchedEffect(selectedSort) {
        applySort()
    }

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
                sortOptions.forEach { option ->
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

        LazyColumn {
            items(sortedList) { word ->
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
                        Column {
                            Text(word.word, style = MaterialTheme.typography.titleMedium)
                            Text(
                                word.meaning,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        IconButton(onClick = {
                            WrongNoteManager.remove(word)
                            rawWrongList = rawWrongList.filter { it.vocaId != word.vocaId }
                            applySort()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "삭제")
                        }
                    }
                }
            }
        }
    }
}



