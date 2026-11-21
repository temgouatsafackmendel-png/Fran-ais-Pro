package com.example.features.academy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.database.Achievement
import com.example.core.database.CorrectionHistory
import com.example.core.database.DictationAttempt
import com.example.core.database.ExerciseAttempt
import com.example.core.database.Lesson
import com.example.core.database.LessonProgress
import com.example.core.database.StreakReward
import com.example.core.database.StreakState
import com.example.core.database.UserProfile
import com.example.core.database.VocabularyProgress
import com.example.core.design.AppTopBar
import com.example.core.design.CategoryBadge
import com.example.core.design.CefrLevelPill
import com.example.core.design.CurriculumIcons
import com.example.core.design.EditorialCard
import com.example.core.design.LessonCard
import com.example.core.design.LevelCard
import com.example.core.design.PrimaryButton
import com.example.core.design.SectionHeader
import com.example.learning.content.DictionarySeed
import com.example.learning.progression.AcademyStreakEngine
import com.example.learning.progression.AcademyStreakSummary
import com.example.learning.progression.AchievementProgressEngine
import com.example.learning.progression.AchievementProgressItem
import com.example.learning.progression.AdaptiveProgressionEngine
import com.example.learning.progression.CoursePathEngine
import com.example.learning.progression.CurriculumRoadmap
import com.example.learning.progression.LessonUnlockStatus
import com.example.learning.progression.LevelRoadmapState
import com.example.learning.progression.StreakDayDot
import com.example.learning.progression.StreakHealthStatus
import com.example.navigation.Routes
import com.example.ui.theme.FrancaisProDimens
import com.example.ui.theme.FrancaisProTextStyles
import com.example.ui.theme.LocalSemanticColors
import kotlinx.coroutines.launch

private enum class BadgeFilter(val label: String, val tagSuffix: String) {
    ALL("Tous", "ALL"),
    UNLOCKED("Débloqués", "UNLOCKED"),
    LOCKED("À débloquer", "LOCKED")
}

/**
 * Écran 7 : Académie du français — Parcours progressif structuré A1 → C2 avec déblocage
 * séquentiel des niveaux, système complet de Badges & Réussites, et outils linguistiques intégrés.
 */
@Composable
fun AcademyScreen(
    lessons: List<Lesson>,
    progress: List<LessonProgress>,
    onOpenLevelLessons: (String) -> Unit,
    onOpenDictionary: () -> Unit,
    onOpenConjugation: () -> Unit,
    onOpenVocabulary: () -> Unit,
    onOpenLesson: (String) -> Unit = {},
    achievements: List<Achievement> = emptyList(),
    exerciseAttempts: List<ExerciseAttempt> = emptyList(),
    vocabularyProgress: List<VocabularyProgress> = emptyList(),
    correctionHistory: List<CorrectionHistory> = emptyList(),
    dictationAttempts: List<DictationAttempt> = emptyList(),
    profile: UserProfile? = null,
    streakSummary: AcademyStreakSummary = AcademyStreakEngine.buildSummary(
        streakState = null,
        practiceDays = emptyList(),
        rewards = AcademyStreakEngine.defaultRewards()
    ),
    streakState: StreakState? = null,
    onClaimStreakReward: (String) -> Unit = {},
    onStartCategoryTraining: (String?) -> Unit = {},
    onNavigateRoute: (String) -> Unit = {}
) {
    val roadmap = remember(lessons, progress) {
        CoursePathEngine.buildRoadmap(lessons, progress)
    }
    val effectiveAchievements = remember(achievements) {
        achievements.ifEmpty { DictionarySeed.achievements() }
    }
    val achievementItems = remember(
        effectiveAchievements,
        lessons,
        progress,
        exerciseAttempts,
        vocabularyProgress,
        correctionHistory,
        dictationAttempts,
        profile,
        streakState
    ) {
        AchievementProgressEngine.evaluateProgress(
            achievements = effectiveAchievements,
            lessons = lessons,
            lessonProgress = progress,
            exerciseAttempts = exerciseAttempts,
            vocabularyProgress = vocabularyProgress,
            correctionHistory = correctionHistory,
            dictationAttempts = dictationAttempts,
            profile = profile,
            streakState = streakState
        )
    }
    val unlockedCount = remember(achievementItems) {
        achievementItems.count { it.achievement.isUnlocked }
    }
    var badgeFilter by remember { mutableStateOf(BadgeFilter.ALL) }
    val filteredBadges = remember(achievementItems, badgeFilter) {
        when (badgeFilter) {
            BadgeFilter.ALL -> achievementItems
            BadgeFilter.UNLOCKED -> achievementItems.filter { it.achievement.isUnlocked }
            BadgeFilter.LOCKED -> achievementItems.filter { !it.achievement.isUnlocked }
        }
    }

    var lockedNotice by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val semantic = LocalSemanticColors.current

    // Index de la section Badges dans la LazyColumn pour défilement rapide depuis l'en-tête
    val achievementsSectionIndex = 5 + roadmap.levels.size

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        val hPadding = FrancaisProDimens.screenHorizontalPadding(maxWidth)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = FrancaisProDimens.MaxContentWidth)
                .testTag("academy_roadmap_list"),
            contentPadding = PaddingValues(horizontal = hPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 0. En-tête de la page Académie + pilule Badges cliquable
            item(key = "academy_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Académie", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            "Parcours progressif CECRL · 24 cours structurés A1 → C2",
                            style = MaterialTheme.typography.bodySmall,
                            color = semantic.textSecondary
                        )
                    }
                    Surface(
                        onClick = {
                            scope.launch { listState.animateScrollToItem(achievementsSectionIndex) }
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = semantic.warningContainer,
                        border = BorderStroke(1.dp, semantic.warning.copy(alpha = 0.35f)),
                        modifier = Modifier.testTag("academy_badges_header_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = "Voir les badges de l'Académie",
                                tint = semantic.warning,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "$unlockedCount/${achievementItems.size}",
                                style = FrancaisProTextStyles.MonoMetric,
                                color = semantic.onWarningContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 1. Carte Héro : Progression globale + XP + CTA Continuer le parcours
            item(key = "academy_hero") {
                AcademyRoadmapHeroCard(
                    roadmap = roadmap,
                    bonusXpEarned = streakSummary.totalBonusXpEarned,
                    unlockedBadgesCount = unlockedCount,
                    totalBadgesCount = achievementItems.size,
                    onContinueClick = {
                        val next = roadmap.nextRecommendedLesson
                        if (next != null) {
                            onOpenLesson(next.id)
                        } else {
                            onOpenLevelLessons(roadmap.activeLevel)
                        }
                    }
                )
            }

            // 2. Suivi des séries quotidiennes & récompenses de l'Académie (Room)
            item(key = "academy_streak") {
                AcademyDailyStreakCard(
                    summary = streakSummary,
                    onClaimReward = onClaimStreakReward
                )
            }

            // 2. Section Outils linguistiques structurée avec icônes modernes
            item(key = "academy_tools") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(
                        title = "Outils linguistiques",
                        subtitle = "Références hors ligne pour accompagner vos cours",
                        icon = Icons.Filled.AutoAwesome
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickReferenceToolCard(
                            title = "Dictionnaire",
                            subtitle = "Définitions & syn.",
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            accentColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            onClick = onOpenDictionary,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("academy_open_dictionary")
                        )
                        QuickReferenceToolCard(
                            title = "Conjugueur",
                            subtitle = "Temps & modes",
                            icon = Icons.Filled.Translate,
                            accentColor = semantic.syntaxColor,
                            containerColor = semantic.syntaxContainer,
                            onClick = onOpenConjugation,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("academy_open_conjugation")
                        )
                        QuickReferenceToolCard(
                            title = "Vocabulaire",
                            subtitle = "Révision SM-2",
                            icon = Icons.Filled.Bookmark,
                            accentColor = semantic.success,
                            containerColor = semantic.successContainer,
                            onClick = onOpenVocabulary,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("academy_open_vocabulary")
                        )
                    }
                }
            }

            // 3. Section Parcours progressif CECRL (A1 -> C2)
            item(key = "academy_roadmap_header") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(
                        title = "Parcours progressif CECRL",
                        subtitle = "Validez chaque niveau pour débloquer le palier suivant",
                        icon = Icons.Filled.Route
                    )
                    AnimatedVisibility(visible = lockedNotice != null) {
                        LockedStepBanner(
                            message = lockedNotice.orEmpty(),
                            onDismiss = { lockedNotice = null },
                            modifier = Modifier.testTag("academy_locked_banner")
                        )
                    }
                }
            }

            // 4..9. Paliers A1 -> C2
            itemsIndexed(roadmap.levels, key = { _, lvl -> lvl.level }) { idx, levelState ->
                val isLast = idx == roadmap.levels.lastIndex
                RoadmapTimelineRow(
                    isCompleted = levelState.isCompleted,
                    isUnlocked = levelState.isUnlocked,
                    isLast = isLast,
                    icon = CurriculumIcons.forLevel(levelState.level),
                    stepLabel = levelState.level
                ) {
                    LevelCard(
                        level = levelState.level,
                        title = levelState.title,
                        difficultyLabel = levelState.difficultyLabel,
                        completedCount = levelState.completedCount,
                        totalCount = levelState.totalCount,
                        nextLessonTitle = levelState.nextUnlockedLesson?.title,
                        skillsPreview = levelState.skillsPreview,
                        isLocked = !levelState.isUnlocked,
                        lockReason = levelState.lockReason,
                        xpEarned = levelState.xpEarned,
                        icon = CurriculumIcons.forLevel(levelState.level),
                        onClick = {
                            if (levelState.isUnlocked) {
                                lockedNotice = null
                                onOpenLevelLessons(levelState.level)
                            } else {
                                lockedNotice = levelState.lockReason
                                    ?: "Terminez le niveau précédent pour débloquer ${levelState.level}."
                            }
                        },
                        modifier = Modifier.testTag("academy_level_${levelState.level}")
                    )
                }
            }

            // Section Badges & Réussites de l'Académie
            item(key = "academy_achievements_header") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.testTag("academy_achievements_section")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            SectionHeader(
                                title = "Badges & Réussites",
                                subtitle = "Débloquez vos distinctions en réussissant cours et exercices",
                                icon = Icons.Filled.EmojiEvents
                            )
                        }
                        CategoryBadge(
                            label = "$unlockedCount / ${achievementItems.size}",
                            color = semantic.warning,
                            containerColor = semantic.warningContainer,
                            icon = Icons.Filled.EmojiEvents
                        )
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(BadgeFilter.entries) { filter ->
                            val count = when (filter) {
                                BadgeFilter.ALL -> achievementItems.size
                                BadgeFilter.UNLOCKED -> unlockedCount
                                BadgeFilter.LOCKED -> achievementItems.size - unlockedCount
                            }
                            FilterChip(
                                selected = badgeFilter == filter,
                                onClick = { badgeFilter = filter },
                                label = { Text("${filter.label} ($count)") },
                                modifier = Modifier.testTag("academy_badge_filter_${filter.tagSuffix}")
                            )
                        }
                    }
                }
            }

            if (filteredBadges.isEmpty()) {
                item(key = "academy_badges_empty") {
                    EditorialCard {
                        Text(
                            text = if (badgeFilter == BadgeFilter.UNLOCKED) {
                                "Aucun badge débloqué pour l'instant : terminez un chapitre ou réussissez 3 exercices de grammaire pour obtenir votre premier trophée."
                            } else {
                                "Félicitations ! Vous avez déjà débloqué tous les badges disponibles dans l'Académie."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = semantic.textSecondary
                        )
                    }
                }
            } else {
                items(filteredBadges, key = { "badge_${it.achievement.id}" }) { item ->
                    AcademyAchievementBadgeCard(
                        item = item,
                        onActionClick = {
                            when {
                                item.exerciseCategoryFilter != null -> {
                                    onStartCategoryTraining(item.exerciseCategoryFilter)
                                }
                                item.targetRoute == Routes.VOCABULARY -> {
                                    onOpenVocabulary()
                                }
                                item.targetRoute?.startsWith("lesson_detail/") == true -> {
                                    onOpenLesson(item.targetRoute.removePrefix("lesson_detail/"))
                                }
                                item.targetRoute?.startsWith("lesson_list/") == true -> {
                                    onOpenLevelLessons(item.targetRoute.removePrefix("lesson_list/"))
                                }
                                item.targetRoute != null -> {
                                    onNavigateRoute(item.targetRoute)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AcademyRoadmapHeroCard(
    roadmap: CurriculumRoadmap,
    bonusXpEarned: Int,
    unlockedBadgesCount: Int,
    totalBadgesCount: Int,
    onContinueClick: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    val nextTitle = roadmap.nextRecommendedLesson?.title

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("academy_hero_card"),
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
                    color = Color.White.copy(alpha = 0.14f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = CurriculumIcons.forLevel(roadmap.activeLevel),
                            contentDescription = null,
                            tint = semantic.onNavyHeader,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Palier actif · ${roadmap.activeLevel}",
                            style = MaterialTheme.typography.labelMedium,
                            color = semantic.onNavyHeader,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                    color = Color.White.copy(alpha = 0.14f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = "Points d'expérience",
                            tint = semantic.warning,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "${roadmap.totalXp + bonusXpEarned} XP · $unlockedBadgesCount/$totalBadgesCount",
                            style = FrancaisProTextStyles.MonoMetric,
                            color = semantic.onNavyHeader,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Progression du cursus",
                        style = MaterialTheme.typography.bodySmall,
                        color = semantic.onNavyHeader.copy(alpha = 0.75f)
                    )
                    Text(
                        text = "${roadmap.completedLessonsCount} / ${roadmap.totalLessonsCount} cours validés",
                        style = MaterialTheme.typography.titleLarge,
                        color = semantic.onNavyHeader,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Niveaux ouverts",
                        style = MaterialTheme.typography.bodySmall,
                        color = semantic.onNavyHeader.copy(alpha = 0.75f)
                    )
                    Text(
                        text = "${roadmap.unlockedLevelsCount} / ${roadmap.totalLevelsCount}",
                        style = FrancaisProTextStyles.MonoMetric,
                        color = semantic.onNavyHeader,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            val trackColor = Color.White.copy(alpha = 0.16f)
            val fillColor = semantic.success
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            ) {
                val r = CornerRadius(size.height / 2f, size.height / 2f)
                drawRoundRect(color = trackColor, size = size, cornerRadius = r)
                val w = size.width * roadmap.overallProgressRatio.coerceIn(0f, 1f)
                if (w > 0f) {
                    drawRoundRect(color = fillColor, size = Size(w, size.height), cornerRadius = r)
                }
            }

            Spacer(Modifier.height(14.dp))

            PrimaryButton(
                text = if (!nextTitle.isNullOrBlank()) "Continuer : $nextTitle" else "Explorer le niveau ${roadmap.activeLevel}",
                onClick = onContinueClick,
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                containerColor = semantic.primaryBlue,
                contentColor = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("academy_continue_btn")
            )
        }
    }
}

@Composable
private fun AcademyDailyStreakCard(
    summary: AcademyStreakSummary,
    onClaimReward: (String) -> Unit
) {
    val semantic = LocalSemanticColors.current
    val (statusLabel, statusTint, statusBg) = when (summary.status) {
        StreakHealthStatus.PRACTICED_TODAY ->
            Triple("Validée aujourd'hui", semantic.success, semantic.successContainer)
        StreakHealthStatus.AT_RISK_TODAY ->
            Triple("À pratiquer aujourd'hui", semantic.warning, semantic.warningContainer)
        StreakHealthStatus.FREEZE_READY ->
            Triple("Bouclier prêt", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
        StreakHealthStatus.INACTIVE_OR_NEW ->
            Triple("Prête à démarrer", semantic.textSecondary, semantic.surfaceSubtle)
    }

    EditorialCard(
        contentPadding = 16.dp,
        borderColor = if (summary.claimableRewards.isNotEmpty()) {
            semantic.warning.copy(alpha = 0.55f)
        } else {
            semantic.border
        },
        modifier = Modifier.testTag("academy_streak_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (summary.currentStreak > 0) semantic.warningContainer else semantic.surfaceSubtle,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = "Série quotidienne de l'Académie",
                            tint = if (summary.currentStreak > 0) semantic.warning else semantic.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "${summary.currentStreak} ${if (summary.currentStreak <= 1) "jour de série" else "jours de série"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = summary.statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = semantic.textSecondary
                    )
                }
            }
            CategoryBadge(
                label = statusLabel,
                color = statusTint,
                containerColor = statusBg
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StreakStatChip(
                icon = Icons.Filled.EmojiEvents,
                label = "Record : ${summary.bestStreak} j",
                tint = semantic.warning,
                containerColor = semantic.warningContainer,
                modifier = Modifier
                    .weight(1f)
                    .testTag("streak_best_chip")
            )
            StreakStatChip(
                icon = Icons.Filled.Shield,
                label = "${summary.freezeTokensAvailable} bouclier${if (summary.freezeTokensAvailable > 1) "s" else ""}",
                tint = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .weight(1f)
                    .testTag("streak_freeze_chip")
            )
            StreakStatChip(
                icon = Icons.Filled.Bolt,
                label = "+${summary.totalBonusXpEarned} XP",
                tint = semantic.success,
                containerColor = semantic.successContainer,
                modifier = Modifier
                    .weight(1f)
                    .testTag("streak_bonus_xp_chip")
            )
        }

        Spacer(Modifier.height(14.dp))

        // Bande des 7 derniers jours
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = semantic.surfaceSubtle,
            border = BorderStroke(1.dp, semantic.border),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("streak_7day_strip")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                summary.last7Days.forEachIndexed { idx, dot ->
                    StreakDayDotColumn(
                        dot = dot,
                        modifier = Modifier.testTag("streak_day_dot_$idx")
                    )
                }
            }
        }

        summary.nextReward?.let { next ->
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (next.isUnlocked && !next.isClaimed) {
                        "Palier prêt à réclamer : ${next.title}"
                    } else {
                        "Prochain palier : ${next.title} (${next.requiredStreakDays} j)"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${summary.currentStreak.coerceAtMost(next.requiredStreakDays)}/${next.requiredStreakDays} j",
                    style = FrancaisProTextStyles.MonoMetric,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(6.dp))
            val trackColor = semantic.surfaceSubtle
            val fillColor = if (next.isUnlocked) semantic.warning else MaterialTheme.colorScheme.primary
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
            ) {
                val r = CornerRadius(size.height / 2f, size.height / 2f)
                drawRoundRect(color = trackColor, size = size, cornerRadius = r)
                val w = size.width * summary.nextRewardProgressRatio.coerceIn(0f, 1f)
                if (w > 0f) {
                    drawRoundRect(color = fillColor, size = Size(w, size.height), cornerRadius = r)
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "Récompenses de régularité",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            summary.rewards.forEach { reward ->
                StreakRewardRow(
                    reward = reward,
                    onClaimClick = { onClaimReward(reward.id) }
                )
            }
        }
    }
}

@Composable
private fun StreakStatChip(
    icon: ImageVector,
    label: String,
    tint: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerColor.copy(alpha = 0.7f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StreakDayDotColumn(
    dot: StreakDayDot,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    val circleBg = when {
        dot.practiced -> semantic.success
        dot.freezeUsed -> MaterialTheme.colorScheme.primary
        dot.isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val circleBorder = when {
        dot.practiced -> semantic.success
        dot.freezeUsed -> MaterialTheme.colorScheme.primary
        dot.isToday -> MaterialTheme.colorScheme.primary
        else -> semantic.border
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = if (dot.isToday) "Auj." else dot.dayInitial,
            style = MaterialTheme.typography.labelSmall,
            color = if (dot.isToday) MaterialTheme.colorScheme.primary else semantic.textSecondary,
            fontWeight = if (dot.isToday) FontWeight.Bold else FontWeight.Medium
        )
        Surface(
            shape = CircleShape,
            color = circleBg,
            border = BorderStroke(1.5.dp, circleBorder),
            modifier = Modifier.size(30.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                when {
                    dot.practiced -> Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Session pratiquée",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    dot.freezeUsed -> Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = "Bouclier utilisé",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    dot.isToday -> Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@Composable
private fun StreakRewardRow(
    reward: StreakReward,
    onClaimClick: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    val canClaim = reward.isUnlocked && !reward.isClaimed
    val bgColor = when {
        canClaim -> semantic.warningContainer.copy(alpha = 0.5f)
        reward.isClaimed -> semantic.successContainer.copy(alpha = 0.35f)
        else -> semantic.surfaceSubtle
    }
    val border = when {
        canClaim -> semantic.warning.copy(alpha = 0.6f)
        reward.isClaimed -> semantic.success.copy(alpha = 0.45f)
        else -> semantic.border
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.dp, border),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("streak_reward_${reward.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = when {
                    reward.isClaimed -> semantic.successContainer
                    canClaim -> semantic.warningContainer
                    else -> MaterialTheme.colorScheme.surface
                },
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when {
                            reward.isClaimed -> Icons.Filled.CheckCircle
                            canClaim -> Icons.Filled.CardGiftcard
                            else -> Icons.Filled.Lock
                        },
                        contentDescription = reward.title,
                        tint = when {
                            reward.isClaimed -> semantic.success
                            canClaim -> semantic.warning
                            else -> semantic.textSecondary
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${reward.requiredStreakDays} j · ${reward.title}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "+${reward.bonusXp} XP · +${reward.freezeTokensGranted} bouclier${if (reward.freezeTokensGranted > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = semantic.textSecondary
                )
            }

            when {
                canClaim -> {
                    PrimaryButton(
                        text = "Réclamer",
                        onClick = onClaimClick,
                        containerColor = semantic.warning,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("streak_claim_${reward.id}")
                    )
                }
                reward.isClaimed -> {
                    CategoryBadge(
                        label = "Réclamé",
                        color = semantic.success,
                        containerColor = semantic.successContainer,
                        icon = Icons.Filled.Check,
                        modifier = Modifier.testTag("streak_claimed_${reward.id}")
                    )
                }
                else -> {
                    CategoryBadge(
                        label = "${reward.requiredStreakDays} j",
                        color = semantic.textSecondary,
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }
    }
}

@Composable
private fun AcademyAchievementBadgeCard(
    item: AchievementProgressItem,
    onActionClick: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    val ach = item.achievement
    val unlocked = ach.isUnlocked
    val iconColor = if (unlocked) semantic.warning else semantic.textSecondary
    val iconContainer = if (unlocked) semantic.warningContainer else semantic.surfaceSubtle
    val barFill = if (unlocked) semantic.success else MaterialTheme.colorScheme.primary
    val trackColor = semantic.surfaceSubtle

    EditorialCard(
        onClick = if (!unlocked && item.targetRoute != null) onActionClick else null,
        containerColor = if (unlocked) semantic.successContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface,
        borderColor = if (unlocked) semantic.success.copy(alpha = 0.45f) else semantic.border,
        contentPadding = 14.dp,
        modifier = Modifier.testTag("academy_badge_${ach.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconContainer,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (unlocked) semantic.warning.copy(alpha = 0.45f) else semantic.border
                ),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = CurriculumIcons.forAchievement(ach.iconName),
                        contentDescription = ach.title,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ach.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    CategoryBadge(
                        label = if (unlocked) "Obtenu" else item.domainLabel,
                        color = if (unlocked) semantic.success else MaterialTheme.colorScheme.primary,
                        containerColor = if (unlocked) semantic.successContainer else MaterialTheme.colorScheme.primaryContainer,
                        icon = if (unlocked) Icons.Filled.Verified else null
                    )
                }

                Text(
                    text = ach.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = semantic.textSecondary
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                    ) {
                        val r = CornerRadius(size.height / 2f, size.height / 2f)
                        drawRoundRect(color = trackColor, size = size, cornerRadius = r)
                        if (item.progressRatio > 0f) {
                            drawRoundRect(
                                color = barFill,
                                size = Size(size.width * item.progressRatio, size.height),
                                cornerRadius = r
                            )
                        }
                    }
                    Text(
                        text = item.progressText,
                        style = FrancaisProTextStyles.MonoMetric,
                        color = if (unlocked) semantic.success else semantic.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (!unlocked && item.targetRoute != null) {
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.actionHint,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickReferenceToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    EditorialCard(
        onClick = onClick,
        contentPadding = 12.dp,
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = containerColor,
            modifier = Modifier.size(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = semantic.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Écran 8 : Parcours détaillé d'un niveau CECRL — Chapitres séquentiels (01 → 04)
 * avec déblocage progressif, filtres thématiques avec icônes et objectif de palier.
 */
@Composable
fun LessonListScreen(
    level: String,
    lessons: List<Lesson>,
    progress: List<LessonProgress>,
    onOpenLesson: (String) -> Unit,
    onBack: () -> Unit
) {
    val roadmap = remember(lessons, progress) {
        CoursePathEngine.buildRoadmap(lessons, progress)
    }
    val levelState: LevelRoadmapState? = remember(roadmap, level) {
        roadmap.levels.firstOrNull { it.level == level }
    }
    val allSteps = remember(roadmap, levelState, level) {
        if (level == "ALL") roadmap.levels.flatMap { it.steps } else levelState?.steps.orEmpty()
    }
    val themes = remember(allSteps) {
        listOf("Tous") + allSteps.map { it.lesson.theme }.distinct()
    }
    var selectedTheme by remember { mutableStateOf("Tous") }
    var lockedChapterNotice by remember { mutableStateOf<String?>(null) }

    val filteredSteps = remember(allSteps, selectedTheme) {
        if (selectedTheme == "Tous") allSteps else allSteps.filter { it.lesson.theme == selectedTheme }
    }
    val semantic = LocalSemanticColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(
            title = if (level == "ALL") "Tous les cours" else "Parcours $level",
            subtitle = levelState?.title ?: (AdaptiveProgressionEngine.cefrLabels[level] ?: "Académie complète"),
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = FrancaisProDimens.MaxContentWidth)
                .align(Alignment.CenterHorizontally)
                .testTag("lesson_list_column"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (levelState != null) {
                item(key = "level_summary_hero") {
                    LevelJourneySummaryCard(levelState = levelState)
                }
            }

            if (themes.size > 1) {
                item(key = "theme_filters") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(themes) { theme ->
                            FilterChip(
                                selected = selectedTheme == theme,
                                onClick = { selectedTheme = theme },
                                leadingIcon = if (theme != "Tous") {
                                    {
                                        Icon(
                                            imageVector = CurriculumIcons.forTheme(theme),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                label = { Text(theme) }
                            )
                        }
                    }
                }
            }

            item(key = "steps_header") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader(
                        title = "Étapes du parcours",
                        subtitle = "Chaque chapitre validé débloque automatiquement le suivant",
                        icon = Icons.Filled.Route
                    )
                    AnimatedVisibility(visible = lockedChapterNotice != null) {
                        LockedStepBanner(
                            message = lockedChapterNotice.orEmpty(),
                            onDismiss = { lockedChapterNotice = null },
                            modifier = Modifier.testTag("lesson_list_locked_banner")
                        )
                    }
                }
            }

            itemsIndexed(filteredSteps, key = { _, step -> step.lesson.id }) { idx, step ->
                val isCompleted = step.status == LessonUnlockStatus.COMPLETED
                val isLocked = step.status == LessonUnlockStatus.LOCKED
                val isCurrent = step.status == LessonUnlockStatus.CURRENT
                val isLast = idx == filteredSteps.lastIndex

                RoadmapTimelineRow(
                    isCompleted = isCompleted,
                    isUnlocked = !isLocked,
                    isLast = isLast,
                    icon = when {
                        isCompleted -> Icons.Filled.Check
                        isLocked -> Icons.Filled.Lock
                        else -> Icons.Filled.PlayArrow
                    },
                    stepLabel = step.chapterNumber.toString().padStart(2, '0')
                ) {
                    LessonCard(
                        title = step.lesson.title,
                        summary = step.lesson.summary,
                        theme = step.lesson.theme,
                        level = step.lesson.level,
                        estimatedMinutes = step.lesson.estimatedMinutes,
                        isCompleted = isCompleted,
                        scorePercent = step.scorePercent,
                        chapterNumber = step.chapterNumber,
                        isLocked = isLocked,
                        isCurrent = isCurrent,
                        lockReason = step.lockReason,
                        onClick = {
                            if (!isLocked) {
                                lockedChapterNotice = null
                                onOpenLesson(step.lesson.id)
                            } else {
                                lockedChapterNotice = step.lockReason
                                    ?: "Validez d'abord le chapitre précédent pour ouvrir ce cours."
                            }
                        },
                        modifier = Modifier.testTag("lesson_card_${step.lesson.id}")
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelJourneySummaryCard(levelState: LevelRoadmapState) {
    val semantic = LocalSemanticColors.current
    val nextLevel = CoursePathEngine.nextLevelCode(levelState.level)

    EditorialCard(
        contentPadding = 16.dp,
        borderColor = if (levelState.isCompleted) semantic.success.copy(alpha = 0.45f)
        else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
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
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (levelState.isCompleted) semantic.successContainer else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = CurriculumIcons.forLevel(levelState.level),
                            contentDescription = null,
                            tint = if (levelState.isCompleted) semantic.success else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CefrLevelPill(level = levelState.level)
                        Text(
                            text = levelState.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = levelState.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = semantic.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            CategoryBadge(
                label = "${levelState.xpEarned} XP",
                color = semantic.success,
                containerColor = semantic.successContainer,
                icon = Icons.Filled.Bolt
            )
        }

        Spacer(Modifier.height(12.dp))

        val trackColor = semantic.surfaceSubtle
        val barColor = if (levelState.isCompleted) semantic.success else MaterialTheme.colorScheme.primary
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        ) {
            val r = CornerRadius(size.height / 2f, size.height / 2f)
            drawRoundRect(color = trackColor, size = size, cornerRadius = r)
            if (levelState.progressRatio > 0f) {
                drawRoundRect(
                    color = barColor,
                    size = Size(size.width * levelState.progressRatio, size.height),
                    cornerRadius = r
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${levelState.completedCount} / ${levelState.totalCount} chapitres validés",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (levelState.isCompleted) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (nextLevel != null) Icons.Filled.LockOpen else Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = semantic.success,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = if (nextLevel != null) "Niveau $nextLevel débloqué !" else "Cursus complété !",
                        style = MaterialTheme.typography.labelMedium,
                        color = semantic.success,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (nextLevel != null) {
                Text(
                    text = "Objectif : débloquer $nextLevel",
                    style = MaterialTheme.typography.labelSmall,
                    color = semantic.textSecondary
                )
            }
        }
    }
}

@Composable
internal fun RoadmapTimelineRow(
    isCompleted: Boolean,
    isUnlocked: Boolean,
    isLast: Boolean,
    icon: ImageVector,
    stepLabel: String,
    content: @Composable () -> Unit
) {
    val semantic = LocalSemanticColors.current
    val nodeBg = when {
        isCompleted -> semantic.success
        isUnlocked -> MaterialTheme.colorScheme.primary
        else -> semantic.border
    }
    val nodeFg = when {
        isCompleted || isUnlocked -> Color.White
        else -> semantic.textSecondary
    }
    val lineColor = when {
        isCompleted -> semantic.success.copy(alpha = 0.55f)
        isUnlocked -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        else -> semantic.border.copy(alpha = 0.6f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier
                .width(34.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(nodeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Filled.Verified else icon,
                    contentDescription = "Étape $stepLabel",
                    tint = nodeFg,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.5.dp)
                        .weight(1f)
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(lineColor)
                )
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

@Composable
internal fun LockedStepBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    Surface(
        shape = RoundedCornerShape(FrancaisProDimens.RadiusCard),
        color = semantic.warningContainer,
        border = BorderStroke(1.dp, semantic.warning.copy(alpha = 0.4f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = semantic.warning,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = semantic.onWarningContainer,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Fermer l'alerte",
                    tint = semantic.onWarningContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
