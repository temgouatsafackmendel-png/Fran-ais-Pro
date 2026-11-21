package com.example.features.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.database.CorrectionHistory
import com.example.core.database.DictationAttempt
import com.example.core.database.ExerciseAttempt
import com.example.core.database.Lesson
import com.example.core.database.LessonProgress
import com.example.core.database.VocabularyProgress
import com.example.core.design.AppTopBar
import com.example.core.design.CategoryBadge
import com.example.core.design.DomainSparkline
import com.example.core.design.EditorialCard
import com.example.core.design.MultiSkillTrendChart
import com.example.core.design.PrimaryButton
import com.example.core.design.SecondaryButton
import com.example.core.design.StructuredSectionTitle
import com.example.learning.progression.DailySkillPoint
import com.example.learning.progression.PerformanceOverviewData
import com.example.learning.progression.PerformanceTimeframe
import com.example.learning.progression.PerformanceTrendsEngine
import com.example.learning.progression.SkillDomain
import com.example.learning.progression.SkillTrendSummary
import com.example.navigation.Routes
import com.example.ui.theme.FrancaisProDimens
import com.example.ui.theme.FrancaisProTextStyles
import com.example.ui.theme.LocalSemanticColors

/**
 * Page Aperçu des performances : tendances réelles sur 30 jours en Grammaire, Vocabulaire et Écriture.
 * Alimentée exclusivement par les données locales Room (aucune donnée simulée).
 */
@Composable
fun PerformanceOverviewScreen(
    lessons: List<Lesson>,
    lessonProgress: List<LessonProgress>,
    exerciseAttempts: List<ExerciseAttempt>,
    vocabularyProgress: List<VocabularyProgress>,
    correctionHistory: List<CorrectionHistory>,
    dictationAttempts: List<DictationAttempt>,
    onNavigate: (String) -> Unit,
    onTrainGrammar: () -> Unit,
    onBack: () -> Unit,
    nowMillis: Long = System.currentTimeMillis()
) {
    val semantic = LocalSemanticColors.current
    val primary = MaterialTheme.colorScheme.primary

    var selectedTimeframeName by rememberSaveable { mutableStateOf(PerformanceTimeframe.DAYS_30.name) }
    val timeframe = remember(selectedTimeframeName) {
        PerformanceTimeframe.entries.firstOrNull { it.name == selectedTimeframeName }
            ?: PerformanceTimeframe.DAYS_30
    }

    var visibleDomains by remember { mutableStateOf(SkillDomain.entries.toSet()) }

    val overview: PerformanceOverviewData = remember(
        lessons,
        lessonProgress,
        exerciseAttempts,
        vocabularyProgress,
        correctionHistory,
        dictationAttempts,
        timeframe,
        nowMillis
    ) {
        PerformanceTrendsEngine.compute(
            lessons = lessons,
            lessonProgress = lessonProgress,
            exerciseAttempts = exerciseAttempts,
            vocabularyProgress = vocabularyProgress,
            correctionHistory = correctionHistory,
            dictationAttempts = dictationAttempts,
            timeframe = timeframe,
            nowMillis = nowMillis
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(
            title = "Aperçu des performances",
            subtitle = "Tendances Grammaire, Vocabulaire & Écriture",
            onBack = onBack
        )

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val hPadding = FrancaisProDimens.screenHorizontalPadding(maxWidth)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = FrancaisProDimens.MaxContentWidth)
                    .testTag("performance_overview_list"),
                contentPadding = PaddingValues(horizontal = hPadding, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. En-tête héroïque avec score global, delta sur la période et sélecteur 7j / 14j / 30j
                item(key = "perf_hero") {
                    PerformanceHeroCard(
                        overview = overview,
                        selectedTimeframe = timeframe,
                        onSelectTimeframe = { selectedTimeframeName = it.name }
                    )
                }

                // 2. Invitation transparente si aucune donnée n'est encore enregistrée
                if (!overview.hasAnyActivity) {
                    item(key = "perf_empty_guide") {
                        EditorialCard(
                            borderColor = primary.copy(alpha = 0.35f),
                            modifier = Modifier.testTag("perf_empty_callout")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.Insights,
                                            contentDescription = null,
                                            tint = primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Vos courbes démarrent dès votre première session",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "Réalisez un exercice, une révision de vocabulaire ou une analyse dans le correcteur pour tracer vos progrès réels sur 30 jours.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = semantic.textSecondary
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            PrimaryButton(
                                text = "Lancer un entraînement rapide",
                                icon = Icons.Filled.PlayArrow,
                                onClick = onTrainGrammar,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("perf_empty_start_btn")
                            )
                        }
                    }
                }

                // 3. Graphique multi-courbes interactif sur 7 / 14 / 30 jours
                item(key = "perf_multi_chart") {
                    EditorialCard {
                        StructuredSectionTitle(
                            title = "Tendances sur ${timeframe.days} jours",
                            icon = Icons.AutoMirrored.Filled.ShowChart,
                            accentColor = primary,
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Touchez ou glissez le doigt sur le graphique pour inspecter chaque journée, ou filtrez les compétences ci-dessous.",
                            style = MaterialTheme.typography.bodySmall,
                            color = semantic.textSecondary
                        )
                        Spacer(Modifier.height(14.dp))
                        MultiSkillTrendChart(
                            points = overview.points,
                            visibleDomains = visibleDomains,
                            onToggleDomain = { domain ->
                                visibleDomains = if (domain in visibleDomains) {
                                    if (visibleDomains.size > 1) visibleDomains - domain else visibleDomains
                                } else {
                                    visibleDomains + domain
                                }
                            }
                        )
                    }
                }

                // 4. Détail par compétence (Grammaire, Vocabulaire, Écriture)
                item(key = "perf_domains_header") {
                    StructuredSectionTitle(
                        title = "Détail par compétence",
                        icon = Icons.Filled.CalendarMonth,
                        accentColor = semantic.grammarColor,
                        containerColor = semantic.grammarContainer
                    )
                }

                item(key = "perf_domain_grammar") {
                    SkillDomainTrendCard(
                        summary = overview.grammarSummary,
                        points = overview.points,
                        icon = Icons.Filled.AccountTree,
                        accentColor = semantic.grammarColor,
                        containerColor = semantic.grammarContainer,
                        ctaLabel = "Travailler la grammaire",
                        onTrain = onTrainGrammar
                    )
                }

                item(key = "perf_domain_vocabulary") {
                    SkillDomainTrendCard(
                        summary = overview.vocabularySummary,
                        points = overview.points,
                        icon = Icons.Filled.AutoAwesome,
                        accentColor = semantic.success,
                        containerColor = semantic.successContainer,
                        ctaLabel = "Réviser le vocabulaire",
                        onTrain = { onNavigate(Routes.VOCABULARY) }
                    )
                }

                item(key = "perf_domain_writing") {
                    SkillDomainTrendCard(
                        summary = overview.writingSummary,
                        points = overview.points,
                        icon = Icons.Filled.EditNote,
                        accentColor = primary,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ctaLabel = "Pratiquer l'écriture",
                        onTrain = { onNavigate(Routes.CORRECTOR) }
                    )
                }

                // 5. Synthèse analytique & passerelle vers Statistiques complètes
                item(key = "perf_insights_footer") {
                    EditorialCard {
                        Text(
                            text = "Synthèse de la période",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        val strongest = overview.strongestDomain
                        val focus = overview.focusDomain
                        val insightText = when {
                            strongest == null ->
                                "Aucune compétence évaluée sur les ${timeframe.days} derniers jours. Chaque exercice, révision ou correction enrichit automatiquement cette vue."
                            focus != null && focus.domain != strongest.domain ->
                                "Point fort actuel : ${strongest.domain.labelFr} (${strongest.currentScore} %). Axe prioritaire recommandé : ${focus.domain.labelFr} (${focus.currentScore} %) pour équilibrer votre profil."
                            else ->
                                "Compétence active : ${strongest.domain.labelFr} (${strongest.currentScore} %). Variez avec le vocabulaire ou le studio d'écriture pour suivre les 3 courbes en parallèle."
                        }
                        Text(
                            text = insightText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = semantic.textSecondary
                        )
                        Spacer(Modifier.height(12.dp))
                        SecondaryButton(
                            text = "Voir les statistiques hebdomadaires & erreurs",
                            icon = Icons.Filled.BarChart,
                            onClick = { onNavigate(Routes.STATISTICS) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("perf_open_statistics_btn")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformanceHeroCard(
    overview: PerformanceOverviewData,
    selectedTimeframe: PerformanceTimeframe,
    onSelectTimeframe: (PerformanceTimeframe) -> Unit
) {
    val semantic = LocalSemanticColors.current
    val heroStart = semantic.navyHeader
    val heroEnd = semantic.primaryBlue

    Surface(
        shape = RoundedCornerShape(FrancaisProDimens.RadiusCard),
        color = heroStart,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("perf_hero_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(heroStart, heroEnd)
                    )
                )
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Indice de maîtrise (${selectedTimeframe.days} jours)",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.82f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = overview.overallScore?.let { "$it %" } ?: "—",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            DeltaPillOnDark(delta = overview.overallDelta, hasData = overview.overallScore != null)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                        color = Color.White.copy(alpha = 0.14f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "${overview.activeDaysCount}/${selectedTimeframe.days} j",
                                style = FrancaisProTextStyles.MonoMetric,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${overview.totalSessionsCount} act.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.80f)
                            )
                        }
                    }
                }

                // Sélecteur de période : 7 j · 14 j · 30 j
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(FrancaisProDimens.RadiusButton))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PerformanceTimeframe.entries.forEach { option ->
                        val active = option == selectedTimeframe
                        Surface(
                            shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                            color = if (active) Color.White else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(FrancaisProDimens.RadiusSmall))
                                .clickable(role = Role.Tab) { onSelectTimeframe(option) }
                                .testTag("perf_range_${option.days}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = option.labelFr,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                    color = if (active) heroStart else Color.White.copy(alpha = 0.90f)
                                )
                            }
                        }
                    }
                }

                // Micro-légende honnête
                Text(
                    text = "Calculé en temps réel depuis votre base locale hors ligne.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.72f)
                )
            }
        }
    }
}

@Composable
private fun DeltaPillOnDark(delta: Int, hasData: Boolean) {
    val label = when {
        !hasData -> "En attente"
        delta > 0 -> "+$delta pts"
        delta < 0 -> "$delta pts"
        else -> "Stable"
    }
    val icon = when {
        !hasData || delta == 0 -> Icons.AutoMirrored.Filled.TrendingFlat
        delta > 0 -> Icons.AutoMirrored.Filled.TrendingUp
        else -> Icons.AutoMirrored.Filled.TrendingDown
    }
    Surface(
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.18f),
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SkillDomainTrendCard(
    summary: SkillTrendSummary,
    points: List<DailySkillPoint>,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    ctaLabel: String,
    onTrain: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    val scores = remember(points, summary.domain) { points.map { it.scoreFor(summary.domain) } }

    val (deltaLabel, deltaColor, deltaBg) = when {
        !summary.hasData -> Triple("Aucune donnée", semantic.textSecondary, semantic.surfaceSubtle)
        summary.deltaPoints > 0 -> Triple("+${summary.deltaPoints} pts", semantic.success, semantic.successContainer)
        summary.deltaPoints < 0 -> Triple("${summary.deltaPoints} pts", semantic.danger, semantic.dangerContainer)
        else -> Triple("0 pt · stable", semantic.textSecondary, semantic.surfaceSubtle)
    }

    EditorialCard(
        modifier = Modifier.testTag("perf_domain_card_${summary.domain.name}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = containerColor,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = summary.domain.labelFr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = summary.domain.sourcesHintFr,
                        style = MaterialTheme.typography.labelSmall,
                        color = semantic.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = summary.currentScore?.let { "$it %" } ?: "—",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (summary.hasData) accentColor else semantic.textSecondary
                )
                CategoryBadge(
                    label = deltaLabel,
                    color = deltaColor,
                    containerColor = deltaBg
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        DomainSparkline(
            scores = scores,
            accentColor = accentColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${summary.totalSessions} activité(s) · ${summary.activeDays} j actif(s)",
                style = MaterialTheme.typography.labelMedium,
                color = semantic.textSecondary
            )
            Surface(
                onClick = onTrain,
                shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                color = containerColor.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.30f)),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("perf_train_${summary.domain.name}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = ctaLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                    Spacer(Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
