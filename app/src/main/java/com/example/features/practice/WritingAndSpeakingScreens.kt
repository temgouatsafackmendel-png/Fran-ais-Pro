package com.example.features.practice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.core.audio.SpeechCapabilities
import com.example.core.design.AppTopBar
import com.example.core.design.CategoryBadge
import com.example.core.design.CefrLevelPill
import com.example.core.design.CorrectionCard
import com.example.core.design.EditorialCard
import com.example.core.design.PrimaryButton
import com.example.core.design.SecondaryButton
import com.example.core.design.SuccessFeedback
import com.example.learning.content.AcademyLessonsSeed
import com.example.learning.dictation.DictationDiffEngine
import com.example.linguistics.analysis.HybridCorrectionEngine
import com.example.linguistics.model.CorrectionIssue
import com.example.ui.theme.FrancaisProDimens
import com.example.ui.theme.FrancaisProTextStyles
import com.example.ui.theme.LocalSemanticColors

/**
 * Écran 17 : Atelier de rédaction (10 modèles guidés + analyse locale maîtrisée par l'utilisateur).
 */
@Composable
fun WritingWorkshopScreen(
    correctionEngine: HybridCorrectionEngine,
    onSaveSession: (original: String, corrected: String, issues: List<CorrectionIssue>, applied: Int) -> Unit,
    onBack: () -> Unit
) {
    val templates = remember { AcademyLessonsSeed.writingTemplates() }
    var selectedTemplate by remember { mutableStateOf(templates.first()) }
    var draftText by remember(selectedTemplate.id) { mutableStateOf(selectedTemplate.starterDraft) }
    var reportIssues by remember(selectedTemplate.id) { mutableStateOf<List<CorrectionIssue>>(emptyList()) }
    var analyzed by remember(selectedTemplate.id) { mutableStateOf(false) }
    var appliedCount by remember(selectedTemplate.id) { mutableIntStateOf(0) }
    val semantic = LocalSemanticColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(
            title = "Atelier de rédaction",
            subtitle = "10 modèles guidés",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = FrancaisProDimens.MaxContentWidth)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(templates, key = { it.id }) { tpl ->
                        FilterChip(
                            selected = selectedTemplate.id == tpl.id,
                            onClick = { selectedTemplate = tpl },
                            label = { Text(tpl.category) }
                        )
                    }
                }
            }

            item {
                EditorialCard {
                    Text(selectedTemplate.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(selectedTemplate.objective, style = MaterialTheme.typography.bodySmall, color = semantic.textSecondary)
                    Spacer(Modifier.height(10.dp))
                    selectedTemplate.tips.forEach { tip ->
                        Text("• $tip", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }

            item {
                EditorialCard {
                    OutlinedTextField(
                        value = draftText,
                        onValueChange = { draftText = it },
                        label = { Text("Votre rédaction") },
                        shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .testTag("writing_editor_input")
                    )
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton(
                        text = "Analyser et améliorer",
                        icon = Icons.Filled.Spellcheck,
                        enabled = draftText.isNotBlank(),
                        onClick = {
                            val rep = correctionEngine.analyze(draftText)
                            reportIssues = rep.issues
                            analyzed = true
                            onSaveSession(selectedTemplate.starterDraft, draftText, rep.issues, appliedCount)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("writing_analyze_btn")
                    )
                }
            }

            if (analyzed) {
                if (reportIssues.isEmpty()) {
                    item {
                        SuccessFeedback(
                            title = "Rédaction soignée",
                            message = "Aucune faute ni lourdeur détectée par le moteur linguistique local."
                        )
                    }
                } else {
                    items(reportIssues, key = { it.id }) { issue ->
                        CorrectionCard(
                            issue = issue,
                            onAccept = {
                                draftText = correctionEngine.applySingleIssue(draftText, issue)
                                appliedCount++
                                reportIssues = correctionEngine.analyze(draftText).issues
                            },
                            onDismiss = {
                                reportIssues = reportIssues.filterNot { it.id == issue.id }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Écran 18 : Expression orale (prononciation, liaisons, dictée vocale locale + repli guidé).
 */
@Composable
fun SpeakingScreen(
    capabilities: SpeechCapabilities,
    isListening: Boolean,
    recognizedText: String,
    sttError: String?,
    speechRate: Float,
    onSpeakSentence: (String, Float) -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onManualTranscriptChange: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val drills = remember { AcademyLessonsSeed.speakingDrills() }
    var currentIdx by remember { mutableIntStateOf(0) }
    val drill = drills[currentIdx % drills.size]
    val semantic = LocalSemanticColors.current

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted) onStartListening()
    }

    val comparison = remember(drill.sentence, recognizedText) {
        if (recognizedText.isBlank()) null else DictationDiffEngine.evaluate(drill.sentence, recognizedText)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(
            title = "Expression orale",
            subtitle = "Prononciation, liaisons et répétition",
            onBack = {
                onStopListening()
                onBack()
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = FrancaisProDimens.MaxContentWidth)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                EditorialCard(contentPadding = 18.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CefrLevelPill(level = drill.level)
                        CategoryBadge(
                            label = drill.focus,
                            color = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text("Phrase à prononcer :", style = MaterialTheme.typography.labelMedium, color = semantic.textSecondary)
                    Spacer(Modifier.height(6.dp))
                    Text(drill.sentence, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(drill.phoneticGuide, style = FrancaisProTextStyles.MonoMetric, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text("Conseil : ${drill.coachingTip}", style = MaterialTheme.typography.bodySmall, color = semantic.textSecondary)

                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SecondaryButton(
                            text = "Modèle audio",
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            enabled = capabilities.ttsReady,
                            onClick = { onSpeakSentence(drill.sentence, speechRate) },
                            modifier = Modifier.weight(1f)
                        )
                        PrimaryButton(
                            text = if (isListening) "Arrêter" else "Parler",
                            icon = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                            onClick = {
                                if (isListening) {
                                    onStopListening()
                                } else if (hasAudioPermission) {
                                    onStartListening()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("speaking_record_btn")
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = capabilities.sttStatusMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = semantic.textSecondary
                    )

                    if (sttError != null) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                            color = semantic.warningContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(sttError, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(10.dp))
                        }
                    }
                }
            }

            item {
                EditorialCard {
                    Text("Transcription et correspondance", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = recognizedText,
                        onValueChange = onManualTranscriptChange,
                        placeholder = { Text("Le texte reconnu apparaît ici (ou saisissez votre transcription)…") },
                        shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("speaking_transcript_input")
                    )

                    if (comparison != null) {
                        Spacer(Modifier.height(12.dp))
                        val pct = ((comparison.exactWordsCount.toFloat() / comparison.totalReferenceWords.coerceAtLeast(1)) * 100).toInt()
                        Surface(
                            shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                            color = if (pct >= 80) semantic.successContainer else semantic.warningContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Correspondance : $pct % (${comparison.exactWordsCount}/${comparison.totalReferenceWords} mots)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(comparison.summaryFeedback, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            item {
                SecondaryButton(
                    text = "Phrase suivante (${(currentIdx % drills.size) + 1}/${drills.size})",
                    onClick = {
                        currentIdx = (currentIdx + 1) % drills.size
                        onManualTranscriptChange("")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
