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
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Spellcheck
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsSelected
import com.example.core.design.AppBottomNavigation
import com.example.core.design.BottomNavDestination
import com.example.navigation.NavigationMotion
import com.example.navigation.Routes
import com.example.ui.theme.FrancaisProTheme
import org.junit.Assert.assertEquals
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

    @Test
    fun `navigation motion uses exact 200ms for main tabs and resolves slide direction`() {
        assertEquals(200, NavigationMotion.TAB_TRANSITION_DURATION_MS)
        assertEquals(200, NavigationMotion.TAB_ENTER_DURATION_MS)
        assertEquals(200, NavigationMotion.TAB_EXIT_DURATION_MS)

        val durations = listOf(
            NavigationMotion.TAB_TRANSITION_DURATION_MS,
            NavigationMotion.TAB_ICON_DURATION_MS,
            NavigationMotion.TAB_SCALE_DURATION_MS,
            NavigationMotion.TAB_BAR_ENTER_DURATION_MS,
            NavigationMotion.TAB_BAR_EXIT_DURATION_MS,
            NavigationMotion.DRILL_IN_DURATION_MS,
            NavigationMotion.DRILL_OUT_DURATION_MS
        )
        durations.forEach { ms ->
            assertTrue("Duration $ms must be in 150..250ms", ms in 150..250)
        }

        // Droite (+1) : Accueil -> Corriger -> Apprendre -> Exercices -> Profil
        assertEquals(1, NavigationMotion.tabSlideDirection(Routes.DASHBOARD, Routes.CORRECTOR))
        assertEquals(1, NavigationMotion.tabSlideDirection(Routes.CORRECTOR, Routes.PROFILE))
        // Gauche (-1) : Profil -> Accueil, Exercices -> Apprendre
        assertEquals(-1, NavigationMotion.tabSlideDirection(Routes.PROFILE, Routes.DASHBOARD))
        assertEquals(-1, NavigationMotion.tabSlideDirection(Routes.EXERCISES, Routes.ACADEMY))
        // Même onglet ou écran secondaire (0)
        assertEquals(0, NavigationMotion.tabSlideDirection(Routes.ACADEMY, Routes.ACADEMY))
        assertEquals(0, NavigationMotion.tabSlideDirection(Routes.DASHBOARD, Routes.SETTINGS))
    }

    @Test
    fun `animated content and bottom navigation switch smoothly across all five main destinations`() {
        val destinations = listOf(
            BottomNavDestination(Routes.DASHBOARD, "Accueil", Icons.Filled.Home, Icons.Outlined.Home, "tab_home"),
            BottomNavDestination(Routes.CORRECTOR, "Corriger", Icons.Filled.Spellcheck, Icons.Outlined.Spellcheck, "tab_correct"),
            BottomNavDestination(Routes.ACADEMY, "Apprendre", Icons.Filled.School, Icons.Outlined.School, "tab_learn"),
            BottomNavDestination(Routes.EXERCISES, "Exercices", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter, "tab_exercises"),
            BottomNavDestination(Routes.PROFILE, "Profil", Icons.Filled.Person, Icons.Outlined.Person, "tab_profile")
        )
        var selectedRoute = Routes.DASHBOARD

        composeRule.setContent {
            FrancaisProTheme(darkTheme = false) {
                var current by remember { mutableStateOf(Routes.DASHBOARD) }
                Column {
                    AnimatedContent(
                        targetState = current,
                        transitionSpec = {
                            NavigationMotion.mainDestinationContentTransform(
                                fromRoute = initialState,
                                toRoute = targetState,
                                reducedMotion = false
                            )
                        },
                        label = "test_main_tabs"
                    ) { tab ->
                        Text("Screen:$tab")
                    }
                    AppBottomNavigation(
                        destinations = destinations,
                        currentRoute = current,
                        onSelect = {
                            current = it
                            selectedRoute = it
                        }
                    )
                }
            }
        }

        composeRule.onNodeWithTag("tab_home").assertIsSelected()
        composeRule.onNodeWithText("Screen:${Routes.DASHBOARD}").assertIsDisplayed()

        listOf(
            "tab_correct" to Routes.CORRECTOR,
            "tab_learn" to Routes.ACADEMY,
            "tab_exercises" to Routes.EXERCISES,
            "tab_profile" to Routes.PROFILE,
            "tab_home" to Routes.DASHBOARD
        ).forEach { (tag, expectedRoute) ->
            composeRule.onNodeWithTag(tag).performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithTag(tag).assertIsSelected()
            composeRule.onNodeWithText("Screen:$expectedRoute").assertIsDisplayed()
            assertEquals(expectedRoute, selectedRoute)
        }
    }
}
