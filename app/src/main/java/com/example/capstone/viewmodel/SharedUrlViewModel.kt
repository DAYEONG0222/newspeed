package com.example.capstone.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * [SharedUrlViewModel]
 * 뉴스 상세 화면으로 이동할 때 URL을 안전하게 공유하기 위한 ViewModel
 * - Navigation 인자에 URL을 직접 넘기기 어려울 때 사용
 * - StateFlow를 통해 Compose에서 자동 반영 가능
 */
class SharedUrlViewModel : ViewModel() {

    // 현재 선택된 기사 URL을 저장하는 상태
    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url

    /** 외부에서 URL을 설정하는 함수 */
    fun setUrl(newUrl: String) {
        _url.value = newUrl
    }
}
