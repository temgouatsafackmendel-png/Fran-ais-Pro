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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.database.DailyGoal
import com.example.core.database.ExerciseAttempt
import com.example.core.database.Lesson
import com.example.core.database.LessonProgress
import com.example.core.database.UserProfile
import com.example.core.design.AppIconButton
import com.example.core.design.CategoryBadge
import com.example.core.design.EditorialCard
import com.example.core.design.LessonCard
import com.example.core.design.ProgressCard
import com.example.core.design.SectionHeader
import com.example.core.design.StatisticsCard
import com.example.core.design.UserInitialsAvatar
import com.example.learning.progression.LinguisticProfileSummary
import com.example.navigation.Routes
import com.example.ui.theme.FrancaisProDimens
import com.example.ui.theme.LocalSemanticColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Écran 4 : Accueil / Tableau de bord Premium (lisible en moins de 5 secondes, sans surcharge).
 */
@Composable
fun DashboardScreen(
    profile: UserProfile?,
    adaptiveSummary: LinguisticProfileSummary,
    lessons: List<Lesson>,
    lessonProgress: List<LessonProgress>,
    exerciseAttempts: List<ExerciseAttempt>,
    dailyGoals: List<DailyGoal>,
    dailyTargetMinutes: Int,
    onNavigate: (String) -> Unit,
    onStartCategoryTraining: (String) -> Unit
) {
    val semantic = LocalSemanticColors.current
    val todayEpoch = System.currentTimeMillis() / 86_400_000L
    val todayGoal = dailyGoals.firstOrNull { it.epochDay == todayEpoch }
    val todayMinutes = todayGoal?.completedMinutes ?: 0
    val completedMap = remember(lessonProgress) { lessonProgress.associateBy { it.lessonId } }
    val succeededExercises = remember(exerciseAttempts) { exerciseAttempts.count { it.isCorrect } }

    val formattedDate = remember {
        SimpleDateFormat("EEEE d MMMM", Locale.FRANCE)
            .format(Date())
            .replaceFirstChar { it.titlecase(Locale.FRANCE) }
    }

    // Prochaines leçons à reprendre (priorité au niveau estimé de l'utilisateur)
    val continueLessons = remember(lessons, completedMap, adaptiveSummary.estimatedLevel) {
        val uncompletedSameLevel = lessons.filter {
            it.level == adaptiveSummary.estimatedLevel && completedMap[it.id]?.isCompleted != true
        }
        val uncompletedAny = lessons.filter { completedMap[it.id]?.isCompleted != true }
        (uncompletedSameLevel + uncompletedAny + lessons).distinctBy { it.id }.take(2)
    }
    val nextLesson = continueLessons.firstOrNull()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        val hPadding = FrancaisProDimens.screenHorizontalPadding(maxWidth)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = FrancaisProDimens.MaxContentWidth)
                .testTag("dashboard_list"),
            contentPadding = PaddingValues(horizontal = hPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. En-tête : Salutation, date du jour, avatar local, paramètres
            item {
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
                        UserInitialsAvatar(
                            displayName = profile?.displayName ?: "Apprenant",
                            size = 44.dp,
                            modifier = Modifier.clickable { onNavigate(Routes.PROFILE) }
                        )
                        Column {
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.captionCompat(),
                                color = semantic.textSecondary
                            )
                            Text(
                                text = "Bonjour, ${profile?.displayName ?: "Apprenant"}",
                                style = MaterialTheme.typography.headlineMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    AppIconButton(
                        icon = Icons.Filled.Settings,
                        contentDescription = "Paramètres",
                        onClick = { onNavigate(Routes.SETTINGS) },
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.testTag("dash_settings_btn")
                    )
                }
            }

            // 2. Carte principale ProgressCard
            item {
                ProgressCard(
                    level = adaptiveSummary.estimatedLevel,
                    levelLabel = adaptiveSummary.levelLabelFr,
                    globalScore = adaptiveSummary.globalScore,
                    streakDays = profile?.streakDays ?: 0,
                    todayMinutes = todayMinutes,
                    dailyTargetMinutes = dailyTargetMinutes,
                    ctaLabel = "Continuer mon apprentissage",
                    onContinueClick = {
                        if (nextLesson != null) {
                            onNavigate(Routes.lessonDetail(nextLesson.id))
                        } else {
                            onNavigate(Routes.ACADEMY)
                        }
                    }
                )
            }

            // 3. Votre entraînement du jour (4 domaines clés en 2x2)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader(
                        title = "Votre entraînement du jour",
                        actionText = "Exercices",
                        onAction = { onNavigate(Routes.EXERCISES) }
                    )
                    val tiles = listOf(
                        TrainingTileSpec("Orthographe", "Accords & pièges", Icons.Filled.Spellcheck, semantic.spellingColor, semantic.spellingContainer, "train_orthographe") {
                            onStartCategoryTraining("Orthographe")
                        },
                        TrainingTileSpec("Grammaire", "Règles & homophones", Icons.AutoMirrored.Filled.MenuBook, semantic.grammarColor, semantic.grammarContainer, "train_grammaire") {
                            onStartCategoryTraining("Grammaire")
                        },
                        TrainingTileSpec("Conjugaison", "Temps & verbes", Icons.Filled.Translate, semantic.syntaxColor, semantic.syntaxContainer, "train_conjugaison") {
                            onNavigate(Routes.CONJUGATION)
                        },
                        TrainingTileSpec("Vocabulaire", "Mémoire SM-2", Icons.Filled.Bookmark, semantic.success, semantic.successContainer, "train_vocabulaire") {
                            onNavigate(Routes.VOCABULARY)
                        }
                    )
                    tiles.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { spec ->
                                TrainingDomainCard(spec = spec, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // 4. Continuer à apprendre (dernières leçons commencées / suivantes)
            if (continueLessons.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader(
                            title = "Continuer à apprendre",
                            actionText = "Académie",
                            onAction = { onNavigate(Routes.ACADEMY) }
                        )
                        continueLessons.forEach { lesson ->
                            val lp = completedMap[lesson.id]
                            LessonCard(
                                title = lesson.title,
                                summary = lesson.summary,
                                theme = lesson.theme,
                                level = lesson.level,
                                estimatedMinutes = lesson.estimatedMinutes,
                                isCompleted = lp?.isCompleted == true,
                                scorePercent = lp?.quizScorePercent,
                                onClick = { onNavigate(Routes.lessonDetail(lesson.id)) },
                                modifier = Modifier.testTag("dash_lesson_${lesson.id}")
                            )
                        }
                    }
                }
            }

            // 5. Vos progrès (4 statistiques propres)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(
                        title = "Vos progrès",
                        actionText = "Statistiques",
                        onAction = { onNavigate(Routes.STATISTICS) }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatisticsCard(
                            value = "${profile?.totalLearningMinutes ?: 0} min",
                            label = "Temps d'étude",
                            icon = Icons.Filled.Schedule,
                            modifier = Modifier.weight(1f)
                        )
                        StatisticsCard(
                            value = "$succeededExercises",
                            label = "Exercices réussis",
                            icon = Icons.Filled.CheckCircle,
                            accentColor = semantic.success,
                            accentContainer = semantic.successContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatisticsCard(
                            value = "${profile?.totalCorrectionsCount ?: 0}",
                            label = "Fautes corrigées",
                            icon = Icons.Filled.Spellcheck,
                            accentColor = semantic.syntaxColor,
                            accentContainer = semantic.syntaxContainer,
                            modifier = Modifier.weight(1f)
                        )
                        StatisticsCard(
                            value = "${profile?.streakDays ?: 0} j",
                            label = "Série active",
                            icon = Icons.Filled.LocalFireDepartment,
                            accentColor = semantic.warning,
                            accentContainer = semantic.warningContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 6. À améliorer (points faibles détectés + ateliers secondaires)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(title = "À améliorer")
                    if (adaptiveSummary.weakSkills.isNotEmpty()) {
                        EditorialCard {
                            adaptiveSummary.weakSkills.forEachIndexed { idx, stat ->
                                if (idx > 0) Spacer(Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onStartCategoryTraining(stat.skillCategory) }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(stat.skillCategory, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(
                                            "${stat.errorCount} erreur(s) · ${stat.totalAttempts} essai(s)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = semantic.textSecondary
                                        )
                                    }
                                    CategoryBadge(
                                        label = "${stat.masteryPercent} %",
                                        color = semantic.danger,
                                        containerColor = semantic.dangerContainer
                                    )
                                }
                            }
                        }
                    } else {
                        adaptiveSummary.recommendations.take(2).forEach { rec ->
                            EditorialCard(onClick = { onNavigate(rec.targetRoute) }) {
                                Text(rec.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text(rec.reason, style = MaterialTheme.typography.bodySmall, color = semantic.textSecondary)
                            }
                        }
                    }
                }
            }

            // Raccourcis ateliers complémentaires (accès fluide sans encombrer la navigation)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(title = "Ateliers & outils")
                    val tools = listOf(
                        Triple("Dictionnaire", Icons.AutoMirrored.Filled.MenuBook, Routes.DICTIONARY),
                        Triple("Dictées", Icons.Filled.Mic, Routes.DICTATION),
                        Triple("Rédaction", Icons.Filled.EditNote, Routes.WRITING),
                        Triple("Expression orale", Icons.Filled.RecordVoiceOver, Routes.SPEAKING),
                        Triple("Historique", Icons.Filled.History, Routes.HISTORY),
                        Triple("Favoris", Icons.Filled.Favorite, Routes.FAVORITES),
                        Triple("Statistiques", Icons.Filled.BarChart, Routes.STATISTICS)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(tools) { (label, icon, route) ->
                            Surface(
                                onClick = { onNavigate(route) },
                                shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, semantic.border),
                                modifier = Modifier.testTag("quick_tool_$route")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Text(label, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class TrainingTileSpec(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tint: Color,
    val container: Color,
    val tag: String,
    val onClick: () -> Unit
)

@Composable
private fun TrainingDomainCard(
    spec: TrainingTileSpec,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    EditorialCard(
        onClick = spec.onClick,
        contentPadding = 14.dp,
        modifier = modifier.testTag(spec.tag)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(spec.container),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = spec.icon, contentDescription = null, tint = spec.tint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = spec.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = spec.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = semantic.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun androidx.compose.material3.Typography.captionCompat() = labelSmall
