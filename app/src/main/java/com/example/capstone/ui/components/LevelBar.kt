package com.example.capstone.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.capstone.data.local.UserLevelStore
import com.example.capstone.data.model.LanguageType
import com.example.capstone.data.model.UserLevel
import com.example.capstone.data.service.LevelTestService

/** 뉴스 화면 상단: 현재 레벨 표시 + 직접 변경 + 레벨 테스트(일본어·스페인어) */
@Composable
fun LevelBar(langCode: String, navController: NavController, modifier: Modifier = Modifier) {
    val level by UserLevelStore.level(langCode).collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val testable = LanguageType.fromCode(langCode)?.let { LevelTestService.supports(it) } ?: false

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("내 레벨: ${level.displayName}", style = MaterialTheme.typography.titleSmall)
        Box {
            TextButton(onClick = { expanded = true }) { Text("변경") }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                UserLevel.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.displayName) },
                        onClick = {
                            UserLevelStore.set(langCode, option)
                            expanded = false
                        }
                    )
                }
            }
        }
        if (testable) {
            TextButton(onClick = { navController.navigate("level_test/$langCode") }) { Text("레벨 테스트") }
        }
    }
}

/** My Level 탭이 비었을 때 안내 */
@Composable
fun EmptyLevelMessage(langCode: String, modifier: Modifier = Modifier) {
    val level by UserLevelStore.level(langCode).collectAsState()
    Text(
        "지금 불러온 기사 중에 ${level.displayName} 기사가 없어요.\nOther Level 탭을 보거나 레벨을 바꿔 보세요.",
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier.padding(vertical = 24.dp)
    )
}
