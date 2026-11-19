package com.example.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserProfile::class,
        Lesson::class,
        LessonProgress::class,
        Exercise::class,
        ExerciseAttempt::class,
        DictionaryEntry::class,
        RecentDictionarySearch::class,
        Verb::class,
        Conjugation::class,
        CorrectionHistory::class,
        CorrectionError::class,
        VocabularyWord::class,
        VocabularyProgress::class,
        Dictation::class,
        DictationAttempt::class,
        DailyGoal::class,
        Achievement::class,
        LearningStatistics::class,
        Favorite::class,
        AppSettings::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FrancaisProDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun lessonDao(): LessonDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun dictionaryDao(): DictionaryDao
    abstract fun conjugationDao(): ConjugationDao
    abstract fun correctionDao(): CorrectionDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun dictationDao(): DictationDao
    abstract fun statsAndMetaDao(): StatsAndMetaDao

    companion object {
        const val DATABASE_NAME = "francais_pro.db"

        /**
         * Migrations explicites pour garantir la préservation complète des données
         * locales lors des montées de schéma.
         */
        val MIGRATIONS: Array<Migration> = arrayOf(
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `recent_dictionary_searches` (
                            `normalizedQuery` TEXT NOT NULL,
                            `queryText` TEXT NOT NULL,
                            `searchedAt` INTEGER NOT NULL,
                            PRIMARY KEY(`normalizedQuery`)
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_recent_dictionary_searches_searchedAt` ON `recent_dictionary_searches` (`searchedAt`)"
                    )
                }
            }
        )

        @Volatile
        private var INSTANCE: FrancaisProDatabase? = null

        fun getInstance(context: Context): FrancaisProDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FrancaisProDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(*MIGRATIONS)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
