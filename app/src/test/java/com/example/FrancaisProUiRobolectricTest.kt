package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasTestTag
import com.example.core.database.UserProfile
import com.example.features.academy.LessonDetailScreen
import com.example.features.correction.CorrectorScreen
import com.example.features.correction.CorrectorUiState
import com.example.features.dashboard.DashboardScreen
import com.example.features.onboarding.OnboardingScreen
import com.example.learning.content.AcademyLessonsSeed
import com.example.learning.content.ExercisesAndDictationsSeed
import com.example.learning.progression.AdaptiveProgressionEngine
import com.example.linguistics.analysis.HybridCorrectionEngine
import com.example.ui.theme.FrancaisProTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FrancaisProUiRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `onboarding 7 steps advances and can be skipped anytime`() {
        var skipped = false
        composeRule.setContent {
            FrancaisProTheme(darkTheme = false) {
                OnboardingScreen(
                    onFinishOnboarding = { _, _, _, _ -> },
                    onSkipToDashboard = { skipped = true }
                )
            }
        }

        // Étape 1 : Présentation
        composeRule.onNodeWithText("Votre français mérite le meilleur.").assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding_next_btn").performClick()

        // Étape 2 : Correcteur
        composeRule.onNodeWithText("Comprenez et éliminez vos erreurs.").assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding_next_btn").performClick()

        // Étape 3 : Académie
        composeRule.onNodeWithText("Progressez du débutant à l'expert.").assertIsDisplayed()

        // Ignorer accessible immédiatement
        composeRule.onNodeWithTag("onboarding_skip_button").performClick()
        assertTrue(skipped)
    }

    @Test
    fun `dashboard displays key sections and triggers continue action`() {
        var navigatedRoute: String? = null
        val summary = AdaptiveProgressionEngine.computeProfileSummary("B1", 4, emptyList())

        composeRule.setContent {
            FrancaisProTheme(darkTheme = false) {
                DashboardScreen(
                    profile = UserProfile(displayName = "Claire", estimatedLevel = "B1", streakDays = 3, totalLearningMinutes = 45),
                    adaptiveSummary = summary,
                    lessons = emptyList(),
                    lessonProgress = emptyList(),
                    exerciseAttempts = emptyList(),
                    dailyGoals = emptyList(),
                    dailyTargetMinutes = 15,
                    onNavigate = { navigatedRoute = it },
                    onStartCategoryTraining = {}
                )
            }
        }

        composeRule.onNodeWithText("Bonjour, Claire").assertIsDisplayed()
        composeRule.onNodeWithText("Votre entraînement du jour").assertIsDisplayed()
        composeRule.onNodeWithTag("dashboard_continue_learning_btn").performClick()
        assertTrue(navigatedRoute != null)
    }

    @Test
    fun `corrector renders word counter, analyze action, and highlighted issues cleanly`() {
        val sample = "Les etudiants apprend le francais."
        val report = HybridCorrectionEngine().analyze(sample)
        var analyzedClicked = false

        composeRule.setContent {
            FrancaisProTheme(darkTheme = false) {
                CorrectorScreen(
                    uiState = CorrectorUiState(
                        inputText = sample,
                        baselineOriginalText = sample,
                        report = report
                    ),
                    sampleTexts = listOf("Exemple" to sample),
                    onTextChange = {},
                    onAnalyze = { analyzedClicked = true },
                    onOpenResult = {},
                    onLoadSample = {},
                    onFixAll = {},
                    onAcceptIssue = {},
                    onDismissIssue = {},
                    onUndo = {},
                    onRedo = {},
                    onClear = {},
                    onOpenHistory = {},
                    onOpenWritingWorkshop = {}
                )
            }
        }

        composeRule.onNodeWithTag("corrector_word_counter").assertIsDisplayed()
        composeRule.onNodeWithTag("corrector_analyze_button").performClick()
        assertTrue(analyzedClicked)
        composeRule.onNodeWithTag("corrector_list")
            .performScrollToNode(hasTestTag("error_highlight_text"))
        composeRule.onNodeWithTag("error_highlight_text").assertIsDisplayed()
    }

    @Test
    fun `lesson detail renders 8 structured sections including rule and pitfalls`() {
        val lessons = AcademyLessonsSeed.lessons()
        val exercises = ExercisesAndDictationsSeed.exercises()
        val firstLesson = lessons.first()

        composeRule.setContent {
            FrancaisProTheme(darkTheme = false) {
                LessonDetailScreen(
                    lesson = firstLesson,
                    allLessons = lessons,
                    allExercises = exercises,
                    progress = null,
                    favorites = emptyList(),
                    onToggleFavorite = {},
                    onOpenLesson = {},
                    onStartQuiz = {},
                    onMarkCompleted = { _, _ -> },
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithText("Objectif de la leçon").assertExists()
        composeRule.onNodeWithText("Explication").assertExists()
        composeRule.onNodeWithText("Règle à retenir").assertExists()
        composeRule.onNodeWithText("Erreurs fréquentes").assertExists()
        composeRule.onNodeWithText("Mini-exercice intégré").assertExists()
    }
}
