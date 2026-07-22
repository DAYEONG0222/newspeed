package com.example.capstone.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstone.theme.capstoneTheme

/**
 * 다크 모드 토글을 포함한 Settings 화면
 *
 * @param isDarkMode 현재 다크 모드 활성화 여부 (true = 다크 모드, false = 라이트 모드)
 * @param onToggleDarkMode 다크 모드 스위치 토글 시 호출되는 콜백
 */
@Composable
fun Settings(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 제목 폰트 크기 키움
        Text(
            text = "Settings",
            fontSize = 34.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 다크 모드 토글 Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // 레이블 폰트 크기 키움
            Text(
                text = "Dark Mode",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 25.sp
            )
            Spacer(modifier = Modifier.weight(1f))

            // 스위치 크기를 1.5배 확대
            Switch(
                checked = isDarkMode,
                onCheckedChange = { checked ->
                    onToggleDarkMode(checked)
                },
                modifier = Modifier.scale(1.3f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    capstoneTheme(darkTheme = false) {
        Settings(
            isDarkMode = false,
            onToggleDarkMode = {}
        )
    }
}
