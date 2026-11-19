package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import com.example.core.design.AppBottomNavigation
import com.example.core.design.AppTopBar
import com.example.core.design.BottomNavDestination
import com.example.core.design.ErrorCategoryBreakdownChart
import com.example.core.design.PrimaryButton
import com.example.core.design.SecondaryButton
import com.example.core.design.WeeklyMinutesBarChart
import com.example.navigation.NavigationMotion
import com.example.navigation.Routes
import com.example.ui.theme.FrancaisProDarkColorScheme
import com.example.ui.theme.FrancaisProLightColorScheme
import com.example.ui.theme.FrancaisProPalette
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

    @Test
    fun `material3 color schemes expose exact light and dark palette tokens`() {
        // Light mode tokens
        assertEquals(Color(0xFF101B36), FrancaisProPalette.Light.primaryNavy)
        assertEquals(Color(0xFF356AE6), FrancaisProLightColorScheme.primary)
        assertEquals(Color(0xFF6B8FF0), FrancaisProPalette.Light.secondaryBlue)
        assertEquals(Color(0xFFF4F6FA), FrancaisProLightColorScheme.background)
        assertEquals(Color(0xFFFFFFFF), FrancaisProLightColorScheme.surface)
        assertEquals(Color(0xFF172033), FrancaisProLightColorScheme.onSurface)
        assertEquals(Color(0xFF788397), FrancaisProLightColorScheme.onSurfaceVariant)
        assertEquals(Color(0xFF16A36A), FrancaisProLightColorScheme.tertiary)
        assertEquals(Color(0xFFF59E0B), FrancaisProPalette.Light.warning)
        assertEquals(Color(0xFFDC4545), FrancaisProLightColorScheme.error)
        assertEquals(Color(0xFFE6EAF0), FrancaisProLightColorScheme.outline)

        // Dark mode tokens
        assertEquals(Color(0xFF0B1220), FrancaisProDarkColorScheme.background)
        assertEquals(Color(0xFF131E31), FrancaisProDarkColorScheme.surface)
        assertEquals(Color(0xFF1B2940), FrancaisProDarkColorScheme.surfaceContainer)
        assertEquals(Color(0xFFF5F7FC), FrancaisProDarkColorScheme.onSurface)
        assertEquals(Color(0xFFA6B2C5), FrancaisProDarkColorScheme.onSurfaceVariant)
        assertEquals(Color(0xFF7098FF), FrancaisProDarkColorScheme.primary)
    }

    @Test
    fun `primary button secondary button and app top bar render and handle interactions`() {
        var primaryClicked = false
        var secondaryClicked = false
        var backClicked = false

        composeRule.setContent {
            FrancaisProTheme(darkTheme = true) {
                Column {
                    AppTopBar(
                        title = "Académie",
                        subtitle = "Niveau B1",
                        onBack = { backClicked = true }
                    )
                    PrimaryButton(
                        text = "Continuer",
                        onClick = { primaryClicked = true },
                        modifier = Modifier.testTag("test_primary_btn")
                    )
                    PrimaryButton(
                        text = "Chargement",
                        onClick = {},
                        isLoading = true,
                        modifier = Modifier.testTag("test_loading_btn")
                    )
                    SecondaryButton(
                        text = "Ignorer",
                        onClick = { secondaryClicked = true },
                        modifier = Modifier.testTag("test_secondary_btn")
                    )
                }
            }
        }

        composeRule.onNodeWithText("Académie").assertIsDisplayed()
        composeRule.onNodeWithText("Niveau B1").assertIsDisplayed()
        composeRule.onNodeWithTag("top_bar_back_button").performClick()
        assertTrue(backClicked)

        composeRule.onNodeWithTag("test_primary_btn").performClick()
        assertTrue(primaryClicked)

        composeRule.onNodeWithTag("test_loading_btn").assertIsNotEnabled()

        composeRule.onNodeWithTag("test_secondary_btn").performClick()
        assertTrue(secondaryClicked)
    }

    @Test
    fun `statistics weekly bar chart and error breakdown donut support interactive inspection`() {
        val sampleDays = listOf(
            "Lu" to 10, "Ma" to 20, "Me" to 15, "Je" to 5, "Ve" to 25, "Sa" to 18, "Di" to 12
        )
        val sampleErrors = listOf(
            Triple("Orthographe", 6, Color(0xFFDC4545)),
            Triple("Grammaire", 3, Color(0xFFF59E0B)),
            Triple("Style", 1, Color(0xFF356AE6))
        )

        composeRule.setContent {
            FrancaisProTheme(darkTheme = false) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    WeeklyMinutesBarChart(days = sampleDays, targetMinutes = 15)
                    ErrorCategoryBreakdownChart(items = sampleErrors)
                }
            }
        }

        // Sélection interactive d'un jour dans le graphique hebdomadaire
        composeRule.onNodeWithTag("statistics_weekly_chart").assertIsDisplayed()
        composeRule.onNodeWithTag("weekly_day_bar_1").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Ma · 20 min").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Objectif atteint").performScrollTo().assertIsDisplayed()

        // Sélection interactive d'une catégorie dans le donut de répartition des erreurs
        composeRule.onNodeWithTag("error_donut_center_summary").performScrollTo()
        composeRule.onNodeWithText("10").assertIsDisplayed()
        composeRule.onNodeWithTag("error_category_row_0").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("error_donut_center_summary").performScrollTo()
        composeRule.onNodeWithText("60 %").assertIsDisplayed()
    }
}
