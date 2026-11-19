package com.example.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core.design.AppBottomNavigation
import com.example.core.design.AppNavigationRail
import com.example.core.design.BottomNavDestination
import com.example.di.AppContainer
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
import com.example.features.profile.ProfileScreen
import com.example.features.profile.SettingsScreen
import com.example.features.reference.ConjugationScreen
import com.example.features.reference.DictionaryScreen
import com.example.features.reference.WordDetailScreen
import com.example.ui.theme.FrancaisProTheme
import com.example.ui.theme.LocalReducedMotion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val mainTabs = listOf(
    BottomNavDestination(Routes.DASHBOARD, "Accueil", Icons.Filled.Home, Icons.Outlined.Home, "tab_home"),
    BottomNavDestination(Routes.CORRECTOR, "Corriger", Icons.Filled.Spellcheck, Icons.Outlined.Spellcheck, "tab_correct"),
    BottomNavDestination(Routes.ACADEMY, "Apprendre", Icons.Filled.School, Icons.Outlined.School, "tab_learn"),
    BottomNavDestination(Routes.EXERCISES, "Exercices", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter, "tab_exercises"),
    BottomNavDestination(Routes.PROFILE, "Profil", Icons.Filled.Person, Icons.Outlined.Person, "tab_profile")
)

@Composable
fun FrancaisProApp(container: AppContainer) {
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
        val navDuration = if (reducedMotion) 0 else 190

        val profile by mainViewModel.userProfile.collectAsStateWithLifecycle()
        val adaptiveSummary by mainViewModel.adaptiveSummary.collectAsStateWithLifecycle()
        val lessons by mainViewModel.lessons.collectAsStateWithLifecycle()
        val lessonProgress by mainViewModel.lessonProgress.collectAsStateWithLifecycle()
        val exercises by mainViewModel.exercises.collectAsStateWithLifecycle()
        val exerciseAttempts by mainViewModel.exerciseAttempts.collectAsStateWithLifecycle()
        val dictEntries by mainViewModel.dictionaryEntries.collectAsStateWithLifecycle()
        val recentDictEntries by mainViewModel.recentDictionaryEntries.collectAsStateWithLifecycle()
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

        val correctorState by correctorViewModel.uiState.collectAsStateWithLifecycle()
        val speechCaps by container.speechManager.capabilities.collectAsStateWithLifecycle()
        val isListening by container.speechManager.isListening.collectAsStateWithLifecycle()
        val recognizedText by container.speechManager.recognizedText.collectAsStateWithLifecycle()
        val sttError by container.speechManager.sttError.collectAsStateWithLifecycle()

        val showPrimaryNav = currentRoute in Routes.topLevelTabs

        fun navigateToTab(route: String) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 600.dp

            Row(modifier = Modifier.fillMaxSize()) {
                if (showPrimaryNav && useNavigationRail) {
                    AppNavigationRail(
                        destinations = mainTabs,
                        currentRoute = currentRoute,
                        onSelect = ::navigateToTab
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        if (showPrimaryNav && !useNavigationRail) {
                            AppBottomNavigation(
                                destinations = mainTabs,
                                currentRoute = currentRoute,
                                onSelect = ::navigateToTab
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Routes.SPLASH,
                        enterTransition = { fadeIn(animationSpec = tween(navDuration)) },
                        exitTransition = { fadeOut(animationSpec = tween(navDuration)) },
                        popEnterTransition = { fadeIn(animationSpec = tween(navDuration)) },
                        popExitTransition = { fadeOut(animationSpec = tween(navDuration)) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // 1. Splash
                        composable(Routes.SPLASH) {
                            SplashScreen(
                                profile = profile,
                                onNavigateToOnboarding = {
                                    navController.navigate(Routes.ONBOARDING) {
                                        popUpTo(Routes.SPLASH) { inclusive = true }
                                    }
                                },
                                onNavigateToDashboard = {
                                    navController.navigate(Routes.DASHBOARD) {
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
                                    navController.navigate(Routes.DASHBOARD) {
                                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                                    }
                                },
                                onSkipToDashboard = { name ->
                                    mainViewModel.completeOnboarding(name)
                                    navController.navigate(Routes.DASHBOARD) {
                                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 3. Évaluation initiale autonome
                        composable(Routes.ASSESSMENT) {
                            InitialAssessmentScreen(
                                onFinished = { correct, total ->
                                    mainViewModel.completePlacement(correct, total)
                                    navController.navigate(Routes.DASHBOARD) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 4. Accueil / Tableau de bord
                        composable(Routes.DASHBOARD) {
                            DashboardScreen(
                                profile = profile,
                                adaptiveSummary = adaptiveSummary,
                                lessons = lessons,
                                lessonProgress = lessonProgress,
                                exerciseAttempts = exerciseAttempts,
                                dailyGoals = dailyGoals,
                                dailyTargetMinutes = prefs.dailyGoalMinutes,
                                onNavigate = { target ->
                                    if (target in Routes.topLevelTabs) navigateToTab(target)
                                    else navController.navigate(target)
                                },
                                onStartCategoryTraining = { category ->
                                    mainViewModel.setExerciseCategoryFilter(category)
                                    navigateToTab(Routes.EXERCISES)
                                }
                            )
                        }

                        // 5. Correcteur
                        composable(Routes.CORRECTOR) {
                            CorrectorScreen(
                                uiState = correctorState,
                                sampleTexts = correctorViewModel.sampleTexts,
                                onTextChange = correctorViewModel::updateText,
                                onAnalyze = { correctorViewModel.analyzeCurrentText() },
                                onOpenResult = { navController.navigate(Routes.CORRECTION_RESULT) },
                                onLoadSample = correctorViewModel::loadSample,
                                onFixAll = correctorViewModel::applyAllSafeCorrections,
                                onAcceptIssue = correctorViewModel::acceptIssue,
                                onDismissIssue = correctorViewModel::dismissIssue,
                                onUndo = correctorViewModel::undo,
                                onRedo = correctorViewModel::redo,
                                onClear = correctorViewModel::clearAll,
                                onOpenHistory = { navController.navigate(Routes.HISTORY) },
                                onOpenWritingWorkshop = { navController.navigate(Routes.WRITING) }
                            )
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

                        // 7. Académie
                        composable(Routes.ACADEMY) {
                            AcademyScreen(
                                lessons = lessons,
                                progress = lessonProgress,
                                onOpenLevelLessons = { lvl -> navController.navigate(Routes.lessonList(lvl)) },
                                onOpenDictionary = { navController.navigate(Routes.DICTIONARY) },
                                onOpenConjugation = { navController.navigate(Routes.CONJUGATION) },
                                onOpenVocabulary = { navController.navigate(Routes.VOCABULARY) }
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
                                onBack = { navController.popBackStack() }
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

                        // 11. Dictionnaire
                        composable(Routes.DICTIONARY) {
                            DictionaryScreen(
                                entries = dictEntries,
                                recentEntries = recentDictEntries,
                                favorites = favorites,
                                onOpenWord = { id ->
                                    mainViewModel.markDictionaryWordViewed(id)
                                    navController.navigate(Routes.wordDetail(id))
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 12. Fiche de mot
                        composable(
                            route = Routes.WORD_DETAIL,
                            arguments = listOf(navArgument("wordId") { type = NavType.StringType })
                        ) { entry ->
                            val wordId = entry.arguments?.getString("wordId") ?: ""
                            val word = dictEntries.firstOrNull { it.id == wordId }
                            WordDetailScreen(
                                entry = word,
                                favorites = favorites,
                                onToggleFavorite = { w ->
                                    mainViewModel.toggleFavorite("WORD", w.id, w.word, w.definition)
                                },
                                onSpeakWord = { w -> container.speechManager.speak(w, prefs.ttsSpeechRate) },
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

                        // Hub Exercices
                        composable(Routes.EXERCISES) {
                            ExercisesScreen(
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
                                onSaveSession = { orig, corr, issues, applied ->
                                    CoroutineScope(Dispatchers.IO).launch {
                                        container.repository.saveCorrectionSession(orig, corr, issues, applied, "REDACTION")
                                    }
                                },
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

                        // 22. Profil
                        composable(Routes.PROFILE) {
                            ProfileScreen(
                                profile = profile,
                                adaptiveSummary = adaptiveSummary,
                                achievements = achievements,
                                onUpdateName = mainViewModel::updateDisplayName,
                                onNavigate = { navController.navigate(it) }
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
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
