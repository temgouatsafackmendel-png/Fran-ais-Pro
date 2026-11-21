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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    val spacing = com.example.ui.theme.LocalSpacing.current
    val reducedMotion = LocalReducedMotion.current
    val rawProgress = (todayMinutes.toFloat() / dailyTargetMinutes.coerceAtLeast(5)).coerceIn(0f, 1f)
    val goalReached = todayMinutes >= dailyTargetMinutes && dailyTargetMinutes > 0
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(if (reducedMotion) 0 else 240),
        label = "daily_goal_progress"
    )
    val heroBrush = Brush.linearGradient(
        colors = listOf(
            semantic.navyHeader,
            semantic.navyHeader,
            semantic.primaryBlue.copy(alpha = 0.24f)
        )
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(FrancaisProDimens.RadiusSurface),
        color = semantic.navyHeader,
        shadowElevation = spacing.elevatedCardElevation,
        border = BorderStroke(spacing.borderWidth, Color.White.copy(alpha = 0.10f))
    ) {
        Column(
            modifier = Modifier
                .background(semantic.navyHeader)
                .background(heroBrush)
                .padding(spacing.space20)
        ) {
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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (goalReached) {
                        Surface(
                            shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                            color = semantic.success.copy(alpha = 0.22f),
                            border = BorderStroke(1.dp, semantic.success.copy(alpha = 0.45f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Objectif atteint",
                                    tint = semantic.success,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Objectif ✓",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = semantic.onNavyHeader,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
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
            }

            Spacer(Modifier.height(spacing.space16))

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
                        style = FrancaisProTextStyles.MonoMetric.copy(
                            fontSize = MaterialTheme.typography.titleSmall.fontSize,
                            fontWeight = FontWeight.Bold
                        ),
                        color = semantic.onNavyHeader
                    )
                }
            }

            Spacer(Modifier.height(spacing.space12))

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

            Spacer(Modifier.height(spacing.space16))

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
 * 8. LevelCard — Carte de parcours CECRL (Académie) avec état verrouillé / actif / validé,
 * icône moderne du niveau, XP et aperçu du prochain chapitre.
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
    modifier: Modifier = Modifier,
    isLocked: Boolean = false,
    lockReason: String? = null,
    xpEarned: Int = 0,
    icon: ImageVector = CurriculumIcons.forLevel(level)
) {
    val semantic = LocalSemanticColors.current
    val ratio = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount
    val percent = (ratio * 100).toInt()
    val isMastered = totalCount > 0 && completedCount == totalCount

    val cardContainerColor = when {
        isLocked -> semantic.surfaceSubtle.copy(alpha = 0.72f)
        isMastered -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surface
    }
    val cardBorderColor = when {
        isLocked -> semantic.border.copy(alpha = 0.7f)
        isMastered -> semantic.success.copy(alpha = 0.45f)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    }
    val iconContainer = when {
        isLocked -> semantic.border.copy(alpha = 0.45f)
        isMastered -> semantic.successContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val iconTint = when {
        isLocked -> semantic.textSecondary
        isMastered -> semantic.success
        else -> MaterialTheme.colorScheme.primary
    }

    EditorialCard(
        onClick = onClick,
        containerColor = cardContainerColor,
        borderColor = cardBorderColor,
        modifier = modifier.fillMaxWidth()
    ) {
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
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = iconContainer,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isLocked) Icons.Filled.Lock else icon,
                            contentDescription = if (isLocked) "Niveau $level verrouillé" else "Niveau $level",
                            tint = iconTint,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CefrLevelPill(level = level)
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isLocked) semantic.textSecondary else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "$completedCount / $totalCount cours · $difficultyLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = semantic.textSecondary
                    )
                }
            }
            when {
                isLocked -> {
                    CategoryBadge(
                        label = "Verrouillé",
                        color = semantic.textSecondary,
                        containerColor = semantic.border.copy(alpha = 0.4f),
                        icon = Icons.Filled.Lock
                    )
                }
                isMastered -> {
                    CategoryBadge(
                        label = "Validé",
                        color = semantic.success,
                        containerColor = semantic.successContainer,
                        icon = Icons.Filled.Verified
                    )
                }
                else -> {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$percent %",
                            style = FrancaisProTextStyles.MonoMetric,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        if (xpEarned > 0) {
                            Text(
                                text = "+$xpEarned XP",
                                style = MaterialTheme.typography.labelSmall,
                                color = semantic.success,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        val trackColor = if (isLocked) semantic.border.copy(alpha = 0.4f) else semantic.surfaceSubtle
        val barColor = if (isMastered) semantic.success else MaterialTheme.colorScheme.primary
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
        ) {
            val r = CornerRadius(size.height / 2f, size.height / 2f)
            drawRoundRect(color = trackColor, size = size, cornerRadius = r)
            if (ratio > 0f && !isLocked) {
                drawRoundRect(color = barColor, size = Size(size.width * ratio, size.height), cornerRadius = r)
            }
        }

        if (skillsPreview.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Notions : ${skillsPreview.joinToString(" · ")}",
                style = MaterialTheme.typography.bodySmall,
                color = semantic.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isLocked && !lockReason.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                color = semantic.border.copy(alpha = 0.25f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = semantic.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = lockReason,
                        style = MaterialTheme.typography.labelMedium,
                        color = semantic.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else if (!nextLessonTitle.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                color = if (isMastered) semantic.successContainer.copy(alpha = 0.55f) else semantic.surfaceSubtle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isMastered) Icons.Filled.Bolt else Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = if (isMastered) semantic.success else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isMastered) "Réviser : $nextLessonTitle" else "Prochain cours : $nextLessonTitle",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = if (isMastered) semantic.success else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * 9. LessonCard — Carte de chapitre structurée avec icône thématique moderne, numéro de chapitre,
 * durée et statut d'accès progressif (Validée / En cours / Verrouillé).
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
    modifier: Modifier = Modifier,
    chapterNumber: Int? = null,
    isLocked: Boolean = false,
    isCurrent: Boolean = false,
    lockReason: String? = null
) {
    val semantic = LocalSemanticColors.current
    val (themeColor, themeBg) = CurriculumIcons.themeAccent(
        theme = theme,
        semantic = semantic,
        primary = MaterialTheme.colorScheme.primary,
        primaryContainer = MaterialTheme.colorScheme.primaryContainer
    )
    val themeIcon = CurriculumIcons.forTheme(theme)

    val containerColor = when {
        isLocked -> semantic.surfaceSubtle.copy(alpha = 0.7f)
        isCurrent -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        isLocked -> semantic.border.copy(alpha = 0.65f)
        isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        isCompleted -> semantic.success.copy(alpha = 0.4f)
        else -> semantic.border
    }

    EditorialCard(
        onClick = onClick,
        containerColor = containerColor,
        borderColor = borderColor,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isLocked) semantic.border.copy(alpha = 0.4f) else themeBg,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isLocked) Icons.Filled.Lock else themeIcon,
                            contentDescription = null,
                            tint = if (isLocked) semantic.textSecondary else themeColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                if (chapterNumber != null) {
                    CategoryBadge(
                        label = "Chap. ${chapterNumber.toString().padStart(2, '0')}",
                        color = if (isLocked) semantic.textSecondary else MaterialTheme.colorScheme.primary,
                        containerColor = if (isLocked) semantic.border.copy(alpha = 0.35f) else MaterialTheme.colorScheme.primaryContainer
                    )
                } else {
                    CefrLevelPill(level = level)
                }
                CategoryBadge(
                    label = theme,
                    color = if (isLocked) semantic.textSecondary else themeColor,
                    containerColor = if (isLocked) semantic.border.copy(alpha = 0.35f) else themeBg
                )
            }
            when {
                isCompleted -> {
                    CategoryBadge(
                        label = if (scorePercent != null) "Validée · $scorePercent %" else "Validée",
                        color = semantic.success,
                        containerColor = semantic.successContainer,
                        icon = Icons.Filled.CheckCircle
                    )
                }
                isLocked -> {
                    CategoryBadge(
                        label = "Verrouillé",
                        color = semantic.textSecondary,
                        containerColor = semantic.border.copy(alpha = 0.35f),
                        icon = Icons.Filled.Lock
                    )
                }
                isCurrent -> {
                    CategoryBadge(
                        label = "À suivre",
                        color = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        icon = Icons.Filled.PlayArrow
                    )
                }
                else -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = semantic.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$estimatedMinutes min",
                            style = FrancaisProTextStyles.MonoMetric,
                            color = semantic.textSecondary
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isLocked) semantic.textSecondary else MaterialTheme.colorScheme.onSurface,
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
        if (isLocked && !lockReason.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = lockReason,
                style = MaterialTheme.typography.labelSmall,
                color = semantic.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        } else if (!isLocked && !isCompleted) {
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = semantic.textSecondary,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "$estimatedMinutes min · Cours + mini-exercice + quiz",
                    style = MaterialTheme.typography.labelSmall,
                    color = semantic.textSecondary
                )
            }
        }
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
                    style = FrancaisProTextStyles.MonoLarge,
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

