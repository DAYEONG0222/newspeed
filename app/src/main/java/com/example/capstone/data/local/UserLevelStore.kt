package com.example.capstone.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.capstone.data.model.UserLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 언어별 사용자 레벨 (초급/중급/고급).
 * 기존에는 세 뉴스 ViewModel 모두 userLevel = "beginner" 로 고정돼 있어서
 * My Level 탭에는 항상 초급 기사만 나왔다. 레벨 테스트 결과나 직접 선택한 값을 여기 저장한다.
 */
object UserLevelStore {
    private const val PREFS = "user_level"
    private lateinit var prefs: SharedPreferences
    private val flows = mutableMapOf<String, MutableStateFlow<UserLevel>>()

    fun init(context: Context) {
        if (!::prefs.isInitialized) {
            prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        }
    }

    private fun read(lang: String): UserLevel {
        if (!::prefs.isInitialized) return UserLevel.BEGINNER
        val name = prefs.getString(lang, null) ?: return UserLevel.BEGINNER
        return runCatching { UserLevel.valueOf(name) }.getOrDefault(UserLevel.BEGINNER)
    }

    @Synchronized
    private fun flowOf(lang: String): MutableStateFlow<UserLevel> =
        flows.getOrPut(lang) { MutableStateFlow(read(lang)) }

    fun level(lang: String): StateFlow<UserLevel> = flowOf(lang).asStateFlow()

    fun set(lang: String, level: UserLevel) {
        if (::prefs.isInitialized) prefs.edit().putString(lang, level.name).apply()
        flowOf(lang).value = level
    }
}
