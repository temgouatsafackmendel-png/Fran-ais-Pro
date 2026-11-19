package com.example.core.backup

import androidx.room.withTransaction
import com.example.core.database.Achievement
import com.example.core.database.AppSettings
import com.example.core.database.CorrectionError
import com.example.core.database.CorrectionHistory
import com.example.core.database.DailyGoal
import com.example.core.database.DictationAttempt
import com.example.core.database.ExerciseAttempt
import com.example.core.database.Favorite
import com.example.core.database.FrancaisProDatabase
import com.example.core.database.LearningStatistics
import com.example.core.database.LessonProgress
import com.example.core.database.UserProfile
import com.example.core.database.VocabularyProgress
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@JsonClass(generateAdapter = true)
data class FrancaisProBackupPayload(
    val appId: String = "FRANCAIS_PRO",
    val schemaVersion: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val includeHistory: Boolean = true,
    val userProfile: UserProfile?,
    val appSettings: AppSettings?,
    val lessonProgress: List<LessonProgress> = emptyList(),
    val exerciseAttempts: List<ExerciseAttempt> = emptyList(),
    val vocabularyProgress: List<VocabularyProgress> = emptyList(),
    val dictationAttempts: List<DictationAttempt> = emptyList(),
    val dailyGoals: List<DailyGoal> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val learningStatistics: List<LearningStatistics> = emptyList(),
    val favorites: List<Favorite> = emptyList(),
    val correctionHistory: List<CorrectionHistory> = emptyList(),
    val correctionErrors: List<CorrectionError> = emptyList()
)

sealed class BackupResult {
    data class ExportSuccess(val json: String, val itemCount: Int) : BackupResult()
    data class ImportSuccess(val restoredSummary: String) : BackupResult()
    data class Failure(val reason: String) : BackupResult()
}

class BackupManager(
    private val db: FrancaisProDatabase
) {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(FrancaisProBackupPayload::class.java).indent("  ")

    suspend fun exportToJson(includeHistory: Boolean): BackupResult {
        return try {
            val history = if (includeHistory) db.correctionDao().getHistorySnapshot() else emptyList()
            val errors = if (includeHistory) db.correctionDao().getAllErrorsSnapshot() else emptyList()

            val payload = FrancaisProBackupPayload(
                includeHistory = includeHistory,
                userProfile = db.userProfileDao().getProfile(),
                appSettings = db.statsAndMetaDao().getSettings(),
                lessonProgress = db.lessonDao().getAllProgressSnapshot(),
                exerciseAttempts = db.exerciseDao().getAllAttemptsSnapshot(),
                vocabularyProgress = db.vocabularyDao().getAllProgressSnapshot(),
                dictationAttempts = db.dictationDao().getAttemptsSnapshot(),
                dailyGoals = db.statsAndMetaDao().getAllDailyGoalsSnapshot(),
                achievements = db.statsAndMetaDao().getAchievementsSnapshot(),
                learningStatistics = db.statsAndMetaDao().getStatisticsSnapshot(),
                favorites = db.statsAndMetaDao().getFavoritesSnapshot(),
                correctionHistory = history,
                correctionErrors = errors
            )
            val json = adapter.toJson(payload)
            val count = payload.lessonProgress.size +
                payload.exerciseAttempts.size +
                payload.vocabularyProgress.size +
                payload.favorites.size +
                payload.correctionHistory.size
            BackupResult.ExportSuccess(json, count)
        } catch (e: Exception) {
            BackupResult.Failure("Impossible d'exporter la sauvegarde : ${e.message ?: "erreur inconnue"}")
        }
    }

    fun validateJson(rawJson: String): Result<FrancaisProBackupPayload> {
        if (rawJson.isBlank()) {
            return Result.failure(IllegalArgumentException("Le fichier de sauvegarde est vide."))
        }
        return try {
            val payload = adapter.fromJson(rawJson)
                ?: return Result.failure(IllegalArgumentException("Format JSON non reconnu."))
            if (payload.appId != "FRANCAIS_PRO") {
                return Result.failure(IllegalArgumentException("Ce fichier n'est pas une sauvegarde FRANÇAIS PRO valide."))
            }
            if (payload.schemaVersion < 1 || payload.schemaVersion > 1) {
                return Result.failure(IllegalArgumentException("Version de schéma non prise en charge (${payload.schemaVersion})."))
            }
            Result.success(payload)
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Fichier JSON corrompu ou invalide : ${e.message}"))
        }
    }

    suspend fun importFromJson(rawJson: String): BackupResult {
        val validated = validateJson(rawJson).getOrElse {
            return BackupResult.Failure(it.message ?: "Fichier invalide.")
        }

        return try {
            // Transaction atomique : aucune perte de données en cas d'échec
            db.withTransaction {
                validated.userProfile?.let { db.userProfileDao().upsert(it.copy(id = 1)) }
                validated.appSettings?.let { db.statsAndMetaDao().upsertSettings(it.copy(id = 1)) }

                if (validated.lessonProgress.isNotEmpty()) {
                    db.lessonDao().clearProgress()
                    validated.lessonProgress.forEach { lp ->
                        if (db.lessonDao().getLessonById(lp.lessonId) != null) {
                            db.lessonDao().upsertProgress(lp.copy(id = 0))
                        }
                    }
                }

                if (validated.exerciseAttempts.isNotEmpty()) {
                    db.exerciseDao().clearAttempts()
                    val validAttempts = validated.exerciseAttempts.filter {
                        db.exerciseDao().getExerciseById(it.exerciseId) != null
                    }.map { it.copy(id = 0) }
                    db.exerciseDao().insertAttempts(validAttempts)
                }

                if (validated.vocabularyProgress.isNotEmpty()) {
                    db.vocabularyDao().clearProgress()
                    validated.vocabularyProgress.forEach { vp ->
                        db.vocabularyDao().upsertProgress(vp.copy(id = 0))
                    }
                }

                if (validated.dictationAttempts.isNotEmpty()) {
                    db.dictationDao().clearAttempts()
                    val validDict = validated.dictationAttempts.filter {
                        db.dictationDao().getById(it.dictationId) != null
                    }.map { it.copy(id = 0) }
                    db.dictationDao().insertAttempts(validDict)
                }

                if (validated.dailyGoals.isNotEmpty()) {
                    db.statsAndMetaDao().clearDailyGoals()
                    validated.dailyGoals.forEach { db.statsAndMetaDao().upsertDailyGoal(it) }
                }

                if (validated.achievements.isNotEmpty()) {
                    db.statsAndMetaDao().insertAchievements(validated.achievements)
                }

                if (validated.learningStatistics.isNotEmpty()) {
                    db.statsAndMetaDao().clearStatistics()
                    validated.learningStatistics.forEach { db.statsAndMetaDao().upsertStatistic(it.copy(id = 0)) }
                }

                if (validated.favorites.isNotEmpty()) {
                    db.statsAndMetaDao().clearFavorites()
                    validated.favorites.forEach { db.statsAndMetaDao().insertFavorite(it.copy(id = 0)) }
                }

                if (validated.includeHistory && validated.correctionHistory.isNotEmpty()) {
                    db.correctionDao().clearHistory()
                    val oldToNewId = mutableMapOf<Long, Long>()
                    for (hist in validated.correctionHistory) {
                        val newId = db.correctionDao().insertHistory(hist.copy(id = 0))
                        oldToNewId[hist.id] = newId
                    }
                    val remappedErrors = validated.correctionErrors.mapNotNull { err ->
                        val mappedId = oldToNewId[err.historyId] ?: return@mapNotNull null
                        err.copy(id = 0, historyId = mappedId)
                    }
                    if (remappedErrors.isNotEmpty()) {
                        db.correctionDao().insertErrors(remappedErrors)
                    }
                }
            }
            BackupResult.ImportSuccess(
                "Restauration réussie : ${validated.lessonProgress.size} leçons, ${validated.favorites.size} favoris, ${validated.correctionHistory.size} analyses."
            )
        } catch (e: Exception) {
            BackupResult.Failure("Échec lors de la restauration atomique : ${e.message}")
        }
    }
}
