package com.example.capstone.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * [SharedProfileViewModel]
 * 앱 전체에서 공유되는 사용자 프로필 정보(이름, 생년월일)를 보관합니다.
 */
class SharedProfileViewModel : ViewModel() {
    // Compose용 상태 프로퍼티로 선언: 화면에 자동으로 반영됨
    var name by mutableStateOf("")
        private set

    var birthDate by mutableStateOf("")
        private set

    // 이름 업데이트
    fun updateName(newName: String) {
        name = newName
    }

    // 생년월일 업데이트
    fun updateBirthDate(newBirth: String) {
        birthDate = newBirth
    }
}
