// File: app/src/main/java/com/example/capstone/onboarding/UserOnboardingScreen.kt
package com.example.capstone.onboarding

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.capstone.data.local.dao.UserDao
import com.example.capstone.navigation.Screen
import com.example.capstone.viewmodel.SharedProfileViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserOnboardingScreen(
    navController: NavController,
    profileViewModel: SharedProfileViewModel
) {
    var name by remember { mutableStateOf(profileViewModel.name) }
    var birthDateString by remember { mutableStateOf(profileViewModel.birthDate) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "환영합니다!",
            fontSize = 32.sp,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        // 이름 입력 필드
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("이름") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 생년월일 선택 필드 (클릭 시 DatePickerDialog)
        OutlinedTextField(
            value = birthDateString,
            onValueChange = { /* 직접 입력 금지 */ },
            label = { Text("생년월일 선택") },
            placeholder = { Text("YYYY-MM-DD") },
            singleLine = true,
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showDatePicker(context) { selectedDate ->
                        birthDateString = selectedDate
                    }
                }
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                Log.d("Onboarding", "버튼 클릭 → name='$name', birthDate='$birthDateString'")

                val isValidBirth = birthDateString.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))
                if (name.isNotBlank() && isValidBirth) {
                    profileViewModel.updateName(name)
                    profileViewModel.updateBirthDate(birthDateString)

                    Log.d("Onboarding", "네비게이트 시도: ${Screen.Article.route}")
                    navController.navigate(Screen.Article.route)
                } else {
                    Log.d("Onboarding", "입력 형식이 올바르지 않습니다.")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(text = "계속", fontSize = 18.sp)
        }
    }
}

private fun showDatePicker(
    context: android.content.Context,
    onDateSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePicker = DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
        val monthFormatted = String.format("%02d", selectedMonth + 1)
        val dayFormatted = String.format("%02d", selectedDay)
        val dateString = "$selectedYear-$monthFormatted-$dayFormatted"
        onDateSelected(dateString)
    }, year, month, day)

    // 미래 날짜 선택 금지: 오늘 이전 날짜까지만 선택
    datePicker.datePicker.maxDate = calendar.timeInMillis

    datePicker.show()
}
