package com.example.capstone.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstone.theme.capstoneTheme

@Composable
fun Info() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Info",
            fontSize = 30.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InfoPreview() {
    capstoneTheme {
        Info()
    }
}
