package com.example.capstone.screen.article

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.capstone.navigation.Screen

/**
 * [ArticleScreen]
 * 앱의 메인 진입 화면으로, 뉴스 언어 선택 버튼들을 배치
 *
 * @param navController Navigation을 위한 컨트롤러 (언어별 기사 화면으로 이동)
 * @param isDarkMode 현재 다크 모드 활성 상태 (true = 다크 모드, false = 라이트 모드)
 */
@Composable
fun ArticleScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean  // ✅ 여기에 추가
) {
    val cardShape = RoundedCornerShape(32.dp)

    Column(
        modifier = modifier
            .then(Modifier.padding(horizontal = 16.dp))
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 영어 뉴스 버튼
        ArticleButton(
            text = "영어 기사",
            onClick = { navController.navigate(Screen.PrimaryNews.route) },
            shape = cardShape,
            isDarkMode = isDarkMode
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 일본어 뉴스 버튼
        ArticleButton(
            text = "일본어 기사",
            onClick = { navController.navigate(Screen.PrimaryNewsJa.route) },
            shape = cardShape,
            isDarkMode = isDarkMode
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 스페인어 뉴스 버튼
        ArticleButton(
            text = "스페인어 기사",
            onClick = { navController.navigate(Screen.PrimaryNewsEs.route) },
            shape = cardShape,
            isDarkMode = isDarkMode
        )
    }
}

/**
 * 언어 버튼 공통 UI 컴포저블
 *
 * @param text 버튼에 표시할 텍스트
 * @param onClick 클릭 시 실행할 동작
 * @param shape 버튼 모양 (RoundedCornerShape 등)
 * @param isDarkMode 현재 다크 모드 활성 상태 (true = 다크 모드, false = 라이트 모드)
 */
@Composable
fun ArticleButton(
    text: String,
    onClick: () -> Unit,
    shape: RoundedCornerShape,
    isDarkMode: Boolean
) {
    // 다크 모드일 때 버튼 배경을 회색으로, 라이트 모드일 때 RoyalBlue로 설정
    val backgroundColor = if (isDarkMode) {
        Color.Gray
    } else {
        Color(0xFF4169E1)
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 28.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge
        )
    }
}
