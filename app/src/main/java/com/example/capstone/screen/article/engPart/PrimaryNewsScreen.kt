package com.example.capstone.screen.article.engPart

import android.util.Log
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
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.capstone.data.api.RetrofitClient
import com.example.capstone.ui.components.ArticleCard
import com.example.capstone.ui.components.EmptyLevelMessage
import com.example.capstone.ui.components.LevelBar
import com.example.capstone.viewmodel.NewsViewModel
import com.example.capstone.viewmodel.SharedTextViewModel
import com.example.capstone.viewmodel.SharedUrlViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrimaryNewsScreen(
    navController: NavHostController,
    sharedUrlViewModel: SharedUrlViewModel,
    sharedTextViewModel: SharedTextViewModel,
) {
    val owner = LocalViewModelStoreOwner.current
    val viewModel: NewsViewModel = viewModel(viewModelStoreOwner = owner!!)

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val articles by viewModel.primaryArticles.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val categories = listOf(
        "정치" to "politics",
        "경제" to "business",
        "사회" to "world",
        "기술" to "technology",
        "과학" to "science",
        "건강" to "health",
        "스포츠" to "sports",
        "연예" to "entertainment"
    )

    val currentLabel = categories.firstOrNull { it.second == selectedCategory }?.first ?: "정치"
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(selectedCategory) {
        viewModel.fetchNews("en", selectedCategory)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.weight(1f)
            ) {
                TextField(
                    value = currentLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("카테고리") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor()
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { (label, query) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                expanded = false
                                viewModel.setCategory(query)
                                viewModel.fetchNews("en", query, forceRefresh = true)
                            }
                        )
                    }
                }



            }

            IconButton(
                onClick = {
                    viewModel.fetchNews("en", selectedCategory, forceRefresh = true)
                }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "새로고침")
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        LevelBar(langCode = "en", navController = navController)

        when {
            isLoading && articles.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            !isLoading && articles.isEmpty() -> {
                EmptyLevelMessage(langCode = "en")
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(articles) { article ->
                        ArticleCard(article = article) {
                            coroutineScope.launch {
                                try {
                                    sharedUrlViewModel.setUrl(article.url)
                                    val response = RetrofitClient.extractService.extractArticle(mapOf("url" to article.url))
                                    if (response.isSuccessful) {
                                        val text = response.body()?.text ?: ""
                                        sharedTextViewModel.setText(newText = text, lang = "en", newTitle = article.title)
                                        navController.navigate("detail")
                                    } else {
                                        Toast.makeText(context, "본문 추출 실패", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "에러: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}