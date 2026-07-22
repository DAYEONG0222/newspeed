package com.example.capstone.data.local.converter

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class DateConverter {
    // LocalDateTime 변환(년월일시분초)
    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toLocalDateTime(dateString: String?): LocalDateTime? {
        return dateString?.let {
            try {
                // 1. 기본 LocalDateTime 형식 시도 (2024-03-15T14:30:00)
                LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            } catch (e: DateTimeParseException) {
                try {
                    // 2. UTC 시간대 포함 형식 시도 (2025-06-04T06:27:00Z)
                    // Instant로 파싱 후 시스템 기본 시간대로 변환
                    val instant = Instant.parse(it)
                    LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                } catch (e2: DateTimeParseException) {
                    try {
                        // 3. OffsetDateTime 형식 시도 (2025-06-04T06:27:00+09:00)
                        val offsetDateTime = OffsetDateTime.parse(it)
                        offsetDateTime.toLocalDateTime()
                    } catch (e3: DateTimeParseException) {
                        // 4. 모든 파싱 실패 시 null 반환
                        null
                    }
                }
            }
        }
    }

    // LocalDate 변환(년월일)
    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let {
            LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
        }
    }

    // Instant 변환 (Long 타임스탬프 사용)
    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromInstant(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toInstant(timestamp: Long?): Instant? {
        return timestamp?.let {
            Instant.ofEpochMilli(it)
        }
    }
}