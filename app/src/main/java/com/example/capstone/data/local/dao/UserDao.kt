package com.example.capstone.data.local.dao

import androidx.room.*
import com.example.capstone.data.local.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // 앱 초기 설정 시 사용자 생성
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createUser(user: User): Long

    // 현재 사용자 정보 조회
    @Query("SELECT * FROM user LIMIT 1")
    suspend fun getCurrentUser(): User?

    // Flow로 사용자 정보 관찰
    @Query("SELECT * FROM user LIMIT 1")
    fun getCurrentUserFlow(): Flow<User?>

    // 사용자 존재 여부 확인 (기존 유지)
    @Query("SELECT EXISTS(SELECT 1 FROM user)")
    suspend fun hasUser(): Boolean

    // 사용자 정보 업데이트
    @Update
    suspend fun updateUser(user: User): Int
}