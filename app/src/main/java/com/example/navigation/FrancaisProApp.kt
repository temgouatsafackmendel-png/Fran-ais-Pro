package com.example.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import com.example.navigation.NavigationMotion.enterTransition
import com.example.navigation.NavigationMotion.exitTransition
import com.example.navigation.NavigationMotion.popEnterTransition
import com.example.navigation.NavigationMotion.popExitTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Spellcheck
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.FrancaisProApplication
import com.example.core.database.Achievement
import com.example.core.database.StreakReward
import com.example.core.design.AppBottomNavigation
import com.example.core.design.AppNavigationRail
import com.example.core.design.BottomNavDestination
import com.example.core.design.CurriculumIcons
import com.example.core.work.DailyReminderWorker
import com.example.di.AppContainer
import com.example.ui.theme.FrancaisProDimens
import com.example.ui.theme.LocalSemanticColors
import kotlinx.coroutines.delay
import com.example.features.MainAppViewModel
import com.example.features.academy.AcademyScreen
import com.example.features.academy.LessonDetailScreen
import com.example.features.academy.LessonListScreen
import com.example.features.academy.LessonQuizScreen
import com.example.features.correction.CorrectionResultScreen
import com.example.features.correction.CorrectorScreen
import com.example.features.correction.CorrectorViewModel
import com.example.features.dashboard.DashboardScreen
import com.example.features.dashboard.FavoritesScreen
import com.example.features.dashboard.HistoryScreen
import com.example.features.dashboard.PerformanceOverviewScreen
import com.example.features.dashboard.StatisticsScreen
import com.example.features.onboarding.InitialAssessmentScreen
import com.example.features.onboarding.OnboardingScreen
import com.example.features.onboarding.SplashScreen
import com.example.features.practice.DictationResultScreen
import com.example.features.practice.DictationScreen
import com.example.features.practice.ExercisesScreen
import com.example.features.practice.SpeakingScreen
import com.example.features.practice.VocabularyScreen
import com.example.features.practice.WritingWorkshopScreen
import com.example.features.profile.BackupScreen
import com.example.features.profile.DemoAndGuideScreen
import com.example.features.profile.ProfileScreen
import com.example.features.profile.SettingsScreen
import com.example.features.reference.ConjugationScreen
import com.example.features.reference.DictionaryScreen
import com.example.features.reference.WordDetailScreen
import com.example.ui.theme.FrancaisProTheme
import com.example.ui.theme.LocalReducedMotion

private val mainTabs = listOf(
    BottomNavDestination(Routes.DASHBOARD, "Accueil", Icons.Filled.Home, Icons.Outlined.Home, "tab_home"),
    BottomNavDestination(Routes.CORRECTOR, "Corriger", Icons.Filled.Spellcheck, Icons.Outlined.Spellcheck, "tab_correct"),
    BottomNavDestination(Routes.ACADEMY, "Apprendre", Icons.Filled.School, Icons.Outlined.School, "tab_learn"),
    BottomNavDestination(Routes.EXERCISES, "Exercices", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter, "tab_exercises"),
    BottomNavDestination(Routes.PROFILE, "Profil", Icons.Filled.Person, Icons.Outlined.Person, "tab_profile")
)

@Composable
fun FrancaisProApp(
    container: AppContainer,
    pendingNotificationRoute: String? = null,
    onPendingRouteConsumed: () -> Unit = {}
) {
    val mainViewModel: MainAppViewModel = viewModel(
        factory = MainAppViewModel.Factory(container.repository, container.speechManager)
    )
    val correctorViewModel: CorrectorViewModel = viewModel(
        factory = CorrectorViewModel.Factory(container.repository)
    )

    val prefs by mainViewModel.preferences.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val isDark = when (prefs.themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemDark
    }

    FrancaisProTheme(darkTheme = isDark) {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        val reducedMotion = LocalReducedMotion.current
        val barEnterMs = if (reducedMotion) 0 else NavigationMotion.TAB_BAR_ENTER_DURATION_MS
        val barExitMs = if (reducedMotion) 0 else NavigationMotion.TAB_BAR_EXIT_DURATION_MS

        val profile by mainViewModel.userProfile.collectAsStateWithLifecycle()
        val adaptiveSummary by mainViewModel.adaptiveSummary.collectAsStateWithLifecycle()
        val lessons by mainViewModel.lessons.collectAsStateWithLifecycle()
        val lessonProgress by mainViewModel.lessonProgress.collectAsStateWithLifecycle()
        val exercises by mainViewModel.exercises.collectAsStateWithLifecycle()
        val exerciseAttempts by mainViewModel.exerciseAttempts.collectAsStateWithLifecycle()
        val dictEntries by mainViewModel.dictionaryEntries.collectAsStateWithLifecycle()
        val dictionaryUiState by mainViewModel.dictionaryUiState.collectAsStateWithLifecycle()
        val verbs by mainViewModel.verbs.collectAsStateWithLifecycle()
        val selectedVerbTable by mainViewModel.selectedVerbTable.collectAsStateWithLifecycle()
        val identifiedForms by mainViewModel.identifiedForms.collectAsStateWithLifecycle()
        val correctionHistory by mainViewModel.correctionHistory.collectAsStateWithLifecycle()
        val vocabWords by mainViewModel.vocabularyWords.collectAsStateWithLifecycle()
        val vocabProgress by mainViewModel.vocabularyProgress.collectAsStateWithLifecycle()
        val dictations by mainViewModel.dictations.collectAsStateWithLifecycle()
        val dictationAttempts by mainViewModel.dictationAttempts.collectAsStateWithLifecycle()
        val lastDictationResult by mainViewModel.lastDictationResult.collectAsStateWithLifecycle()
        val dailyGoals by mainViewModel.dailyGoals.collectAsStateWithLifecycle()
        val achievements by mainViewModel.achievements.collectAsStateWithLifecycle()
        val statistics by mainViewModel.statistics.collectAsStateWithLifecycle()
        val favorites by mainViewModel.favorites.collectAsStateWithLifecycle()
        val backupStatus by mainViewModel.backupStatus.collectAsStateWithLifecycle()
        val exerciseCategoryFilter by mainViewModel.exerciseCategoryFilter.collectAsStateWithLifecycle()
        val streakState by mainViewModel.streakState.collectAsStateWithLifecycle()
        val academyStreakSummary by mainViewModel.academyStreakSummary.collectAsStateWithLifecycle()

        val correctorState by correctorViewModel.uiState.collectAsStateWithLifecycle()
        val speechCaps by container.speechManager.capabilities.collectAsStateWithLifecycle()
        val isListening by container.speechManager.isListening.collectAsStateWithLifecycle()
        val recognizedText by container.speechManager.recognizedText.collectAsStateWithLifecycle()
        val sttError by container.speechManager.sttError.collectAsStateWithLifecycle()

        var selectedMainTab by rememberSaveable { mutableStateOf(Routes.DASHBOARD) }
        val showPrimaryNav = currentRoute == Routes.MAIN_TABS
        val activeNavRoute = if (showPrimaryNav) selectedMainTab else currentRoute

        val context = LocalContext.current
        val appInstance = context.applicationContext as? FrancaisProApplication
        val isSeedReady by (appInstance?.isSeedReady?.collectAsStateWithLifecycle()
            ?: remember { mutableStateOf(true) })

        var unlockedBadgeBanner by remember { mutableStateOf<Achievement?>(null) }
        var unlockedStreakRewardBanner by remember { mutableStateOf<StreakReward?>(null) }
        LaunchedEffect(mainViewModel) {
            mainViewModel.unlockedAchievementEvents.collect { badge ->
                unlockedBadgeBanner = badge
            }
        }
        LaunchedEffect(mainViewModel) {
            mainViewModel.unlockedStreakRewardEvents.collect { reward ->
                unlockedStreakRewardBanner = reward
            }
        }
        LaunchedEffect(unlockedBadgeBanner) {
            if (unlockedBadgeBanner != null) {
                delay(4500L)
                unlockedBadgeBanner = null
            }
        }
        LaunchedEffect(unlockedStreakRewardBanner) {
            if (unlockedStreakRewardBanner != null) {
                delay(4500L)
                unlockedStreakRewardBanner = null
            }
        }

        fun navigateToTab(route: String) {
            val targetTab = if (route in Routes.topLevelTabs) route else Routes.DASHBOARD
            if (currentRoute == Routes.MAIN_TABS && selectedMainTab == targetTab) return
            selectedMainTab = targetTab
            if (currentRoute != Routes.MAIN_TABS) {
                val popped = navController.popBackStack(Routes.MAIN_TABS, inclusive = false)
                if (!popped) {
                    navController.navigate(Routes.MAIN_TABS) {
                        launchSingleTop = true
                    }
                }
            }
        }

        // Maintient WorkManager synchronisé avec les préférences locales (onboarding, paramètres, restauration JSON)
        LaunchedEffect(isSeedReady, prefs.dailyReminderEnabled, prefs.reminderHour) {
            if (isSeedReady) {
                DailyReminderWorker.schedule(
                    context = context,
                    enabled = prefs.dailyReminderEnabled,
                    reminderHour = prefs.reminderHour
                )
            }
        }

        // Ouvre directement l'Académie (ou la cible) lorsqu'une notification locale de rappel est touchée
        LaunchedEffect(pendingNotificationRoute, currentRoute, profile?.onboardingCompleted) {
            val target = pendingNotificationRoute ?: return@LaunchedEffect
            if (currentRoute == Routes.SPLASH || currentRoute == Routes.ONBOARDING) return@LaunchedEffect
            if (target in Routes.topLevelTabs) {
                navigateToTab(target)
            } else {
                navController.navigate(target) { launchSingleTop = true }
            }
            onPendingRouteConsumed()
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 600.dp

            Row(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = showPrimaryNav && useNavigationRail,
                    enter = fadeIn(tween(barEnterMs, easing = FastOutSlowInEasing)) +
                        slideInHorizontally(tween(barEnterMs, easing = FastOutSlowInEasing)) { -it / 2 },
                    exit = fadeOut(tween(barExitMs, easing = FastOutSlowInEasing)) +
                        slideOutHorizontally(tween(barExitMs, easing = FastOutSlowInEasing)) { -it / 2 }
                ) {
                    AppNavigationRail(
                        destinations = mainTabs,
                        currentRoute = activeNavRoute,
                        onSelect = ::navigateToTab
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        AnimatedVisibility(
                            visible = showPrimaryNav && !useNavigationRail,
                            enter = fadeIn(tween(barEnterMs, easing = FastOutSlowInEasing)) +
                                slideInVertically(tween(barEnterMs, easing = FastOutSlowInEasing)) { it / 2 },
                            exit = fadeOut(tween(barExitMs, easing = FastOutSlowInEasing)) +
                                slideOutVertically(tween(barExitMs, easing = FastOutSlowInEasing)) { it / 2 }
                        ) {
                            AppBottomNavigation(
                                destinations = mainTabs,
                                currentRoute = activeNavRoute,
                                onSelect = ::navigateToTab
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Routes.SPLASH,
                        enterTransition = { enterTransition(reducedMotion) },
                        exitTransition = { exitTransition(reducedMotion) },
                        popEnterTransition = { popEnterTransition(reducedMotion) },
                        popExitTransition = { popExitTransition(reducedMotion) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // 1. Splash
                        composable(Routes.SPLASH) {
                            SplashScreen(
                                profile = profile,
                                isSeedReady = isSeedReady,
                                onNavigateToOnboarding = {
                                    navController.navigate(Routes.ONBOARDING) {
                                        popUpTo(Routes.SPLASH) { inclusive = true }
                                    }
                                },
                                onNavigateToDashboard = {
                                    selectedMainTab = Routes.DASHBOARD
                                    navController.navigate(Routes.MAIN_TABS) {
                                        popUpTo(Routes.SPLASH) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 2. Onboarding 7 étapes (ignorable)
                        composable(Routes.ONBOARDING) {
                            OnboardingScreen(
                                onFinishOnboarding = { name, goalMins, correct, total ->
                                    mainViewModel.completeOnboardingWithGoal(name, goalMins, correct, total)
                                    selectedMainTab = Routes.DASHBOARD
                                    navController.navigate(Routes.MAIN_TABS) {
                                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                                    }
                                },
                                onSkipToDashboard = { name ->
                                    mainViewModel.completeOnboarding(name)
                                    selectedMainTab = Routes.DASHBOARD
                                    navController.navigate(Routes.MAIN_TABS) {
                                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                                    }
                                },
                                onReminderOptIn = { enabled, hour ->
                                    mainViewModel.setDailyReminder(enabled, hour)
                                    DailyReminderWorker.schedule(context, enabled, hour)
                                }
                            )
                        }

                        // 3. Évaluation initiale autonome
                        composable(Routes.ASSESSMENT) {
                            InitialAssessmentScreen(
                                onFinished = { correct, total ->
                                    mainViewModel.completePlacement(correct, total)
                                    selectedMainTab = Routes.DASHBOARD
                                    navController.navigate(Routes.MAIN_TABS) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 4. Conteneur animé des 5 destinations principales (AnimatedContent · 200 ms)
                        composable(Routes.MAIN_TABS) {
                            BackHandler(enabled = selectedMainTab != Routes.DASHBOARD) {
                                selectedMainTab = Routes.DASHBOARD
                            }

                            AnimatedContent(
                                targetState = selectedMainTab,
                                transitionSpec = {
                                    NavigationMotion.mainDestinationContentTransform(
                                        fromRoute = initialState,
                                        toRoute = targetState,
                                        reducedMotion = reducedMotion
                                    )
                                },
                                label = "main_destinations_animated_content",
                                modifier = Modifier.fillMaxSize()
                            ) { activeTab ->
                                when (activeTab) {
                                    Routes.DASHBOARD -> {
                                        val wordOfTheDay = remember(dictEntries) {
                                            if (dictEntries.isEmpty()) null
                                            else {
                                                val epochDay = System.currentTimeMillis() / 86_400_000L
                                                dictEntries[(epochDay % dictEntries.size).toInt()]
                                            }
                                        }
                                        DashboardScreen(
                                            profile = profile,
                                            adaptiveSummary = adaptiveSummary,
                                            lessons = lessons,
                                            lessonProgress = lessonProgress,
                                            exerciseAttempts = exerciseAttempts,
                                            dailyGoals = dailyGoals,
                                            dailyTargetMinutes = prefs.dailyGoalMinutes,
                                            onNavigate = { target ->
                                                if (target.startsWith("word_detail/")) {
                                                    mainViewModel.markDictionaryWordViewed(target.removePrefix("word_detail/"))
                                                }
                                                if (target in Routes.topLevelTabs) navigateToTab(target)
                                                else navController.navigate(target)
                                            },
                                            onStartCategoryTraining = { category ->
                                                mainViewModel.setExerciseCategoryFilter(category)
                                                navigateToTab(Routes.EXERCISES)
                                            },
                                            wordOfTheDay = wordOfTheDay,
                                            onSpeakWord = { w -> container.speechManager.speak(w, prefs.ttsSpeechRate) }
                                        )
                                    }

                                    Routes.CORRECTOR -> CorrectorScreen(
                                        uiState = correctorState,
                                        sampleTexts = correctorViewModel.sampleTexts,
                                        onTextChange = correctorViewModel::updateText,
                                        onAnalyze = { correctorViewModel.analyzeCurrentText() },
                                        onOpenResult = { navController.navigate(Routes.CORRECTION_RESULT) },
                                        onLoadSample = { sample ->
                                            correctorViewModel.loadSample(sample, autoAnalyze = prefs.autoAnalyze)
                                        },
                                        onFixAll = correctorViewModel::applyAllSafeCorrections,
                                        onAcceptIssue = correctorViewModel::acceptIssue,
                                        onDismissIssue = correctorViewModel::dismissIssue,
                                        onUndo = correctorViewModel::undo,
                                        onRedo = correctorViewModel::redo,
                                        onClear = correctorViewModel::clearAll,
                                        onOpenHistory = { navController.navigate(Routes.HISTORY) },
                                        onOpenWritingWorkshop = { navController.navigate(Routes.WRITING) }
                                    )

                                    Routes.ACADEMY -> AcademyScreen(
                                        lessons = lessons,
                                        progress = lessonProgress,
                                        onOpenLevelLessons = { lvl -> navController.navigate(Routes.lessonList(lvl)) },
                                        onOpenDictionary = { navController.navigate(Routes.DICTIONARY) },
                                        onOpenConjugation = { navController.navigate(Routes.CONJUGATION) },
                                        onOpenVocabulary = { navController.navigate(Routes.VOCABULARY) },
                                        onOpenLesson = { id -> navController.navigate(Routes.lessonDetail(id)) },
                                        achievements = achievements,
                                        exerciseAttempts = exerciseAttempts,
                                        vocabularyProgress = vocabProgress,
                                        correctionHistory = correctionHistory,
                                        dictationAttempts = dictationAttempts,
                                        profile = profile,
                                        streakSummary = academyStreakSummary,
                                        streakState = streakState,
                                        onClaimStreakReward = mainViewModel::claimStreakReward,
                                        onStartCategoryTraining = { category ->
                                            mainViewModel.setExerciseCategoryFilter(category)
                                            navigateToTab(Routes.EXERCISES)
                                        },
                                        onNavigateRoute = { target ->
                                            if (target in Routes.topLevelTabs) navigateToTab(target)
                                            else navController.navigate(target)
                                        }
                                    )

                                    Routes.EXERCISES -> ExercisesScreen(
                                        profile = profile,
                                        exercises = exercises,
                                        exerciseAttempts = exerciseAttempts,
                                        statistics = statistics,
                                        initialCategoryFilter = exerciseCategoryFilter,
                                        onClearInitialCategoryFilter = { mainViewModel.setExerciseCategoryFilter(null) },
                                        onSubmitAnswer = mainViewModel::submitExercise,
                                        onOpenDictation = { navController.navigate(Routes.DICTATION) },
                                        onOpenSpeaking = { navController.navigate(Routes.SPEAKING) }
                                    )

                                    else -> ProfileScreen(
                                        profile = profile,
                                        adaptiveSummary = adaptiveSummary,
                                        achievements = achievements,
                                        onUpdateName = mainViewModel::updateDisplayName,
                                        onNavigate = { target ->
                                            if (target in Routes.topLevelTabs) navigateToTab(target)
                                            else navController.navigate(target)
                                        }
                                    )
                                }
                            }
                        }

                        // 6. Résultat de correction
                        composable(Routes.CORRECTION_RESULT) {
                            CorrectionResultScreen(
                                uiState = correctorState,
                                onAcceptIssue = correctorViewModel::acceptIssue,
                                onDismissIssue = correctorViewModel::dismissIssue,
                                onFixAll = correctorViewModel::applyAllSafeCorrections,
                                onToggleCompare = correctorViewModel::toggleBeforeAfter,
                                onUndo = correctorViewModel::undo,
                                onRedo = correctorViewModel::redo,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 8. Liste des cours
                        composable(
                            route = Routes.LESSON_LIST,
                            arguments = listOf(navArgument("level") { type = NavType.StringType })
                        ) { entry ->
                            val lvl = entry.arguments?.getString("level") ?: "A1"
                            LessonListScreen(
                                level = lvl,
                                lessons = lessons,
                                progress = lessonProgress,
                                onOpenLesson = { id -> navController.navigate(Routes.lessonDetail(id)) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 9. Détail d'une leçon (8 sections + mini-exercice + navigation Précédente/Suivante)
                        composable(
                            route = Routes.LESSON_DETAIL,
                            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
                        ) { entry ->
                            val lessonId = entry.arguments?.getString("lessonId") ?: ""
                            val lesson = lessons.firstOrNull { it.id == lessonId }
                            val lp = lessonProgress.firstOrNull { it.lessonId == lessonId }
                            LessonDetailScreen(
                                lesson = lesson,
                                allLessons = lessons,
                                allExercises = exercises,
                                progress = lp,
                                favorites = favorites,
                                onToggleFavorite = { l ->
                                    mainViewModel.toggleFavorite("LESSON", l.id, l.title, "${l.level} · ${l.theme}")
                                },
                                onOpenLesson = { nextId ->
                                    navController.navigate(Routes.lessonDetail(nextId)) {
                                        popUpTo(Routes.LESSON_DETAIL) { inclusive = true }
                                    }
                                },
                                onStartQuiz = { id -> navController.navigate(Routes.lessonQuiz(id)) },
                                onMarkCompleted = { id, pct -> mainViewModel.completeLesson(id, pct) },
                                onBack = { navController.popBackStack() },
                                allProgress = lessonProgress
                            )
                        }

                        // 10. Quiz
                        composable(
                            route = Routes.LESSON_QUIZ,
                            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
                        ) { entry ->
                            val lessonId = entry.arguments?.getString("lessonId") ?: ""
                            val lesson = lessons.firstOrNull { it.id == lessonId }
                            LessonQuizScreen(
                                lesson = lesson,
                                allExercises = exercises,
                                onQuizCompleted = { id, pct -> mainViewModel.completeLesson(id, pct) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 11. Dictionnaire (Room SQLite 100 % hors ligne)
                        composable(Routes.DICTIONARY) {
                            DictionaryScreen(
                                uiState = dictionaryUiState,
                                favorites = favorites,
                                onQueryChange = mainViewModel::updateDictionaryQuery,
                                onCategorySelected = mainViewModel::selectDictionaryCategory,
                                onScopeSelected = mainViewModel::selectDictionaryScope,
                                onToggleFavoritesOnly = mainViewModel::toggleDictionaryFavoritesOnly,
                                onClearFilters = mainViewModel::clearDictionaryFilters,
                                onOpenWord = { id ->
                                    if (dictionaryUiState.query.isNotBlank()) {
                                        mainViewModel.recordDictionarySearch(dictionaryUiState.query)
                                    }
                                    mainViewModel.markDictionaryWordViewed(id)
                                    navController.navigate(Routes.wordDetail(id))
                                },
                                onSpeakWord = { wordText ->
                                    container.speechManager.speak(wordText, prefs.ttsSpeechRate)
                                },
                                onSearchSubmit = mainViewModel::recordDictionarySearch,
                                onSelectRecentSearch = mainViewModel::selectRecentDictionarySearch,
                                onRemoveRecentSearch = mainViewModel::removeRecentDictionarySearch,
                                onClearRecentSearches = mainViewModel::clearRecentDictionarySearches,
                                onAddCustomWord = { word, pos, def, syn, ant, ex, lvl ->
                                    mainViewModel.addCustomDictionaryWord(
                                        word = word,
                                        partOfSpeech = pos,
                                        definition = def,
                                        synonymsCsv = syn,
                                        antonymsCsv = ant,
                                        example = ex,
                                        level = lvl
                                    ) { newId ->
                                        navController.navigate(Routes.wordDetail(newId))
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 12. Fiche de mot (observée réactivement via Room)
                        composable(
                            route = Routes.WORD_DETAIL,
                            arguments = listOf(navArgument("wordId") { type = NavType.StringType })
                        ) { entry ->
                            val wordId = entry.arguments?.getString("wordId") ?: ""
                            val initialWord = remember(wordId, dictEntries) {
                                dictEntries.firstOrNull { it.id == wordId }
                            }
                            val wordFlow = remember(wordId) { mainViewModel.observeDictionaryEntry(wordId) }
                            val word by wordFlow.collectAsStateWithLifecycle(initialValue = initialWord)
                            WordDetailScreen(
                                entry = word,
                                favorites = favorites,
                                onToggleFavorite = { w ->
                                    mainViewModel.toggleFavorite("WORD", w.id, w.word, w.definition)
                                },
                                onSpeakWord = { w -> container.speechManager.speak(w, prefs.ttsSpeechRate) },
                                onRelatedWordClick = { term ->
                                    mainViewModel.resolveRelatedDictionaryWord(
                                        term = term,
                                        onFoundEntryId = { foundId ->
                                            navController.navigate(Routes.wordDetail(foundId))
                                        },
                                        onFallbackSearch = {
                                            val popped = navController.popBackStack(Routes.DICTIONARY, inclusive = false)
                                            if (!popped) navController.navigate(Routes.DICTIONARY)
                                        }
                                    )
                                },
                                onDeleteCustomEntry = { id ->
                                    mainViewModel.deleteCustomDictionaryWord(id) {
                                        navController.popBackStack()
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 13. Conjugaison
                        composable(Routes.CONJUGATION) {
                            ConjugationScreen(
                                verbs = verbs,
                                selectedTable = selectedVerbTable,
                                identifiedForms = identifiedForms,
                                favorites = favorites,
                                onSelectVerb = mainViewModel::selectVerb,
                                onIdentifyForm = mainViewModel::identifyConjugatedForm,
                                onToggleFavoriteVerb = { tbl ->
                                    mainViewModel.toggleFavorite("VERB", tbl.infinitive, tbl.infinitive, "${tbl.groupNumber}e groupe · aux. ${tbl.auxiliary}")
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 14. Dictée
                        composable(Routes.DICTATION) {
                            DictationScreen(
                                dictations = dictations,
                                capabilities = speechCaps,
                                speechRate = prefs.ttsSpeechRate,
                                onSpeak = container.speechManager::speak,
                                onStopSpeak = container.speechManager::stopSpeaking,
                                onEvaluateDictation = { d, userText ->
                                    mainViewModel.submitDictation(d, userText) {
                                        navController.navigate(Routes.DICTATION_RESULT)
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 15. Résultat de dictée
                        composable(Routes.DICTATION_RESULT) {
                            DictationResultScreen(
                                result = lastDictationResult,
                                onRetry = { navController.popBackStack() },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 16. Vocabulaire
                        composable(Routes.VOCABULARY) {
                            VocabularyScreen(
                                words = vocabWords,
                                progress = vocabProgress,
                                onSpeakWord = { w -> container.speechManager.speak(w, prefs.ttsSpeechRate) },
                                onReviewQuality = mainViewModel::reviewVocabulary,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 17. Atelier de rédaction
                        composable(Routes.WRITING) {
                            WritingWorkshopScreen(
                                correctionEngine = container.correctionEngine,
                                onSaveSession = mainViewModel::saveWritingSession,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 18. Expression orale
                        composable(Routes.SPEAKING) {
                            SpeakingScreen(
                                capabilities = speechCaps,
                                isListening = isListening,
                                recognizedText = recognizedText,
                                sttError = sttError,
                                speechRate = prefs.ttsSpeechRate,
                                onSpeakSentence = container.speechManager::speak,
                                onStartListening = container.speechManager::startOfflineListening,
                                onStopListening = container.speechManager::stopListening,
                                onManualTranscriptChange = container.speechManager::setManualTranscript,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 19. Statistiques (4 graphiques réels)
                        composable(Routes.STATISTICS) {
                            StatisticsScreen(
                                adaptiveSummary = adaptiveSummary,
                                statistics = statistics,
                                dailyGoals = dailyGoals,
                                dailyTargetMinutes = prefs.dailyGoalMinutes,
                                exerciseAttempts = exerciseAttempts,
                                dictationAttempts = dictationAttempts,
                                achievements = achievements,
                                onOpenPerformanceOverview = {
                                    navController.navigate(Routes.PERFORMANCE_OVERVIEW)
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 19b. Aperçu des performances (tendances 30 jours Grammaire, Vocabulaire, Écriture)
                        composable(Routes.PERFORMANCE_OVERVIEW) {
                            PerformanceOverviewScreen(
                                lessons = lessons,
                                lessonProgress = lessonProgress,
                                exerciseAttempts = exerciseAttempts,
                                vocabularyProgress = vocabProgress,
                                correctionHistory = correctionHistory,
                                dictationAttempts = dictationAttempts,
                                onNavigate = { target ->
                                    if (target in Routes.topLevelTabs) navigateToTab(target)
                                    else navController.navigate(target)
                                },
                                onTrainGrammar = {
                                    mainViewModel.setExerciseCategoryFilter("Grammaire")
                                    navigateToTab(Routes.EXERCISES)
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 20. Historique
                        composable(Routes.HISTORY) {
                            HistoryScreen(
                                correctionHistory = correctionHistory,
                                dictationAttempts = dictationAttempts,
                                onDeleteCorrection = mainViewModel::deleteHistoryItem,
                                onLoadInCorrector = { text ->
                                    correctorViewModel.loadSample(text)
                                    navigateToTab(Routes.CORRECTOR)
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 21. Favoris
                        composable(Routes.FAVORITES) {
                            FavoritesScreen(
                                favorites = favorites,
                                onOpenFavorite = { fav ->
                                    when (fav.itemType) {
                                        "WORD" -> navController.navigate(Routes.wordDetail(fav.itemId))
                                        "LESSON" -> navController.navigate(Routes.lessonDetail(fav.itemId))
                                        "VERB" -> {
                                            mainViewModel.selectVerb(fav.itemId)
                                            navController.navigate(Routes.CONJUGATION)
                                        }
                                    }
                                },
                                onRemoveFavorite = { fav ->
                                    mainViewModel.toggleFavorite(fav.itemType, fav.itemId, fav.title, fav.subtitle)
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 23. Paramètres
                        composable(Routes.SETTINGS) {
                            SettingsScreen(
                                prefs = prefs,
                                onThemeModeChange = mainViewModel::setThemeMode,
                                onDailyGoalChange = mainViewModel::setDailyGoalMinutes,
                                onSpeechRateChange = mainViewModel::setSpeechRate,
                                onAutoAnalyzeChange = mainViewModel::setAutoAnalyze,
                                onDailyReminderChange = mainViewModel::setDailyReminder,
                                practicedToday = academyStreakSummary.practicedToday,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 24. Sauvegarde
                        composable(Routes.BACKUP) {
                            BackupScreen(
                                includeHistory = prefs.includeHistoryInBackup,
                                backupStatus = backupStatus,
                                onToggleIncludeHistory = mainViewModel::setIncludeHistoryInBackup,
                                onPrepareExport = mainViewModel::prepareBackupExport,
                                onImportJson = mainViewModel::importBackupJson,
                                onBackupFeedback = mainViewModel::reportBackupStatus,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 25. Démo interactive & Guide d'utilisation
                        composable(Routes.USER_GUIDE) {
                            DemoAndGuideScreen(
                                correctionEngine = container.correctionEngine,
                                onOpenInCorrector = { text ->
                                    correctorViewModel.loadSample(text)
                                    navigateToTab(Routes.CORRECTOR)
                                },
                                onNavigateToTab = { tabRoute ->
                                    navigateToTab(tabRoute)
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }

            // Bannières flottantes non-bloquantes (badges & récompenses de série)
            val semantic = LocalSemanticColors.current
            val bannerEnterMs = if (reducedMotion) 0 else 220
            val bannerExitMs = if (reducedMotion) 0 else 200
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                AnimatedVisibility(
                    visible = unlockedStreakRewardBanner != null,
                    enter = fadeIn(tween(bannerEnterMs)) + slideInVertically(tween(bannerEnterMs)) { -it },
                    exit = fadeOut(tween(bannerExitMs)) + slideOutVertically(tween(bannerExitMs)) { -it }
                ) {
                    unlockedStreakRewardBanner?.let { reward ->
                        Surface(
                            shape = RoundedCornerShape(FrancaisProDimens.RadiusCard),
                            color = semantic.navyHeader,
                            border = BorderStroke(1.dp, semantic.success.copy(alpha = 0.65f)),
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("streak_reward_unlocked_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = semantic.successContainer,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.LocalFireDepartment,
                                            contentDescription = reward.title,
                                            tint = semantic.success,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Palier de série débloqué !",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = semantic.success,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${reward.title} · +${reward.bonusXp} XP · +${reward.freezeTokensGranted} bouclier",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = semantic.onNavyHeader,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        unlockedStreakRewardBanner = null
                                        navigateToTab(Routes.ACADEMY)
                                    }
                                ) {
                                    Text(
                                        "Réclamer",
                                        color = semantic.success,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(
                                    onClick = { unlockedStreakRewardBanner = null },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Fermer l'annonce de série",
                                        tint = semantic.onNavyHeader,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = unlockedBadgeBanner != null,
                    enter = fadeIn(tween(bannerEnterMs)) + slideInVertically(tween(bannerEnterMs)) { -it },
                    exit = fadeOut(tween(bannerExitMs)) + slideOutVertically(tween(bannerExitMs)) { -it }
                ) {
                    unlockedBadgeBanner?.let { badge ->
                        Surface(
                            shape = RoundedCornerShape(FrancaisProDimens.RadiusCard),
                            color = semantic.navyHeader,
                            border = BorderStroke(1.dp, semantic.warning.copy(alpha = 0.6f)),
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("achievement_unlocked_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = semantic.warningContainer,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = CurriculumIcons.forAchievement(badge.iconName),
                                            contentDescription = badge.title,
                                            tint = semantic.warning,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Nouveau badge débloqué !",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = semantic.warning,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = badge.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = semantic.onNavyHeader,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        unlockedBadgeBanner = null
                                        navigateToTab(Routes.ACADEMY)
                                    }
                                ) {
                                    Text(
                                        "Académie",
                                        color = semantic.warning,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(
                                    onClick = { unlockedBadgeBanner = null },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Fermer l'annonce de badge",
                                        tint = semantic.onNavyHeader,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
