package com.example.features.correction

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.core.design.AppIconButton
import com.example.core.design.AppTopBar
import com.example.core.design.CategoryBadge
import com.example.core.design.ConfirmationDialog
import com.example.core.design.CorrectionCard
import com.example.core.design.EditorialCard
import com.example.core.design.EmptyState
import com.example.core.design.ErrorDetailBottomSheet
import com.example.core.design.ErrorHighlightText
import com.example.core.design.PrimaryButton
import com.example.core.design.SecondaryButton
import com.example.core.design.SectionHeader
import com.example.core.design.SuccessFeedback
import com.example.linguistics.analysis.DiffType
import com.example.linguistics.analysis.TextReadabilityAnalyzer
import com.example.linguistics.model.CorrectionCategory
import com.example.linguistics.model.CorrectionIssue
import kotlinx.coroutines.delay
import com.example.ui.theme.FrancaisProDimens
import com.example.ui.theme.FrancaisProTextStyles
import com.example.ui.theme.LocalSemanticColors
import com.example.ui.theme.LocalSpacing

/**
 * Écran 5 : Correcteur intelligent — Éditeur confortable, compteurs en direct, surlignage interactif & BottomSheet.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CorrectorScreen(
    uiState: CorrectorUiState,
    sampleTexts: List<Pair<String, String>>,
    onTextChange: (String) -> Unit,
    onAnalyze: () -> Unit,
    onOpenResult: () -> Unit,
    onLoadSample: (String) -> Unit,
    onFixAll: () -> Unit,
    onAcceptIssue: (CorrectionIssue) -> Unit,
    onDismissIssue: (CorrectionIssue) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClear: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenWritingWorkshop: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val semantic = LocalSemanticColors.current

    var inspectedIssue by remember { mutableStateOf<CorrectionIssue?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var selectedIssueFilter by remember { mutableStateOf("ALL") }
    var copiedFeedback by remember { mutableStateOf(false) }

    LaunchedEffect(copiedFeedback) {
        if (copiedFeedback) {
            delay(1800)
            copiedFeedback = false
        }
    }

    val readability = remember(uiState.inputText) {
        TextReadabilityAnalyzer.analyze(uiState.inputText)
    }
    val wordCount = readability.wordCount
    val charCount = readability.characterCount

    val filteredIssues = remember(uiState.activeIssues, selectedIssueFilter) {
        when (selectedIssueFilter) {
            "ORTHO" -> uiState.activeIssues.filter { it.category == CorrectionCategory.ORTHOGRAPHE }
            "GRAM" -> uiState.activeIssues.filter {
                it.category == CorrectionCategory.GRAMMAIRE || it.category == CorrectionCategory.CONJUGAISON
            }
            "SYNTAX" -> uiState.activeIssues.filter {
                it.category == CorrectionCategory.SYNTAXE || it.category == CorrectionCategory.PONCTUATION
            }
            "STYLE" -> uiState.activeIssues.filter { it.category == CorrectionCategory.STYLE }
            else -> uiState.activeIssues
        }
    }

    if (showClearConfirm) {
        ConfirmationDialog(
            title = "Effacer le texte ?",
            message = "Le contenu actuel de l'éditeur sera supprimé.",
            confirmLabel = "Effacer",
            onConfirm = {
                onClear()
                showClearConfirm = false
            },
            onDismiss = { showClearConfirm = false }
        )
    }

    inspectedIssue?.let { issue ->
        ErrorDetailBottomSheet(
            issue = issue,
            onAccept = { onAcceptIssue(issue) },
            onDismissIssue = { onDismissIssue(issue) },
            onClose = { inspectedIssue = null }
        )
    }

    val spacing = LocalSpacing.current
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        val hPadding = spacing.screenHorizontalPadding(maxWidth)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = spacing.maxContentWidth)
                .testTag("corrector_list"),
            contentPadding = PaddingValues(horizontal = hPadding, vertical = spacing.space16),
            verticalArrangement = Arrangement.spacedBy(spacing.space16)
        ) {
            // En-tête sobre
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Correcteur", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            "Orthographe, grammaire, syntaxe et style",
                            style = MaterialTheme.typography.bodySmall,
                            color = semantic.textSecondary
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AppIconButton(
                            icon = Icons.Filled.EditNote,
                            contentDescription = "Atelier de rédaction",
                            onClick = onOpenWritingWorkshop,
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.testTag("corrector_writing_btn")
                        )
                        AppIconButton(
                            icon = Icons.Filled.History,
                            contentDescription = "Historique",
                            onClick = onOpenHistory,
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.testTag("corrector_history_btn")
                        )
                    }
                }
            }

            // Exemples prêts à tester
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sampleTexts.forEach { (label, sample) ->
                        AssistChip(
                            onClick = { onLoadSample(sample) },
                            label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                            shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall)
                        )
                    }
                }
            }

            // Zone d'édition centrée sur le texte + compteurs + barre d'actions
            item {
                EditorialCard(contentPadding = spacing.space16, elevated = true) {
                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = onTextChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(156.dp)
                            .testTag("corrector_text_input"),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = semantic.surfaceSubtle.copy(alpha = 0.4f),
                            unfocusedContainerColor = semantic.surfaceSubtle.copy(alpha = 0.25f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = semantic.border
                        ),
                        placeholder = {
                            Text(
                                "Saisissez ou collez votre texte en français ici…",
                                style = MaterialTheme.typography.bodyLarge,
                                color = semantic.textSecondary
                            )
                        }
                    )

                    if (uiState.isAnalyzing) {
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = semantic.surfaceSubtle
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // Indicateurs rédactionnels en temps réel (lisibilité, phrases, temps de lecture)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("corrector_readability_bar"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryBadge(
                            label = readability.clarityLabel,
                            color = when {
                                readability.wordCount == 0 -> semantic.textSecondary
                                readability.avgWordsPerSentence <= 16 -> semantic.success
                                readability.avgWordsPerSentence <= 24 -> MaterialTheme.colorScheme.primary
                                else -> semantic.warning
                            },
                            containerColor = when {
                                readability.wordCount == 0 -> semantic.surfaceSubtle
                                readability.avgWordsPerSentence <= 16 -> semantic.successContainer
                                readability.avgWordsPerSentence <= 24 -> MaterialTheme.colorScheme.primaryContainer
                                else -> semantic.warningContainer
                            }
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = semantic.textSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${readability.sentenceCount} phr. · lecture ${readability.readingTimeLabel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = semantic.textSecondary
                            )
                        }
                        if (copiedFeedback) {
                            CategoryBadge(
                                label = "Copié",
                                color = semantic.success,
                                containerColor = semantic.successContainer,
                                icon = Icons.Filled.Check
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // Compteurs en temps réel + actions sobres
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$wordCount mot(s) · $charCount car.",
                            style = FrancaisProTextStyles.MonoMetric,
                            color = semantic.textSecondary,
                            modifier = Modifier.testTag("corrector_word_counter")
                        )
                        Row {
                            AppIconButton(
                                icon = Icons.Filled.ContentPaste,
                                contentDescription = "Coller",
                                onClick = {
                                    clipboard.getText()?.text?.let { pasted ->
                                        if (pasted.isNotBlank()) onTextChange(pasted)
                                    }
                                },
                                modifier = Modifier.testTag("corrector_paste_btn")
                            )
                            AppIconButton(
                                icon = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Annuler",
                                enabled = uiState.canUndo,
                                onClick = onUndo,
                                modifier = Modifier.testTag("corrector_undo_btn")
                            )
                            AppIconButton(
                                icon = Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "Rétablir",
                                enabled = uiState.canRedo,
                                onClick = onRedo,
                                modifier = Modifier.testTag("corrector_redo_btn")
                            )
                            AppIconButton(
                                icon = Icons.Filled.ContentCopy,
                                contentDescription = "Copier",
                                enabled = uiState.inputText.isNotBlank(),
                                onClick = {
                                    clipboard.setText(AnnotatedString(uiState.inputText))
                                    copiedFeedback = true
                                },
                                modifier = Modifier.testTag("corrector_copy_btn")
                            )
                            AppIconButton(
                                icon = Icons.Filled.DeleteOutline,
                                contentDescription = "Effacer",
                                enabled = uiState.inputText.isNotBlank(),
                                tint = semantic.danger,
                                onClick = { showClearConfirm = true },
                                modifier = Modifier.testTag("corrector_clear_btn")
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PrimaryButton(
                            text = "Analyser",
                            onClick = onAnalyze,
                            icon = Icons.Filled.Spellcheck,
                            enabled = !uiState.isAnalyzing && uiState.inputText.isNotBlank(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("corrector_analyze_button")
                        )
                        if (uiState.report != null) {
                            SecondaryButton(
                                text = "Rapport",
                                onClick = onOpenResult,
                                icon = Icons.AutoMirrored.Filled.CompareArrows,
                                modifier = Modifier.testTag("corrector_open_result_button")
                            )
                        }
                    }
                }
            }

            if (uiState.statusBanner != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.statusBanner,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            val report = uiState.report
            if (report != null && !uiState.isAnalyzing) {
                // Résumé du score avec jauge circulaire + codes couleur + texte surligné interactif
                item {
                    EditorialCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                QualityScoreRing(score = report.qualityScore)
                                Column {
                                    Text(
                                        text = "Qualité : ${report.qualityScore} / 100",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${uiState.activeIssues.size} point(s) relevé(s) · ${report.analysisDurationMs} ms",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = semantic.textSecondary
                                    )
                                }
                            }
                            if (uiState.activeIssues.isNotEmpty()) {
                                PrimaryButton(
                                    text = "Tout corriger",
                                    onClick = onFixAll,
                                    icon = Icons.Filled.AutoFixHigh,
                                    containerColor = semantic.success,
                                    modifier = Modifier.testTag("corrector_fix_all_button")
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        CategoryBreakdownRow(issues = uiState.activeIssues)

                        if (uiState.activeIssues.isNotEmpty()) {
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = "Touchez un passage souligné pour voir l'explication :",
                                style = MaterialTheme.typography.labelMedium,
                                color = semantic.textSecondary
                            )
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                                color = semantic.surfaceSubtle,
                                border = BorderStroke(1.dp, semantic.border),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ErrorHighlightText(
                                    text = uiState.inputText,
                                    issues = filteredIssues.ifEmpty { uiState.activeIssues },
                                    onIssueClick = { inspectedIssue = it },
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    }
                }

                if (uiState.activeIssues.isEmpty()) {
                    item {
                        SuccessFeedback(
                            title = "Aucune erreur détectée",
                            message = "Votre texte respecte l'ensemble des règles analysées localement."
                        )
                    }
                } else {
                    item(key = "issue_category_filters") {
                        val orthoCount = uiState.activeIssues.count { it.category == CorrectionCategory.ORTHOGRAPHE }
                        val gramCount = uiState.activeIssues.count {
                            it.category == CorrectionCategory.GRAMMAIRE || it.category == CorrectionCategory.CONJUGAISON
                        }
                        val syntaxCount = uiState.activeIssues.count {
                            it.category == CorrectionCategory.SYNTAXE || it.category == CorrectionCategory.PONCTUATION
                        }
                        val styleCount = uiState.activeIssues.count { it.category == CorrectionCategory.STYLE }
                        val filterSpecs = listOf(
                            Triple("ALL", "Toutes (${uiState.activeIssues.size})", uiState.activeIssues.size),
                            Triple("ORTHO", "Orthographe ($orthoCount)", orthoCount),
                            Triple("GRAM", "Grammaire ($gramCount)", gramCount),
                            Triple("SYNTAX", "Syntaxe ($syntaxCount)", syntaxCount),
                            Triple("STYLE", "Style ($styleCount)", styleCount)
                        ).filter { it.first == "ALL" || it.third > 0 }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SectionHeader(
                                title = "Points détectés",
                                subtitle = "Filtrez par domaine linguistique pour corriger méthodiquement",
                                icon = Icons.Filled.FilterList
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(filterSpecs, key = { it.first }) { (key, label, _) ->
                                    FilterChip(
                                        selected = selectedIssueFilter == key,
                                        onClick = { selectedIssueFilter = key },
                                        label = { Text(label) },
                                        modifier = Modifier.testTag("corrector_filter_$key")
                                    )
                                }
                            }
                        }
                    }

                    items(filteredIssues, key = { it.id }) { issue ->
                        CorrectionCard(
                            issue = issue,
                            onAccept = { onAcceptIssue(issue) },
                            onDismiss = { onDismissIssue(issue) },
                            onInspect = { inspectedIssue = issue }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QualityScoreRing(score: Int) {
    val semantic = LocalSemanticColors.current
    val clamped = score.coerceIn(0, 100)
    val ringColor = when {
        clamped >= 85 -> semantic.success
        clamped >= 65 -> MaterialTheme.colorScheme.primary
        else -> semantic.warning
    }
    val trackColor = semantic.surfaceSubtle

    Box(
        modifier = Modifier
            .size(48.dp)
            .testTag("corrector_quality_ring"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * (clamped / 100f),
                useCenter = false,
                style = stroke
            )
        }
        Text(
            text = "$clamped",
            style = FrancaisProTextStyles.MonoMetric,
            fontWeight = FontWeight.Bold,
            color = ringColor
        )
    }
}

/**
 * Écran 6 : Résultat complet de correction, surlignage interactif & Comparaison Avant/Après.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CorrectionResultScreen(
    uiState: CorrectorUiState,
    onAcceptIssue: (CorrectionIssue) -> Unit,
    onDismissIssue: (CorrectionIssue) -> Unit,
    onFixAll: () -> Unit,
    onToggleCompare: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onBack: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val semantic = LocalSemanticColors.current
    var inspectedIssue by remember { mutableStateOf<CorrectionIssue?>(null) }

    inspectedIssue?.let { issue ->
        ErrorDetailBottomSheet(
            issue = issue,
            onAccept = { onAcceptIssue(issue) },
            onDismissIssue = { onDismissIssue(issue) },
            onClose = { inspectedIssue = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(
            title = "Rapport de correction",
            subtitle = "Score ${uiState.report?.qualityScore ?: 100}/100 · ${uiState.appliedCount} appliquée(s)",
            onBack = onBack,
            actions = {
                AppIconButton(icon = Icons.AutoMirrored.Filled.Undo, contentDescription = "Annuler", enabled = uiState.canUndo, onClick = onUndo)
                AppIconButton(icon = Icons.AutoMirrored.Filled.Redo, contentDescription = "Rétablir", enabled = uiState.canRedo, onClick = onRedo)
            }
        )

        if (uiState.report == null) {
            EmptyState(
                icon = Icons.Filled.Spellcheck,
                title = "Aucune analyse active",
                message = "Saisissez un texte dans le correcteur pour consulter le rapport détaillé.",
                actionLabel = "Retour au correcteur",
                onAction = onBack
            )
            return
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = FrancaisProDimens.MaxContentWidth)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Synthèse score & catégories
            item {
                EditorialCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Score de qualité", style = MaterialTheme.typography.labelMedium, color = semantic.textSecondary)
                            Text(
                                "${uiState.report.qualityScore} / 100",
                                style = FrancaisProTextStyles.Heading1,
                                color = if (uiState.report.qualityScore >= 80) semantic.success else MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${uiState.activeIssues.size} restante(s)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${uiState.appliedCount} corrigée(s)", style = MaterialTheme.typography.bodySmall, color = semantic.success)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    CategoryBreakdownRow(issues = uiState.activeIssues)
                }
            }

            // Actions globales : Avant/Après, Copier, Partager
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SecondaryButton(
                        text = if (uiState.showBeforeAfter) "Texte annoté" else "Avant / Après",
                        onClick = onToggleCompare,
                        icon = Icons.AutoMirrored.Filled.CompareArrows,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("result_toggle_compare_btn")
                    )
                    AppIconButton(
                        icon = Icons.Filled.ContentCopy,
                        contentDescription = "Copier le texte corrigé",
                        containerColor = MaterialTheme.colorScheme.surface,
                        onClick = { clipboard.setText(AnnotatedString(uiState.inputText)) },
                        modifier = Modifier.testTag("result_copy_btn")
                    )
                    AppIconButton(
                        icon = Icons.Filled.Share,
                        contentDescription = "Partager le texte corrigé",
                        containerColor = MaterialTheme.colorScheme.surface,
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, uiState.inputText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Partager le texte corrigé"))
                        },
                        modifier = Modifier.testTag("result_share_btn")
                    )
                }
            }

            // Bloc Texte surligné ou Avant / Après
            item {
                EditorialCard {
                    Text(
                        text = if (uiState.showBeforeAfter) "Comparaison avant / après" else "Texte annoté (touchez une faute)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(10.dp))
                    if (uiState.showBeforeAfter) {
                        val annotated = buildAnnotatedString {
                            uiState.diffSegments.forEachIndexed { idx, seg ->
                                if (idx > 0) append(" ")
                                when (seg.type) {
                                    DiffType.UNCHANGED -> append(seg.text)
                                    DiffType.REMOVED -> withStyle(
                                        SpanStyle(
                                            color = semantic.danger,
                                            background = semantic.dangerContainer,
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                    ) { append(seg.text) }
                                    DiffType.ADDED -> withStyle(
                                        SpanStyle(
                                            color = semantic.success,
                                            background = semantic.successContainer,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    ) { append(seg.text) }
                                }
                            }
                        }
                        Text(annotated, style = MaterialTheme.typography.bodyLarge)
                    } else {
                        ErrorHighlightText(
                            text = uiState.inputText,
                            issues = uiState.activeIssues,
                            onIssueClick = { inspectedIssue = it }
                        )
                    }
                }
            }

            if (uiState.activeIssues.isNotEmpty()) {
                item {
                    PrimaryButton(
                        text = "Accepter les corrections certaines",
                        onClick = onFixAll,
                        icon = Icons.Filled.AutoFixHigh,
                        containerColor = semantic.success,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("result_fix_all_btn")
                    )
                }
                items(uiState.activeIssues, key = { it.id }) { issue ->
                    CorrectionCard(
                        issue = issue,
                        onAccept = { onAcceptIssue(issue) },
                        onDismiss = { onDismissIssue(issue) },
                        onInspect = { inspectedIssue = issue }
                    )
                }
            } else {
                item {
                    SuccessFeedback(
                        title = "Texte prêt à l'emploi",
                        message = "Toutes les suggestions ont été traitées. Vous pouvez copier ou partager le texte final."
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryBreakdownRow(issues: List<CorrectionIssue>) {
    val semantic = LocalSemanticColors.current
    val spellingCount = issues.count { it.category == CorrectionCategory.ORTHOGRAPHE }
    val grammarCount = issues.count { it.category == CorrectionCategory.GRAMMAIRE || it.category == CorrectionCategory.CONJUGAISON }
    val syntaxCount = issues.count { it.category == CorrectionCategory.SYNTAXE || it.category == CorrectionCategory.PONCTUATION }
    val styleCount = issues.count { it.category == CorrectionCategory.STYLE }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        SemanticCategoryDotBadge("Orthographe · $spellingCount", semantic.spellingColor, semantic.spellingContainer)
        SemanticCategoryDotBadge("Grammaire · $grammarCount", semantic.grammarColor, semantic.grammarContainer)
        SemanticCategoryDotBadge("Syntaxe · $syntaxCount", semantic.syntaxColor, semantic.syntaxContainer)
        SemanticCategoryDotBadge("Style · $styleCount", semantic.styleColor, semantic.styleContainer)
    }
}

@Composable
private fun SemanticCategoryDotBadge(
    label: String,
    color: androidx.compose.ui.graphics.Color,
    containerColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(color, CircleShape)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun CorrectionIssueCard(
    issue: CorrectionIssue,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) = CorrectionCard(issue = issue, onAccept = onAccept, onDismiss = onDismiss)
