package com.example.capstone.quiz

import com.example.capstone.data.local.entity.Vocabulary

object WrongNoteManager {

    private val wrongList = mutableListOf<Vocabulary>()

    // 오답 저장
    fun addWrongAnswer(word: Vocabulary) {
        wrongList.add(word)
    }

    // 오답 리스트 전체 반환
    fun getAll(): List<Vocabulary> {
        return wrongList.toList() // 외부 수정 방지용 복사본
    }

    // 오답 초기화
    fun clear() {
        wrongList.clear()
    }

    // 언어코드로 필터링된 오답 리스트 반환
    fun getByLang(langCode: String): List<Vocabulary> {
        return wrongList.filter { it.langCode == langCode }
    }

    //오답 리스트에서 특정 단어 삭제
    fun remove(word: Vocabulary) {
        wrongList.removeIf { it.vocaId == word.vocaId }
    }

}