package com.example.capstone.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.capstone.data.model.ArticleCardItem

@Composable
fun ArticleCard(
    article: ArticleCardItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium
            )
            article.description?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = Int.MAX_VALUE
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "난이도: ${article.difficulty ?: "분석 중"}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

