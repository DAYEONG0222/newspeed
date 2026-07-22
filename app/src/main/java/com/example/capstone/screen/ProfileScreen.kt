// File: app/src/main/java/com/example/capstone/screen/ProfileScreen.kt
package com.example.capstone.screen

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.capstone.navigation.Screen
import com.example.capstone.viewmodel.SharedProfileViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: SharedProfileViewModel
) {
    var nameInput by remember { mutableStateOf(profileViewModel.name) }
    var birthInput by remember { mutableStateOf(profileViewModel.birthDate) }
    var isEditing by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("프로필") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (!isEditing) {
                // --- 보기 모드 ---
                Text(
                    text = "이름",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = nameInput.ifBlank { "등록된 이름이 없습니다." },
                    fontSize = 25.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "생년월일",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = birthInput.ifBlank { "등록된 생년월일이 없습니다." },
                    fontSize = 25.sp
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { isEditing = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(text = "수정하기", fontSize = 16.sp)
                }
            } else {
                // --- 편집 모드 ---
                Text(
                    text = "이름 수정",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "생년월일 수정 (클릭하여 선택)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = birthInput,
                    onValueChange = { /* 직접 입력 금지 */ },
                    singleLine = true,
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showDatePicker(context) { selectedDate ->
                                birthInput = selectedDate
                            }
                        }
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (nameInput.isNotBlank() && birthInput.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                            profileViewModel.updateName(nameInput)
                            profileViewModel.updateBirthDate(birthInput)
                            isEditing = false
                        } else {
                            // 잘못된 형식일 때 안내
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(text = "저장하기", fontSize = 16.sp)
                }
            }
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

    datePicker.datePicker.maxDate = calendar.timeInMillis
    datePicker.show()
}
