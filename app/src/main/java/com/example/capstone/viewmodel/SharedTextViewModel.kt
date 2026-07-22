package com.example.capstone.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SharedTextViewModel : ViewModel() {

    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language

    //  일본어 단어 분리를 위한 tokenList 추가
    private val _tokenList = MutableStateFlow<List<String>>(emptyList())
    val tokenList: StateFlow<List<String>> = _tokenList

    /**
     * 기사 본문과 언어, 제목, 단어 리스트(tokenList)를 함께 설정
     */
    fun setText(newText: String, lang: String, newTitle: String, tokens: List<String>? = null) {
        _text.value = newText.trim()
        _language.value = lang
        _title.value = newTitle
        if (tokens != null) {
            _tokenList.value = tokens
        } else {
            _tokenList.value = emptyList()
        }
    }
}
