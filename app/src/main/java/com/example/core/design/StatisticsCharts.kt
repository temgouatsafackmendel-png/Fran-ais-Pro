package com.example.core.design

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.FrancaisProDimens
import com.example.ui.theme.FrancaisProTextStyles
import com.example.ui.theme.LocalReducedMotion
import com.example.ui.theme.LocalSemanticColors
import kotlin.math.roundToInt

/**
 * Graphique 1 : Progression hebdomadaire (BarChart natif inspiré de Recharts :
 * grille cartésienne en pointillés, ligne d'objectif, barres animées et infobulle interactive).
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
    val gridColor = semantic.border
    val track = semantic.surfaceSubtle
    val reducedMotion = LocalReducedMotion.current

    val safeTarget = targetMinutes.coerceAtLeast(5)
    val maxVal = maxOf(safeTarget, days.maxOfOrNull { it.second } ?: 0, 10)
    val totalMinutes = days.sumOf { it.second }
    val activeDaysCount = days.count { it.second >= safeTarget }

    var selectedIndex by remember(days) {
        mutableIntStateOf((days.lastIndex).coerceAtLeast(0))
    }
    var played by remember { mutableStateOf(reducedMotion) }
    LaunchedEffect(days) { played = true }

    val animProgress by animateFloatAsState(
        targetValue = if (played) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (reducedMotion) 0 else 240,
            easing = FastOutSlowInEasing
        ),
        label = "weekly_bar_growth"
    )

    val selectedDay = days.getOrNull(selectedIndex)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("statistics_weekly_chart")
    ) {
        // Infobulle dynamique style Recharts Tooltip
        if (selectedDay != null) {
            val (dayLabel, mins) = selectedDay
            val reached = mins >= safeTarget
            Surface(
                shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                color = semantic.surfaceSubtle,
                border = BorderStroke(1.dp, semantic.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekly_chart_tooltip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(if (reached) success else primary)
                        )
                        Text(
                            text = "$dayLabel · $mins min",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (reached) "Objectif atteint" else "Reste ${safeTarget - mins} min",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (reached) success else semantic.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Zone de graphique avec axe Y et grille cartésienne
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .height(144.dp)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text("${maxVal}m", style = FrancaisProTextStyles.MonoMetric, color = semantic.textSecondary)
                Text("${(maxVal / 2f).roundToInt()}m", style = FrancaisProTextStyles.MonoMetric, color = semantic.textSecondary)
                Text("0m", style = FrancaisProTextStyles.MonoMetric, color = semantic.textSecondary)
            }

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(144.dp)
                    .semantics {
                        contentDescription = "Graphique hebdomadaire : $totalMinutes minutes au total sur 7 jours"
                    }
            ) {
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                val targetDash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                val topPad = 6.dp.toPx()
                val bottomPad = 4.dp.toPx()
                val usableH = (size.height - topPad - bottomPad).coerceAtLeast(1f)

                // Lignes de grille cartésienne horizontales (0 %, 50 %, 100 %)
                listOf(0f, 0.5f, 1f).forEach { level ->
                    val y = topPad + usableH * (1f - level)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = if (level == 0f) null else dashEffect
                    )
                }

                // Ligne de référence d'objectif quotidien
                val targetRatio = (safeTarget.toFloat() / maxVal).coerceIn(0f, 1f)
                val targetY = topPad + usableH * (1f - targetRatio)
                drawLine(
                    color = success.copy(alpha = 0.72f),
                    start = Offset(0f, targetY),
                    end = Offset(size.width, targetY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = targetDash
                )

                // Barres verticales arrondies
                val count = days.size.coerceAtLeast(1)
                val slotW = size.width / count
                val barW = (slotW * 0.46f).coerceAtMost(26.dp.toPx())
                days.forEachIndexed { idx, (_, mins) ->
                    val x = idx * slotW + (slotW - barW) / 2f
                    val corner = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    val isSelected = idx == selectedIndex

                    // Fond de colonne discret
                    drawRoundRect(
                        color = if (isSelected) primary.copy(alpha = 0.08f) else track,
                        topLeft = Offset(x, topPad),
                        size = Size(barW, usableH),
                        cornerRadius = corner
                    )

                    val ratio = ((mins.toFloat() / maxVal).coerceIn(0f, 1f)) * animProgress
                    val barH = usableH * ratio
                    if (barH > 0f) {
                        val baseColor = if (mins >= safeTarget) success else primary
                        val barColor = if (isSelected) baseColor else baseColor.copy(alpha = 0.80f)
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(x, topPad + usableH - barH),
                            size = Size(barW, barH),
                            cornerRadius = corner
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        // Étiquettes de jours interactives (cibles tactiles ≥ 48 dp en hauteur)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 34.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            days.forEachIndexed { idx, (dayLabel, mins) ->
                val isSelected = idx == selectedIndex
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(FrancaisProDimens.RadiusSmall))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                            else Color.Transparent
                        )
                        .clickable(role = Role.Button) { selectedIndex = idx }
                        .padding(vertical = 4.dp)
                        .testTag("weekly_day_bar_$idx")
                ) {
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else semantic.textSecondary
                    )
                    Text(
                        text = "${mins}m",
                        style = FrancaisProTextStyles.MonoMetric,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else semantic.textSecondary
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Légende compacte
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ChartLegendDot(color = success, label = "Objectif ($activeDaysCount/7)")
                ChartLegendDot(color = primary, label = "En cours")
            }
            Text(
                text = "Total : $totalMinutes min",
                style = FrancaisProTextStyles.MonoMetric,
                color = semantic.textSecondary
            )
        }
    }
}

/**
 * Graphique 2 : Évolution des scores (AreaChart natif inspiré de Recharts :
 * courbe lissée Bézier, remplissage dégradé discret, grille cartésienne et sélection de point).
 */
@Composable
fun ScoreEvolutionLineChart(
    scores: List<Int>,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = semantic.border
    val surfaceColor = MaterialTheme.colorScheme.surface
    val reducedMotion = LocalReducedMotion.current

    if (scores.isEmpty()) {
        Text(
            text = "Complétez vos premiers exercices ou dictées pour afficher l'évolution réelle de vos scores.",
            style = MaterialTheme.typography.bodySmall,
            color = semantic.textSecondary
        )
        return
    }

    var selectedIdx by remember(scores) { mutableIntStateOf(scores.lastIndex) }
    var played by remember { mutableStateOf(reducedMotion) }
    LaunchedEffect(scores) { played = true }

    val animProgress by animateFloatAsState(
        targetValue = if (played) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (reducedMotion) 0 else 240,
            easing = FastOutSlowInEasing
        ),
        label = "score_area_anim"
    )

    val averageScore = scores.average().roundToInt()
    val selectedScore = scores.getOrElse(selectedIdx) { scores.last() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("score_evolution_chart")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Session #${selectedIdx + 1} : $selectedScore %",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Moyenne : $averageScore %",
                style = FrancaisProTextStyles.MonoMetric,
                color = semantic.textSecondary
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .height(132.dp)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text("100%", style = FrancaisProTextStyles.MonoMetric, color = semantic.textSecondary)
                Text("50%", style = FrancaisProTextStyles.MonoMetric, color = semantic.textSecondary)
                Text("0%", style = FrancaisProTextStyles.MonoMetric, color = semantic.textSecondary)
            }

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(132.dp)
            ) {
                val padX = 10.dp.toPx()
                val padY = 10.dp.toPx()
                val chartW = (size.width - padX * 2).coerceAtLeast(1f)
                val chartH = (size.height - padY * 2).coerceAtLeast(1f)
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                listOf(0f, 0.5f, 1f).forEach { level ->
                    val y = padY + chartH * (1f - level)
                    drawLine(
                        color = gridColor,
                        start = Offset(padX, y),
                        end = Offset(size.width - padX, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = if (level == 0f) null else dashEffect
                    )
                }

                val stepX = if (scores.size <= 1) 0f else chartW / (scores.size - 1)
                val points = scores.mapIndexed { idx, s ->
                    val x = if (scores.size == 1) size.width / 2f else padX + idx * stepX
                    val scaledScore = (s.coerceIn(0, 100) / 100f) * animProgress
                    val y = padY + chartH * (1f - scaledScore)
                    Offset(x, y)
                }

                if (points.size > 1) {
                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 0 until points.lastIndex) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val cx = (p0.x + p1.x) / 2f
                            cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                        }
                    }

                    val areaPath = Path().apply {
                        addPath(linePath)
                        lineTo(points.last().x, padY + chartH)
                        lineTo(points.first().x, padY + chartH)
                        close()
                    }

                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                lineColor.copy(alpha = 0.22f),
                                lineColor.copy(alpha = 0.02f)
                            ),
                            startY = padY,
                            endY = padY + chartH
                        )
                    )

                    drawPath(
                        path = linePath,
                        color = lineColor,
                        style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                points.forEachIndexed { idx, pt ->
                    val isSelected = idx == selectedIdx
                    if (isSelected) {
                        drawCircle(color = lineColor.copy(alpha = 0.20f), radius = 9.dp.toPx(), center = pt)
                    }
                    drawCircle(color = surfaceColor, radius = 5.dp.toPx(), center = pt)
                    drawCircle(
                        color = lineColor,
                        radius = if (isSelected) 4.dp.toPx() else 3.2.dp.toPx(),
                        center = pt
                    )
                }
            }
        }

        if (scores.size > 1) {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 38.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                scores.indices.forEach { idx ->
                    val isSelected = idx == selectedIdx
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(width = 28.dp, height = 28.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
                            .clickable(role = Role.Button) { selectedIdx = idx }
                    ) {
                        Text(
                            text = "${idx + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else semantic.textSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Graphique 3 : Répartition des erreurs (Donut Chart + Légende interactive inspirée de Recharts).
 * Toucher une catégorie met en évidence son arc et affiche le détail au centre de l'anneau.
 */
@Composable
fun ErrorCategoryBreakdownChart(
    items: List<Triple<String, Int, Color>>,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    val reducedMotion = LocalReducedMotion.current
    val activeItems = remember(items) { items.filter { it.second > 0 }.sortedByDescending { it.second } }
    val total = activeItems.sumOf { it.second }

    if (total == 0) {
        Text(
            text = "Aucune erreur enregistrée pour le moment. Analysez un texte ou faites un exercice pour voir la répartition.",
            style = MaterialTheme.typography.bodySmall,
            color = semantic.textSecondary,
            modifier = modifier.testTag("statistics_error_breakdown_empty")
        )
        return
    }

    var selectedIndex by remember(activeItems) { mutableStateOf<Int?>(null) }
    var played by remember { mutableStateOf(reducedMotion) }
    LaunchedEffect(activeItems) { played = true }

    val sweepProgress by animateFloatAsState(
        targetValue = if (played) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (reducedMotion) 0 else 240,
            easing = FastOutSlowInEasing
        ),
        label = "donut_sweep"
    )

    val selectedEntry = selectedIndex?.let { activeItems.getOrNull(it) }
    val trackColor = semantic.surfaceSubtle

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("statistics_error_breakdown_chart"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Anneau Donut centré + métrique centrale réactive
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(160.dp)
                    .semantics {
                        contentDescription = "Répartition de $total erreurs sur ${activeItems.size} catégories"
                    }
            ) {
                val baseStroke = 18.dp.toPx()
                val highlightStroke = 23.dp.toPx()
                val inset = highlightStroke / 2f + 2.dp.toPx()
                val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
                val topLeft = Offset(inset, inset)

                // Piste de fond discrète
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = baseStroke)
                )

                val gapAngle = if (activeItems.size > 1) 3.5f else 0f
                var startAngle = -90f
                activeItems.forEachIndexed { idx, (_, count, color) ->
                    val rawSweep = (count.toFloat() / total) * 360f * sweepProgress
                    val sweep = (rawSweep - gapAngle).coerceAtLeast(1.5f)
                    val isSelected = selectedIndex == idx
                    val alpha = if (selectedIndex == null || isSelected) 1f else 0.35f

                    drawArc(
                        color = color.copy(alpha = alpha),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(
                            width = if (isSelected) highlightStroke else baseStroke,
                            cap = StrokeCap.Butt
                        )
                    )
                    startAngle += rawSweep
                }
            }

            // Indicateur central animé
            AnimatedContent(
                targetState = selectedEntry,
                transitionSpec = {
                    fadeIn(tween(if (reducedMotion) 0 else 160)) togetherWith
                        fadeOut(tween(if (reducedMotion) 0 else 160))
                },
                label = "donut_center_label"
            ) { focused ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .testTag("error_donut_center_summary")
                ) {
                    if (focused != null) {
                        val pct = ((focused.second.toFloat() / total) * 100f).roundToInt()
                        Text(
                            text = "$pct %",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = focused.third
                        )
                        Text(
                            text = focused.first,
                            style = MaterialTheme.typography.labelSmall,
                            color = semantic.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "$total",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (total > 1) "points notés" else "point noté",
                            style = MaterialTheme.typography.labelSmall,
                            color = semantic.textSecondary
                        )
                    }
                }
            }
        }

        // Légende interactive avec barres proportionnelles
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            activeItems.forEachIndexed { idx, (label, count, color) ->
                val pct = ((count.toFloat() / total) * 100f).roundToInt()
                val isSelected = selectedIndex == idx
                Surface(
                    shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                    color = if (isSelected) semantic.surfaceSubtle else Color.Transparent,
                    border = if (isSelected) BorderStroke(1.dp, color.copy(alpha = 0.45f)) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(FrancaisProDimens.RadiusSmall))
                        .clickable(role = Role.Button) {
                            selectedIndex = if (isSelected) null else idx
                        }
                        .testTag("error_category_row_$idx")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "$count · $pct %",
                                style = FrancaisProTextStyles.MonoMetric,
                                color = color
                            )
                        }
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                        ) {
                            val r = CornerRadius(size.height / 2f, size.height / 2f)
                            drawRoundRect(trackColor, size = size, cornerRadius = r)
                            val w = size.width * (count.toFloat() / total) * sweepProgress
                            if (w > 0f) drawRoundRect(color, size = Size(w, size.height), cornerRadius = r)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Graphique 4 : Compétences maîtrisées (barres horizontales animées par domaine).
 */
@Composable
fun SkillBarChart(
    items: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val semantic = LocalSemanticColors.current
    val trackColor = semantic.surfaceSubtle
    val reducedMotion = LocalReducedMotion.current

    var played by remember { mutableStateOf(reducedMotion) }
    LaunchedEffect(items) { played = true }

    val animProgress by animateFloatAsState(
        targetValue = if (played) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (reducedMotion) 0 else 240,
            easing = FastOutSlowInEasing
        ),
        label = "skill_bar_anim"
    )

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
                    val fillW = size.width * (percent.coerceIn(0, 100) / 100f) * animProgress
                    if (fillW > 0f) {
                        drawRoundRect(color = barColor, size = Size(fillW, size.height), cornerRadius = radius)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartLegendDot(color: Color, label: String) {
    val semantic = LocalSemanticColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = semantic.textSecondary
        )
    }
}
