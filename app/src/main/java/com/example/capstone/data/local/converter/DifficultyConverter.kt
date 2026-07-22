package com.example.capstone.data.local.converter

import androidx.room.TypeConverter
import com.example.capstone.data.local.enums.Difficulty

class DifficultyConverter {

    // 객체를 DB에 저장
    // Grade 객체에서 Int 추출 후 반환
    @TypeConverter
    fun fromDifficulty(difficulty: Difficulty): Int {
        return difficulty.value
    }

    // DB에서 객체를 읽기
    // DB에서 Int값에 해당하는 객체 탐색 후 반환
    @TypeConverter
    fun toDifficulty(value: Int): Difficulty {
        return Difficulty.fromValue(value)
    }
}