package com.example.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun observeProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: UserProfile)
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons ORDER BY level ASC, chapterOrder ASC")
    fun observeAllLessons(): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE level = :level ORDER BY chapterOrder ASC")
    fun observeLessonsByLevel(level: String): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    suspend fun getLessonById(lessonId: String): Lesson?

    @Query("SELECT * FROM lesson_progress")
    fun observeAllProgress(): Flow<List<LessonProgress>>

    @Query("SELECT * FROM lesson_progress")
    suspend fun getAllProgressSnapshot(): List<LessonProgress>

    @Query("SELECT * FROM lesson_progress WHERE lessonId = :lessonId")
    suspend fun getProgressForLesson(lessonId: String): LessonProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<Lesson>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLessonsIgnore(lessons: List<Lesson>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: LessonProgress)

    @Query("DELETE FROM lesson_progress")
    suspend fun clearProgress()

    @Query("SELECT COUNT(*) FROM lessons")
    suspend fun countLessons(): Int
}

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY level ASC, difficulty ASC")
    fun observeAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE lessonId = :lessonId")
    suspend fun getExercisesByLesson(lessonId: String): List<Exercise>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: String): Exercise?

    @Query("SELECT * FROM exercise_attempts ORDER BY attemptedAt DESC")
    fun observeAttempts(): Flow<List<ExerciseAttempt>>

    @Query("SELECT * FROM exercise_attempts ORDER BY attemptedAt DESC")
    suspend fun getAllAttemptsSnapshot(): List<ExerciseAttempt>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<Exercise>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercisesIgnore(exercises: List<Exercise>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: ExerciseAttempt)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempts(attempts: List<ExerciseAttempt>)

    @Query("DELETE FROM exercise_attempts")
    suspend fun clearAttempts()

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun countExercises(): Int
}

@Dao
interface DictionaryDao {
    @Query("SELECT * FROM dictionary_entries ORDER BY word ASC")
    fun observeAllEntries(): Flow<List<DictionaryEntry>>

    @Query(
        """
        SELECT * FROM dictionary_entries
        WHERE (:category IS NULL OR partOfSpeech = :category)
          AND (
              normalizedWord LIKE :query || '%'
              OR normalizedWord LIKE '%' || :query || '%'
              OR word LIKE '%' || :rawQuery || '%'
              OR definition LIKE '%' || :rawQuery || '%'
              OR synonymsPipe LIKE '%' || :rawQuery || '%'
              OR antonymsPipe LIKE '%' || :rawQuery || '%'
          )
        ORDER BY
            CASE
                WHEN normalizedWord = :query THEN 0
                WHEN normalizedWord LIKE :query || '%' THEN 1
                WHEN synonymsPipe LIKE '%' || :rawQuery || '%' THEN 2
                WHEN antonymsPipe LIKE '%' || :rawQuery || '%' THEN 3
                ELSE 4
            END,
            word ASC
        LIMIT 100
        """
    )
    fun searchEntries(query: String, rawQuery: String, category: String? = null): Flow<List<DictionaryEntry>>

    @Query(
        """
        SELECT * FROM dictionary_entries
        WHERE (:category IS NULL OR partOfSpeech = :category)
          AND (
              :rawQuery = ''
              OR definition LIKE '%' || :rawQuery || '%'
              OR normalizedWord LIKE '%' || :query || '%'
          )
        ORDER BY word ASC
        LIMIT 100
        """
    )
    fun searchByDefinition(query: String, rawQuery: String, category: String? = null): Flow<List<DictionaryEntry>>

    @Query(
        """
        SELECT * FROM dictionary_entries
        WHERE (:category IS NULL OR partOfSpeech = :category)
          AND synonymsPipe != ''
          AND (
              :rawQuery = ''
              OR synonymsPipe LIKE '%' || :rawQuery || '%'
              OR normalizedWord LIKE '%' || :query || '%'
          )
        ORDER BY
            CASE WHEN synonymsPipe LIKE '%' || :rawQuery || '%' THEN 0 ELSE 1 END,
            word ASC
        LIMIT 100
        """
    )
    fun searchBySynonym(query: String, rawQuery: String, category: String? = null): Flow<List<DictionaryEntry>>

    @Query(
        """
        SELECT * FROM dictionary_entries
        WHERE (:category IS NULL OR partOfSpeech = :category)
          AND antonymsPipe != ''
          AND (
              :rawQuery = ''
              OR antonymsPipe LIKE '%' || :rawQuery || '%'
              OR normalizedWord LIKE '%' || :query || '%'
          )
        ORDER BY
            CASE WHEN antonymsPipe LIKE '%' || :rawQuery || '%' THEN 0 ELSE 1 END,
            word ASC
        LIMIT 100
        """
    )
    fun searchByAntonym(query: String, rawQuery: String, category: String? = null): Flow<List<DictionaryEntry>>

    @Query("SELECT * FROM dictionary_entries WHERE partOfSpeech = :category ORDER BY word ASC")
    fun observeByCategory(category: String): Flow<List<DictionaryEntry>>

    @Query("SELECT DISTINCT partOfSpeech FROM dictionary_entries ORDER BY partOfSpeech ASC")
    fun observeCategories(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM dictionary_entries")
    fun observeTotalCount(): Flow<Int>

    @Query("SELECT * FROM dictionary_entries WHERE lastViewedAt > 0 ORDER BY lastViewedAt DESC LIMIT 20")
    fun observeRecentEntries(): Flow<List<DictionaryEntry>>

    @Query("SELECT * FROM dictionary_entries WHERE id = :id")
    fun observeById(id: String): Flow<DictionaryEntry?>

    @Query("SELECT * FROM dictionary_entries WHERE id = :id")
    suspend fun getById(id: String): DictionaryEntry?

    @Query("SELECT * FROM dictionary_entries WHERE normalizedWord = :normalizedWord LIMIT 1")
    suspend fun findByNormalizedWord(normalizedWord: String): DictionaryEntry?

    @Query("SELECT * FROM dictionary_entries")
    suspend fun getAllSnapshot(): List<DictionaryEntry>

    @Query("UPDATE dictionary_entries SET lastViewedAt = :timestamp WHERE id = :id")
    suspend fun markViewed(id: String, timestamp: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DictionaryEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<DictionaryEntry>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEntriesIgnore(entries: List<DictionaryEntry>)

    @Query("DELETE FROM dictionary_entries WHERE id = :id")
    suspend fun deleteEntryById(id: String)

    @Query("SELECT COUNT(*) FROM dictionary_entries")
    suspend fun countEntries(): Int

    // --- Historique des 10 dernières recherches du dictionnaire ---
    @Query("SELECT * FROM recent_dictionary_searches ORDER BY searchedAt DESC LIMIT :limit")
    fun observeRecentSearches(limit: Int = 10): Flow<List<RecentDictionarySearch>>

    @Query("SELECT * FROM recent_dictionary_searches ORDER BY searchedAt DESC")
    suspend fun getRecentSearchesSnapshot(): List<RecentDictionarySearch>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecentSearch(search: RecentDictionarySearch)

    @Query(
        """
        DELETE FROM recent_dictionary_searches
        WHERE normalizedQuery NOT IN (
            SELECT normalizedQuery FROM recent_dictionary_searches
            ORDER BY searchedAt DESC
            LIMIT :keepCount
        )
        """
    )
    suspend fun trimRecentSearches(keepCount: Int = 10)

    @Query("DELETE FROM recent_dictionary_searches WHERE normalizedQuery = :normalizedQuery")
    suspend fun deleteRecentSearch(normalizedQuery: String)

    @Query("DELETE FROM recent_dictionary_searches")
    suspend fun clearRecentSearches()
}

@Dao
interface ConjugationDao {
    @Query("SELECT * FROM verbs ORDER BY infinitive ASC")
    fun observeAllVerbs(): Flow<List<Verb>>

    @Query("SELECT * FROM verbs WHERE infinitive = :infinitive")
    suspend fun getVerb(infinitive: String): Verb?

    @Query("SELECT * FROM conjugations WHERE verbInfinitive = :infinitive ORDER BY mood, tense, personIndex")
    suspend fun getConjugationsForVerb(infinitive: String): List<Conjugation>

    @Query("SELECT * FROM conjugations WHERE normalizedForm = :normalizedForm LIMIT 12")
    suspend fun findByConjugatedForm(normalizedForm: String): List<Conjugation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerbs(verbs: List<Verb>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConjugations(conjugations: List<Conjugation>)

    @Query("SELECT COUNT(*) FROM verbs")
    suspend fun countVerbs(): Int
}

@Dao
interface CorrectionDao {
    @Query("SELECT * FROM correction_history ORDER BY createdAt DESC LIMIT 100")
    fun observeHistory(): Flow<List<CorrectionHistory>>

    @Query("SELECT * FROM correction_history ORDER BY createdAt DESC")
    suspend fun getHistorySnapshot(): List<CorrectionHistory>

    @Query("SELECT * FROM correction_errors WHERE historyId = :historyId")
    suspend fun getErrorsForHistory(historyId: Long): List<CorrectionError>

    @Query("SELECT * FROM correction_errors")
    suspend fun getAllErrorsSnapshot(): List<CorrectionError>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: CorrectionHistory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertErrors(errors: List<CorrectionError>)

    @Query("DELETE FROM correction_history WHERE id = :id")
    suspend fun deleteHistory(id: Long)

    @Query("DELETE FROM correction_history")
    suspend fun clearHistory()
}

@Dao
interface VocabularyDao {
    @Query("SELECT * FROM vocabulary_words ORDER BY level ASC, word ASC")
    fun observeAllWords(): Flow<List<VocabularyWord>>

    @Query("SELECT * FROM vocabulary_progress")
    fun observeAllProgress(): Flow<List<VocabularyProgress>>

    @Query("SELECT * FROM vocabulary_progress")
    suspend fun getAllProgressSnapshot(): List<VocabularyProgress>

    @Query("SELECT * FROM vocabulary_words WHERE id = :wordId")
    suspend fun getWordById(wordId: String): VocabularyWord?

    @Query("SELECT * FROM vocabulary_progress WHERE wordId = :wordId")
    suspend fun getProgress(wordId: String): VocabularyProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<VocabularyWord>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: VocabularyProgress)

    @Query("DELETE FROM vocabulary_progress")
    suspend fun clearProgress()

    @Query("SELECT COUNT(*) FROM vocabulary_words")
    suspend fun countWords(): Int
}

@Dao
interface DictationDao {
    @Query("SELECT * FROM dictations ORDER BY level ASC, title ASC")
    fun observeAllDictations(): Flow<List<Dictation>>

    @Query("SELECT * FROM dictations WHERE id = :id")
    suspend fun getById(id: String): Dictation?

    @Query("SELECT * FROM dictation_attempts ORDER BY attemptedAt DESC")
    fun observeAttempts(): Flow<List<DictationAttempt>>

    @Query("SELECT * FROM dictation_attempts ORDER BY attemptedAt DESC")
    suspend fun getAttemptsSnapshot(): List<DictationAttempt>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDictations(dictations: List<Dictation>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: DictationAttempt): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempts(attempts: List<DictationAttempt>)

    @Query("DELETE FROM dictation_attempts")
    suspend fun clearAttempts()

    @Query("SELECT COUNT(*) FROM dictations")
    suspend fun countDictations(): Int
}

@Dao
interface StatsAndMetaDao {
    @Query("SELECT * FROM daily_goals ORDER BY epochDay DESC LIMIT 30")
    fun observeDailyGoals(): Flow<List<DailyGoal>>

    @Query("SELECT * FROM daily_goals WHERE epochDay = :epochDay")
    suspend fun getDailyGoal(epochDay: Long): DailyGoal?

    @Query("SELECT * FROM daily_goals")
    suspend fun getAllDailyGoalsSnapshot(): List<DailyGoal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyGoal(goal: DailyGoal)

    @Query("DELETE FROM daily_goals")
    suspend fun clearDailyGoals()

    @Query("SELECT * FROM achievements ORDER BY isUnlocked DESC, title ASC")
    fun observeAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements")
    suspend fun getAchievementsSnapshot(): List<Achievement>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<Achievement>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievementsIgnore(achievements: List<Achievement>)

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    @Query("SELECT * FROM learning_statistics ORDER BY masteryPercent ASC")
    fun observeStatistics(): Flow<List<LearningStatistics>>

    @Query("SELECT * FROM learning_statistics")
    suspend fun getStatisticsSnapshot(): List<LearningStatistics>

    @Query("SELECT * FROM learning_statistics WHERE skillCategory = :category")
    suspend fun getStatisticByCategory(category: String): LearningStatistics?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStatistic(stat: LearningStatistics)

    @Query("DELETE FROM learning_statistics")
    suspend fun clearStatistics()

    @Query("SELECT * FROM favorites ORDER BY createdAt DESC")
    fun observeFavorites(): Flow<List<Favorite>>

    @Query("SELECT * FROM favorites")
    suspend fun getFavoritesSnapshot(): List<Favorite>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE itemType = :type AND itemId = :itemId)")
    fun observeIsFavorite(type: String, itemId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE itemType = :type AND itemId = :itemId")
    suspend fun deleteFavorite(type: String, itemId: String)

    @Query("DELETE FROM favorites")
    suspend fun clearFavorites()

    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun observeSettings(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettings(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: AppSettings)
}

@Dao
interface AcademyStreakDao {
    @Query("SELECT * FROM streak_state WHERE id = 1")
    fun observeStreakState(): Flow<StreakState?>

    @Query("SELECT * FROM streak_state WHERE id = 1")
    suspend fun getStreakState(): StreakState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStreakState(state: StreakState)

    @Query("SELECT * FROM academy_practice_days ORDER BY epochDay DESC LIMIT 30")
    fun observeRecentPracticeDays(): Flow<List<AcademyPracticeDay>>

    @Query("SELECT * FROM academy_practice_days ORDER BY epochDay DESC")
    suspend fun getAllPracticeDaysSnapshot(): List<AcademyPracticeDay>

    @Query("SELECT * FROM academy_practice_days WHERE epochDay = :epochDay")
    suspend fun getPracticeDay(epochDay: Long): AcademyPracticeDay?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPracticeDay(day: AcademyPracticeDay)

    @Query("DELETE FROM academy_practice_days")
    suspend fun clearPracticeDays()

    @Query("SELECT * FROM streak_rewards ORDER BY requiredStreakDays ASC")
    fun observeStreakRewards(): Flow<List<StreakReward>>

    @Query("SELECT * FROM streak_rewards ORDER BY requiredStreakDays ASC")
    suspend fun getStreakRewardsSnapshot(): List<StreakReward>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStreakRewardsIgnore(rewards: List<StreakReward>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreakRewards(rewards: List<StreakReward>)

    @Update
    suspend fun updateStreakReward(reward: StreakReward)
}
