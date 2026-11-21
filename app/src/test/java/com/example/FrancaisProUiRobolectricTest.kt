package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasTestTag
import com.example.core.database.CorrectionHistory
import com.example.core.database.DictationAttempt
import com.example.core.database.ExerciseAttempt
import com.example.core.database.LessonProgress
import com.example.core.database.RecentDictionarySearch
import com.example.core.database.UserProfile
import com.example.core.database.VocabularyProgress
import com.example.data.DictionarySearchScope
import com.example.features.DictionaryUiState
import com.example.features.academy.AcademyScreen
import com.example.features.academy.LessonDetailScreen
import com.example.features.academy.LessonListScreen
import com.example.features.correction.CorrectorScreen
import com.example.features.correction.CorrectorUiState
import com.example.features.dashboard.DashboardScreen
import com.example.features.dashboard.PerformanceOverviewScreen
import com.example.features.onboarding.OnboardingScreen
import com.example.features.practice.ExercisesScreen
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.FrancaisProDimens
import com.example.ui.theme.FrancaisProLightColorScheme
import com.example.ui.theme.FrancaisProPalette
import com.example.ui.theme.FrancaisProShapes
import com.example.ui.theme.FrancaisProTextStyles
import com.example.ui.theme.FrancaisProTheme
import com.example.ui.theme.FrancaisProThemeTokens
import com.example.ui.theme.FrancaisProTypography
import com.example.ui.theme.FrauncesFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalSpacing
import com.example.ui.theme.PlusJakartaFontFamily
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

        composeRule.onNodeWithText("Objectif du chapitre").assertExists()
        composeRule.onNodeWithText("Cours & règles essentielles").assertExists()
        composeRule.onNodeWithText("Exemples commentés").assertExists()
        composeRule.onNodeWithText("Règle d'or à retenir").assertExists()
        composeRule.onNodeWithText("Pièges fréquents à éviter").assertExists()
        composeRule.onNodeWithText("Mise en pratique immédiate").assertExists()
        composeRule.onNodeWithText("Validation du chapitre").assertExists()
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

    @Test
    fun `academy screen enforces progressive level unlocking and shows lock notice`() {
        val allLessons = AcademyLessonsSeed.lessons()
        var openedLevel: String? = null
        var openedLessonId: String? = null

        composeRule.setContent {
            FrancaisProTheme(darkTheme = false) {
                AcademyScreen(
                    lessons = allLessons,
                    progress = emptyList(),
                    onOpenLevelLessons = { openedLevel = it },
                    onOpenDictionary = {},
                    onOpenConjugation = {},
                    onOpenVocabulary = {},
                    onOpenLesson = { openedLessonId = it }
                )
            }
        }

        // CTA héro ouvre le premier chapitre débloqué de A1
        composeRule.onNodeWithTag("academy_continue_btn").performClick()
        composeRule.waitForIdle()
        assertEquals("a1_ort_01", openedLessonId)

        // Niveau A1 débloqué -> ouvre le parcours A1
        composeRule.onNodeWithTag("academy_roadmap_list")
            .performScrollToNode(hasTestTag("academy_level_A1"))
        composeRule.onNodeWithTag("academy_level_A1").performClick()
        composeRule.waitForIdle()
        assertEquals("A1", openedLevel)

        // Niveau A2 verrouillé -> n'ouvre pas A2 et affiche le bandeau explicatif
        openedLevel = null
        composeRule.onNodeWithTag("academy_roadmap_list")
            .performScrollToNode(hasTestTag("academy_level_A2"))
        composeRule.onNodeWithTag("academy_level_A2").performClick()
        composeRule.waitForIdle()
        assertEquals(null, openedLevel)
        composeRule.onNodeWithTag("academy_roadmap_list")
            .performScrollToNode(hasTestTag("academy_locked_banner"))
        composeRule.onNodeWithTag("academy_locked_banner").assertIsDisplayed()
    }

    @Test
    fun `lesson list screen locks chapter 2 until chapter 1 is completed`() {
        val allLessons = AcademyLessonsSeed.lessons()
        var openedLessonId: String? = null
        val progressState = mutableStateOf<List<LessonProgress>>(emptyList())

        composeRule.setContent {
            FrancaisProTheme(darkTheme = false) {
                LessonListScreen(
                    level = "A1",
                    lessons = allLessons,
                    progress = progressState.value,
                    onOpenLesson = { openedLessonId = it },
                    onBack = {}
                )
            }
        }

        // Chapitre 2 verrouillé tant que chapitre 1 n'est pas validé
        composeRule.onNodeWithTag("lesson_list_column")
            .performScrollToNode(hasTestTag("lesson_card_a1_gram_01"))
        composeRule.onNodeWithTag("lesson_card_a1_gram_01").performClick()
        composeRule.waitForIdle()
        assertEquals(null, openedLessonId)
        composeRule.onNodeWithTag("lesson_list_column")
            .performScrollToNode(hasTestTag("lesson_list_locked_banner"))
        composeRule.onNodeWithTag("lesson_list_locked_banner").assertIsDisplayed()

        // Une fois le chapitre 1 validé, le chapitre 2 devient accessible
        progressState.value = listOf(LessonProgress(lessonId = "a1_ort_01", isCompleted = true, quizScorePercent = 90))
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("lesson_list_column")
            .performScrollToNode(hasTestTag("lesson_card_a1_gram_01"))
        composeRule.onNodeWithTag("lesson_card_a1_gram_01").performClick()
        composeRule.waitForIdle()
        assertEquals("a1_gram_01", openedLessonId)
    }

    @Test
    fun `dictionary screen renders recent searches chips with select remove and clear actions`() {
        val entries = DictionarySeed.entries().take(3)
        val recents = listOf(
            RecentDictionarySearch("eloquence", "Éloquence", 2000L),
            RecentDictionarySearch("syntaxe", "Syntaxe", 1000L)
        )
        var selectedRecent: String? = null
        var removedKey: String? = null
        var cleared = false

        composeRule.setContent {
            FrancaisProTheme(darkTheme = false) {
                DictionaryScreen(
                    uiState = DictionaryUiState(
                        query = "",
                        results = entries,
                        recentSearches = recents,
                        categories = listOf("Tous"),
                        totalCount = 48
                    ),
                    favorites = emptyList(),
                    onQueryChange = {},
                    onCategorySelected = {},
                    onScopeSelected = {},
                    onToggleFavoritesOnly = {},
                    onClearFilters = {},
                    onOpenWord = {},
                    onSelectRecentSearch = { selectedRecent = it.queryText },
                    onRemoveRecentSearch = { removedKey = it },
                    onClearRecentSearches = { cleared = true },
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithTag("dict_recent_searches_section").assertIsDisplayed()
        composeRule.onNodeWithTag("dict_recent_search_eloquence").performClick()
        assertEquals("Éloquence", selectedRecent)

        composeRule.onNodeWithTag("dict_remove_recent_search_syntaxe").performClick()
        assertEquals("syntaxe", removedKey)

        composeRule.onNodeWithTag("dict_clear_recent_searches_btn").performClick()
        assertTrue(cleared)
    }

    @Test
    fun `dashboard displays interactive daily quests card and navigates to target route`() {
        var navigatedRoute: String? = null

        composeRule.setContent {
            FrancaisProTheme(darkTheme = false) {
                DashboardScreen(
                    profile = UserProfile(displayName = "Mendel", estimatedLevel = "A1", globalScore = 70),
                    adaptiveSummary = AdaptiveProgressionEngine.computeProfileSummary("A1", 1, emptyList()),
                    lessons = AcademyLessonsSeed.lessons().take(4),
                    lessonProgress = emptyList(),
                    exerciseAttempts = emptyList(),
                    dailyGoals = emptyList(),
                    dailyTargetMinutes = 15,
                    onNavigate = { navigatedRoute = it },
                    onStartCategoryTraining = {}
                )
            }
        }

        composeRule.onNodeWithTag("dashboard_list")
            .performScrollToNode(hasTestTag("dash_daily_quests_card"))
        composeRule.onNodeWithText("Missions du jour").assertIsDisplayed()
        composeRule.onNodeWithTag("dash_quest_lesson").performClick()
        composeRule.waitForIdle()
        assertEquals(Routes.ACADEMY, navigatedRoute)
    }

    @Test
    fun `corrector studio renders readability metrics quality ring and category filters`() {
        val engine = HybridCorrectionEngine()
        val sample = "Je vais manger a midi et les enfants joue dans la cours."
        val report = engine.analyze(sample)

        composeRule.setContent {
            FrancaisProTheme(darkTheme = false) {
                CorrectorScreen(
                    uiState = CorrectorUiState(
                        inputText = sample,
                        report = report
                    ),
                    sampleTexts = emptyList(),
                    onTextChange = {},
                    onAnalyze = {},
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

        composeRule.onNodeWithTag("corrector_list")
            .performScrollToNode(hasTestTag("corrector_readability_bar"))
        composeRule.onNodeWithTag("corrector_readability_bar").assertIsDisplayed()

        composeRule.onNodeWithTag("corrector_list")
            .performScrollToNode(hasTestTag("corrector_quality_ring"))
        composeRule.onNodeWithTag("corrector_quality_ring").assertIsDisplayed()

        composeRule.onNodeWithTag("corrector_list")
            .performScrollToNode(hasTestTag("corrector_filter_ALL"))
        composeRule.onNodeWithTag("corrector_filter_ALL").assertIsDisplayed()
    }

    @Test
    fun `exercises screen launches express 5-question challenge mode`() {
        val sampleExercises = ExercisesAndDictationsSeed.exercises().take(6)

        composeRule.setContent {
            FrancaisProTheme(darkTheme = false) {
                ExercisesScreen(
                    profile = UserProfile(displayName = "Mendel", streakDays = 3),
                    exercises = sampleExercises,
                    exerciseAttempts = emptyList(),
                    statistics = emptyList(),
                    initialCategoryFilter = null,
                    onClearInitialCategoryFilter = {},
                    onSubmitAnswer = { _, _, _ -> },
                    onOpenDictation = {},
                    onOpenSpeaking = {}
                )
            }
        }

        composeRule.onNodeWithTag("exercises_daily_challenge_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("exercises_daily_challenge_btn").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Défi Express en cours").assertIsDisplayed()
    }

    @Test
    fun `performance overview renders 30-day multi-skill chart, timeframe switch, and domain CTAs`() {
        val dayMs = 86_400_000L
        val fixedNow = 20_500L * dayMs + 12 * 3_600_000L
        var navigatedRoute: String? = null
        var grammarTrainClicked = false

        val attempts = listOf(
            ExerciseAttempt(exerciseId = "e1", category = "Grammaire", userAnswer = "a", isCorrect = false, scoreEarned = 0, attemptedAt = fixedNow - 18 * dayMs),
            ExerciseAttempt(exerciseId = "e2", category = "Grammaire", userAnswer = "b", isCorrect = true, scoreEarned = 10, attemptedAt = fixedNow - 2 * dayMs)
        )
        val vocabProgress = listOf(
            VocabularyProgress(wordId = "v1", repetitions = 2, intervalDays = 3, nextReviewAt = (fixedNow - 10 * dayMs) + 3 * dayMs, lastQuality = 4),
            VocabularyProgress(wordId = "v2", repetitions = 3, intervalDays = 6, nextReviewAt = (fixedNow - 1 * dayMs) + 6 * dayMs, lastQuality = 5, mastered = true)
        )
        val corrections = listOf(
            CorrectionHistory(
                originalText = "Nous avons préparé une synthèse claire et rigoureuse.",
                correctedText = "Nous avons préparé une synthèse claire et rigoureuse.",
                errorsFoundCount = 0,
                errorsAppliedCount = 0,
                createdAt = fixedNow
            )
        )
        val dictations = listOf(
            DictationAttempt(dictationId = "d1", userText = "texte", scoreOutOf20 = 15f, mistakesCount = 2, summaryFeedback = "Très bien", attemptedAt = fixedNow - 12 * dayMs)
        )

        composeRule.setContent {
            FrancaisProTheme(darkTheme = false) {
                PerformanceOverviewScreen(
                    lessons = AcademyLessonsSeed.lessons(),
                    lessonProgress = emptyList(),
                    exerciseAttempts = attempts,
                    vocabularyProgress = vocabProgress,
                    correctionHistory = corrections,
                    dictationAttempts = dictations,
                    onNavigate = { navigatedRoute = it },
                    onTrainGrammar = { grammarTrainClicked = true },
                    onBack = {},
                    nowMillis = fixedNow
                )
            }
        }

        composeRule.onNodeWithTag("perf_hero_card").assertIsDisplayed()
        composeRule.onNodeWithTag("perf_range_7").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Tendances sur 7 jours").assertIsDisplayed()

        composeRule.onNodeWithTag("perf_range_30").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Tendances sur 30 jours").assertIsDisplayed()

        composeRule.onNodeWithTag("performance_overview_list")
            .performScrollToNode(hasTestTag("performance_trend_chart"))
        composeRule.onNodeWithTag("performance_trend_chart").assertIsDisplayed()
        composeRule.onNodeWithTag("performance_tooltip_card").assertIsDisplayed()

        // Filtres de séries + navigation journalière
        composeRule.onNodeWithTag("perf_toggle_VOCABULARY").performClick()
        composeRule.onNodeWithTag("perf_day_prev").performClick()

        // Cartes par compétence et CTA
        composeRule.onNodeWithTag("performance_overview_list")
            .performScrollToNode(hasTestTag("perf_domain_card_GRAMMAR"))
        composeRule.onNodeWithTag("perf_train_GRAMMAR").performClick()
        assertTrue(grammarTrainClicked)

        composeRule.onNodeWithTag("performance_overview_list")
            .performScrollToNode(hasTestTag("perf_domain_card_WRITING"))
        composeRule.onNodeWithTag("perf_train_WRITING").performClick()
        assertEquals(Routes.CORRECTOR, navigatedRoute)
    }

    @Test
    fun `performance overview shows honest empty coaching state when no activity exists`() {
        var startedTraining = false

        composeRule.setContent {
            FrancaisProTheme(darkTheme = false) {
                PerformanceOverviewScreen(
                    lessons = emptyList(),
                    lessonProgress = emptyList(),
                    exerciseAttempts = emptyList(),
                    vocabularyProgress = emptyList(),
                    correctionHistory = emptyList(),
                    dictationAttempts = emptyList(),
                    onNavigate = {},
                    onTrainGrammar = { startedTraining = true },
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithTag("perf_empty_callout").assertIsDisplayed()
        composeRule.onNodeWithTag("perf_empty_start_btn").performClick()
        assertTrue(startedTraining)
    }

    @Test
    fun `design system tokens expose exact typography scale spacing grid shapes and syntax colors`() {
        assertEquals(Color(0xFF7C3AED), FrancaisProPalette.Light.syntaxViolet)
        assertEquals(Color(0xFF356AE6), FrancaisProPalette.Light.styleBlue)
        assertEquals(Color(0xFFEEF2F8), FrancaisProPalette.Light.surfaceSubtle)
        assertEquals(Color(0xFFA78BFA), FrancaisProPalette.Dark.syntaxViolet)
        assertEquals(Color(0xFF7098FF), FrancaisProPalette.Dark.styleBlue)
        assertEquals(Color(0xFF24344D), FrancaisProPalette.Dark.border)

        assertEquals(FrauncesFontFamily, FrancaisProTypography.displayLarge.fontFamily)
        assertEquals(32.sp, FrancaisProTypography.displayLarge.fontSize)
        assertEquals(FrauncesFontFamily, FrancaisProTypography.displaySmall.fontFamily)
        assertEquals(24.sp, FrancaisProTypography.displaySmall.fontSize)
        assertEquals(PlusJakartaFontFamily, FrancaisProTypography.headlineLarge.fontFamily)
        assertEquals(26.sp, FrancaisProTypography.headlineLarge.fontSize)
        assertEquals(PlusJakartaFontFamily, FrancaisProTypography.bodyLarge.fontFamily)
        assertEquals(16.sp, FrancaisProTypography.bodyLarge.fontSize)
        assertEquals(JetBrainsMonoFontFamily, FrancaisProTextStyles.MonoMetric.fontFamily)
        assertEquals(JetBrainsMonoFontFamily, FrancaisProTextStyles.MonoLarge.fontFamily)

        assertEquals(4.dp, FrancaisProDimens.Space4)
        assertEquals(8.dp, FrancaisProDimens.Space8)
        assertEquals(12.dp, FrancaisProDimens.Space12)
        assertEquals(16.dp, FrancaisProDimens.Space16)
        assertEquals(20.dp, FrancaisProDimens.Space20)
        assertEquals(24.dp, FrancaisProDimens.Space24)
        assertEquals(32.dp, FrancaisProDimens.Space32)
        assertEquals(16.dp, FrancaisProDimens.screenHorizontalPadding(360.dp))
        assertEquals(20.dp, FrancaisProDimens.screenHorizontalPadding(412.dp))
        assertEquals(1.dp, FrancaisProDimens.BorderStrokeWidth)

        var capturedSpacingOk = false
        var capturedSemanticOk = false
        composeRule.setContent {
            FrancaisProTheme(darkTheme = false) {
                val spacing = LocalSpacing.current
                val tokensSpacing = FrancaisProThemeTokens.spacing
                val tokensColors = FrancaisProThemeTokens.colors
                capturedSpacingOk = spacing == tokensSpacing &&
                    spacing.space16 == 16.dp &&
                    spacing.cardElevation == 1.dp &&
                    spacing.elevatedCardElevation == 2.dp
                capturedSemanticOk = tokensColors.syntaxColor == Color(0xFF7C3AED) &&
                    tokensColors.styleColor == Color(0xFF356AE6) &&
                    FrancaisProShapes.small != FrancaisProShapes.medium
            }
        }
        assertTrue(capturedSpacingOk)
        assertTrue(capturedSemanticOk)
    }

    @Test
    fun `dictionary card quick audio button speaks entry word`() {
        var spokenWord: String? = null
        val entries = DictionarySeed.entries().take(2)

        composeRule.setContent {
            FrancaisProTheme(darkTheme = false) {
                DictionaryScreen(
                    uiState = DictionaryUiState(
                        query = "",
                        results = entries,
                        categories = listOf("Tous"),
                        totalCount = 48
                    ),
                    favorites = emptyList(),
                    onQueryChange = {},
                    onCategorySelected = {},
                    onScopeSelected = {},
                    onToggleFavoritesOnly = {},
                    onClearFilters = {},
                    onOpenWord = {},
                    onSpeakWord = { spokenWord = it },
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithTag("dict_speak_${entries.first().id}").performClick()
        assertEquals(entries.first().word, spokenWord)
    }

    @Test
    fun `dashboard progress trends card navigates to performance overview`() {
        var navigatedRoute: String? = null

        composeRule.setContent {
            FrancaisProTheme(darkTheme = false) {
                DashboardScreen(
                    profile = UserProfile(displayName = "Mendel", estimatedLevel = "B1", globalScore = 75),
                    adaptiveSummary = AdaptiveProgressionEngine.computeProfileSummary("B1", 4, emptyList()),
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

        composeRule.onNodeWithTag("dashboard_list")
            .performScrollToNode(hasTestTag("dash_progress_trends_link"))
        composeRule.onNodeWithTag("dash_progress_trends_link").performClick()
        assertEquals(Routes.PERFORMANCE_OVERVIEW, navigatedRoute)
    }

    @Test
    fun `academy screen displays achievements showcase with filter chips and category launch`() {
        val allLessons = AcademyLessonsSeed.lessons()
        val seededAchievements = DictionarySeed.achievements().map {
            if (it.id == "ach_first_lesson") it.copy(isUnlocked = true, unlockedAt = 1234L) else it
        }
        var launchedCategory: String? = null

        composeRule.setContent {
            FrancaisProTheme(darkTheme = false) {
                AcademyScreen(
                    lessons = allLessons,
                    progress = listOf(LessonProgress(lessonId = "a1_ort_01", isCompleted = true, quizScorePercent = 90)),
                    onOpenLevelLessons = {},
                    onOpenDictionary = {},
                    onOpenConjugation = {},
                    onOpenVocabulary = {},
                    achievements = seededAchievements,
                    onStartCategoryTraining = { launchedCategory = it }
                )
            }
        }

        // Pilule compteur visible dans l'en-tête
        composeRule.onNodeWithTag("academy_badges_header_pill").assertIsDisplayed()

        // Défilement vers la section Badges & Réussites
        composeRule.onNodeWithTag("academy_roadmap_list")
            .performScrollToNode(hasTestTag("academy_achievements_section"))
        composeRule.onNodeWithText("Badges & Réussites").assertIsDisplayed()

        // Vérifier le filtre Débloqués
        composeRule.onNodeWithTag("academy_badge_filter_UNLOCKED").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("academy_roadmap_list")
            .performScrollToNode(hasTestTag("academy_badge_ach_first_lesson"))
        composeRule.onNodeWithTag("academy_badge_ach_first_lesson").assertIsDisplayed()

        // Vérifier le filtre À débloquer et le badge Gourou de la grammaire
        composeRule.onNodeWithTag("academy_badge_filter_LOCKED").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("academy_roadmap_list")
            .performScrollToNode(hasTestTag("academy_badge_ach_grammar_guru"))
        composeRule.onNodeWithTag("academy_badge_ach_grammar_guru").performClick()
        composeRule.waitForIdle()
        assertEquals("Grammaire", launchedCategory)
    }

    @Test
    fun `onboarding interactive demo and recap flow completes with custom name and daily goal`() {
        var finishedName: String? = null
        var finishedGoal: Int? = null

        composeRule.setContent {
            FrancaisProTheme(darkTheme = false) {
                OnboardingScreen(
                    onFinishOnboarding = { name, goal, _, _ ->
                        finishedName = name
                        finishedGoal = goal
                    },
                    onSkipToDashboard = {}
                )
            }
        }

        // Étape 1 -> 2
        composeRule.onNodeWithTag("onboarding_next_btn").performClick()
        composeRule.waitForIdle()

        // Étape 2 : essai interactif du correcteur hors ligne
        composeRule.onNodeWithTag("onboarding_try_corrector_btn").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("« Ils sont partis tôt. »").assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding_next_btn").performClick()

        // Étape 3 : Académie + aperçu des badges
        composeRule.onNodeWithText("Gourou de la grammaire").assertExists()
        composeRule.onNodeWithTag("onboarding_next_btn").performClick()

        // Étape 4 : Hors ligne -> Étape 5 : Évaluation
        composeRule.onNodeWithTag("onboarding_next_btn").performClick()
        composeRule.onNodeWithTag("onboarding_skip_assessment_btn").performClick()

        // Étape 6 : Profil -> saisie du prénom -> Étape 7
        composeRule.onNodeWithTag("onboarding_name_input").performScrollTo()
        composeRule.onNodeWithTag("onboarding_next_btn").performClick()

        // Étape 7 : choix de l'objectif 10 min + récapitulatif
        composeRule.onNodeWithTag("onboarding_goal_10").performScrollTo().performClick()
        composeRule.onNodeWithTag("onboarding_summary_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding_finish_btn").performClick()
        composeRule.waitForIdle()

        assertEquals("", finishedName)
        assertEquals(10, finishedGoal)
    }

    @Test
    fun `academy daily streak card displays 7-day tracker shields and claims unlocked milestone reward`() {
        val todayEpoch = System.currentTimeMillis() / 86_400_000L
        val state = com.example.core.database.StreakState(
            currentStreak = 3,
            bestStreak = 4,
            lastPracticeEpochDay = todayEpoch,
            freezeTokensAvailable = 2,
            totalBonusXpEarned = 0,
            totalAcademySessions = 5
        )
        val days = listOf(
            com.example.core.database.AcademyPracticeDay(epochDay = todayEpoch - 2, lessonsCompletedCount = 1, streakQualifies = true),
            com.example.core.database.AcademyPracticeDay(epochDay = todayEpoch - 1, streakQualifies = true, isFreezeUsed = true),
            com.example.core.database.AcademyPracticeDay(epochDay = todayEpoch, exercisesCompletedCount = 1, streakQualifies = true)
        )
        val rewards = com.example.learning.progression.AcademyStreakEngine.defaultRewards().map { reward ->
            if (reward.id == "streak_reward_3") reward.copy(isUnlocked = true, unlockedAt = 1000L) else reward
        }
        val summary = com.example.learning.progression.AcademyStreakEngine.buildSummary(state, days, rewards)
        var claimedRewardId: String? = null

        composeRule.setContent {
            FrancaisProTheme(darkTheme = false) {
                AcademyScreen(
                    lessons = AcademyLessonsSeed.lessons(),
                    progress = emptyList(),
                    onOpenLevelLessons = {},
                    onOpenDictionary = {},
                    onOpenConjugation = {},
                    onOpenVocabulary = {},
                    streakSummary = summary,
                    streakState = state,
                    onClaimStreakReward = { claimedRewardId = it }
                )
            }
        }

        composeRule.onNodeWithTag("academy_roadmap_list")
            .performScrollToNode(hasTestTag("academy_streak_card"))
        composeRule.onNodeWithTag("academy_streak_card").assertIsDisplayed()
        composeRule.onNodeWithText("3 jours de série").assertIsDisplayed()
        composeRule.onNodeWithTag("streak_7day_strip").assertIsDisplayed()
        composeRule.onNodeWithTag("streak_day_dot_6").assertIsDisplayed()
        composeRule.onNodeWithTag("streak_freeze_chip").assertIsDisplayed()

        composeRule.onNodeWithTag("streak_claim_streak_reward_3").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals("streak_reward_3", claimedRewardId)
    }
}
