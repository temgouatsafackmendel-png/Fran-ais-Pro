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
import com.example.core.database.UserProfile
import com.example.core.database.Verb
import com.example.core.database.VocabularyProgress
import com.example.core.database.VocabularyWord
import com.example.core.datastore.LocalUserPreferences
import com.example.data.FrancaisProRepository
import com.example.learning.dictation.DictationEvaluation
import com.example.learning.progression.AdaptiveProgressionEngine
import com.example.learning.progression.LinguisticProfileSummary
import com.example.linguistics.conjugation.IdentifiedVerbForm
import com.example.linguistics.conjugation.VerbConjugationTable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LastDictationResultState(
    val dictation: Dictation,
    val userText: String,
    val evaluation: DictationEvaluation
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
            repository.updateDailyGoalMinutes(dailyGoalMinutes)
            repository.completeOnboarding(name)
            if (placementCorrect != null && placementTotal != null && placementTotal > 0) {
                val level = AdaptiveProgressionEngine.estimateLevelFromPlacement(placementCorrect, placementTotal)
                val pct = (placementCorrect * 100) / placementTotal
                repository.completeInitialAssessment(level, pct)
            }
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

    // Actions Dictionnaire & Conjugueur
    fun markDictionaryWordViewed(id: String) {
        viewModelScope.launch { repository.getDictionaryEntry(id) }
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
