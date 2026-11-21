package com.example

import com.example.core.database.LessonProgress
import com.example.learning.content.AcademyLessonsSeed
import com.example.learning.content.ExercisesAndDictationsSeed
import com.example.learning.progression.CoursePathEngine
import com.example.learning.progression.LessonUnlockStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoursePathEngineTest {

    @Test
    fun `seeded curriculum contains 24 structured courses with 4 chapters per CECRL level and linked exercises`() {
        val lessons = AcademyLessonsSeed.lessons()
        val exercises = ExercisesAndDictationsSeed.exercises()

        assertEquals(24, lessons.size)
        CoursePathEngine.cefrOrder.forEach { level ->
            val levelLessons = lessons.filter { it.level == level }.sortedBy { it.chapterOrder }
            assertEquals("Le niveau $level doit contenir 4 chapitres", 4, levelLessons.size)
            assertEquals(listOf(1, 2, 3, 4), levelLessons.map { it.chapterOrder })
            levelLessons.forEach { lesson ->
                assertTrue(
                    "Chaque cours (${lesson.id}) doit avoir un contenu substantiel",
                    lesson.ruleContent.length >= 80 && lesson.examplesJson.contains("||")
                )
                assertTrue(
                    "Chaque cours (${lesson.id}) doit posséder au moins un QCM lié",
                    exercises.any { it.lessonId == lesson.id && it.optionsPipe.isNotBlank() }
                )
            }
        }
    }

    @Test
    fun `initial roadmap unlocks only A1 chapter 1 and locks A2 to C2 with explicit reasons`() {
        val lessons = AcademyLessonsSeed.lessons()
        val roadmap = CoursePathEngine.buildRoadmap(lessons, emptyList())

        assertEquals("A1", roadmap.activeLevel)
        assertEquals(1, roadmap.unlockedLevelsCount)
        assertEquals(6, roadmap.totalLevelsCount)
        assertEquals(0, roadmap.completedLessonsCount)
        assertEquals(24, roadmap.totalLessonsCount)
        assertEquals(0, roadmap.totalXp)

        val a1 = roadmap.levels.first { it.level == "A1" }
        assertTrue(a1.isUnlocked)
        assertFalse(a1.isCompleted)
        assertNull(a1.lockReason)
        assertEquals(LessonUnlockStatus.CURRENT, a1.steps[0].status)
        assertEquals(LessonUnlockStatus.LOCKED, a1.steps[1].status)
        assertEquals(LessonUnlockStatus.LOCKED, a1.steps[2].status)
        assertEquals(LessonUnlockStatus.LOCKED, a1.steps[3].status)
        assertNotNull(a1.steps[1].lockReason)
        assertEquals(a1.steps[0].lesson.id, roadmap.nextRecommendedLesson?.id)

        val a2 = roadmap.levels.first { it.level == "A2" }
        assertFalse(a2.isUnlocked)
        assertNotNull(a2.lockReason)
        assertTrue(a2.lockReason!!.contains("A1"))
        assertTrue(a2.steps.all { it.status == LessonUnlockStatus.LOCKED })
    }

    @Test
    fun `completing chapters sequentially unlocks next chapter then unlocks next CECRL level with XP milestone`() {
        val lessons = AcademyLessonsSeed.lessons()
        val a1Lessons = lessons.filter { it.level == "A1" }.sortedBy { it.chapterOrder }
        val a2Lessons = lessons.filter { it.level == "A2" }.sortedBy { it.chapterOrder }

        // Après validation du chapitre 1 de A1 (score 95 % -> 50 + 20 XP excellence)
        val afterCh1 = CoursePathEngine.buildRoadmap(
            lessons = lessons,
            progress = listOf(LessonProgress(lessonId = a1Lessons[0].id, isCompleted = true, quizScorePercent = 95))
        )
        val a1Step2 = afterCh1.levels.first { it.level == "A1" }.steps[1]
        val a1Step3 = afterCh1.levels.first { it.level == "A1" }.steps[2]
        assertEquals(LessonUnlockStatus.CURRENT, a1Step2.status)
        assertEquals(LessonUnlockStatus.LOCKED, a1Step3.status)
        assertEquals(70, afterCh1.totalXp)
        assertFalse(afterCh1.levels.first { it.level == "A2" }.isUnlocked)
        assertEquals(a1Lessons[1].id, afterCh1.nextRecommendedLesson?.id)

        // Après validation des 4 chapitres de A1 -> A2 débloqué + bonus palier +100 XP
        val allA1Progress = a1Lessons.map { l ->
            LessonProgress(lessonId = l.id, isCompleted = true, quizScorePercent = 80)
        }
        val afterA1 = CoursePathEngine.buildRoadmap(lessons, allA1Progress)
        val a1Done = afterA1.levels.first { it.level == "A1" }
        val a2Open = afterA1.levels.first { it.level == "A2" }
        val b1Locked = afterA1.levels.first { it.level == "B1" }

        assertTrue(a1Done.isCompleted)
        assertTrue(a2Open.isUnlocked)
        assertFalse(b1Locked.isUnlocked)
        assertEquals("A2", afterA1.activeLevel)
        assertEquals(2, afterA1.unlockedLevelsCount)
        assertEquals(LessonUnlockStatus.CURRENT, a2Open.steps[0].status)
        assertEquals(LessonUnlockStatus.LOCKED, a2Open.steps[1].status)
        assertEquals(a2Lessons[0].id, afterA1.nextRecommendedLesson?.id)
        // 4 * 50 XP + 100 XP milestone = 300 XP
        assertEquals(300, afterA1.totalXp)
    }
}
