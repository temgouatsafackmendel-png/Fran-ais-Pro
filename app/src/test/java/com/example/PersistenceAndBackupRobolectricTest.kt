package com.example

import android.content.Context
import android.content.pm.PackageManager
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.backup.BackupManager
import com.example.core.backup.BackupResult
import com.example.core.database.DatabaseSeeder
import com.example.core.database.Favorite
import com.example.core.database.FrancaisProDatabase
import com.example.core.database.LessonProgress
import com.example.core.work.DailyReminderWorker
import com.example.linguistics.conjugation.FrenchConjugatorEngine
import kotlinx.coroutines.test.runTest
import java.util.Calendar
import java.util.concurrent.TimeUnit
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
class PersistenceAndBackupRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: FrancaisProDatabase
    private lateinit var backupManager: BackupManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, FrancaisProDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        backupManager = BackupManager(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `mode avion et confidentialite - aucune permission INTERNET declaree`() {
        val pkgInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )
        val perms = pkgInfo.requestedPermissions?.toList().orEmpty()
        assertFalse(
            "FRANÇAIS PRO ne doit pas déclarer android.permission.INTERNET.",
            perms.contains(android.Manifest.permission.INTERNET)
        )
    }

    @Test
    fun `initialisation locale Room et export-import JSON atomique`() = runTest {
        val seeder = DatabaseSeeder(db, FrenchConjugatorEngine())
        seeder.seedIfNeeded()

        assertTrue(db.lessonDao().countLessons() >= 10)
        assertTrue(db.exerciseDao().countExercises() >= 10)
        assertTrue(db.dictionaryDao().countEntries() >= 20)
        assertTrue(db.conjugationDao().countVerbs() >= 20)
        assertTrue(db.dictationDao().countDictations() >= 4)
        assertTrue(db.vocabularyDao().countWords() >= 8)

        // Simuler des données utilisateur locales
        db.lessonDao().upsertProgress(
            LessonProgress(lessonId = "a1_ort_01", isCompleted = true, quizScorePercent = 95, completedAt = 1000L)
        )
        db.statsAndMetaDao().insertFavorite(
            Favorite(itemType = "WORD", itemId = "dic_01", title = "Accord", subtitle = "Conformité grammaticale")
        )

        // Exporter en JSON
        val export = backupManager.exportToJson(includeHistory = true)
        assertTrue(export is BackupResult.ExportSuccess)
        val exportedJson = (export as BackupResult.ExportSuccess).json

        // Tenter un import corrompu -> doit échouer sans détruire les données existantes
        val badImport = backupManager.importFromJson("{ invalid json")
        assertTrue(badImport is BackupResult.Failure)
        assertEquals(1, db.lessonDao().getAllProgressSnapshot().size)

        // Vider la progression puis restaurer depuis l'export valide
        db.lessonDao().clearProgress()
        db.statsAndMetaDao().clearFavorites()
        assertEquals(0, db.lessonDao().getAllProgressSnapshot().size)

        val restore = backupManager.importFromJson(exportedJson)
        assertTrue(restore is BackupResult.ImportSuccess)
        assertEquals(1, db.lessonDao().getAllProgressSnapshot().size)
        assertEquals(95, db.lessonDao().getAllProgressSnapshot().first().quizScorePercent)
        assertEquals(1, db.statsAndMetaDao().getFavoritesSnapshot().size)
    }

    @Test
    fun `re-seeding lessons with IGNORE preserves existing user lesson progress without cascade wipe`() = runTest {
        val seeder = DatabaseSeeder(db, FrenchConjugatorEngine())
        seeder.seedIfNeeded()

        db.lessonDao().upsertProgress(
            LessonProgress(lessonId = "a1_ort_01", isCompleted = true, quizScorePercent = 100, completedAt = 2000L)
        )
        assertEquals(1, db.lessonDao().getAllProgressSnapshot().size)

        // Simule une montée de version qui appelle insertLessonsIgnore sur des leçons déjà existantes
        db.lessonDao().insertLessonsIgnore(com.example.learning.content.AcademyLessonsSeed.lessons())

        val remaining = db.lessonDao().getAllProgressSnapshot()
        assertEquals(1, remaining.size)
        assertEquals("a1_ort_01", remaining.first().lessonId)
        assertEquals(100, remaining.first().quizScorePercent)
    }

    @Test
    fun `backup import ignores orphaned vocabularyProgress ids without foreign key crash`() = runTest {
        val seeder = DatabaseSeeder(db, FrenchConjugatorEngine())
        seeder.seedIfNeeded()

        val validWordId = com.example.learning.content.ExercisesAndDictationsSeed.vocabularyWords().first().id
        val backupJsonWithOrphan = """
            {
              "appId": "FRANCAIS_PRO",
              "schemaVersion": 1,
              "exportedAt": 1700000000000,
              "includeHistory": false,
              "userProfile": null,
              "appSettings": null,
              "vocabularyProgress": [
                {
                  "id": 1,
                  "wordId": "$validWordId",
                  "repetitions": 2,
                  "intervalDays": 6,
                  "easeFactor": 2.5,
                  "nextReviewAt": 1700500000000,
                  "lastQuality": 5,
                  "lapsesCount": 0,
                  "mastered": true
                },
                {
                  "id": 2,
                  "wordId": "retired_nonexistent_word_id",
                  "repetitions": 1,
                  "intervalDays": 1,
                  "easeFactor": 2.5,
                  "nextReviewAt": 1700100000000,
                  "lastQuality": 3,
                  "lapsesCount": 0,
                  "mastered": false
                }
              ]
            }
        """.trimIndent()

        val result = backupManager.importFromJson(backupJsonWithOrphan)
        assertTrue("Import should succeed skipping orphan wordId: $result", result is BackupResult.ImportSuccess)
        val restored = db.vocabularyDao().getAllProgressSnapshot()
        assertEquals(1, restored.size)
        assertEquals(validWordId, restored.first().wordId)
    }

    @Test
    fun `daily reminder delay targets configured hour on same day or next day`() {
        val noon = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 20, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // 19h le même jour = exactement 7 heures
        val delaySameDay = DailyReminderWorker.computeInitialDelayMillis(targetHour = 19, nowMillis = noon)
        assertEquals(TimeUnit.HOURS.toMillis(7), delaySameDay)

        // 9h déjà passé à midi -> programmé demain à 9h = 21 heures
        val delayNextDay = DailyReminderWorker.computeInitialDelayMillis(targetHour = 9, nowMillis = noon)
        assertEquals(TimeUnit.HOURS.toMillis(21), delayNextDay)
    }
}
