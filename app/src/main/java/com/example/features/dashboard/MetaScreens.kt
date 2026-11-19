package com.example.features.dashboard

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.database.Achievement
import com.example.core.database.CorrectionHistory
import com.example.core.database.DailyGoal
import com.example.core.database.DictationAttempt
import com.example.core.database.ExerciseAttempt
import com.example.core.database.Favorite
import com.example.core.database.LearningStatistics
import com.example.core.design.AppIconButton
import com.example.core.design.AppTopBar
import com.example.core.design.CategoryBadge
import com.example.core.design.ConfirmationDialog
import com.example.core.design.EditorialCard
import com.example.core.design.EmptyState
import com.example.core.design.ErrorCategoryBreakdownChart
import com.example.core.design.ScoreEvolutionLineChart
import com.example.core.design.SectionHeader
import com.example.core.design.SkillBarChart
import com.example.core.design.WeeklyMinutesBarChart
import com.example.learning.progression.LinguisticProfileSummary
import com.example.ui.theme.FrancaisProDimens
import com.example.ui.theme.FrancaisProTextStyles
import com.example.ui.theme.LocalSemanticColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Écran 19 : Statistiques détaillées avec 4 graphiques réels alimentés par Room.
 * 1. Progression hebdomadaire · 2. Évolution des scores · 3. Répartition des erreurs · 4. Compétences maîtrisées.
 */
@Composable
fun StatisticsScreen(
    adaptiveSummary: LinguisticProfileSummary,
    statistics: List<LearningStatistics>,
    dailyGoals: List<DailyGoal>,
    dailyTargetMinutes: Int,
    exerciseAttempts: List<ExerciseAttempt>,
    dictationAttempts: List<DictationAttempt>,
    achievements: List<Achievement>,
    onBack: () -> Unit
) {
    val semantic = LocalSemanticColors.current

    // 1. Données réelles des 7 derniers jours (DailyGoal)
    val weeklyDays = remember(dailyGoals) {
        val todayEpoch = System.currentTimeMillis() / 86_400_000L
        val byDay = dailyGoals.associateBy { it.epochDay }
        val dayFmt = SimpleDateFormat("EE", Locale.FRANCE)
        (6 downTo 0).map { offset ->
            val epoch = todayEpoch - offset
            val label = dayFmt.format(Date(epoch * 86_400_000L)).take(2).replaceFirstChar { it.uppercase() }
            label to (byDay[epoch]?.completedMinutes ?: 0)
        }
    }

    // 2. Courbe réelle d'évolution des scores (exercices + dictées)
    val scoreSeries = remember(exerciseAttempts, dictationAttempts) {
        val combined = buildList {
            exerciseAttempts.forEach { add(it.attemptedAt to if (it.isCorrect) 100 else 0) }
            dictationAttempts.forEach { add(it.attemptedAt to ((it.scoreOutOf20 / 20f) * 100f).toInt()) }
        }.sortedBy { it.first }.takeLast(10)
        combined.map { it.second }
    }

    // 3. Répartition réelle des erreurs par catégorie linguistique
    val errorBreakdown = remember(statistics, semantic) {
        statistics.map { stat ->
            val color = when {
                stat.skillCategory.contains("Ortho", ignoreCase = true) -> semantic.spellingColor
                stat.skillCategory.contains("Gram", ignoreCase = true) || stat.skillCategory.contains("Conj", ignoreCase = true) -> semantic.grammarColor
                stat.skillCategory.contains("Synt", ignoreCase = true) || stat.skillCategory.contains("Ponc", ignoreCase = true) -> semantic.syntaxColor
                else -> semantic.styleColor
            }
            Triple(stat.skillCategory, stat.errorCount, color)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(
            title = "Statistiques",
            subtitle = "Progression réelle et badges",
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
            // Graphique 1 : Progression hebdomadaire
            item {
                EditorialCard {
                    Text("Progression hebdomadaire", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Minutes d'apprentissage par jour (objectif : $dailyTargetMinutes min)",
                        style = MaterialTheme.typography.bodySmall,
                        color = semantic.textSecondary
                    )
                    Spacer(Modifier.height(14.dp))
                    WeeklyMinutesBarChart(days = weeklyDays, targetMinutes = dailyTargetMinutes)
                }
            }

            // Graphique 2 : Évolution des scores
            item {
                EditorialCard {
                    Text("Évolution des scores", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Dernières évaluations enregistrées (exercices et dictées)",
                        style = MaterialTheme.typography.bodySmall,
                        color = semantic.textSecondary
                    )
                    Spacer(Modifier.height(12.dp))
                    ScoreEvolutionLineChart(scores = scoreSeries)
                }
            }

            // Graphique 3 : Répartition des erreurs
            item {
                EditorialCard {
                    Text("Répartition des erreurs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    ErrorCategoryBreakdownChart(items = errorBreakdown)
                }
            }

            // Graphique 4 : Compétences maîtrisées
            item {
                EditorialCard {
                    Text("Compétences maîtrisées", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    SkillBarChart(items = statistics.map { it.skillCategory to it.masteryPercent })
                }
            }

            item {
                EditorialCard {
                    Text("Bilan de niveau", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Niveau estimé : ${adaptiveSummary.estimatedLevel} (${adaptiveSummary.levelLabelFr}) · Score global : ${adaptiveSummary.globalScore}/100",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        adaptiveSummary.levelDisclaimer,
                        style = MaterialTheme.typography.bodySmall,
                        color = semantic.textSecondary
                    )
                }
            }

            item {
                SectionHeader(title = "Réalisations (${achievements.count { it.isUnlocked }}/${achievements.size})")
            }

            items(achievements, key = { it.id }) { ach ->
                EditorialCard(
                    containerColor = if (ach.isUnlocked) semantic.successContainer else MaterialTheme.colorScheme.surface
                ) {
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
                            Icon(
                                Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                tint = if (ach.isUnlocked) semantic.success else semantic.textSecondary
                            )
                            Column {
                                Text(ach.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(ach.description, style = MaterialTheme.typography.bodySmall, color = semantic.textSecondary)
                            }
                        }
                        CategoryBadge(
                            label = if (ach.isUnlocked) "Débloqué" else "En cours",
                            color = if (ach.isUnlocked) semantic.success else semantic.textSecondary,
                            containerColor = if (ach.isUnlocked) MaterialTheme.colorScheme.surface else semantic.surfaceSubtle
                        )
                    }
                }
            }
        }
    }
}

/**
 * Écran 20 : Historique local avec ConfirmationDialog avant suppression.
 */
@Composable
fun HistoryScreen(
    correctionHistory: List<CorrectionHistory>,
    dictationAttempts: List<DictationAttempt>,
    onDeleteCorrection: (Long) -> Unit,
    onLoadInCorrector: (String) -> Unit,
    onBack: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE) }
    val semantic = LocalSemanticColors.current
    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }

    pendingDeleteId?.let { idToDelete ->
        ConfirmationDialog(
            title = "Supprimer cette analyse ?",
            message = "Cette entrée sera retirée définitivement de votre historique local.",
            confirmLabel = "Supprimer",
            onConfirm = {
                onDeleteCorrection(idToDelete)
                pendingDeleteId = null
            },
            onDismiss = { pendingDeleteId = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(
            title = "Historique",
            subtitle = "${correctionHistory.size} analyse(s) · ${dictationAttempts.size} dictée(s)",
            onBack = onBack
        )

        if (correctionHistory.isEmpty() && dictationAttempts.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.History,
                title = "Historique vide",
                message = "Vos analyses du correcteur, ateliers de rédaction et dictées apparaîtront ici."
            )
            return
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = FrancaisProDimens.MaxContentWidth)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (correctionHistory.isNotEmpty()) {
                item {
                    SectionHeader(title = "Analyses et rédactions")
                }
                items(correctionHistory, key = { it.id }) { item ->
                    EditorialCard(onClick = { onLoadInCorrector(item.correctedText) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryBadge(
                                label = "${item.sourceModule} · ${item.errorsFoundCount} point(s)",
                                color = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                            AppIconButton(
                                icon = Icons.Filled.DeleteOutline,
                                contentDescription = "Supprimer cette entrée",
                                tint = semantic.danger,
                                onClick = { pendingDeleteId = item.id }
                            )
                        }
                        Text(
                            text = item.correctedText,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = formatter.format(Date(item.createdAt)),
                            style = FrancaisProTextStyles.MonoMetric,
                            color = semantic.textSecondary
                        )
                    }
                }
            }

            if (dictationAttempts.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(6.dp))
                    SectionHeader(title = "Dictées récentes")
                }
                items(dictationAttempts, key = { "dict_${it.id}" }) { att ->
                    EditorialCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Note : ${att.scoreOutOf20} / 20", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(formatter.format(Date(att.attemptedAt)), style = FrancaisProTextStyles.MonoMetric, color = semantic.textSecondary)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(att.summaryFeedback, style = MaterialTheme.typography.bodySmall, color = semantic.textSecondary)
                    }
                }
            }
        }
    }
}

/**
 * Écran 21 : Favoris (mots, leçons, verbes).
 */
@Composable
fun FavoritesScreen(
    favorites: List<Favorite>,
    onOpenFavorite: (Favorite) -> Unit,
    onRemoveFavorite: (Favorite) -> Unit,
    onBack: () -> Unit
) {
    val semantic = LocalSemanticColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(
            title = "Favoris",
            subtitle = "${favorites.size} élément(s) enregistré(s)",
            onBack = onBack
        )

        if (favorites.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Favorite,
                title = "Aucun favori pour le moment",
                message = "Enregistrez des mots, des leçons ou des verbes pour les retrouver immédiatement."
            )
            return
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = FrancaisProDimens.MaxContentWidth)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(favorites, key = { "${it.itemType}_${it.itemId}" }) { fav ->
                EditorialCard(onClick = { onOpenFavorite(fav) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            CategoryBadge(
                                label = when (fav.itemType) {
                                    "WORD" -> "Mot"
                                    "LESSON" -> "Leçon"
                                    "VERB" -> "Verbe"
                                    else -> fav.itemType
                                },
                                color = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(fav.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                fav.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = semantic.textSecondary,
                                maxLines = 2
                            )
                        }
                        AppIconButton(
                            icon = Icons.Filled.DeleteOutline,
                            contentDescription = "Retirer des favoris",
                            tint = semantic.danger,
                            onClick = { onRemoveFavorite(fav) }
                        )
                    }
                }
            }
        }
    }
}
