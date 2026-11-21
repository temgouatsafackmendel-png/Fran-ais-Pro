package com.example.core.database

import androidx.room.withTransaction
import com.example.learning.content.AcademyLessonsSeed
import com.example.learning.content.DictionarySeed
import com.example.learning.content.ExercisesAndDictationsSeed
import com.example.linguistics.conjugation.FrenchConjugatorEngine
import com.example.linguistics.model.FrenchNormalizer

/**
 * Initialise de manière idempotente la base locale Room au premier lancement
 * avec les contenus pédagogiques, dictionnaire, conjugaisons, dictées et badges vérifiés.
 */
class DatabaseSeeder(
    private val db: FrancaisProDatabase,
    private val conjugator: FrenchConjugatorEngine
) {

    suspend fun seedIfNeeded() {
        db.withTransaction {
            if (db.userProfileDao().getProfile() == null) {
                db.userProfileDao().upsert(UserProfile())
            }
            if (db.statsAndMetaDao().getSettings() == null) {
                db.statsAndMetaDao().upsertSettings(AppSettings())
            }
            val seedLessons = AcademyLessonsSeed.lessons()
            if (db.lessonDao().countLessons() < seedLessons.size) {
                db.lessonDao().insertLessonsIgnore(seedLessons)
            }
            val seedExercises = ExercisesAndDictationsSeed.exercises()
            if (db.exerciseDao().countExercises() < seedExercises.size) {
                db.exerciseDao().insertExercisesIgnore(seedExercises)
            }
            val seedEntries = DictionarySeed.entries()
            if (db.dictionaryDao().countEntries() < seedEntries.size) {
                db.dictionaryDao().insertEntriesIgnore(seedEntries)
            }
            if (db.vocabularyDao().countWords() == 0) {
                db.vocabularyDao().insertWords(ExercisesAndDictationsSeed.vocabularyWords())
            }
            if (db.dictationDao().countDictations() == 0) {
                db.dictationDao().insertDictations(ExercisesAndDictationsSeed.dictations())
            }
            val seedAchievements = DictionarySeed.achievements()
            val existingAchievements = db.statsAndMetaDao().getAchievementsSnapshot()
            if (existingAchievements.size < seedAchievements.size) {
                db.statsAndMetaDao().insertAchievementsIgnore(seedAchievements)
            }
            // Met à jour uniquement les libellés d'anciens badges conservés en conservant l'état débloqué
            val existingById = existingAchievements.associateBy { it.id }
            seedAchievements.forEach { seeded ->
                val current = existingById[seeded.id]
                if (current != null && (current.title != seeded.title || current.description != seeded.description || current.iconName != seeded.iconName)) {
                    db.statsAndMetaDao().updateAchievement(
                        current.copy(
                            title = seeded.title,
                            description = seeded.description,
                            iconName = seeded.iconName
                        )
                    )
                }
            }
            val seedRewards = DictionarySeed.streakRewards()
            val existingRewards = db.academyStreakDao().getStreakRewardsSnapshot()
            if (existingRewards.size < seedRewards.size) {
                db.academyStreakDao().insertStreakRewardsIgnore(seedRewards)
            }
            if (db.academyStreakDao().getStreakState() == null) {
                val existingProfile = db.userProfileDao().getProfile()
                val legacyStreak = (existingProfile?.streakDays ?: 0).coerceAtLeast(0)
                val legacyDay = existingProfile?.lastActiveEpochDay ?: 0L
                db.academyStreakDao().upsertStreakState(
                    StreakState(
                        id = 1,
                        currentStreak = legacyStreak,
                        bestStreak = legacyStreak,
                        lastPracticeEpochDay = legacyDay,
                        freezeTokensAvailable = 1,
                        totalBonusXpEarned = 0,
                        totalAcademySessions = if (legacyStreak > 0) 1 else 0
                    )
                )
                if (legacyStreak > 0) {
                    val now = System.currentTimeMillis()
                    db.academyStreakDao().getStreakRewardsSnapshot().forEach { reward ->
                        if (!reward.isUnlocked && legacyStreak >= reward.requiredStreakDays) {
                            db.academyStreakDao().updateStreakReward(
                                reward.copy(isUnlocked = true, unlockedAt = now)
                            )
                        }
                    }
                }
            }
            if (db.statsAndMetaDao().getStatisticsSnapshot().isEmpty()) {
                val baseSkills = listOf(
                    "Orthographe", "Grammaire", "Conjugaison", "Syntaxe", "Vocabulaire", "Ponctuation", "Style"
                )
                baseSkills.forEach { skill ->
                    db.statsAndMetaDao().upsertStatistic(
                        LearningStatistics(skillCategory = skill, totalAttempts = 0, successCount = 0, errorCount = 0, masteryPercent = 0)
                    )
                }
            }
            if (db.conjugationDao().countVerbs() == 0) {
                val verbs = mutableListOf<Verb>()
                val conjugations = mutableListOf<Conjugation>()
                for (inf in conjugator.referenceVerbs) {
                    val table = conjugator.conjugate(inf) ?: continue
                    verbs += Verb(
                        infinitive = table.infinitive,
                        normalizedInfinitive = FrenchNormalizer.stripAccents(table.infinitive),
                        groupNumber = table.groupNumber,
                        auxiliary = table.auxiliary,
                        presentParticiple = table.presentParticiple,
                        pastParticiple = table.pastParticiple,
                        isIrregular = table.isIrregular,
                        modelInfo = table.modelInfo
                    )
                    table.tenses.forEach { (tense, entries) ->
                        val mood = when {
                            tense.startsWith("Conditionnel") -> "Conditionnel"
                            tense.startsWith("Subjonctif") -> "Subjonctif"
                            tense.startsWith("Impératif") -> "Impératif"
                            else -> "Indicatif"
                        }
                        entries.forEachIndexed { idx, row ->
                            if (row.form != "—") {
                                conjugations += Conjugation(
                                    verbInfinitive = table.infinitive,
                                    mood = mood,
                                    tense = tense,
                                    personIndex = idx,
                                    pronoun = row.pronoun,
                                    conjugatedForm = row.form,
                                    normalizedForm = FrenchNormalizer.stripAccents(row.form.lowercase())
                                )
                            }
                        }
                    }
                }
                db.conjugationDao().insertVerbs(verbs)
                db.conjugationDao().insertConjugations(conjugations)
            }
        }
    }
}
