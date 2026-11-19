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
            if (db.lessonDao().countLessons() == 0) {
                db.lessonDao().insertLessons(AcademyLessonsSeed.lessons())
            }
            if (db.exerciseDao().countExercises() == 0) {
                db.exerciseDao().insertExercises(ExercisesAndDictationsSeed.exercises())
            }
            if (db.dictionaryDao().countEntries() == 0) {
                db.dictionaryDao().insertEntries(DictionarySeed.entries())
            }
            if (db.vocabularyDao().countWords() == 0) {
                db.vocabularyDao().insertWords(ExercisesAndDictationsSeed.vocabularyWords())
            }
            if (db.dictationDao().countDictations() == 0) {
                db.dictationDao().insertDictations(ExercisesAndDictationsSeed.dictations())
            }
            if (db.statsAndMetaDao().getAchievementsSnapshot().isEmpty()) {
                db.statsAndMetaDao().insertAchievements(DictionarySeed.achievements())
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
