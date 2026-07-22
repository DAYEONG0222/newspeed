package com.example.capstone.screen.article.spanishPart

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.capstone.ui.components.ArticleCard
import com.example.capstone.viewmodel.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondaryNewsScreenEs(
    navController: NavHostController,
    sharedUrlViewModel: SharedUrlViewModel,
    sharedTextViewModel: SharedTextViewModel,
    viewModel: EsNewsViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val articles by viewModel.secondaryArticles.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val allArticles by viewModel.articles.collectAsState()

    LaunchedEffect(Unit) {
        if (!viewModel.hasFetched(selectedCategory)) {
            viewModel.fetchSpanishNews(selectedCategory)
        }
    }

    val categories = listOf(
        "정치", "경제", "사회", "기술", "과학", "건강", "스포츠", "연예"
    )

    var sortOption by remember { mutableStateOf("기본") }
    val difficultyOrder = { difficulty: String? ->
        when (difficulty) {
            "초급" -> 1
            "중급" -> 2
            "고급" -> 3
            else -> Int.MAX_VALUE
        }
    }

    val sortedArticles = when (sortOption) {
        "난이도 오름차순" -> articles.sortedBy { difficultyOrder(it.difficulty) }
        "난이도 내림차순" -> articles.sortedByDescending { difficultyOrder(it.difficulty) }
        else -> articles
    }

    var expandedCategory by remember { mutableStateOf(false) }
    var expandedSort by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = !expandedCategory },
                modifier = Modifier.weight(1f)
            ) {
                TextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("카테고리") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCategory) },
                    modifier = Modifier.menuAnchor()
                )
                DropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { label ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                expandedCategory = false
                                viewModel.setCategory(label)
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = expandedSort,
                onExpandedChange = { expandedSort = !expandedSort },
                modifier = Modifier.weight(1f)
            ) {
                TextField(
                    value = sortOption,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("정렬") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedSort) },
                    modifier = Modifier.menuAnchor()
                )
                DropdownMenu(
                    expanded = expandedSort,
                    onDismissRequest = { expandedSort = false }
                ) {
                    listOf("기본", "난이도 오름차순", "난이도 내림차순").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                sortOption = option
                                expandedSort = false
                            }
                        )
                    }
                }
            }

            IconButton(onClick = {
                viewModel.fetchSpanishNews(selectedCategory, forceRefresh = true)
            }) {
                Icon(Icons.Default.Refresh, contentDescription = "새로고침")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading && sortedArticles.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            !isLoading && sortedArticles.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("중급/고급 기사가 없습니다.", style = MaterialTheme.typography.bodyLarge)
                }
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sortedArticles) { article ->
                        ArticleCard(article = article) {
                            coroutineScope.launch {
                                sharedUrlViewModel.setUrl(article.url ?: "")
                                sharedTextViewModel.setText(
                                    newText = article.content ?: "내용 없음",
                                    lang = "es",
                                    newTitle = article.title ?: "제목 없음"
                                )
                                navController.navigate("detail-es")
                            }
                        }
                    }
                }
            }
        }
    }
}
