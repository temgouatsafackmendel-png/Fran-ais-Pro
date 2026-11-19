package com.example.features.correction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.FrancaisProRepository
import com.example.linguistics.analysis.DiffSegment
import com.example.linguistics.model.CorrectionIssue
import com.example.linguistics.model.CorrectionReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CorrectorUiState(
    val inputText: String = "",
    val baselineOriginalText: String = "",
    val report: CorrectionReport? = null,
    val dismissedIssueIds: Set<String> = emptySet(),
    val appliedCount: Int = 0,
    val undoStack: List<String> = emptyList(),
    val redoStack: List<String> = emptyList(),
    val isAnalyzing: Boolean = false,
    val showBeforeAfter: Boolean = false,
    val diffSegments: List<DiffSegment> = emptyList(),
    val statusBanner: String? = null
) {
    val activeIssues: List<CorrectionIssue>
        get() = report?.issues?.filterNot { it.id in dismissedIssueIds }.orEmpty()
    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()
}

class CorrectorViewModel(
    private val repository: FrancaisProRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CorrectorUiState())
    val uiState: StateFlow<CorrectorUiState> = _uiState.asStateFlow()

    val sampleTexts: List<Pair<String, String>> = listOf(
        "Exemple à corriger" to "Les etudiants apprend le francais. Elle a mangée et ils son parti tôt. Je vais travaillé dans sa entreprise. Il faut que tu viens. Si j'aurais su, je serais venu. Les fleurs que j'ai acheté sont belles.",
        "Email professionnel" to "Bonjour , malgrès le délai court, je vous envoie le rapport. Au jour d'aujourd'hui, nous devons appeller le client pour trouvé une solution.",
        "Phrase sans faute" to "Les étudiants apprennent le français. Les fleurs que j'ai achetées embellissent leur maison."
    )

    fun updateText(newText: String) {
        _uiState.update { state ->
            state.copy(
                inputText = newText,
                statusBanner = null
            )
        }
    }

    fun loadSample(text: String) {
        _uiState.update {
            CorrectorUiState(
                inputText = text,
                baselineOriginalText = text
            )
        }
        analyzeCurrentText()
    }

    fun analyzeCurrentText(sourceModule: String = "CORRECTEUR", onComplete: (() -> Unit)? = null) {
        val text = _uiState.value.inputText
        if (text.isBlank()) {
            _uiState.update { it.copy(statusBanner = "Saisissez ou collez un texte à analyser.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, statusBanner = null) }
            val report = repository.analyzeText(text)
            val baseline = _uiState.value.baselineOriginalText.ifBlank { text }
            val diff = repository.correctionEngine.computeWordDiff(baseline, text)
            _uiState.update {
                it.copy(
                    baselineOriginalText = baseline,
                    report = report,
                    dismissedIssueIds = emptySet(),
                    isAnalyzing = false,
                    diffSegments = diff
                )
            }
            repository.saveCorrectionSession(
                originalText = baseline,
                correctedText = text,
                issues = report.issues,
                appliedCount = _uiState.value.appliedCount,
                sourceModule = sourceModule
            )
            onComplete?.invoke()
        }
    }

    fun acceptIssue(issue: CorrectionIssue) {
        val current = _uiState.value
        val updatedText = repository.correctionEngine.applySingleIssue(current.inputText, issue)
        if (updatedText == current.inputText) {
            dismissIssue(issue)
            return
        }
        viewModelScope.launch {
            val newReport = repository.analyzeText(updatedText)
            val diff = repository.correctionEngine.computeWordDiff(current.baselineOriginalText, updatedText)
            _uiState.update { state ->
                state.copy(
                    inputText = updatedText,
                    undoStack = state.undoStack + state.inputText,
                    redoStack = emptyList(),
                    appliedCount = state.appliedCount + 1,
                    report = newReport,
                    diffSegments = diff,
                    statusBanner = "Correction appliquée : « ${issue.suggestion} »"
                )
            }
        }
    }

    fun dismissIssue(issue: CorrectionIssue) {
        _uiState.update { state ->
            state.copy(
                dismissedIssueIds = state.dismissedIssueIds + issue.id,
                statusBanner = "Suggestion ignorée."
            )
        }
    }

    fun applyAllSafeCorrections() {
        val current = _uiState.value
        val active = current.activeIssues
        if (active.isEmpty()) return

        val safeCount = active.count { !it.isAmbiguous && it.suggestion != it.originalFragment }
        if (safeCount == 0) {
            _uiState.update {
                it.copy(statusBanner = "Les suggestions restantes sont stylistiques ou ambiguës : validez-les individuellement.")
            }
            return
        }

        val updatedText = repository.correctionEngine.applySafeCorrections(current.inputText, active)
        viewModelScope.launch {
            val newReport = repository.analyzeText(updatedText)
            val diff = repository.correctionEngine.computeWordDiff(current.baselineOriginalText, updatedText)
            _uiState.update { state ->
                state.copy(
                    inputText = updatedText,
                    undoStack = state.undoStack + state.inputText,
                    redoStack = emptyList(),
                    appliedCount = state.appliedCount + safeCount,
                    report = newReport,
                    diffSegments = diff,
                    statusBanner = "$safeCount correction(s) fiable(s) appliquée(s). Les suggestions ambiguës ont été préservées."
                )
            }
            repository.saveCorrectionSession(
                originalText = current.baselineOriginalText,
                correctedText = updatedText,
                issues = active,
                appliedCount = safeCount
            )
        }
    }

    fun undo() {
        val current = _uiState.value
        if (current.undoStack.isEmpty()) return
        val previous = current.undoStack.last()
        viewModelScope.launch {
            val newReport = repository.analyzeText(previous)
            val diff = repository.correctionEngine.computeWordDiff(current.baselineOriginalText, previous)
            _uiState.update { state ->
                state.copy(
                    inputText = previous,
                    undoStack = state.undoStack.dropLast(1),
                    redoStack = state.redoStack + state.inputText,
                    report = newReport,
                    diffSegments = diff,
                    statusBanner = "Dernière modification annulée."
                )
            }
        }
    }

    fun redo() {
        val current = _uiState.value
        if (current.redoStack.isEmpty()) return
        val restored = current.redoStack.last()
        viewModelScope.launch {
            val newReport = repository.analyzeText(restored)
            val diff = repository.correctionEngine.computeWordDiff(current.baselineOriginalText, restored)
            _uiState.update { state ->
                state.copy(
                    inputText = restored,
                    undoStack = state.undoStack + state.inputText,
                    redoStack = state.redoStack.dropLast(1),
                    report = newReport,
                    diffSegments = diff,
                    statusBanner = "Modification rétablie."
                )
            }
        }
    }

    fun toggleBeforeAfter() {
        _uiState.update { it.copy(showBeforeAfter = !it.showBeforeAfter) }
    }

    fun clearAll() {
        _uiState.value = CorrectorUiState()
    }

    class Factory(private val repository: FrancaisProRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CorrectorViewModel(repository) as T
        }
    }
}
