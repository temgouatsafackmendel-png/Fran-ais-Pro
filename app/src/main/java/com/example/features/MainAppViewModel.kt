package com.example.features

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.audio.LocalSpeechManager
import com.example.core.backup.BackupResult
import com.example.core.database.Achievement
import com.example.core.database.CorrectionHistory
import com.example.core.database.DailyGoal
import com.example.core.database.Dictation
import com.example.core.database.DictationAttempt
import com.example.core.database.DictionaryEntry
import com.example.core.database.Exercise
import com.example.core.database.ExerciseAttempt
import com.example.core.database.Favorite
import com.example.core.database.LearningStatistics
import com.example.core.database.Lesson
import com.example.core.database.LessonProgress
import com.example.core.database.RecentDictionarySearch
import com.example.core.database.StreakReward
import com.example.core.database.StreakState
import com.example.core.database.UserProfile
import com.example.core.database.Verb
import com.example.core.database.VocabularyProgress
import com.example.core.database.VocabularyWord
import com.example.core.datastore.LocalUserPreferences
import com.example.data.DictionarySearchScope
import com.example.data.FrancaisProRepository
import com.example.learning.dictation.DictationEvaluation
import com.example.learning.progression.AcademyStreakEngine
import com.example.learning.progression.AcademyStreakSummary
import com.example.learning.progression.AdaptiveProgressionEngine
import com.example.learning.progression.CoursePathEngine
import com.example.learning.progression.CurriculumRoadmap
import com.example.learning.progression.LinguisticProfileSummary
import com.example.linguistics.conjugation.IdentifiedVerbForm
import com.example.linguistics.conjugation.VerbConjugationTable
import com.example.linguistics.model.CorrectionIssue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LastDictationResultState(
    val dictation: Dictation,
    val userText: String,
    val evaluation: DictationEvaluation
)

/**
 * État réactif du Dictionnaire alimenté exclusivement par la base locale Room.
 */
data class DictionaryUiState(
    val query: String = "",
    val selectedCategory: String = "Tous",
    val selectedScope: DictionarySearchScope = DictionarySearchScope.ALL,
    val favoritesOnly: Boolean = false,
    val results: List<DictionaryEntry> = emptyList(),
    val fuzzySuggestions: List<DictionaryEntry> = emptyList(),
    val recentSearches: List<RecentDictionarySearch> = emptyList(),
    val recentEntries: List<DictionaryEntry> = emptyList(),
    val categories: List<String> = listOf("Tous"),
    val totalCount: Int = 0
)

private data class DictionaryFilterParams(
    val query: String,
    val category: String,
    val scope: DictionarySearchScope,
    val favoritesOnly: Boolean
)

class MainAppViewModel(
    val repository: FrancaisProRepository,
    val speechManager: LocalSpeechManager
) : ViewModel() {

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val preferences: StateFlow<LocalUserPreferences> = repository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LocalUserPreferences())

    val lessons: StateFlow<List<Lesson>> = repository.allLessons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lessonProgress: StateFlow<List<LessonProgress>> = repository.allLessonProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exercises: StateFlow<List<Exercise>> = repository.allExercises
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exerciseAttempts: StateFlow<List<ExerciseAttempt>> = repository.exerciseAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dictionaryEntries: StateFlow<List<DictionaryEntry>> = repository.allDictionaryEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentDictionaryEntries: StateFlow<List<DictionaryEntry>> = repository.recentDictionaryEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val verbs: StateFlow<List<Verb>> = repository.allVerbs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val correctionHistory: StateFlow<List<CorrectionHistory>> = repository.correctionHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vocabularyWords: StateFlow<List<VocabularyWord>> = repository.vocabularyWords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vocabularyProgress: StateFlow<List<VocabularyProgress>> = repository.vocabularyProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dictations: StateFlow<List<Dictation>> = repository.allDictations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dictationAttempts: StateFlow<List<DictationAttempt>> = repository.dictationAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyGoals: StateFlow<List<DailyGoal>> = repository.dailyGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<Achievement>> = repository.achievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unlockedAchievementEvents: SharedFlow<Achievement> = repository.unlockedAchievementEvents

    val streakState: StateFlow<StreakState?> = repository.streakState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val unlockedStreakRewardEvents: SharedFlow<StreakReward> = repository.unlockedStreakRewardEvents

    val academyStreakSummary: StateFlow<AcademyStreakSummary> = combine(
        repository.streakState,
        repository.academyPracticeDays,
        repository.streakRewards
    ) { state, days, rewards ->
        AcademyStreakEngine.buildSummary(state, days, rewards)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AcademyStreakEngine.buildSummary(null, emptyList(), AcademyStreakEngine.defaultRewards())
    )

    val statistics: StateFlow<List<LearningStatistics>> = repository.statistics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<Favorite>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adaptiveSummary: StateFlow<LinguisticProfileSummary> = combine(
        userProfile,
        lessonProgress,
        statistics
    ) { profile, progress, stats ->
        AdaptiveProgressionEngine.computeProfileSummary(
            currentEstimatedLevel = profile?.estimatedLevel ?: "A1",
            completedLessonsCount = progress.count { it.isCompleted },
            stats = stats
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AdaptiveProgressionEngine.computeProfileSummary("A1", 0, emptyList())
    )

    val curriculumRoadmap: StateFlow<CurriculumRoadmap> = combine(
        lessons,
        lessonProgress
    ) { allLessons, allProgress ->
        CoursePathEngine.buildRoadmap(allLessons, allProgress)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CoursePathEngine.buildRoadmap(emptyList(), emptyList())
    )

    // --- État Dictionnaire réactif (Room SQLite) ---
    private val _dictionaryQuery = MutableStateFlow("")
    private val _dictionaryCategory = MutableStateFlow("Tous")
    private val _dictionaryScope = MutableStateFlow(DictionarySearchScope.ALL)
    private val _dictionaryFavoritesOnly = MutableStateFlow(false)

    private val dictionaryFilters: Flow<DictionaryFilterParams> = combine(
        _dictionaryQuery,
        _dictionaryCategory,
        _dictionaryScope,
        _dictionaryFavoritesOnly
    ) { q, cat, scope, favOnly ->
        DictionaryFilterParams(q, cat, scope, favOnly)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dictionaryQueryMatches: Flow<Pair<List<DictionaryEntry>, List<DictionaryEntry>>> =
        dictionaryFilters.flatMapLatest { params ->
            repository.searchDictionary(
                query = params.query,
                category = params.category,
                scope = params.scope
            ).map { sqlResults ->
                val fuzzy = if (sqlResults.isEmpty() && params.query.trim().length >= 2) {
                    repository.fuzzyDictionarySuggestions(params.query)
                } else {
                    emptyList()
                }
                sqlResults to fuzzy
            }
        }

    val dictionaryUiState: StateFlow<DictionaryUiState> = combine(
        dictionaryFilters,
        dictionaryQueryMatches,
        repository.recentDictionarySearches,
        repository.recentDictionaryEntries,
        repository.dictionaryCategories,
        repository.dictionaryTotalCount,
        repository.favorites
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val params = values[0] as DictionaryFilterParams
        @Suppress("UNCHECKED_CAST")
        val matchesPair = values[1] as Pair<List<DictionaryEntry>, List<DictionaryEntry>>
        @Suppress("UNCHECKED_CAST")
        val recentSearches = values[2] as List<RecentDictionarySearch>
        @Suppress("UNCHECKED_CAST")
        val recents = values[3] as List<DictionaryEntry>
        @Suppress("UNCHECKED_CAST")
        val dbCategories = values[4] as List<String>
        val totalCount = values[5] as Int
        @Suppress("UNCHECKED_CAST")
        val favs = values[6] as List<Favorite>

        val favWordIds = favs.filter { it.itemType == "WORD" }.map { it.itemId }.toSet()
        val finalResults = if (params.favoritesOnly) {
            matchesPair.first.filter { it.id in favWordIds }
        } else {
            matchesPair.first
        }

        DictionaryUiState(
            query = params.query,
            selectedCategory = params.category,
            selectedScope = params.scope,
            favoritesOnly = params.favoritesOnly,
            results = finalResults,
            fuzzySuggestions = matchesPair.second,
            recentSearches = recentSearches,
            recentEntries = recents,
            categories = listOf("Tous") + dbCategories,
            totalCount = totalCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DictionaryUiState())

    // État Dictée récente
    private val _lastDictationResult = MutableStateFlow<LastDictationResultState?>(null)
    val lastDictationResult: StateFlow<LastDictationResultState?> = _lastDictationResult.asStateFlow()

    // État Conjugueur
    private val _selectedVerbTable = MutableStateFlow<VerbConjugationTable?>(repository.conjugateVerb("apprendre"))
    val selectedVerbTable: StateFlow<VerbConjugationTable?> = _selectedVerbTable.asStateFlow()

    private val _identifiedForms = MutableStateFlow<List<IdentifiedVerbForm>>(emptyList())
    val identifiedForms: StateFlow<List<IdentifiedVerbForm>> = _identifiedForms.asStateFlow()

    // État Sauvegarde JSON
    private val _backupStatus = MutableStateFlow<String?>(null)
    val backupStatus: StateFlow<String?> = _backupStatus.asStateFlow()

    private val _pendingExportJson = MutableStateFlow<String?>(null)
    val pendingExportJson: StateFlow<String?> = _pendingExportJson.asStateFlow()

    // Filtre de catégorie pré-sélectionné depuis l'Accueil (Votre entraînement du jour)
    private val _exerciseCategoryFilter = MutableStateFlow<String?>(null)
    val exerciseCategoryFilter: StateFlow<String?> = _exerciseCategoryFilter.asStateFlow()

    fun setExerciseCategoryFilter(category: String?) {
        _exerciseCategoryFilter.value = category
    }

    // Actions Onboarding & Profil
    fun completeOnboarding(name: String) {
        viewModelScope.launch { repository.completeOnboarding(name) }
    }

    fun completeOnboardingWithGoal(
        name: String,
        dailyGoalMinutes: Int,
        placementCorrect: Int?,
        placementTotal: Int?
    ) {
        viewModelScope.launch {
            repository.completeOnboardingFlow(
                displayName = name,
                dailyGoalMinutes = dailyGoalMinutes,
                placementCorrect = placementCorrect,
                placementTotal = placementTotal
            )
        }
    }

    fun completePlacement(correctCount: Int, totalCount: Int) {
        val level = AdaptiveProgressionEngine.estimateLevelFromPlacement(correctCount, totalCount)
        val pct = if (totalCount == 0) 0 else (correctCount * 100) / totalCount
        viewModelScope.launch { repository.completeInitialAssessment(level, pct) }
    }

    fun updateDisplayName(name: String) {
        viewModelScope.launch { repository.updateDisplayName(name) }
    }

    // Actions Académie & Exercices
    fun completeLesson(lessonId: String, quizScorePercent: Int) {
        viewModelScope.launch { repository.completeLesson(lessonId, quizScorePercent) }
    }

    fun submitExercise(exercise: Exercise, answer: String, isCorrect: Boolean) {
        viewModelScope.launch { repository.submitExerciseAttempt(exercise, answer, isCorrect) }
    }

    fun claimStreakReward(rewardId: String) {
        viewModelScope.launch { repository.claimStreakReward(rewardId) }
    }

    // Actions Dictionnaire (Room) & Conjugueur
    fun updateDictionaryQuery(query: String) {
        _dictionaryQuery.value = query
    }

    fun selectDictionaryCategory(category: String) {
        _dictionaryCategory.value = category
    }

    fun selectDictionaryScope(scope: DictionarySearchScope) {
        _dictionaryScope.value = scope
    }

    fun toggleDictionaryFavoritesOnly() {
        _dictionaryFavoritesOnly.value = !_dictionaryFavoritesOnly.value
    }

    fun clearDictionaryFilters() {
        _dictionaryQuery.value = ""
        _dictionaryCategory.value = "Tous"
        _dictionaryScope.value = DictionarySearchScope.ALL
        _dictionaryFavoritesOnly.value = false
    }

    fun observeDictionaryEntry(id: String): Flow<DictionaryEntry?> =
        repository.observeDictionaryEntry(id)

    fun markDictionaryWordViewed(id: String) {
        viewModelScope.launch { repository.getDictionaryEntry(id) }
    }

    fun recordDictionarySearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { repository.recordDictionarySearch(trimmed) }
    }

    fun selectRecentDictionarySearch(search: RecentDictionarySearch) {
        _dictionaryQuery.value = search.queryText
        _dictionaryFavoritesOnly.value = false
        viewModelScope.launch { repository.recordDictionarySearch(search.queryText) }
    }

    fun removeRecentDictionarySearch(normalizedQuery: String) {
        viewModelScope.launch { repository.removeRecentDictionarySearch(normalizedQuery) }
    }

    fun clearRecentDictionarySearches() {
        viewModelScope.launch { repository.clearRecentDictionarySearches() }
    }

    fun resolveRelatedDictionaryWord(
        term: String,
        onFoundEntryId: (String) -> Unit,
        onFallbackSearch: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.recordDictionarySearch(term)
            val found = repository.findDictionaryEntryByWord(term)
            if (found != null) {
                repository.getDictionaryEntry(found.id)
                onFoundEntryId(found.id)
            } else {
                _dictionaryQuery.value = term
                _dictionaryCategory.value = "Tous"
                _dictionaryScope.value = DictionarySearchScope.ALL
                _dictionaryFavoritesOnly.value = false
                onFallbackSearch(term)
            }
        }
    }

    fun addCustomDictionaryWord(
        word: String,
        partOfSpeech: String,
        definition: String,
        synonymsCsv: String,
        antonymsCsv: String,
        example: String,
        level: String,
        onCreated: (String) -> Unit = {}
    ) {
        if (word.isBlank() || definition.isBlank()) return
        val synonyms = synonymsCsv.split(",", ";", "|").map { it.trim() }.filter { it.isNotEmpty() }
        val antonyms = antonymsCsv.split(",", ";", "|").map { it.trim() }.filter { it.isNotEmpty() }
        viewModelScope.launch {
            val created = repository.addCustomDictionaryEntry(
                word = word,
                partOfSpeech = partOfSpeech,
                definition = definition,
                synonyms = synonyms,
                antonyms = antonyms,
                example = example,
                level = level
            )
            onCreated(created.id)
        }
    }

    fun deleteCustomDictionaryWord(id: String, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteCustomDictionaryEntry(id)
            onDeleted()
        }
    }

    fun selectVerb(infinitive: String) {
        val table = repository.conjugateVerb(infinitive)
        if (table != null) {
            _selectedVerbTable.value = table
            _identifiedForms.value = emptyList()
        } else {
            identifyConjugatedForm(infinitive)
        }
    }

    fun identifyConjugatedForm(query: String) {
        viewModelScope.launch {
            val matches = repository.identifyConjugatedForm(query)
            _identifiedForms.value = matches
            matches.firstOrNull()?.let { first ->
                repository.conjugateVerb(first.infinitive)?.let { _selectedVerbTable.value = it }
            }
        }
    }

    // Actions Dictée
    fun submitDictation(dictation: Dictation, userText: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val eval = repository.evaluateAndSaveDictation(dictation, userText)
            _lastDictationResult.value = LastDictationResultState(dictation, userText, eval)
            onDone()
        }
    }

    // Actions Vocabulaire SM-2
    fun reviewVocabulary(wordId: String, quality: Int) {
        viewModelScope.launch { repository.reviewVocabularyWord(wordId, quality) }
    }

    // Actions Favoris & Historique
    fun toggleFavorite(type: String, itemId: String, title: String, subtitle: String) {
        viewModelScope.launch { repository.toggleFavorite(type, itemId, title, subtitle) }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch { repository.deleteCorrectionHistory(id) }
    }

    // Actions Paramètres
    fun setThemeMode(mode: String) {
        viewModelScope.launch { repository.updateThemeMode(mode) }
    }

    fun setDailyGoalMinutes(minutes: Int) {
        viewModelScope.launch { repository.updateDailyGoalMinutes(minutes) }
    }

    fun setSpeechRate(rate: Float) {
        viewModelScope.launch { repository.updateSpeechRate(rate) }
    }

    fun setAutoAnalyze(enabled: Boolean) {
        viewModelScope.launch { repository.updateAutoAnalyze(enabled) }
    }

    fun setIncludeHistoryInBackup(enabled: Boolean) {
        viewModelScope.launch { repository.updateIncludeHistoryInBackup(enabled) }
    }

    fun setDailyReminder(enabled: Boolean, hour: Int) {
        viewModelScope.launch { repository.updateDailyReminder(enabled, hour) }
    }

    // Actions Sauvegarde JSON
    fun prepareBackupExport(includeHistory: Boolean, onJsonReady: (String) -> Unit) {
        viewModelScope.launch {
            when (val res = repository.exportBackupJson(includeHistory)) {
                is BackupResult.ExportSuccess -> {
                    _pendingExportJson.value = res.json
                    _backupStatus.value = "Export prêt (${res.itemCount} éléments enregistrés)."
                    onJsonReady(res.json)
                }
                is BackupResult.Failure -> _backupStatus.value = res.reason
                else -> Unit
            }
        }
    }

    fun importBackupJson(rawJson: String) {
        viewModelScope.launch {
            when (val res = repository.importBackupJson(rawJson)) {
                is BackupResult.ImportSuccess -> _backupStatus.value = res.restoredSummary
                is BackupResult.Failure -> _backupStatus.value = res.reason
                else -> Unit
            }
        }
    }

    fun saveWritingSession(
        originalText: String,
        correctedText: String,
        issues: List<CorrectionIssue>,
        appliedCount: Int
    ) {
        viewModelScope.launch {
            repository.saveCorrectionSession(
                originalText = originalText,
                correctedText = correctedText,
                issues = issues,
                appliedCount = appliedCount,
                sourceModule = "REDACTION"
            )
        }
    }

    fun reportBackupStatus(message: String) {
        _backupStatus.value = message
    }

    fun clearBackupStatus() {
        _backupStatus.value = null
    }

    class Factory(
        private val repository: FrancaisProRepository,
        private val speechManager: LocalSpeechManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainAppViewModel(repository, speechManager) as T
        }
    }
}
