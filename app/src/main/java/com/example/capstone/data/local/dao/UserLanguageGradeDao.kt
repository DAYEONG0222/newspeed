package com.example.capstone.data.local.dao

import androidx.room.*
import com.example.capstone.data.local.entity.UserLanguageGrade
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface UserLanguageGradeDao {

    // =============================================================================================
    // INSERT/UPDATE 작업

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGrade(grade: UserLanguageGrade): Long

    @Query("""
        INSERT OR REPLACE INTO user_language_grade 
        (name, language_code, grade, updated_time) 
        VALUES (:name, :langCode, :grade, :updatedTime)
    """)
    suspend fun insertOrUpdateGradeByParams(
        name: String,
        langCode: String,
        grade: String,
        updatedTime: LocalDateTime
    ): Long

    // =============================================================================================
    // READ 작업 (Native Query)

    @Query("""
        SELECT * FROM user_language_grade 
        WHERE name = :userName 
        ORDER BY updated_time DESC
    """)
    suspend fun getUserLanguageGrades(userName: String): List<UserLanguageGrade>

    @Query("""
        SELECT * FROM user_language_grade 
        WHERE name = :userName 
        ORDER BY updated_time DESC
    """)
    fun getUserLanguageGradesFlow(userName: String): Flow<List<UserLanguageGrade>>

    @Query("""
        SELECT * FROM user_language_grade 
        WHERE name = :userName AND language_code = :langCode
    """)
    suspend fun getUserLanguageGrade(userName: String, langCode: String): UserLanguageGrade?

    @Query("""
        SELECT * FROM user_language_grade 
        WHERE name = :userName AND language_code = :langCode
    """)
    fun getUserLanguageGradeFlow(userName: String, langCode: String): Flow<UserLanguageGrade?>

    @Query("""
        SELECT * FROM user_language_grade 
        ORDER BY name, language_code
    """)
    suspend fun getAllUserLanguageGrades(): List<UserLanguageGrade>

    @Query("""
        SELECT * FROM user_language_grade 
        WHERE language_code = :langCode 
        ORDER BY updated_time DESC
    """)
    suspend fun getGradesByLanguage(langCode: String): List<UserLanguageGrade>

    @Query("""
        SELECT * FROM user_language_grade 
        WHERE grade = :grade 
        ORDER BY updated_time DESC
    """)
    suspend fun getUsersByGrade(grade: String): List<UserLanguageGrade>

    // =============================================================================================
    // 10문제 3등급 기준 통계 쿼리

    @Query("""
        SELECT 
            '초급' as grade, 
            COUNT(*) as count 
        FROM user_language_grade 
        WHERE language_code = :langCode AND grade = '초급'
        
        UNION ALL
        
        SELECT 
            '중급' as grade, 
            COUNT(*) as count 
        FROM user_language_grade 
        WHERE language_code = :langCode AND grade = '중급'
        
        UNION ALL
        
        SELECT 
            '고급' as grade, 
            COUNT(*) as count 
        FROM user_language_grade 
        WHERE language_code = :langCode AND grade = '고급'
        
        ORDER BY 
            CASE grade 
                WHEN '초급' THEN 1 
                WHEN '중급' THEN 2 
                WHEN '고급' THEN 3 
            END
    """)
    suspend fun getGradeDistributionByLanguage(langCode: String): List<GradeCount>

    @Query("""
        SELECT language_code, COUNT(*) as count 
        FROM user_language_grade 
        GROUP BY language_code 
        ORDER BY count DESC
    """)
    suspend fun getLanguageDistribution(): List<LanguageCount>

    @Query("""
        SELECT COUNT(*) FROM user_language_grade 
        WHERE name = :userName
    """)
    suspend fun getUserLanguageCount(userName: String): Int

    @Query("""
        SELECT COUNT(*) FROM user_language_grade 
        WHERE language_code = :langCode
    """)
    suspend fun getLanguageUserCount(langCode: String): Int

    @Query("""
        SELECT COUNT(*) FROM user_language_grade 
        WHERE grade = :grade
    """)
    suspend fun getGradeUserCount(grade: String): Int

    // 초급자 수 (0-50%)
    @Query("""
        SELECT COUNT(*) FROM user_language_grade 
        WHERE grade = '초급'
    """)
    suspend fun getBeginnerCount(): Int

    // 중급자 수 (60-70%)
    @Query("""
        SELECT COUNT(*) FROM user_language_grade 
        WHERE grade = '중급'
    """)
    suspend fun getIntermediateCount(): Int

    // 고급자 수 (80-100%)
    @Query("""
        SELECT COUNT(*) FROM user_language_grade 
        WHERE grade = '고급'
    """)
    suspend fun getAdvancedCount(): Int

    // 언어별 등급 분포 퍼센티지
    @Query("""
        SELECT 
            grade,
            COUNT(*) as count,
            ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM user_language_grade WHERE language_code = :langCode), 2) as percentage
        FROM user_language_grade 
        WHERE language_code = :langCode 
        GROUP BY grade
        ORDER BY 
            CASE grade 
                WHEN '초급' THEN 1 
                WHEN '중급' THEN 2 
                WHEN '고급' THEN 3 
            END
    """)
    suspend fun getGradeDistributionWithPercentage(langCode: String): List<GradeCountWithPercentage>

    // =============================================================================================
    // DELETE 작업 (Native Query)

    @Query("""
        DELETE FROM user_language_grade 
        WHERE name = :userName AND language_code = :langCode
    """)
    suspend fun deleteUserLanguageGrade(userName: String, langCode: String): Int

    @Query("""
        DELETE FROM user_language_grade 
        WHERE name = :userName
    """)
    suspend fun deleteAllUserLanguageGrades(userName: String): Int

    @Query("""
        DELETE FROM user_language_grade 
        WHERE language_code = :langCode
    """)
    suspend fun deleteAllGradesByLanguage(langCode: String): Int

    @Query("""
        DELETE FROM user_language_grade 
        WHERE updated_time < :beforeDate
    """)
    suspend fun deleteOldGrades(beforeDate: LocalDateTime): Int
}

// =============================================================================================
// 10문제 3등급 기준 데이터 클래스들

data class GradeCount(
    val grade: String,      // '초급', '중급', '고급'
    val count: Int
)

data class GradeCountWithPercentage(
    val grade: String,      // '초급', '중급', '고급'
    val count: Int,
    val percentage: Double  // 해당 언어에서의 비율
)

data class LanguageCount(
    val languageCode: String,   // 'es', 'ja', 'en'
    val count: Int
)