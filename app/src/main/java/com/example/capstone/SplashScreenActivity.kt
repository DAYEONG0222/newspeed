package com.example.capstone

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.capstone.R
import kotlinx.coroutines.delay

class SplashScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) setContent가 실행돼야 스플래시 UI가 그려짐
        setContent {
            SplashScreenContent {
                // 2) 지연 뒤 MainActivity로 전환
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}

@Composable
fun SplashScreenContent(onTimeout: () -> Unit) {
    // 3) 컴포저블이 화면에 랜더링될 때, LaunchedEffect가 실행됨
    LaunchedEffect(Unit) {
        delay(2000L) // 2초 대기
        onTimeout()
    }

    // 4) 전체 화면 중앙에 로고 표시
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_splash_logo),
            contentDescription = "Splash Logo",
            modifier = Modifier
                .width(200.dp)
                .height(200.dp)
        )
    }
}
