package com.example.features.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.core.audio.SpeechCapabilities
import com.example.core.database.Dictation
import com.example.core.design.AppIconButton
import com.example.core.design.AppTopBar
import com.example.core.design.CategoryBadge
import com.example.core.design.CefrLevelPill
import com.example.core.design.EditorialCard
import com.example.core.design.EmptyState
import com.example.core.design.PrimaryButton
import com.example.core.design.SecondaryButton
import com.example.core.design.SectionHeader
import com.example.features.LastDictationResultState
import com.example.learning.dictation.DictationMistakeType
import com.example.ui.theme.FrancaisProDimens
import com.example.ui.theme.FrancaisProTextStyles
import com.example.ui.theme.LocalSemanticColors
import kotlinx.coroutines.delay

/**
 * Écran 14 : Dictée immersive — sélection du niveau, grand contrôle audio (Lire / Stop / Rejouer / vitesse + repli visuel), éditeur confortable.
 */
@Composable
fun DictationScreen(
    dictations: List<Dictation>,
    capabilities: SpeechCapabilities,
    speechRate: Float,
    onSpeak: (String, Float) -> Unit,
    onStopSpeak: () -> Unit,
    onEvaluateDictation: (Dictation, String) -> Unit,
    onBack: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    var selectedLevel by remember { mutableStateOf("Tous") }
    val filtered = remember(dictations, selectedLevel) {
        if (selectedLevel == "Tous") dictations else dictations.filter { it.level == selectedLevel }
    }
    var selectedDictationId by remember { mutableStateOf<String?>(null) }
    val active = filtered.firstOrNull { it.id == selectedDictationId } ?: filtered.firstOrNull()

    var userText by remember(active?.id) { mutableStateOf("") }
    var localRate by remember(speechRate) { mutableStateOf(speechRate) }
    var visualPromptActive by remember(active?.id) { mutableStateOf(false) }
    var visualSegmentIdx by remember(active?.id) { mutableIntStateOf(0) }

    val visualSegments = remember(active?.id) {
        active?.referenceText?.split(Regex("(?<=[.!?])\\s+")).orEmpty()
    }

    LaunchedEffect(visualPromptActive, visualSegmentIdx) {
        if (visualPromptActive && visualSegments.isNotEmpty()) {
            delay(4500L)
            if (visualSegmentIdx + 1 < visualSegments.size) {
                visualSegmentIdx++
            } else {
                visualPromptActive = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(
            title = "Dictée",
            subtitle = "Écoute, rédaction et correction mot à mot",
            onBack = {
                onStopSpeak()
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
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Tous", "A1", "A2", "B1", "B2", "C1")) { lvl ->
                        FilterChip(
                            selected = selectedLevel == lvl,
                            onClick = {
                                selectedLevel = lvl
                                selectedDictationId = null
                            },
                            label = { Text(lvl) }
                        )
                    }
                }
            }

            if (active == null) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Mic,
                        title = "Aucune dictée disponible",
                        message = "Sélectionnez un autre niveau CECRL."
                    )
                }
            } else {
                item {
                    EditorialCard(contentPadding = 18.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                CefrLevelPill(level = active.level)
                                CategoryBadge(
                                    label = active.theme,
                                    color = MaterialTheme.colorScheme.primary,
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            }
                            Text("${active.wordCount} mots", style = FrancaisProTextStyles.MonoMetric, color = semantic.textSecondary)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(active.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = capabilities.ttsStatusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = semantic.textSecondary
                        )
                        Spacer(Modifier.height(14.dp))

                        // Grands contrôles de lecture audio
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PrimaryButton(
                                text = "Écouter",
                                icon = Icons.AutoMirrored.Filled.VolumeUp,
                                enabled = capabilities.ttsReady,
                                onClick = { onSpeak(active.referenceText, localRate) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dictation_play_btn")
                            )
                            AppIconButton(
                                icon = Icons.Filled.Replay,
                                contentDescription = "Rejouer plus lentement",
                                enabled = capabilities.ttsReady,
                                containerColor = semantic.surfaceSubtle,
                                onClick = { onSpeak(active.referenceText, (localRate - 0.15f).coerceAtLeast(0.7f)) }
                            )
                            AppIconButton(
                                icon = Icons.Filled.Stop,
                                contentDescription = "Arrêter la lecture",
                                enabled = capabilities.ttsReady,
                                containerColor = semantic.surfaceSubtle,
                                onClick = onStopSpeak
                            )
                        }

                        Spacer(Modifier.height(10.dp))
                        // Sélecteur rapide de vitesse de lecture
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Vitesse :", style = MaterialTheme.typography.labelMedium, color = semantic.textSecondary)
                            listOf(0.8f to "Lente", 0.95f to "Normale", 1.15f to "Rapide").forEach { (rate, label) ->
                                FilterChip(
                                    selected = kotlin.math.abs(localRate - rate) < 0.08f,
                                    onClick = { localRate = rate },
                                    label = { Text(label) }
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        SecondaryButton(
                            text = if (visualPromptActive) "Masquer la lecture visuelle" else "Mode lecture visuelle rythmée",
                            icon = Icons.Filled.Visibility,
                            onClick = {
                                visualSegmentIdx = 0
                                visualPromptActive = !visualPromptActive
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dictation_visual_fallback_btn")
                        )

                        if (visualPromptActive && visualSegments.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        "Segment ${visualSegmentIdx + 1}/${visualSegments.size} — mémorisez puis écrivez :",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        visualSegments[visualSegmentIdx],
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        OutlinedTextField(
                            value = userText,
                            onValueChange = { userText = it },
                            label = { Text("Votre dictée") },
                            placeholder = { Text("Rédigez le texte avec les accents et la ponctuation…") },
                            shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = semantic.border
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .testTag("dictation_input")
                        )

                        Spacer(Modifier.height(14.dp))
                        PrimaryButton(
                            text = "Valider et corriger ma dictée",
                            icon = Icons.Filled.CheckCircle,
                            enabled = userText.isNotBlank(),
                            onClick = {
                                onStopSpeak()
                                onEvaluateDictation(active, userText)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dictation_submit_btn")
                        )
                    }
                }

                item {
                    SectionHeader(title = "Choisir une autre dictée (${filtered.size})")
                }
                items(filtered, key = { it.id }) { d ->
                    EditorialCard(
                        onClick = { selectedDictationId = d.id },
                        containerColor = if (d.id == active.id) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.surface,
                        borderColor = if (d.id == active.id) MaterialTheme.colorScheme.primary else semantic.border
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(d.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            CefrLevelPill(level = d.level)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("${d.theme} · ${d.wordCount} mots", style = MaterialTheme.typography.bodySmall, color = semantic.textSecondary)
                    }
                }
            }
        }
    }
}

/**
 * Écran 15 : Résultat détaillé d'une dictée (Note /20, diff mot-à-mot, règles clés).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DictationResultScreen(
    result: LastDictationResultState?,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    val semantic = LocalSemanticColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(
            title = "Résultat de la dictée",
            subtitle = result?.dictation?.title,
            onBack = onBack
        )

        if (result == null) {
            EmptyState(
                icon = Icons.Filled.Mic,
                title = "Aucun résultat récent",
                message = "Complétez une dictée pour consulter l'analyse détaillée des écarts.",
                actionLabel = "Aller aux dictées",
                onAction = onBack
            )
            return
        }

        val eval = result.evaluation
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = FrancaisProDimens.MaxContentWidth)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                EditorialCard(
                    containerColor = if (eval.scoreOutOf20 >= 14f) semantic.successContainer else semantic.warningContainer,
                    contentPadding = 20.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Note finale", style = MaterialTheme.typography.labelLarge)
                            Text("${eval.scoreOutOf20} / 20", style = FrancaisProTextStyles.Display)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${eval.mistakesCount} faute(s)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${eval.exactWordsCount}/${eval.totalReferenceWords} mots exacts", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(eval.summaryFeedback, style = MaterialTheme.typography.bodyMedium)
                }
            }

            item {
                EditorialCard {
                    Text("Comparaison mot à mot", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        eval.diffs.forEach { d ->
                            when (d.type) {
                                DictationMistakeType.OK -> Text(d.expected.orEmpty(), style = MaterialTheme.typography.bodyMedium)
                                DictationMistakeType.ACCENT_OR_CASE, DictationMistakeType.SPELLING -> {
                                    Surface(shape = RoundedCornerShape(8.dp), color = semantic.dangerContainer) {
                                        Text(
                                            text = "${d.actual} → ${d.expected}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = semantic.danger,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                DictationMistakeType.OMISSION -> {
                                    Surface(shape = RoundedCornerShape(8.dp), color = semantic.warningContainer) {
                                        Text(
                                            text = "[+ ${d.expected}]",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = semantic.warning,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                DictationMistakeType.ADDITION -> {
                                    Surface(shape = RoundedCornerShape(8.dp), color = semantic.dangerContainer) {
                                        Text(
                                            text = d.actual.orEmpty(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = semantic.danger,
                                            textDecoration = TextDecoration.LineThrough,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val mistakesOnly = eval.diffs.filter { it.type != DictationMistakeType.OK }
            if (mistakesOnly.isNotEmpty()) {
                item {
                    EditorialCard {
                        Text("Analyse des fautes (${mistakesOnly.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        mistakesOnly.forEach { m ->
                            Text("• [${m.type.labelFr}] ${m.note}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 3.dp))
                        }
                    }
                }
            }

            item {
                EditorialCard {
                    Text("Règles clés de cette dictée", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    result.dictation.keyRulesPipe.split("|").filter { it.isNotBlank() }.forEach { rule ->
                        Text("• $rule", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }

            item {
                PrimaryButton(
                    text = "Nouvelle dictée",
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
