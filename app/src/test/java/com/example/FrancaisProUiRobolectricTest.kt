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
import com.example.data.DictionarySearchScope
import com.example.features.DictionaryUiState
import com.example.features.academy.LessonDetailScreen
import com.example.features.correction.CorrectorScreen
import com.example.features.correction.CorrectorUiState
import com.example.features.dashboard.DashboardScreen
import com.example.features.onboarding.OnboardingScreen
import com.example.features.profile.DemoAndGuideScreen
import com.example.features.reference.DictionaryScreen
import com.example.features.reference.WordDetailScreen
import com.example.learning.content.AcademyLessonsSeed
import com.example.learning.content.DictionarySeed
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

    @Test
    fun `demo and guide screen runs live offline correction and switches to user manual`() {
        var openedInCorrector: String? = null
        var navigatedTab: String? = null

        composeRule.setContent {
            FrancaisProTheme(darkTheme = false) {
                DemoAndGuideScreen(
                    correctionEngine = HybridCorrectionEngine(),
                    onOpenInCorrector = { openedInCorrector = it },
                    onNavigateToTab = { navigatedTab = it },
                    onBack = {}
                )
            }
        }

        // Onglet Démo interactive actif par défaut
        composeRule.onNodeWithText("Essai en direct du Correcteur").assertIsDisplayed()
        composeRule.onNodeWithTag("demo_scenario_1").performClick()
        composeRule.waitForIdle()

        // Appliquer toutes les corrections sur le scénario réel (scroll dans le viewport Robolectric)
        composeRule.onNodeWithTag("demo_list")
            .performScrollToNode(hasTestTag("demo_tour_step_0"))
        composeRule.onNodeWithTag("demo_apply_all_btn").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("demo_open_in_corrector_btn").performClick()
        composeRule.waitForIdle()
        assertTrue(!openedInCorrector.isNullOrBlank())

        // Bascule vers l'onglet Guide d'utilisation
        composeRule.onNodeWithTag("guide_tab_manual").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Démarrage & Autonomie 100 % hors ligne").assertIsDisplayed()
        composeRule.onNodeWithTag("guide_manual_list")
            .performScrollToNode(hasTestTag("guide_section_1"))
        composeRule.onNodeWithTag("guide_section_1").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Maîtriser le Correcteur interactif").assertIsDisplayed()
        assertTrue(navigatedTab == null)
    }

    @Test
    fun `dictionary screen displays definitions synonyms antonyms and interactive detail chips`() {
        val entries = DictionarySeed.entries().take(3)
        var selectedScope = DictionarySearchScope.ALL
        var openedWordId: String? = null
        var clickedRelatedTerm: String? = null

        composeRule.setContent {
            var currentWordId by remember { mutableStateOf<String?>(null) }
            FrancaisProTheme(darkTheme = false) {
                if (currentWordId == null) {
                    DictionaryScreen(
                        uiState = DictionaryUiState(
                            query = "",
                            selectedCategory = "Tous",
                            selectedScope = selectedScope,
                            results = entries,
                            categories = listOf("Tous", "Nom", "Verbe"),
                            totalCount = 48
                        ),
                        favorites = emptyList(),
                        onQueryChange = {},
                        onCategorySelected = {},
                        onScopeSelected = { selectedScope = it },
                        onToggleFavoritesOnly = {},
                        onClearFilters = {},
                        onOpenWord = {
                            openedWordId = it
                            currentWordId = it
                        },
                        onBack = {}
                    )
                } else {
                    WordDetailScreen(
                        entry = entries.first { it.id == currentWordId },
                        favorites = emptyList(),
                        onToggleFavorite = {},
                        onSpeakWord = {},
                        onRelatedWordClick = { clickedRelatedTerm = it },
                        onBack = { currentWordId = null }
                    )
                }
            }
        }

        // Vérifie les filtres Synonymes / Antonymes et l'ouverture d'une entrée
        composeRule.onNodeWithTag("dict_scope_SYNONYMS").performClick()
        assertEquals(DictionarySearchScope.SYNONYMS, selectedScope)
        composeRule.onNodeWithTag("dict_item_dic_01").performClick()
        composeRule.waitForIdle()
        assertEquals("dic_01", openedWordId)

        // Vérifie l'interactivité d'un synonyme dans WordDetailScreen
        composeRule.onNodeWithTag("word_synonym_harmonie").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals("harmonie", clickedRelatedTerm)
    }

    @Test
    fun `dashboard displays word of the day card with audio and detail navigation`() {
        val sampleWord = DictionarySeed.entries().first()
        var navigatedRoute: String? = null
        var spokenWord: String? = null

        composeRule.setContent {
            FrancaisProTheme(darkTheme = false) {
                DashboardScreen(
                    profile = UserProfile(displayName = "Mendel", estimatedLevel = "B1", globalScore = 76),
                    adaptiveSummary = AdaptiveProgressionEngine.computeProfileSummary("B1", 4, emptyList()),
                    lessons = AcademyLessonsSeed.lessons().take(2),
                    lessonProgress = emptyList(),
                    exerciseAttempts = emptyList(),
                    dailyGoals = emptyList(),
                    dailyTargetMinutes = 15,
                    onNavigate = { navigatedRoute = it },
                    onStartCategoryTraining = {},
                    wordOfTheDay = sampleWord,
                    onSpeakWord = { spokenWord = it }
                )
            }
        }

        composeRule.onNodeWithTag("dashboard_list")
            .performScrollToNode(hasTestTag("dash_word_of_the_day_card"))
        composeRule.onNodeWithText("Mot du jour").assertIsDisplayed()
        composeRule.onNodeWithTag("dash_wotd_speak_btn").performClick()
        composeRule.waitForIdle()
        assertEquals(sampleWord.word, spokenWord)

        composeRule.onNodeWithTag("dash_word_of_the_day_card").performClick()
        composeRule.waitForIdle()
        assertEquals(Routes.wordDetail(sampleWord.id), navigatedRoute)
    }
}
