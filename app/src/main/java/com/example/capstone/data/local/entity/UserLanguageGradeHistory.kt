package com.example.capstone.data.local.entity

import androidx.room.*
import java.time.LocalDateTime

@Entity(
    tableName = "user_language_grade_history",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["name"],
            childColumns = ["name"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Code::class,
            parentColumns = ["code"],
            childColumns = ["language_code"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["name", "language_code", "updated_time"]),  // 복합 인덱스로 대부분 커버
        Index(value = ["updated_time"])  // 전체 이력 조회용
    ]
)
data class UserLanguageGradeHistory(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "grade_history_id")
    val gradeHistoryId : Long =0,

    @ColumnInfo(name = "name")
    val name : String,
    @ColumnInfo(name = "language_code")
    val langCode : String,
    @ColumnInfo(name = "grade")
    val grade : String,
    @ColumnInfo(name = "updated_time")
    val updatedTime : LocalDateTime,
    @ColumnInfo(name = "accuracy_percentage")
    val accuracyPercentage : Double
)
