package com.example.capstone.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.capstone.data.local.converter.DateConverter
import com.example.capstone.data.local.dao.CodeDao
import com.example.capstone.data.local.dao.VocabularyDao
import com.example.capstone.data.local.dao.WrongAnswerDao
import com.example.capstone.data.local.entity.Code
import com.example.capstone.data.local.entity.User
import com.example.capstone.data.local.entity.Vocabulary
import com.example.capstone.data.local.entity.WrongAnswer

@Database(
    entities = [
        User::class,
        Vocabulary::class,
        Code::class,
        WrongAnswer::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    // DAO 접근 메서드
//    abstract fun userDao(): UserDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun codeDao(): CodeDao
    abstract fun wrongAnswerDao(): WrongAnswerDao

    companion object {
        // 싱글톤 인스턴스
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // v1 → v2: 오답노트 테이블 추가. 단어장 데이터를 지우지 않도록 명시적 마이그레이션 사용
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `wrong_answer` (
                        `wrong_answer_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `word` TEXT NOT NULL, `meaning` TEXT NOT NULL, `language_code` TEXT NOT NULL,
                        `wrong_count` INTEGER NOT NULL, `last_wrong_at` INTEGER NOT NULL)"""
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_wrong_answer_word_language_code` " +
                        "ON `wrong_answer` (`word`, `language_code`)"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "capstone_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration() // 마이그레이션이 없는 버전 변경 시에만 데이터 삭제
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
