package com.example.capstone.data.local.dao

import androidx.room.*
import com.example.capstone.data.local.entity.UserLanguageGradeHistory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface UserLanguageGradeHistoryDao {

    // =============================================================================================
    // INSERT 작업

    @Insert
    suspend fun insertGradeHistory(history: UserLanguageGradeHistory): Long

    @Query("""
        INSERT INTO user_language_grade_history 
        (name, language_code, grade, accuracy_percentage, updated_time) 
        VALUES (:name, :langCode, :grade, :accuracyPercentage, :updatedDate)
    """)
    suspend fun insertGradeHistoryByParams(
        name: String,
        langCode: String,
        grade: String,
        accuracyPercentage: Double,
        updatedDate: LocalDateTime
    ): Long

    // =============================================================================================
    // READ 작업 (Native Query)

    @Query("""
        SELECT * FROM user_language_grade_history 
        WHERE name = :userName AND language_code = :langCode 
        ORDER BY updated_time DESC
    """)
    suspend fun getUserLanguageGradeHistory(userName: String, langCode: String): List<UserLanguageGradeHistory>

    @Query("""
        SELECT * FROM user_language_grade_history 
        WHERE name = :userName AND language_code = :langCode 
        ORDER BY updated_time DESC
    """)
    fun getUserLanguageGradeHistoryFlow(userName: String, langCode: String): Flow<List<UserLanguageGradeHistory>>

    @Query("""
        SELECT * FROM user_language_grade_history 
        WHERE name = :userName 
        ORDER BY updated_time DESC
    """)
    suspend fun getAllUserGradeHistory(userName: String): List<UserLanguageGradeHistory>

    @Query("""
        SELECT * FROM user_language_grade_history 
        WHERE name = :userName AND language_code = :langCode 
        ORDER BY updated_time DESC 
        LIMIT 1
    """)
    suspend fun getLatestUserLanguageGrade(userName: String, langCode: String): UserLanguageGradeHistory?

    @Query("""
        SELECT * FROM user_language_grade_history 
        WHERE name = :userName AND language_code = :langCode 
        ORDER BY updated_time DESC 
        LIMIT :count
    """)
    suspend fun getRecentTestResults(userName: String, langCode: String, count: Int): List<UserLanguageGradeHistory>

    // =============================================================================================
    // 10문제 3등급 기준 통계 쿼리

    @Query("""
        SELECT AVG(accuracy_percentage) 
        FROM user_language_grade_history 
        WHERE name = :userName AND language_code = :langCode
    """)
    suspend fun getAverageAccuracyByLanguage(userName: String, langCode: String): Double?

    @Query("""
        SELECT MAX(accuracy_percentage) 
        FROM user_language_grade_history 
        WHERE name = :userName AND language_code = :langCode
    """)
    suspend fun getHighestAccuracyByLanguage(userName: String, langCode: String): Double?

    @Query("""
        SELECT COUNT(*) 
        FROM user_language_grade_history 
        WHERE name = :userName AND language_code = :langCode
    """)
    suspend fun getTotalTestCountByLanguage(userName: String, langCode: String): Int

    // 10문제 기준 맞힌 개수별 통계
    @Query("""
        SELECT 
            ROUND(accuracy_percentage / 10) * 10 as scoreRange,
            COUNT(*) as count
        FROM user_language_grade_history 
        WHERE name = :userName AND language_code = :langCode 
        GROUP BY ROUND(accuracy_percentage / 10) * 10
        ORDER BY scoreRange
    """)
    suspend fun getScoreRangeDistribution(userName: String, langCode: String): List<ScoreRangeCount>

    // 3등급 기준 테스트 횟수
    @Query("""
        SELECT 
            '초급' as grade,
            COUNT(*) as count,
            AVG(accuracy_percentage) as avgAccuracy
        FROM user_language_grade_history 
        WHERE name = :userName AND language_code = :langCode 
        AND accuracy_percentage < 60.0
        
        UNION ALL
        
        SELECT 
            '중급' as grade,
            COUNT(*) as count,
            AVG(accuracy_percentage) as avgAccuracy
        FROM user_language_grade_history 
        WHERE name = :userName AND language_code = :langCode 
        AND accuracy_percentage >= 60.0 AND accuracy_percentage < 80.0
        
        UNION ALL
        
        SELECT 
            '고급' as grade,
            COUNT(*) as count,
            AVG(accuracy_percentage) as avgAccuracy
        FROM user_language_grade_history 
        WHERE name = :userName AND language_code = :langCode 
        AND accuracy_percentage >= 80.0
        
        ORDER BY 
            CASE grade 
                WHEN '초급' THEN 1 
                WHEN '중급' THEN 2 
                WHEN '고급' THEN 3 
            END
    """)
    suspend fun getGradeDistributionWithAccuracy(userName: String, langCode: String): List<GradeDistributionWithAccuracy>

    // 최근 N회 평균 정확도 (개선도 측정용)
    @Query("""
        SELECT AVG(accuracy_percentage) 
        FROM (
            SELECT accuracy_percentage 
            FROM user_language_grade_history 
            WHERE name = :userName AND language_code = :langCode 
            ORDER BY updated_time DESC 
            LIMIT :recentCount
        )
    """)
    suspend fun getRecentAverageAccuracy(userName: String, langCode: String, recentCount: Int): Double?

    // 월별 학습 진도 (10문제 기준)
    @Query("""
        SELECT 
            strftime('%Y-%m', updated_time) as month,
            COUNT(*) as testCount,
            AVG(accuracy_percentage) as avgAccuracy,
            MAX(accuracy_percentage) as maxAccuracy,
            AVG(ROUND(accuracy_percentage / 10)) as avgCorrectAnswers,
            SUM(CASE WHEN accuracy_percentage >= 80.0 THEN 1 ELSE 0 END) as advancedTests,
            SUM(CASE WHEN accuracy_percentage >= 60.0 AND accuracy_percentage < 80.0 THEN 1 ELSE 0 END) as intermediateTests,
            SUM(CASE WHEN accuracy_percentage < 60.0 THEN 1 ELSE 0 END) as beginnerTests
        FROM user_language_grade_history 
        WHERE name = :userName AND language_code = :langCode 
        AND updated_time >= datetime('now', '-12 months')
        GROUP BY strftime('%Y-%m', updated_time)
        ORDER BY month DESC
    """)
    suspend fun getMonthlyLearningProgress(userName: String, langCode: String): List<MonthlyLearningProgress>

    // 학습 진행도 분석 (이전 테스트와 비교)
    @Query("""
        SELECT 
            *,
            LAG(accuracy_percentage) OVER (ORDER BY updated_time) as previousAccuracy,
            LAG(grade) OVER (ORDER BY updated_time) as previousGrade,
            ROUND(accuracy_percentage / 10) as correctAnswers,
            CASE 
                WHEN LAG(accuracy_percentage) OVER (ORDER BY updated_time) IS NULL THEN 'FIRST_TEST'
                WHEN accuracy_percentage > LAG(accuracy_percentage) OVER (ORDER BY updated_time) THEN 'IMPROVED'
                WHEN accuracy_percentage < LAG(accuracy_percentage) OVER (ORDER BY updated_time) THEN 'DECLINED'
                ELSE 'SAME'
            END as progressStatus
        FROM user_language_grade_history 
        WHERE name = :userName AND language_code = :langCode 
        ORDER BY updated_time DESC
        LIMIT :limit
    """)
    suspend fun getLearningProgressAnalysis(userName: String, langCode: String, limit: Int): List<LearningProgressData>

    // 종합 통계 (10문제 3등급 기준)
    @Query("""
        SELECT 
            COUNT(*) as totalTests,
            AVG(accuracy_percentage) as overallAverage,
            MAX(accuracy_percentage) as bestScore,
            ROUND(AVG(accuracy_percentage) / 10) as avgCorrectAnswers,
            COUNT(CASE WHEN accuracy_percentage >= 80.0 THEN 1 END) as advancedCount,
            COUNT(CASE WHEN accuracy_percentage >= 60.0 AND accuracy_percentage < 80.0 THEN 1 END) as intermediateCount,
            COUNT(CASE WHEN accuracy_percentage < 60.0 THEN 1 END) as beginnerCount,
            ROUND(COUNT(CASE WHEN accuracy_percentage >= 80.0 THEN 1 END) * 100.0 / COUNT(*), 1) as advancedPercentage,
            ROUND(COUNT(CASE WHEN accuracy_percentage >= 60.0 AND accuracy_percentage < 80.0 THEN 1 END) * 100.0 / COUNT(*), 1) as intermediatePercentage,
            ROUND(COUNT(CASE WHEN accuracy_percentage < 60.0 THEN 1 END) * 100.0 / COUNT(*), 1) as beginnerPercentage
        FROM user_language_grade_history 
        WHERE name = :userName AND language_code = :langCode
    """)
    suspend fun getUserOverallStats(userName: String, langCode: String): UserOverallStats?

    // =============================================================================================
    // DELETE 작업 (Native Query)

    @Query("""
        DELETE FROM user_language_grade_history 
        WHERE grade_history_id = :id
    """)
    suspend fun deleteGradeHistoryById(id: Long): Int

    @Query("""
        DELETE FROM user_language_grade_history 
        WHERE name = :userName AND language_code = :langCode
    """)
    suspend fun deleteUserLanguageGradeHistory(userName: String, langCode: String): Int

    @Query("""
        DELETE FROM user_language_grade_history 
        WHERE name = :userName
    """)
    suspend fun deleteAllUserGradeHistory(userName: String): Int
}

// =============================================================================================
// 10문제 3등급 기준 데이터 클래스들

data class ScoreRangeCount(
    val scoreRange: Double,     // 0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100
    val count: Int
)

data class GradeDistributionWithAccuracy(
    val grade: String,          // '초급', '중급', '고급'
    val count: Int,
    val avgAccuracy: Double
)

data class MonthlyLearningProgress(
    val month: String,          // '2025-01'
    val testCount: Int,
    val avgAccuracy: Double,
    val maxAccuracy: Double,
    val avgCorrectAnswers: Double,  // 10문제 기준 평균 맞힌 개수
    val advancedTests: Int,     // 고급 등급 테스트 수 (8-10개)
    val intermediateTests: Int, // 중급 등급 테스트 수 (6-7개)
    val beginnerTests: Int      // 초급 등급 테스트 수 (0-5개)
)

data class LearningProgressData(
    val gradeHistoryId: Long,
    val name: String,
    val langCode: String,
    val grade: String,
    val accuracyPercentage: Double,
    val updatedDate: LocalDateTime,
    val previousAccuracy: Double?,
    val previousGrade: String?,
    val correctAnswers: Int,        // 10문제 기준 맞힌 개수
    val progressStatus: String      // 'IMPROVED', 'DECLINED', 'SAME', 'FIRST_TEST'
)

data class UserOverallStats(
    val totalTests: Int,
    val overallAverage: Double,
    val bestScore: Double,
    val avgCorrectAnswers: Int,     // 10문제 기준 평균 맞힌 개수
    val advancedCount: Int,         // 고급 등급 횟수 (8-10개)
    val intermediateCount: Int,     // 중급 등급 횟수 (6-7개)
    val beginnerCount: Int,         // 초급 등급 횟수 (0-5개)
    val advancedPercentage: Double, // 고급 등급 비율
    val intermediatePercentage: Double, // 중급 등급 비율
    val beginnerPercentage: Double  // 초급 등급 비율
)