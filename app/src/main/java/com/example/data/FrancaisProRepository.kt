package com.example.data

import androidx.room.withTransaction
import com.example.core.backup.BackupManager
import com.example.core.backup.BackupResult
import com.example.core.database.Achievement
import com.example.core.database.AppSettings
import com.example.core.database.CorrectionError
import com.example.core.database.CorrectionHistory
import com.example.core.database.DailyGoal
import com.example.core.database.Dictation
import com.example.core.database.DictationAttempt
import com.example.core.database.DictionaryEntry
import com.example.core.database.Exercise
import com.example.core.database.ExerciseAttempt
import com.example.core.database.Favorite
import com.example.core.database.FrancaisProDatabase
import com.example.core.database.LearningStatistics
import com.example.core.database.Lesson
import com.example.core.database.LessonProgress
import com.example.core.database.UserProfile
import com.example.core.database.Verb
import com.example.core.database.VocabularyProgress
import com.example.core.database.VocabularyWord
import com.example.core.datastore.LocalUserPreferences
import com.example.core.datastore.PreferencesRepository
import com.example.learning.dictation.DictationDiffEngine
import com.example.learning.dictation.DictationEvaluation
import com.example.learning.spacedrepetition.Sm2Scheduler
import com.example.linguistics.analysis.HybridCorrectionEngine
import com.example.linguistics.conjugation.FrenchConjugatorEngine
import com.example.linguistics.conjugation.IdentifiedVerbForm
import com.example.linguistics.conjugation.VerbConjugationTable
import com.example.linguistics.model.CorrectionIssue
import com.example.linguistics.model.CorrectionReport
import com.example.linguistics.model.FrenchNormalizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FrancaisProRepository(
    private val db: FrancaisProDatabase,
    private val prefs: PreferencesRepository,
    val correctionEngine: HybridCorrectionEngine,
    val conjugatorEngine: FrenchConjugatorEngine,
    private val backupManager: BackupManager
) {
    // Flux réactifs Room & DataStore
    val userProfile: Flow<UserProfile?> = db.userProfileDao().observeProfile()
    val preferences: Flow<LocalUserPreferences> = prefs.preferencesFlow
    val allLessons: Flow<List<Lesson>> = db.lessonDao().observeAllLessons()
    val allLessonProgress: Flow<List<LessonProgress>> = db.lessonDao().observeAllProgress()
    val allExercises: Flow<List<Exercise>> = db.exerciseDao().observeAllExercises()
    val exerciseAttempts: Flow<List<ExerciseAttempt>> = db.exerciseDao().observeAttempts()
    val allDictionaryEntries: Flow<List<DictionaryEntry>> = db.dictionaryDao().observeAllEntries()
    val recentDictionaryEntries: Flow<List<DictionaryEntry>> = db.dictionaryDao().observeRecentEntries()
    val dictionaryCategories: Flow<List<String>> = db.dictionaryDao().observeCategories()
    val dictionaryTotalCount: Flow<Int> = db.dictionaryDao().observeTotalCount()
    val allVerbs: Flow<List<Verb>> = db.conjugationDao().observeAllVerbs()
    val correctionHistory: Flow<List<CorrectionHistory>> = db.correctionDao().observeHistory()
    val vocabularyWords: Flow<List<VocabularyWord>> = db.vocabularyDao().observeAllWords()
    val vocabularyProgress: Flow<List<VocabularyProgress>> = db.vocabularyDao().observeAllProgress()
    val allDictations: Flow<List<Dictation>> = db.dictationDao().observeAllDictations()
    val dictationAttempts: Flow<List<DictationAttempt>> = db.dictationDao().observeAttempts()
    val dailyGoals: Flow<List<DailyGoal>> = db.statsAndMetaDao().observeDailyGoals()
    val achievements: Flow<List<Achievement>> = db.statsAndMetaDao().observeAchievements()
    val statistics: Flow<List<LearningStatistics>> = db.statsAndMetaDao().observeStatistics()
    val favorites: Flow<List<Favorite>> = db.statsAndMetaDao().observeFavorites()
    val appSettings: Flow<AppSettings?> = db.statsAndMetaDao().observeSettings()

    private fun currentEpochDay(): Long = System.currentTimeMillis() / 86_400_000L

    // --- Profil, Onboarding & Évaluation ---
    suspend fun completeOnboarding(displayName: String = "Apprenant") {
        val current = db.userProfileDao().getProfile() ?: UserProfile()
        db.userProfileDao().upsert(
            current.copy(
                displayName = displayName.ifBlank { "Apprenant" },
                onboardingCompleted = true
            )
        )
    }

    suspend fun completeInitialAssessment(estimatedLevel: String, scorePercent: Int) {
        val current = db.userProfileDao().getProfile() ?: UserProfile()
        db.userProfileDao().upsert(
            current.copy(
                estimatedLevel = estimatedLevel,
                globalScore = maxOf(current.globalScore, scorePercent),
                onboardingCompleted = true,
                initialAssessmentCompleted = true
            )
        )
        recordActivity(minutes = 3)
    }

    suspend fun updateDisplayName(name: String) {
        val current = db.userProfileDao().getProfile() ?: UserProfile()
        db.userProfileDao().upsert(current.copy(displayName = name.trim().ifBlank { "Apprenant" }))
    }

    // --- Correcteur hors ligne ---
    suspend fun analyzeText(text: String): CorrectionReport = withContext(Dispatchers.Default) {
        correctionEngine.analyze(text)
    }

    suspend fun saveCorrectionSession(
        originalText: String,
        correctedText: String,
        issues: List<CorrectionIssue>,
        appliedCount: Int,
        sourceModule: String = "CORRECTEUR"
    ): Long {
        val historyId = db.withTransaction {
            val id = db.correctionDao().insertHistory(
                CorrectionHistory(
                    originalText = originalText,
                    correctedText = correctedText,
                    errorsFoundCount = issues.size,
                    errorsAppliedCount = appliedCount,
                    sourceModule = sourceModule
                )
            )
            if (issues.isNotEmpty()) {
                val entities = issues.map { issue ->
                    CorrectionError(
                        historyId = id,
                        originalFragment = issue.originalFragment,
                        suggestion = issue.suggestion,
                        category = issue.category.labelFr,
                        explanation = issue.explanation,
                        example = issue.example,
                        confidence = issue.confidence,
                        startIndex = issue.startIndex,
                        endIndex = issue.endIndex,
                        wasAccepted = appliedCount > 0
                    )
                }
                db.correctionDao().insertErrors(entities)
            }
            id
        }

        // Mettre à jour statistiques par catégorie d'erreur pour l'apprentissage adaptatif
        val profile = db.userProfileDao().getProfile() ?: UserProfile()
        db.userProfileDao().upsert(
            profile.copy(totalCorrectionsCount = profile.totalCorrectionsCount + appliedCount.coerceAtLeast(1))
        )
        issues.groupBy { it.category.labelFr }.forEach { (cat, list) ->
            updateSkillStat(category = cat, isSuccess = false, weight = list.size.coerceAtMost(3))
        }
        if (issues.isEmpty() && originalText.length > 20) {
            updateSkillStat(category = "Orthographe", isSuccess = true)
            updateSkillStat(category = "Grammaire", isSuccess = true)
        }
        recordActivity(minutes = 2, correctionsDelta = 1)
        unlockAchievement("ach_first_correction")
        return historyId
    }

    suspend fun deleteCorrectionHistory(id: Long) {
        db.correctionDao().deleteHistory(id)
    }

    suspend fun getCorrectionErrorsForHistory(historyId: Long): List<CorrectionError> {
        return db.correctionDao().getErrorsForHistory(historyId)
    }

    // --- Académie & Leçons ---
    suspend fun getLessonById(id: String): Lesson? = db.lessonDao().getLessonById(id)

    suspend fun getExercisesForLesson(lessonId: String): List<Exercise> =
        db.exerciseDao().getExercisesByLesson(lessonId)

    suspend fun completeLesson(lessonId: String, quizScorePercent: Int) {
        val existing = db.lessonDao().getProgressForLesson(lessonId)
        db.lessonDao().upsertProgress(
            LessonProgress(
                id = existing?.id ?: 0L,
                lessonId = lessonId,
                isCompleted = true,
                quizScorePercent = maxOf(existing?.quizScorePercent ?: 0, quizScorePercent),
                completedAt = System.currentTimeMillis(),
                reviewCount = (existing?.reviewCount ?: 0) + 1
            )
        )
        val lesson = db.lessonDao().getLessonById(lessonId)
        if (lesson != null) {
            updateSkillStat(lesson.theme, isSuccess = quizScorePercent >= 60)
        }
        recordActivity(minutes = lesson?.estimatedMinutes ?: 5)
        unlockAchievement("ach_first_lesson")
    }

    // --- Exercices ---
    suspend fun submitExerciseAttempt(exercise: Exercise, userAnswer: String, isCorrect: Boolean) {
        val points = if (isCorrect) exercise.points else 0
        db.exerciseDao().insertAttempt(
            ExerciseAttempt(
                exerciseId = exercise.id,
                category = exercise.category,
                userAnswer = userAnswer,
                isCorrect = isCorrect,
                scoreEarned = points
            )
        )
        updateSkillStat(exercise.category, isSuccess = isCorrect)
        recordActivity(minutes = 2, exercisesDelta = 1)
        val successes = db.exerciseDao().getAllAttemptsSnapshot().count { it.isCorrect }
        if (successes >= 5) unlockAchievement("ach_5_exercises")
    }

    // --- Dictionnaire local (Room SQLite 100 % hors ligne) ---
    fun searchDictionary(
        query: String,
        category: String? = null,
        scope: DictionarySearchScope = DictionarySearchScope.ALL
    ): Flow<List<DictionaryEntry>> {
        val trimmed = query.trim()
        val activeCategory = category?.takeIf { it.isNotBlank() && it != "Tous" }
        val normalized = FrenchNormalizer.stripAccents(trimmed)
        return when (scope) {
            DictionarySearchScope.ALL -> {
                if (trimmed.isEmpty()) {
                    if (activeCategory == null) allDictionaryEntries
                    else db.dictionaryDao().observeByCategory(activeCategory)
                } else {
                    db.dictionaryDao().searchEntries(normalized, trimmed, activeCategory)
                }
            }
            DictionarySearchScope.DEFINITIONS ->
                db.dictionaryDao().searchByDefinition(normalized, trimmed, activeCategory)
            DictionarySearchScope.SYNONYMS ->
                db.dictionaryDao().searchBySynonym(normalized, trimmed, activeCategory)
            DictionarySearchScope.ANTONYMS ->
                db.dictionaryDao().searchByAntonym(normalized, trimmed, activeCategory)
        }
    }

    suspend fun fuzzyDictionarySuggestions(query: String): List<DictionaryEntry> = withContext(Dispatchers.Default) {
        val norm = FrenchNormalizer.stripAccents(query.trim())
        if (norm.length < 2) return@withContext emptyList()
        val all = db.dictionaryDao().getAllSnapshot()
        all.mapNotNull { entry ->
            val wordDist = correctionEngine.lexicon.damerauLevenshtein(norm, entry.normalizedWord)
            val synHit = entry.synonymsPipe.split("|")
                .map { FrenchNormalizer.stripAccents(it.trim()) }
                .filter { it.isNotEmpty() }
                .minOfOrNull { correctionEngine.lexicon.damerauLevenshtein(norm, it) } ?: 99
            val antHit = entry.antonymsPipe.split("|")
                .map { FrenchNormalizer.stripAccents(it.trim()) }
                .filter { it.isNotEmpty() }
                .minOfOrNull { correctionEngine.lexicon.damerauLevenshtein(norm, it) } ?: 99
            val bestDist = minOf(wordDist, synHit, antHit)
            if (bestDist <= 2 || entry.normalizedWord.contains(norm)) entry to bestDist else null
        }
            .sortedBy { it.second }
            .map { it.first }
            .take(6)
    }

    fun observeDictionaryEntry(id: String): Flow<DictionaryEntry?> = db.dictionaryDao().observeById(id)

    suspend fun getDictionaryEntry(id: String): DictionaryEntry? {
        val item = db.dictionaryDao().getById(id)
        if (item != null) {
            db.dictionaryDao().markViewed(id, System.currentTimeMillis())
        }
        return item
    }

    suspend fun findDictionaryEntryByWord(word: String): DictionaryEntry? {
        val norm = FrenchNormalizer.stripAccents(word.trim())
        if (norm.isEmpty()) return null
        return db.dictionaryDao().findByNormalizedWord(norm)
    }

    suspend fun addCustomDictionaryEntry(
        word: String,
        partOfSpeech: String,
        definition: String,
        synonyms: List<String>,
        antonyms: List<String>,
        example: String,
        level: String = "B1"
    ): DictionaryEntry {
        val cleanWord = word.trim().replaceFirstChar { it.uppercase() }
        val normalized = FrenchNormalizer.stripAccents(cleanWord)
        val entry = DictionaryEntry(
            id = "custom_${normalized}_${System.currentTimeMillis()}",
            word = cleanWord,
            normalizedWord = normalized,
            definition = definition.trim(),
            partOfSpeech = partOfSpeech.trim().ifBlank { "Nom" },
            gender = "Personnel",
            pluralForm = "—",
            pronunciation = "/$normalized/",
            synonymsPipe = synonyms.map { it.trim() }.filter { it.isNotEmpty() }.joinToString("|"),
            antonymsPipe = antonyms.map { it.trim() }.filter { it.isNotEmpty() }.joinToString("|"),
            examplesPipe = example.trim(),
            expressionsPipe = "",
            level = level,
            lastViewedAt = System.currentTimeMillis()
        )
        db.dictionaryDao().insertEntry(entry)
        return entry
    }

    suspend fun deleteCustomDictionaryEntry(id: String) {
        if (id.startsWith("custom_")) {
            db.dictionaryDao().deleteEntryById(id)
        }
    }

    // --- Conjugueur local ---
    fun conjugateVerb(infinitive: String): VerbConjugationTable? =
        conjugatorEngine.conjugate(infinitive)

    suspend fun identifyConjugatedForm(query: String): List<IdentifiedVerbForm> = withContext(Dispatchers.Default) {
        val engineMatches = conjugatorEngine.identifyForm(query)
        if (engineMatches.isNotEmpty()) return@withContext engineMatches
        val norm = FrenchNormalizer.stripAccents(query.trim().lowercase())
        db.conjugationDao().findByConjugatedForm(norm).map {
            IdentifiedVerbForm(
                infinitive = it.verbInfinitive,
                tense = it.tense,
                pronoun = it.pronoun,
                matchedForm = it.conjugatedForm
            )
        }
    }

    // --- Dictées ---
    suspend fun getDictation(id: String): Dictation? = db.dictationDao().getById(id)

    suspend fun evaluateAndSaveDictation(dictation: Dictation, userText: String): DictationEvaluation {
        val eval = withContext(Dispatchers.Default) {
            DictationDiffEngine.evaluate(dictation.referenceText, userText)
        }
        db.dictationDao().insertAttempt(
            DictationAttempt(
                dictationId = dictation.id,
                userText = userText,
                scoreOutOf20 = eval.scoreOutOf20,
                mistakesCount = eval.mistakesCount,
                summaryFeedback = eval.summaryFeedback
            )
        )
        updateSkillStat("Orthographe", isSuccess = eval.scoreOutOf20 >= 14f)
        recordActivity(minutes = 4, exercisesDelta = 1)
        unlockAchievement("ach_first_dictation")
        return eval
    }

    // --- Vocabulaire (Répétition espacée SM-2) ---
    suspend fun reviewVocabularyWord(wordId: String, quality: Int) {
        val current = db.vocabularyDao().getProgress(wordId)
        val updated = Sm2Scheduler.schedule(current, wordId, quality)
        db.vocabularyDao().upsertProgress(updated)
        updateSkillStat("Vocabulaire", isSuccess = quality >= 3)
        recordActivity(minutes = 2, exercisesDelta = 1)
        val reviewed = db.vocabularyDao().getAllProgressSnapshot().size
        if (reviewed >= 5) unlockAchievement("ach_vocab_master")
    }

    // --- Favoris ---
    fun observeIsFavorite(type: String, itemId: String): Flow<Boolean> =
        db.statsAndMetaDao().observeIsFavorite(type, itemId)

    suspend fun toggleFavorite(type: String, itemId: String, title: String, subtitle: String) {
        val existing = db.statsAndMetaDao().getFavoritesSnapshot()
            .any { it.itemType == type && it.itemId == itemId }
        if (existing) {
            db.statsAndMetaDao().deleteFavorite(type, itemId)
        } else {
            db.statsAndMetaDao().insertFavorite(
                Favorite(itemType = type, itemId = itemId, title = title, subtitle = subtitle)
            )
        }
    }

    suspend fun removeFavorite(type: String, itemId: String) {
        db.statsAndMetaDao().deleteFavorite(type, itemId)
    }

    // --- Paramètres ---
    suspend fun updateThemeMode(mode: String) {
        prefs.updateThemeMode(mode)
        val settings = db.statsAndMetaDao().getSettings() ?: AppSettings()
        db.statsAndMetaDao().upsertSettings(settings.copy(themeMode = mode))
    }

    suspend fun updateDailyGoalMinutes(minutes: Int) {
        prefs.updateDailyGoal(minutes)
        val settings = db.statsAndMetaDao().getSettings() ?: AppSettings()
        db.statsAndMetaDao().upsertSettings(settings.copy(dailyGoalMinutes = minutes))
    }

    suspend fun updateSpeechRate(rate: Float) {
        prefs.updateSpeechRate(rate)
        val settings = db.statsAndMetaDao().getSettings() ?: AppSettings()
        db.statsAndMetaDao().upsertSettings(settings.copy(ttsSpeechRate = rate))
    }

    suspend fun updateAutoAnalyze(enabled: Boolean) {
        prefs.updateAutoAnalyze(enabled)
        val settings = db.statsAndMetaDao().getSettings() ?: AppSettings()
        db.statsAndMetaDao().upsertSettings(settings.copy(autoAnalyzeCorrection = enabled))
    }

    suspend fun updateIncludeHistoryInBackup(enabled: Boolean) {
        prefs.updateIncludeHistoryInBackup(enabled)
        val settings = db.statsAndMetaDao().getSettings() ?: AppSettings()
        db.statsAndMetaDao().upsertSettings(settings.copy(includeHistoryInBackup = enabled))
    }

    suspend fun updateDailyReminder(enabled: Boolean, hour: Int) {
        prefs.updateDailyReminder(enabled, hour)
        val settings = db.statsAndMetaDao().getSettings() ?: AppSettings()
        db.statsAndMetaDao().upsertSettings(settings.copy(dailyReminderEnabled = enabled, reminderHour = hour))
    }

    // --- Sauvegarde & Restauration JSON ---
    suspend fun exportBackupJson(includeHistory: Boolean): BackupResult =
        backupManager.exportToJson(includeHistory)

    suspend fun importBackupJson(rawJson: String): BackupResult =
        backupManager.importFromJson(rawJson)

    // --- Métriques internes & Série ---
    private suspend fun updateSkillStat(category: String, isSuccess: Boolean, weight: Int = 1) {
        val current = db.statsAndMetaDao().getStatisticByCategory(category)
            ?: LearningStatistics(skillCategory = category)
        val newTotal = current.totalAttempts + weight
        val newSuccess = current.successCount + if (isSuccess) weight else 0
        val newErrors = current.errorCount + if (!isSuccess) weight else 0
        val mastery = if (newTotal == 0) 0 else ((newSuccess.toFloat() / newTotal) * 100).toInt().coerceIn(0, 100)
        db.statsAndMetaDao().upsertStatistic(
            current.copy(
                totalAttempts = newTotal,
                successCount = newSuccess,
                errorCount = newErrors,
                masteryPercent = mastery,
                lastUpdatedAt = System.currentTimeMillis()
            )
        )
    }

    private suspend fun recordActivity(minutes: Int, exercisesDelta: Int = 0, correctionsDelta: Int = 0) {
        val today = currentEpochDay()
        val settings = db.statsAndMetaDao().getSettings() ?: AppSettings()
        val existingGoal = db.statsAndMetaDao().getDailyGoal(today)
            ?: DailyGoal(epochDay = today, targetMinutes = settings.dailyGoalMinutes)

        val updatedMinutes = existingGoal.completedMinutes + minutes
        db.statsAndMetaDao().upsertDailyGoal(
            existingGoal.copy(
                targetMinutes = settings.dailyGoalMinutes,
                completedMinutes = updatedMinutes,
                exercisesDone = existingGoal.exercisesDone + exercisesDelta,
                correctionsDone = existingGoal.correctionsDone + correctionsDelta,
                goalReached = updatedMinutes >= settings.dailyGoalMinutes
            )
        )

        val profile = db.userProfileDao().getProfile() ?: UserProfile()
        val newStreak = when {
            profile.lastActiveEpochDay == today -> profile.streakDays.coerceAtLeast(1)
            profile.lastActiveEpochDay == today - 1L -> profile.streakDays + 1
            else -> 1
        }
        val allStats = db.statsAndMetaDao().getStatisticsSnapshot().filter { it.totalAttempts > 0 }
        val avgScore = if (allStats.isEmpty()) profile.globalScore else allStats.map { it.masteryPercent }.average().toInt()

        db.userProfileDao().upsert(
            profile.copy(
                streakDays = newStreak,
                lastActiveEpochDay = today,
                totalLearningMinutes = profile.totalLearningMinutes + minutes,
                globalScore = maxOf(profile.globalScore, avgScore)
            )
        )
        if (newStreak >= 3) unlockAchievement("ach_streak_3")
    }

    private suspend fun unlockAchievement(id: String) {
        val existing = db.statsAndMetaDao().getAchievementsSnapshot().firstOrNull { it.id == id } ?: return
        if (!existing.isUnlocked) {
            db.statsAndMetaDao().updateAchievement(
                existing.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis())
            )
        }
    }
}
