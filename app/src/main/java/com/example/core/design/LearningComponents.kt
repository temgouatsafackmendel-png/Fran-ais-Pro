package com.example.core.design

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.linguistics.model.CorrectionCategory
import com.example.linguistics.model.CorrectionIssue
import com.example.ui.theme.FrancaisProDimens
import com.example.ui.theme.FrancaisProTextStyles
import com.example.ui.theme.LocalReducedMotion
import com.example.ui.theme.LocalSemanticColors
import com.example.ui.theme.SemanticColors

/**
 * Résout le couple (couleur principale, conteneur doux) selon la catégorie linguistique du correcteur :
 * Rouge = Orthographe · Orange = Grammaire/Conjugaison · Violet = Syntaxe/Ponctuation · Bleu = Style.
 */
fun issueSemanticPalette(category: CorrectionCategory, semantic: SemanticColors): Pair<Color, Color> = when (category) {
    CorrectionCategory.ORTHOGRAPHE -> semantic.spellingColor to semantic.spellingContainer
    CorrectionCategory.GRAMMAIRE, CorrectionCategory.CONJUGAISON -> semantic.grammarColor to semantic.grammarContainer
    CorrectionCategory.SYNTAXE, CorrectionCategory.PONCTUATION -> semantic.syntaxColor to semantic.syntaxContainer
    CorrectionCategory.STYLE -> semantic.styleColor to semantic.styleContainer
}

/**
 * 7. ProgressCard — Carte principale du tableau de bord (niveau, score, objectif du jour, CTA).
 */
@Composable
fun ProgressCard(
    level: String,
    levelLabel: String,
    globalScore: Int,
    streakDays: Int,
    todayMinutes: Int,
    dailyTargetMinutes: Int,
    ctaLabel: String,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    val reducedMotion = LocalReducedMotion.current
    val rawProgress = (todayMinutes.toFloat() / dailyTargetMinutes.coerceAtLeast(5)).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(if (reducedMotion) 0 else 240),
        label = "daily_goal_progress"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(FrancaisProDimens.RadiusSurface),
        color = semantic.navyHeader,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(FrancaisProDimens.Space20)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                    color = Color.White.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Niveau $level · $levelLabel",
                        style = MaterialTheme.typography.labelMedium,
                        color = semantic.onNavyHeader,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                    color = Color.White.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = "Série active",
                            tint = semantic.warning,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "$streakDays j",
                            style = FrancaisProTextStyles.MonoMetric,
                            color = semantic.onNavyHeader
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Score de maîtrise",
                        style = MaterialTheme.typography.bodySmall,
                        color = semantic.onNavyHeader.copy(alpha = 0.72f)
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$globalScore",
                            style = FrancaisProTextStyles.Display,
                            color = semantic.onNavyHeader
                        )
                        Text(
                            text = " / 100",
                            style = MaterialTheme.typography.titleMedium,
                            color = semantic.onNavyHeader.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Objectif du jour",
                        style = MaterialTheme.typography.bodySmall,
                        color = semantic.onNavyHeader.copy(alpha = 0.72f)
                    )
                    Text(
                        text = "$todayMinutes / $dailyTargetMinutes min",
                        style = MaterialTheme.typography.titleSmall,
                        color = semantic.onNavyHeader,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            val trackColor = Color.White.copy(alpha = 0.16f)
            val fillColor = semantic.success
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            ) {
                val radius = CornerRadius(size.height / 2f, size.height / 2f)
                drawRoundRect(color = trackColor, size = size, cornerRadius = radius)
                val fillWidth = size.width * animatedProgress
                if (fillWidth > 0f) {
                    drawRoundRect(color = fillColor, size = Size(fillWidth, size.height), cornerRadius = radius)
                }
            }

            Spacer(Modifier.height(16.dp))

            PrimaryButton(
                text = ctaLabel,
                onClick = onContinueClick,
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                containerColor = semantic.primaryBlue,
                contentColor = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_continue_learning_btn")
            )
        }
    }
}

/**
 * 8. LevelCard — Carte de parcours CECRL (Académie).
 */
@Composable
fun LevelCard(
    level: String,
    title: String,
    difficultyLabel: String,
    completedCount: Int,
    totalCount: Int,
    nextLessonTitle: String?,
    skillsPreview: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    val ratio = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount
    val percent = (ratio * 100).toInt()
    val isMastered = totalCount > 0 && completedCount == totalCount

    EditorialCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                CefrLevelPill(level = level)
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$totalCount leçons · $difficultyLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = semantic.textSecondary
                    )
                }
            }
            if (isMastered) {
                CategoryBadge("Validé", semantic.success, semantic.successContainer, icon = Icons.Filled.CheckCircle)
            } else {
                Text(
                    text = "$percent %",
                    style = FrancaisProTextStyles.MonoMetric,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        val trackColor = semantic.surfaceSubtle
        val barColor = if (isMastered) semantic.success else MaterialTheme.colorScheme.primary
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
        ) {
            val r = CornerRadius(size.height / 2f, size.height / 2f)
            drawRoundRect(color = trackColor, size = size, cornerRadius = r)
            if (ratio > 0f) {
                drawRoundRect(color = barColor, size = Size(size.width * ratio, size.height), cornerRadius = r)
            }
        }

        if (skillsPreview.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Compétences : ${skillsPreview.joinToString(" · ")}",
                style = MaterialTheme.typography.bodySmall,
                color = semantic.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (!nextLessonTitle.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                color = semantic.surfaceSubtle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isMastered) "Réviser : $nextLessonTitle" else "Suivante : $nextLessonTitle",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * 9. LessonCard — Carte de leçon claire avec statut, thème et durée.
 */
@Composable
fun LessonCard(
    title: String,
    summary: String,
    theme: String,
    level: String,
    estimatedMinutes: Int,
    isCompleted: Boolean,
    scorePercent: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    EditorialCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                CefrLevelPill(level = level)
                CategoryBadge(
                    label = theme,
                    color = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
            if (isCompleted) {
                CategoryBadge(
                    label = if (scorePercent != null) "Validée · $scorePercent %" else "Validée",
                    color = semantic.success,
                    containerColor = semantic.successContainer,
                    icon = Icons.Filled.CheckCircle
                )
            } else {
                Text(
                    text = "$estimatedMinutes min",
                    style = FrancaisProTextStyles.MonoMetric,
                    color = semantic.textSecondary
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = summary,
            style = MaterialTheme.typography.bodySmall,
            color = semantic.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 10. ExerciseCard — Carte d'exercice sélectionnable dans la banque.
 */
@Composable
fun ExerciseCard(
    level: String,
    typeLabel: String,
    category: String,
    promptPreview: String,
    points: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    EditorialCard(
        onClick = onClick,
        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        else MaterialTheme.colorScheme.surface,
        borderColor = if (isSelected) MaterialTheme.colorScheme.primary else semantic.border,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                CefrLevelPill(level = level)
                Text(
                    text = "$category · $typeLabel",
                    style = MaterialTheme.typography.labelMedium,
                    color = semantic.textSecondary
                )
            }
            Text(
                text = "+$points XP",
                style = FrancaisProTextStyles.MonoMetric,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = promptPreview,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 11. CorrectionCard — Carte d'anomalie linguistique avec code couleur sémantique, explication et actions.
 */
@Composable
fun CorrectionCard(
    issue: CorrectionIssue,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    onInspect: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    val (catColor, catBg) = issueSemanticPalette(issue.category, semantic)

    EditorialCard(
        onClick = onInspect,
        borderColor = catColor.copy(alpha = 0.35f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryBadge(label = issue.category.labelFr, color = catColor, containerColor = catBg)
            Text(
                text = "Confiance ${(issue.confidence * 100).toInt()} %",
                style = FrancaisProTextStyles.MonoMetric,
                color = semantic.textSecondary
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = semantic.dangerContainer
            ) {
                Text(
                    text = issue.originalFragment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = semantic.danger,
                    textDecoration = TextDecoration.LineThrough,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Text("→", style = MaterialTheme.typography.titleSmall, color = semantic.textSecondary)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = semantic.successContainer
            ) {
                Text(
                    text = issue.suggestion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = semantic.success,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = issue.explanation,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Exemple : ${issue.example}",
            style = MaterialTheme.typography.bodySmall,
            color = semantic.textSecondary
        )
        if (issue.isAmbiguous) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Suggestion contextuelle : vérification manuelle recommandée.",
                style = MaterialTheme.typography.labelSmall,
                color = semantic.warning
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (issue.suggestion != issue.originalFragment) {
                PrimaryButton(
                    text = "Accepter",
                    onClick = onAccept,
                    icon = Icons.Filled.Check,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("issue_accept_${issue.id}")
                )
            }
            SecondaryButton(
                text = "Ignorer",
                onClick = onDismiss,
                icon = Icons.Filled.Close,
                modifier = Modifier
                    .weight(1f)
                    .testTag("issue_dismiss_${issue.id}")
            )
        }
    }
}

/**
 * 12. ErrorHighlightText — Texte interactif où chaque faute est surlignée et soulignée selon sa catégorie.
 * Toucher une faute ouvre immédiatement le BottomSheet d'explication pédagogique.
 */
@Suppress("DEPRECATION")
@Composable
fun ErrorHighlightText(
    text: String,
    issues: List<CorrectionIssue>,
    onIssueClick: (CorrectionIssue) -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    val sortedIssues = issues
        .filter { it.startIndex >= 0 && it.endIndex <= text.length && it.startIndex < it.endIndex }
        .sortedBy { it.startIndex }

    val annotated = buildAnnotatedString {
        var cursor = 0
        sortedIssues.forEach { issue ->
            if (issue.startIndex >= cursor) {
                if (issue.startIndex > cursor) {
                    append(text.substring(cursor, issue.startIndex))
                }
                val (fg, bg) = issueSemanticPalette(issue.category, semantic)
                pushStringAnnotation(tag = "ISSUE_ID", annotation = issue.id)
                pushStyle(
                    SpanStyle(
                        color = fg,
                        background = bg,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline
                    )
                )
                append(text.substring(issue.startIndex, issue.endIndex))
                pop()
                pop()
                cursor = issue.endIndex
            }
        }
        if (cursor < text.length) {
            append(text.substring(cursor))
        }
    }

    ClickableText(
        text = annotated,
        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        modifier = modifier.testTag("error_highlight_text"),
        onClick = { offset ->
            annotated.getStringAnnotations(tag = "ISSUE_ID", start = offset, end = offset)
                .firstOrNull()
                ?.let { ann ->
                    sortedIssues.firstOrNull { it.id == ann.item }?.let(onIssueClick)
                }
        }
    )
}

/**
 * 13. StatisticsCard — Indicateur chiffré propre avec icône et accent sémantique.
 */
@Composable
fun StatisticsCard(
    value: String,
    label: String,
    icon: ImageVector,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    accentContainer: Color = MaterialTheme.colorScheme.primaryContainer,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    EditorialCard(modifier = modifier, contentPadding = 14.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = semantic.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Graphique 1 : Progression hebdomadaire (barres verticales — minutes sur 7 jours réels).
 */
@Composable
fun WeeklyMinutesBarChart(
    days: List<Pair<String, Int>>,
    targetMinutes: Int,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    val primary = MaterialTheme.colorScheme.primary
    val success = semantic.success
    val track = semantic.surfaceSubtle
    val maxVal = maxOf(targetMinutes, days.maxOfOrNull { it.second } ?: 1, 10)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp)
        ) {
            val count = days.size.coerceAtLeast(1)
            val slotW = size.width / count
            val barW = (slotW * 0.48f).coerceAtMost(28.dp.toPx())
            days.forEachIndexed { idx, (_, mins) ->
                val x = idx * slotW + (slotW - barW) / 2f
                val corner = CornerRadius(barW / 2.5f, barW / 2.5f)
                drawRoundRect(
                    color = track,
                    topLeft = Offset(x, 0f),
                    size = Size(barW, size.height),
                    cornerRadius = corner
                )
                val ratio = (mins.toFloat() / maxVal).coerceIn(0f, 1f)
                val barH = size.height * ratio
                if (barH > 0f) {
                    drawRoundRect(
                        color = if (mins >= targetMinutes) success else primary,
                        topLeft = Offset(x, size.height - barH),
                        size = Size(barW, barH),
                        cornerRadius = corner
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            days.forEach { (dayLabel, mins) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(dayLabel, style = MaterialTheme.typography.labelSmall, color = semantic.textSecondary)
                    Text("${mins}m", style = FrancaisProTextStyles.MonoMetric, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

/**
 * Graphique 2 : Évolution des scores (courbe lissée sur les dernières tentatives réelles).
 */
@Composable
fun ScoreEvolutionLineChart(
    scores: List<Int>,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = semantic.border

    if (scores.isEmpty()) {
        Text(
            text = "Complétez vos premiers exercices ou dictées pour afficher l'évolution réelle de vos scores.",
            style = MaterialTheme.typography.bodySmall,
            color = semantic.textSecondary
        )
        return
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val pad = 12.dp.toPx()
        val chartW = (size.width - pad * 2).coerceAtLeast(1f)
        val chartH = (size.height - pad * 2).coerceAtLeast(1f)
        // Lignes repères 0 %, 50 %, 100 %
        listOf(0f, 0.5f, 1f).forEach { level ->
            val y = pad + chartH * (1f - level)
            drawLine(gridColor, Offset(pad, y), Offset(size.width - pad, y), strokeWidth = 1.dp.toPx())
        }
        val stepX = if (scores.size <= 1) 0f else chartW / (scores.size - 1)
        val points = scores.mapIndexed { idx, s ->
            val x = if (scores.size == 1) size.width / 2f else pad + idx * stepX
            val y = pad + chartH * (1f - (s.coerceIn(0, 100) / 100f))
            Offset(x, y)
        }
        if (points.size > 1) {
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(path, color = lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        }
        points.forEach { pt ->
            drawCircle(color = lineColor, radius = 4.5.dp.toPx(), center = pt)
        }
    }
}

/**
 * Graphique 3 : Répartition des erreurs par catégorie (Orthographe, Grammaire, Conjugaison, Syntaxe, Style).
 */
@Composable
fun ErrorCategoryBreakdownChart(
    items: List<Triple<String, Int, Color>>,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    val total = items.sumOf { it.second }
    if (total == 0) {
        Text(
            text = "Aucune erreur enregistrée pour le moment. Analysez un texte ou faites un exercice pour voir la répartition.",
            style = MaterialTheme.typography.bodySmall,
            color = semantic.textSecondary
        )
        return
    }
    val track = semantic.surfaceSubtle
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { (label, count, color) ->
            val pct = ((count.toFloat() / total) * 100).toInt()
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text("$count ($pct %)", style = FrancaisProTextStyles.MonoMetric, color = color)
                }
                Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
                    val r = CornerRadius(size.height / 2f, size.height / 2f)
                    drawRoundRect(track, size = size, cornerRadius = r)
                    val w = size.width * (count.toFloat() / total)
                    if (w > 0f) drawRoundRect(color, size = Size(w, size.height), cornerRadius = r)
                }
            }
        }
    }
}

/**
 * Graphique 4 : Compétences maîtrisées (barres horizontales par domaine).
 */
@Composable
fun SkillBarChart(
    items: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val semantic = LocalSemanticColors.current
    val trackColor = semantic.surfaceSubtle

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { (label, percent) ->
            val barColor = when {
                percent >= 75 -> semantic.success
                percent >= 45 -> primaryColor
                percent > 0 -> semantic.warning
                else -> semantic.textSecondary.copy(alpha = 0.45f)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text("$percent %", style = FrancaisProTextStyles.MonoMetric, color = barColor)
                }
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp)
                ) {
                    val radius = CornerRadius(size.height / 2f, size.height / 2f)
                    drawRoundRect(color = trackColor, size = size, cornerRadius = radius)
                    val fillW = size.width * (percent.coerceIn(0, 100) / 100f)
                    if (fillW > 0f) {
                        drawRoundRect(color = barColor, size = Size(fillW, size.height), cornerRadius = radius)
                    }
                }
            }
        }
    }
}
