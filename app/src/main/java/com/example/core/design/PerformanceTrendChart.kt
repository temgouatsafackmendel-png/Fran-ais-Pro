package com.example.core.design

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.learning.progression.DailySkillPoint
import com.example.learning.progression.SkillDomain
import com.example.ui.theme.FrancaisProDimens
import com.example.ui.theme.FrancaisProTextStyles
import com.example.ui.theme.LocalReducedMotion
import com.example.ui.theme.LocalSemanticColors
import kotlin.math.roundToInt

/**
 * Graphique multi-courbes interactif inspiré de Recharts / D3, réalisé nativement en Jetpack Compose Canvas.
 * Affiche jusqu'à 3 séries superposées (Grammaire, Vocabulaire, Écriture) avec remplissages dégradés,
 * grille cartésienne en pointillés, filtrage par compétence et curseur tactile d'inspection quotidienne.
 */
@Composable
fun MultiSkillTrendChart(
    points: List<DailySkillPoint>,
    visibleDomains: Set<SkillDomain>,
    onToggleDomain: (SkillDomain) -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    val primary = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val gridColor = semantic.border
    val reducedMotion = LocalReducedMotion.current

    val domainColors = mapOf(
        SkillDomain.GRAMMAR to semantic.grammarColor,
        SkillDomain.VOCABULARY to semantic.success,
        SkillDomain.WRITING to primary
    )

    val defaultIndex = remember(points) {
        points.indexOfLast { it.totalEvents > 0 }
            .takeIf { it >= 0 }
            ?: points.lastIndex.coerceAtLeast(0)
    }
    var selectedIndex by remember(points) { mutableIntStateOf(defaultIndex) }

    var played by remember { mutableStateOf(reducedMotion) }
    LaunchedEffect(points, visibleDomains) { played = true }

    val animProgress by animateFloatAsState(
        targetValue = if (played) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (reducedMotion) 0 else 280,
            easing = FastOutSlowInEasing
        ),
        label = "multi_skill_curve_reveal"
    )

    val safeSelectedIdx = selectedIndex.coerceIn(0, (points.lastIndex).coerceAtLeast(0))
    val selectedPoint = points.getOrNull(safeSelectedIdx)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("performance_trend_chart"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Filtres interactifs de séries (Grammaire · Vocabulaire · Écriture)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SkillDomain.entries.forEach { domain ->
                val isSelected = domain in visibleDomains
                val accent = domainColors[domain] ?: primary
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) accent.copy(alpha = 0.14f) else semantic.surfaceSubtle,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) accent.copy(alpha = 0.65f) else semantic.border
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Checkbox) { onToggleDomain(domain) }
                        .testTag("perf_toggle_${domain.name}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) accent else semantic.textSecondary.copy(alpha = 0.35f))
                        )
                        Spacer(Modifier.size(6.dp))
                        Text(
                            text = domain.labelFr,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else semantic.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 2. Infobulle interactive style Recharts Tooltip (avec navigation jour précédent / suivant)
        if (selectedPoint != null) {
            Surface(
                shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                color = semantic.surfaceSubtle,
                border = BorderStroke(1.dp, semantic.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("performance_tooltip_card")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedPoint.fullDateLabel,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (selectedPoint.totalEvents > 0) {
                                    "${selectedPoint.totalEvents} activité(s) enregistrée(s)"
                                } else {
                                    "Aucune nouvelle activité ce jour"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = semantic.textSecondary
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            DayStepperButton(
                                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Jour précédent",
                                enabled = safeSelectedIdx > 0,
                                onClick = { selectedIndex = (safeSelectedIdx - 1).coerceAtLeast(0) },
                                tag = "perf_day_prev"
                            )
                            DayStepperButton(
                                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Jour suivant",
                                enabled = safeSelectedIdx < points.lastIndex,
                                onClick = { selectedIndex = (safeSelectedIdx + 1).coerceAtMost(points.lastIndex) },
                                tag = "perf_day_next"
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SkillDomain.entries.forEach { domain ->
                            val accent = domainColors[domain] ?: primary
                            val score = selectedPoint.scoreFor(domain)
                            val events = selectedPoint.eventsFor(domain)
                            val active = domain in visibleDomains
                            Surface(
                                shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = if (active) 1f else 0.55f),
                                border = BorderStroke(
                                    1.dp,
                                    if (active) accent.copy(alpha = 0.30f) else semantic.border
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(if (active) accent else semantic.textSecondary.copy(alpha = 0.3f))
                                        )
                                        Text(
                                            text = domain.labelFr,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = semantic.textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = if (score != null) "$score %" else "—",
                                        style = FrancaisProTextStyles.MonoMetric,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active && score != null) accent else semantic.textSecondary
                                    )
                                    Text(
                                        text = if (events > 0) "+$events act." else "stable",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = semantic.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Zone Canvas interactive (grille + courbes Bézier + aires dégradées + curseur vertical)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .height(196.dp)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text("100%", style = FrancaisProTextStyles.MonoMetric, color = semantic.textSecondary)
                Text("75%", style = FrancaisProTextStyles.MonoMetric, color = semantic.textSecondary)
                Text("50%", style = FrancaisProTextStyles.MonoMetric, color = semantic.textSecondary)
                Text("25%", style = FrancaisProTextStyles.MonoMetric, color = semantic.textSecondary)
                Text("0%", style = FrancaisProTextStyles.MonoMetric, color = semantic.textSecondary)
            }

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(196.dp)
                    .semantics {
                        contentDescription =
                            "Graphique d'évolution sur ${points.size} jours pour la grammaire, le vocabulaire et l'écriture"
                    }
                    .pointerInput(points) {
                        detectTapGestures { offset ->
                            if (points.size > 1 && size.width > 0) {
                                val padPx = 10.dp.toPx()
                                val usableW = (size.width - padPx * 2f).coerceAtLeast(1f)
                                val ratio = ((offset.x - padPx) / usableW).coerceIn(0f, 1f)
                                selectedIndex = (ratio * (points.size - 1)).roundToInt()
                            }
                        }
                    }
                    .pointerInput(points) {
                        detectHorizontalDragGestures { change, _ ->
                            change.consume()
                            if (points.size > 1 && size.width > 0) {
                                val padPx = 10.dp.toPx()
                                val usableW = (size.width - padPx * 2f).coerceAtLeast(1f)
                                val ratio = ((change.position.x - padPx) / usableW).coerceIn(0f, 1f)
                                selectedIndex = (ratio * (points.size - 1)).roundToInt()
                            }
                        }
                    }
            ) {
                val padX = 10.dp.toPx()
                val padY = 10.dp.toPx()
                val chartW = (size.width - padX * 2f).coerceAtLeast(1f)
                val chartH = (size.height - padY * 2f).coerceAtLeast(1f)
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                val crosshairDash = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)

                // Lignes de grille horizontales (0 %, 25 %, 50 %, 75 %, 100 %)
                listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { level ->
                    val y = padY + chartH * (1f - level)
                    drawLine(
                        color = gridColor,
                        start = Offset(padX, y),
                        end = Offset(size.width - padX, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = if (level == 0f) null else dashEffect
                    )
                }

                if (points.isEmpty()) return@Canvas
                val stepX = if (points.size <= 1) 0f else chartW / (points.size - 1)
                fun xForIndex(idx: Int): Float =
                    if (points.size == 1) size.width / 2f else padX + idx * stepX

                fun yForScore(score: Int): Float {
                    val ratio = ((score.coerceIn(0, 100) / 100f) * animProgress).coerceIn(0f, 1f)
                    return padY + chartH * (1f - ratio)
                }

                // Curseur vertical (crosshair) au jour sélectionné
                val cursorX = xForIndex(safeSelectedIdx)
                drawLine(
                    color = primary.copy(alpha = 0.45f),
                    start = Offset(cursorX, padY),
                    end = Offset(cursorX, padY + chartH),
                    strokeWidth = 1.4.dp.toPx(),
                    pathEffect = crosshairDash
                )

                // Tracé des courbes Bézier + aires dégradées pour chaque compétence visible
                SkillDomain.entries.forEach { domain ->
                    if (domain !in visibleDomains) return@forEach
                    val color = domainColors[domain] ?: primary
                    val seriesCoords = points.mapIndexedNotNull { idx, pt ->
                        pt.scoreFor(domain)?.let { score -> idx to Offset(xForIndex(idx), yForScore(score)) }
                    }
                    if (seriesCoords.isEmpty()) return@forEach

                    if (seriesCoords.size > 1) {
                        val linePath = Path().apply {
                            moveTo(seriesCoords.first().second.x, seriesCoords.first().second.y)
                            for (i in 0 until seriesCoords.lastIndex) {
                                val p0 = seriesCoords[i].second
                                val p1 = seriesCoords[i + 1].second
                                val cx = (p0.x + p1.x) / 2f
                                cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                            }
                        }

                        val areaPath = Path().apply {
                            addPath(linePath)
                            lineTo(seriesCoords.last().second.x, padY + chartH)
                            lineTo(seriesCoords.first().second.x, padY + chartH)
                            close()
                        }

                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    color.copy(alpha = 0.16f),
                                    color.copy(alpha = 0.01f)
                                ),
                                startY = padY,
                                endY = padY + chartH
                            )
                        )

                        drawPath(
                            path = linePath,
                            color = color,
                            style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Points marqués sur les jours actifs + anneau prononcé sur le jour sélectionné
                    seriesCoords.forEach { (idx, coord) ->
                        val isSelected = idx == safeSelectedIdx
                        val hasEventsOnDay = points[idx].eventsFor(domain) > 0
                        if (isSelected) {
                            drawCircle(
                                color = color.copy(alpha = 0.22f),
                                radius = 9.dp.toPx(),
                                center = coord
                            )
                            drawCircle(color = surfaceColor, radius = 5.5.dp.toPx(), center = coord)
                            drawCircle(color = color, radius = 4.dp.toPx(), center = coord)
                        } else if (hasEventsOnDay || seriesCoords.size == 1) {
                            drawCircle(color = surfaceColor, radius = 3.8.dp.toPx(), center = coord)
                            drawCircle(color = color, radius = 2.5.dp.toPx(), center = coord)
                        }
                    }
                }
            }
        }

        // 4. Étiquettes de dates X espacées proprement (sans chevauchement)
        if (points.isNotEmpty()) {
            val tickIndices = remember(points.size) {
                when {
                    points.size <= 1 -> listOf(0)
                    points.size <= 7 -> listOf(0, 2, 4, points.lastIndex).distinct()
                    points.size <= 14 -> listOf(0, 4, 9, points.lastIndex).distinct()
                    else -> listOf(0, 7, 15, 22, points.lastIndex).distinct()
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 38.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                tickIndices.forEach { idx ->
                    val label = points.getOrNull(idx)?.shortDateLabel.orEmpty()
                    val isNearSelected = idx == safeSelectedIdx
                    Text(
                        text = label,
                        style = FrancaisProTextStyles.MonoMetric,
                        fontWeight = if (isNearSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isNearSelected) primary else semantic.textSecondary
                    )
                }
            }
        }
    }
}

/**
 * Mini courbe sparkline native utilisée sur chaque carte de compétence (Grammaire, Vocabulaire, Écriture).
 */
@Composable
fun DomainSparkline(
    scores: List<Int?>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    val trackColor = semantic.border

    Canvas(modifier = modifier) {
        val nonNull = scores.mapIndexedNotNull { idx, value -> value?.let { idx to it } }
        val padY = 4.dp.toPx()
        val usableH = (size.height - padY * 2f).coerceAtLeast(1f)
        drawLine(
            color = trackColor,
            start = Offset(0f, size.height - padY),
            end = Offset(size.width, size.height - padY),
            strokeWidth = 1.dp.toPx()
        )
        if (nonNull.isEmpty()) return@Canvas

        val totalSlots = (scores.size - 1).coerceAtLeast(1)
        val pts = nonNull.map { (idx, score) ->
            val x = if (scores.size <= 1) size.width / 2f else (idx.toFloat() / totalSlots) * size.width
            val y = padY + usableH * (1f - (score.coerceIn(0, 100) / 100f))
            Offset(x, y)
        }

        if (pts.size > 1) {
            val path = Path().apply {
                moveTo(pts.first().x, pts.first().y)
                for (i in 0 until pts.lastIndex) {
                    val p0 = pts[i]
                    val p1 = pts[i + 1]
                    val cx = (p0.x + p1.x) / 2f
                    cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                }
            }
            val fill = Path().apply {
                addPath(path)
                lineTo(pts.last().x, size.height - padY)
                lineTo(pts.first().x, size.height - padY)
                close()
            }
            drawPath(
                path = fill,
                brush = Brush.verticalGradient(
                    colors = listOf(accentColor.copy(alpha = 0.20f), Color.Transparent),
                    startY = padY,
                    endY = size.height - padY
                )
            )
            drawPath(
                path = path,
                color = accentColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        val last = pts.last()
        drawCircle(color = accentColor, radius = 3.2.dp.toPx(), center = last)
    }
}

@Composable
private fun DayStepperButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    val semantic = LocalSemanticColors.current
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, semantic.border),
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .testTag(tag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (enabled) MaterialTheme.colorScheme.onSurface else semantic.textSecondary.copy(alpha = 0.35f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
