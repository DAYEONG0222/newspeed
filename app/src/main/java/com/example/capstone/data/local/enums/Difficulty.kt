package com.example.capstone.data.local.enums

enum class Difficulty(
    val value : Int,
    val korName : String
) {
    BEGINNER(1,"입문"),         /**이거 초급 중급 고급만 해야할듯? */
    ELEMENTARY(2,"초급"),
    INTERMEDIATE(3,"중급"),
    ADVANCED(4, "고급"),
    EXPERT(5,"전문");

    companion object {
        // value를 입력받아 등급 반환
        fun fromValue(value: Int): Difficulty {
            return entries.find { it.value == value } ?: BEGINNER   // 비교 실패시 BEGINNER 반환
        }

        // 대소문자 구분없이 문자열 입력받아 등급 반환
        fun fromString(name: String): Difficulty {
            return try {
                valueOf(name.uppercase())   // 입력을 대문자로 변환(enum이 대문자로 작성되어있으므로)
            } catch (e: IllegalArgumentException) {
                // 일치하는 이름이 없으면 기본값으로 BEGINNER 반환
                BEGINNER
            }
        }

        // 한글 명칭으로 등급 반환
        fun fromKorName(korName: String): Difficulty {
            return entries.find { it.korName == korName } ?: BEGINNER   // 비교 실패시 BEGINNER 반환
        }
    }
}