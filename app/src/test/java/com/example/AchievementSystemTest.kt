package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.backup.BackupManager
import com.example.core.database.Achievement
import com.example.core.database.DatabaseSeeder
import com.example.core.database.Exercise
import com.example.core.database.ExerciseAttempt
import com.example.core.database.FrancaisProDatabase
import com.example.core.database.LessonProgress
import com.example.core.database.UserProfile
import com.example.core.database.VocabularyProgress
import com.example.core.datastore.PreferencesRepository
import com.example.data.FrancaisProRepository
import com.example.learning.content.AcademyLessonsSeed
import com.example.learning.content.DictionarySeed
import com.example.learning.content.ExercisesAndDictationsSeed
import com.example.learning.progression.AchievementProgressEngine
import com.example.linguistics.analysis.HybridCorrectionEngine
import com.example.linguistics.conjugation.FrenchConjugatorEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AchievementSystemTest {

    private lateinit var context: Context
    private lateinit var db: FrancaisProDatabase
    private lateinit var repository: FrancaisProRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, FrancaisProDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val conjugator = FrenchConjugatorEngine()
        val seeder = DatabaseSeeder(db, conjugator)
        kotlinx.coroutines.runBlocking {
            seeder.seedIfNeeded()
        }
        repository = FrancaisProRepository(
            db = db,
            prefs = PreferencesRepository(context),
            correctionEngine = HybridCorrectionEngine(),
            conjugatorEngine = conjugator,
            backupManager = BackupManager(db)
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `evaluateProgress tracks partial and unlocked progress for Grammar Guru and Vocabulary Master`() {
        val achievements = DictionarySeed.achievements()
        val attempts = listOf(
            ExerciseAttempt(exerciseId = "g1", category = "Grammaire", userAnswer = "a", isCorrect = true, scoreEarned = 10),
            ExerciseAttempt(exerciseId = "g2", category = "Grammaire", userAnswer = "b", isCorrect = true, scoreEarned = 10),
            ExerciseAttempt(exerciseId = "g3", category = "Grammaire", userAnswer = "c", isCorrect = false, scoreEarned = 0)
        )
        val vocabProgress = (1..5).map { idx ->
            VocabularyProgress(wordId = "v_$idx", repetitions = 1, intervalDays = 1)
        }

        val evaluated = AchievementProgressEngine.evaluateProgress(
            achievements = achievements.map {
                if (it.id == "ach_vocab_master") it.copy(isUnlocked = true, unlockedAt = 1000L) else it
            },
            lessons = AcademyLessonsSeed.lessons(),
            lessonProgress = listOf(LessonProgress(lessonId = "a1_ort_01", isCompleted = true, quizScorePercent = 90)),
            exerciseAttempts = attempts,
            vocabularyProgress = vocabProgress,
            correctionHistory = emptyList(),
            dictationAttempts = emptyList(),
            profile = UserProfile(streakDays = 2)
        )

        val grammarGuru = evaluated.first { it.achievement.id == "ach_grammar_guru" }
        assertEquals(2, grammarGuru.current)
        assertEquals(3, grammarGuru.target)
        assertEquals("2 / 3 exercices", grammarGuru.progressText)
        assertFalse(grammarGuru.achievement.isUnlocked)

        val vocabMaster = evaluated.first { it.achievement.id == "ach_vocab_master" }
        assertTrue(vocabMaster.achievement.isUnlocked)
        assertEquals("Débloqué", vocabMaster.progressText)
    }

    @Test
    fun `submitting 3 correct grammar exercises unlocks Grammar Guru and emits unlock event`() = runTest {
        val emitted = mutableListOf<Achievement>()
        val job = launch {
            repository.unlockedAchievementEvents.collect { emitted += it }
        }

        val grammarExercises = ExercisesAndDictationsSeed.exercises()
            .filter { it.category.equals("Grammaire", ignoreCase = true) }
            .take(3)
        assertEquals(3, grammarExercises.size)

        grammarExercises.forEach { ex ->
            repository.submitExerciseAttempt(ex, ex.expectedAnswer, true)
        }

        val snapshot = db.statsAndMetaDao().getAchievementsSnapshot()
        val grammarGuru = snapshot.first { it.id == "ach_grammar_guru" }
        assertTrue("Gourou de la grammaire doit être débloqué après 3 exercices réussis", grammarGuru.isUnlocked)
        assertTrue(grammarGuru.unlockedAt > 0L)
        assertTrue(emitted.any { it.id == "ach_grammar_guru" })

        job.cancel()
    }

    @Test
    fun `database upgrade seeding adds new badges without resetting existing unlocked state`() = runTest {
        // Simuler une ancienne base ne contenant que les 6 badges initiaux avec un badge déjà débloqué
        val unlockedFirst = Achievement(
            id = "ach_first_lesson",
            title = "Premiers pas à l'Académie",
            description = "Ancien libellé",
            iconName = "school",
            isUnlocked = true,
            unlockedAt = 999_000L
        )
        db.statsAndMetaDao().insertAchievements(listOf(unlockedFirst))
        db.statsAndMetaDao().insertAchievementsIgnore(DictionarySeed.achievements())

        val afterSeed = db.statsAndMetaDao().getAchievementsSnapshot()
        assertEquals(DictionarySeed.achievements().size, afterSeed.size)
        val preserved = afterSeed.first { it.id == "ach_first_lesson" }
        assertTrue(preserved.isUnlocked)
        assertEquals(999_000L, preserved.unlockedAt)
        assertTrue(afterSeed.any { it.id == "ach_grammar_guru" && it.title == "Gourou de la grammaire" })
        assertTrue(afterSeed.any { it.id == "ach_vocab_master" && it.title == "Maître du vocabulaire" })
    }

    @Test
    fun `completeOnboardingFlow saves display name, goal minutes, placement level, and completion flag`() = runTest {
        repository.completeOnboardingFlow(
            displayName = "  Camille  ",
            dailyGoalMinutes = 20,
            placementCorrect = 6,
            placementTotal = 7
        )

        val profile = db.userProfileDao().getProfile()!!
        val prefs = repository.preferences.first()
        assertEquals("Camille", profile.displayName)
        assertTrue(profile.onboardingCompleted)
        assertTrue(profile.initialAssessmentCompleted)
        assertEquals(20, prefs.dailyGoalMinutes)
        assertTrue(profile.estimatedLevel in listOf("B2", "C1", "C2"))
    }
}
