package com.example.features.academy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.database.Exercise
import com.example.core.database.Favorite
import com.example.core.database.Lesson
import com.example.core.database.LessonProgress
import com.example.core.design.AppIconButton
import com.example.core.design.AppTopBar
import com.example.core.design.CategoryBadge
import com.example.core.design.CefrLevelPill
import com.example.core.design.CurriculumIcons
import com.example.core.design.EditorialCard
import com.example.core.design.EmptyState
import com.example.core.design.PrimaryButton
import com.example.core.design.SecondaryButton
import com.example.core.design.StructuredSectionTitle
import com.example.core.design.SuccessFeedback
import com.example.learning.progression.CoursePathEngine
import com.example.ui.theme.FrancaisProDimens
import com.example.ui.theme.FrancaisProTextStyles
import com.example.ui.theme.LocalSemanticColors

/**
 * Écran 9 : Cours structuré en 7 sections numérotées avec icônes modernes, barre de lecture,
 * mini-exercice intégré, validation progressive et déblocage automatique du chapitre suivant.
 */
@Composable
fun LessonDetailScreen(
    lesson: Lesson?,
    allLessons: List<Lesson>,
    allExercises: List<Exercise>,
    progress: LessonProgress?,
    favorites: List<Favorite>,
    onToggleFavorite: (Lesson) -> Unit,
    onOpenLesson: (String) -> Unit,
    onStartQuiz: (String) -> Unit,
    onMarkCompleted: (String, Int) -> Unit,
    onBack: () -> Unit,
    allProgress: List<LessonProgress> = emptyList()
) {
    val semantic = LocalSemanticColors.current
    val scrollState = rememberScrollState()
    val readingProgress by remember {
        derivedStateOf {
            if (scrollState.maxValue <= 0) 0.25f
            else (scrollState.value.toFloat() / scrollState.maxValue).coerceIn(0.08f, 1f)
        }
    }

    val miniExercise = remember(lesson?.id, allExercises) {
        allExercises.firstOrNull { it.lessonId == lesson?.id && it.optionsPipe.isNotBlank() }
            ?: allExercises.firstOrNull { it.level == lesson?.level && it.optionsPipe.isNotBlank() }
    }
    var miniChoice by remember(lesson?.id) { mutableStateOf("") }
    var miniChecked by remember(lesson?.id) { mutableStateOf(false) }
    var miniCorrect by remember(lesson?.id) { mutableStateOf(false) }

    val orderedLessons = remember(allLessons, lesson?.level) {
        allLessons.filter { it.level == lesson?.level }.sortedBy { it.chapterOrder }
    }
    val currentPos = orderedLessons.indexOfFirst { it.id == lesson?.id }
    val prevLesson = if (currentPos > 0) orderedLessons[currentPos - 1] else null
    val nextLessonInLevel = if (currentPos >= 0 && currentPos + 1 < orderedLessons.size) orderedLessons[currentPos + 1] else null

    val nextLevelCode = lesson?.let { CoursePathEngine.nextLevelCode(it.level) }
    val firstLessonOfNextLevel = remember(allLessons, nextLevelCode) {
        if (nextLevelCode != null) {
            allLessons.filter { it.level == nextLevelCode }.minByOrNull { it.chapterOrder }
        } else null
    }

    val isCurrentCompleted = progress?.isCompleted == true
    val isNextLessonUnlocked = remember(isCurrentCompleted, nextLessonInLevel, allProgress) {
        isCurrentCompleted || (nextLessonInLevel != null && allProgress.any { it.lessonId == nextLessonInLevel.id && it.isCompleted })
    }
    val effectiveProgressList = remember(allProgress, progress) {
        if (progress != null && allProgress.none { it.lessonId == progress.lessonId }) {
            allProgress + progress
        } else allProgress
    }
    val isLevelNowCompleted = remember(orderedLessons, effectiveProgressList) {
        orderedLessons.isNotEmpty() && orderedLessons.all { l ->
            effectiveProgressList.any { it.lessonId == l.id && it.isCompleted }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isFav = lesson != null && favorites.any { it.itemType == "LESSON" && it.itemId == lesson.id }
        AppTopBar(
            title = lesson?.moduleTitle ?: "Cours",
            subtitle = lesson?.let {
                val chapLabel = if (currentPos >= 0) "Chapitre ${currentPos + 1}/${orderedLessons.size}" else it.theme
                "${it.level} · $chapLabel · ${it.estimatedMinutes} min"
            },
            onBack = onBack,
            actions = {
                if (lesson != null) {
                    AppIconButton(
                        icon = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFav) "Retirer des favoris" else "Ajouter aux favoris",
                        tint = if (isFav) semantic.danger else MaterialTheme.colorScheme.onSurface,
                        onClick = { onToggleFavorite(lesson) },
                        modifier = Modifier.testTag("lesson_fav_btn")
                    )
                }
            }
        )

        if (lesson == null) {
            EmptyState(
                icon = Icons.Filled.School,
                title = "Leçon introuvable",
                message = "Sélectionnez un chapitre débloqué depuis l'Académie.",
                actionLabel = "Retour",
                onAction = onBack
            )
            return
        }

        val (themeColor, themeBg) = CurriculumIcons.themeAccent(
            theme = lesson.theme,
            semantic = semantic,
            primary = MaterialTheme.colorScheme.primary,
            primaryContainer = MaterialTheme.colorScheme.primaryContainer
        )

        LinearProgressIndicator(
            progress = { readingProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = semantic.surfaceSubtle
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = FrancaisProDimens.MaxContentWidth)
                .align(Alignment.CenterHorizontally)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // En-tête éditorial du chapitre
            EditorialCard(
                contentPadding = 18.dp,
                borderColor = if (isCurrentCompleted) semantic.success.copy(alpha = 0.45f) else themeColor.copy(alpha = 0.35f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CefrLevelPill(level = lesson.level)
                    if (currentPos >= 0) {
                        CategoryBadge(
                            label = "Chap. ${(currentPos + 1).toString().padStart(2, '0')}/${orderedLessons.size}",
                            color = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                    CategoryBadge(
                        label = lesson.theme,
                        color = themeColor,
                        containerColor = themeBg,
                        icon = CurriculumIcons.forTheme(lesson.theme)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(lesson.title, style = FrancaisProTextStyles.Heading1)
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = semantic.textSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "${lesson.estimatedMinutes} min · +${CoursePathEngine.XP_PER_LESSON} XP",
                            style = MaterialTheme.typography.labelMedium,
                            color = semantic.textSecondary
                        )
                    }
                    if (isCurrentCompleted) {
                        CategoryBadge(
                            label = "Validée · ${progress?.quizScorePercent ?: 100} %",
                            color = semantic.success,
                            containerColor = semantic.successContainer,
                            icon = Icons.Filled.Verified
                        )
                    }
                }
            }

            // Section 01 : Objectif pédagogique
            EditorialCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)) {
                StructuredSectionTitle(
                    title = "Objectif du chapitre",
                    stepLabel = "ÉTAPE 01",
                    icon = Icons.Filled.Flag
                )
                Spacer(Modifier.height(10.dp))
                Text(lesson.summary, style = MaterialTheme.typography.bodyLarge)
            }

            // Section 02 : Cours & règles essentielles
            EditorialCard {
                StructuredSectionTitle(
                    title = "Cours & règles essentielles",
                    stepLabel = "ÉTAPE 02",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    accentColor = themeColor,
                    containerColor = themeBg
                )
                Spacer(Modifier.height(10.dp))
                Text(lesson.ruleContent, style = MaterialTheme.typography.bodyLarge)
            }

            // Section 03 : Exemples commentés
            val examples = lesson.examplesJson.split("||").filter { it.isNotBlank() }
            if (examples.isNotEmpty()) {
                EditorialCard {
                    StructuredSectionTitle(
                        title = "Exemples commentés",
                        stepLabel = "ÉTAPE 03",
                        icon = Icons.Filled.FormatQuote,
                        accentColor = semantic.syntaxColor,
                        containerColor = semantic.syntaxContainer
                    )
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        examples.forEachIndexed { idx, ex ->
                            Surface(
                                shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                                color = semantic.surfaceSubtle,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "${idx + 1}.",
                                        style = FrancaisProTextStyles.MonoMetric,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = ex,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 04 : Encadré « Règle d'or à retenir »
            val keyTakeaway = lesson.ruleContent.split(".").firstOrNull()?.trim().orEmpty().ifBlank { lesson.summary }
            Surface(
                shape = RoundedCornerShape(FrancaisProDimens.RadiusCard),
                color = semantic.successContainer,
                border = BorderStroke(1.dp, semantic.success.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    StructuredSectionTitle(
                        title = "Règle d'or à retenir",
                        stepLabel = "ÉTAPE 04",
                        icon = Icons.Filled.Lightbulb,
                        accentColor = semantic.success,
                        containerColor = semantic.success.copy(alpha = 0.16f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "$keyTakeaway.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = semantic.onSuccessContainer
                    )
                }
            }

            // Section 05 : Encadré « Pièges fréquents à éviter »
            Surface(
                shape = RoundedCornerShape(FrancaisProDimens.RadiusCard),
                color = semantic.warningContainer,
                border = BorderStroke(1.dp, semantic.warning.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    StructuredSectionTitle(
                        title = "Pièges fréquents à éviter",
                        stepLabel = "ÉTAPE 05",
                        icon = Icons.Filled.WarningAmber,
                        accentColor = semantic.warning,
                        containerColor = semantic.warning.copy(alpha = 0.16f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = lesson.pitfallsContent,
                        style = MaterialTheme.typography.bodyMedium,
                        color = semantic.onWarningContainer
                    )
                }
            }

            // Section 06 : Mise en pratique immédiate (mini-exercice intégré)
            if (miniExercise != null) {
                val options = miniExercise.optionsPipe.split("|").filter { it.isNotBlank() }
                EditorialCard {
                    StructuredSectionTitle(
                        title = "Mise en pratique immédiate",
                        stepLabel = "ÉTAPE 06",
                        icon = Icons.Filled.FactCheck
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(miniExercise.instruction, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(6.dp))
                    Text(miniExercise.promptText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))

                    options.forEach { opt ->
                        val isChosen = miniChoice == opt
                        val bg = when {
                            !miniChecked -> if (isChosen) MaterialTheme.colorScheme.primaryContainer else semantic.surfaceSubtle
                            opt.equals(miniExercise.expectedAnswer, ignoreCase = true) -> semantic.successContainer
                            isChosen -> semantic.dangerContainer
                            else -> semantic.surfaceSubtle
                        }
                        Surface(
                            onClick = { if (!miniChecked) miniChoice = opt },
                            shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                            color = bg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isChosen, onClick = { if (!miniChecked) miniChoice = opt })
                                Text(opt, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    if (!miniChecked) {
                        PrimaryButton(
                            text = "Vérifier ma réponse",
                            enabled = miniChoice.isNotBlank(),
                            onClick = {
                                miniCorrect = miniChoice.equals(miniExercise.expectedAnswer, ignoreCase = true)
                                miniChecked = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("lesson_mini_check_btn")
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                            color = if (miniCorrect) semantic.successContainer else semantic.warningContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (miniCorrect) "Excellent ! Règle assimilée." else "Réponse attendue : ${miniExercise.expectedAnswer}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(miniExercise.explanation, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // Section 07 : Validation du chapitre & déblocage de la suite
            EditorialCard(
                borderColor = if (isCurrentCompleted) semantic.success.copy(alpha = 0.45f) else semantic.border
            ) {
                StructuredSectionTitle(
                    title = "Validation du chapitre",
                    stepLabel = "ÉTAPE 07",
                    icon = Icons.Filled.Quiz,
                    accentColor = if (isCurrentCompleted) semantic.success else MaterialTheme.colorScheme.primary,
                    containerColor = if (isCurrentCompleted) semantic.successContainer else MaterialTheme.colorScheme.primaryContainer
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (isCurrentCompleted) {
                        if (nextLessonInLevel != null) {
                            "Chapitre validé ! Le chapitre suivant est maintenant débloqué."
                        } else if (isLevelNowCompleted && nextLevelCode != null) {
                            "Félicitations ! Vous avez complété le niveau ${lesson.level} et débloqué le niveau $nextLevelCode."
                        } else {
                            "Chapitre validé et enregistré dans votre parcours."
                        }
                    } else {
                        "Réussissez le quiz (≥ ${CoursePathEngine.QUIZ_PASS_THRESHOLD_PERCENT} %) ou validez ce cours pour débloquer l'étape suivante."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = semantic.textSecondary
                )
                Spacer(Modifier.height(12.dp))

                PrimaryButton(
                    text = "Passer le quiz de validation",
                    onClick = { onStartQuiz(lesson.id) },
                    icon = Icons.Filled.Quiz,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lesson_start_quiz_btn")
                )
                Spacer(Modifier.height(8.dp))
                SecondaryButton(
                    text = if (isCurrentCompleted) "Chapitre déjà validé" else "Valider ce chapitre (+${CoursePathEngine.XP_PER_LESSON} XP)",
                    onClick = { onMarkCompleted(lesson.id, if (miniChecked && miniCorrect) 100 else 90) },
                    icon = Icons.Filled.CheckCircle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lesson_mark_complete_btn")
                )
            }

            // Navigation progressive Précédente / Suivante ou Niveau suivant débloqué
            if (prevLesson != null || nextLessonInLevel != null || (isLevelNowCompleted && firstLessonOfNextLevel != null)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (nextLessonInLevel != null && !isNextLessonUnlocked) {
                        Surface(
                            shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                            color = semantic.surfaceSubtle,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = semantic.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Validez ce chapitre pour débloquer « ${nextLessonInLevel.title} ».",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = semantic.textSecondary
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (prevLesson != null) {
                            SecondaryButton(
                                text = "Précédent",
                                icon = Icons.AutoMirrored.Filled.ArrowBack,
                                onClick = { onOpenLesson(prevLesson.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        when {
                            nextLessonInLevel != null -> {
                                PrimaryButton(
                                    text = "Chapitre suivant",
                                    icon = if (isNextLessonUnlocked) Icons.AutoMirrored.Filled.ArrowForward else Icons.Filled.Lock,
                                    enabled = isNextLessonUnlocked,
                                    onClick = { onOpenLesson(nextLessonInLevel.id) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("lesson_next_btn")
                                )
                            }
                            isLevelNowCompleted && firstLessonOfNextLevel != null && nextLevelCode != null -> {
                                PrimaryButton(
                                    text = "Passer au niveau $nextLevelCode",
                                    icon = Icons.Filled.LockOpen,
                                    onClick = { onOpenLesson(firstLessonOfNextLevel.id) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("lesson_next_level_btn")
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

/**
 * Écran 10 : Quiz de validation d'un chapitre avec seuil de réussite (≥ 60 %)
 * pour débloquer le chapitre ou le niveau CECRL suivant.
 */
@Composable
fun LessonQuizScreen(
    lesson: Lesson?,
    allExercises: List<Exercise>,
    onQuizCompleted: (lessonId: String, scorePercent: Int) -> Unit,
    onBack: () -> Unit
) {
    val quizItems = remember(lesson, allExercises) {
        val exact = allExercises.filter { it.lessonId == lesson?.id && it.optionsPipe.isNotBlank() }
        if (exact.isNotEmpty()) exact else allExercises.filter { it.optionsPipe.isNotBlank() }.take(3)
    }

    var index by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(false) }
    var correctCount by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    val semantic = LocalSemanticColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(
            title = "Quiz · ${lesson?.level ?: "Académie"}",
            subtitle = lesson?.title,
            onBack = onBack
        )

        if (quizItems.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Quiz,
                title = "Quiz prêt",
                message = "Aucun QCM spécifique associé. Vous pouvez valider ce chapitre directement.",
                actionLabel = "Valider le chapitre",
                onAction = {
                    lesson?.let { onQuizCompleted(it.id, 100) }
                    onBack()
                }
            )
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = FrancaisProDimens.MaxContentWidth)
                .align(Alignment.CenterHorizontally)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!finished) {
                val item = quizItems[index]
                val options = item.optionsPipe.split("|").filter { it.isNotBlank() }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question ${index + 1} sur ${quizItems.size}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Seuil de validation : ${CoursePathEngine.QUIZ_PASS_THRESHOLD_PERCENT} %",
                        style = MaterialTheme.typography.labelSmall,
                        color = semantic.textSecondary
                    )
                }

                LinearProgressIndicator(
                    progress = { (index + 1).toFloat() / quizItems.size },
                    modifier = Modifier.fillMaxWidth()
                )

                EditorialCard {
                    Text(item.instruction, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text(item.promptText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))

                    options.forEach { opt ->
                        val isChosen = selected == opt
                        val bg = when {
                            !checked -> if (isChosen) MaterialTheme.colorScheme.primaryContainer else semantic.surfaceSubtle
                            opt.equals(item.expectedAnswer, ignoreCase = true) -> semantic.successContainer
                            isChosen -> semantic.dangerContainer
                            else -> semantic.surfaceSubtle
                        }
                        Surface(
                            onClick = { if (!checked) selected = opt },
                            shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                            color = bg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isChosen, onClick = { if (!checked) selected = opt })
                                Text(opt, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    if (checked) {
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                            color = semantic.surfaceSubtle,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(item.explanation, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
                        }
                    }
                }

                if (!checked) {
                    PrimaryButton(
                        text = "Vérifier",
                        onClick = {
                            if (selected.equals(item.expectedAnswer, ignoreCase = true)) correctCount++
                            checked = true
                        },
                        enabled = selected.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quiz_validate_btn")
                    )
                } else {
                    PrimaryButton(
                        text = if (index + 1 < quizItems.size) "Question suivante" else "Terminer le quiz",
                        onClick = {
                            if (index + 1 < quizItems.size) {
                                index++
                                selected = ""
                                checked = false
                            } else {
                                val pct = (correctCount * 100) / quizItems.size
                                if (pct >= CoursePathEngine.QUIZ_PASS_THRESHOLD_PERCENT) {
                                    lesson?.let { onQuizCompleted(it.id, pct) }
                                }
                                finished = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quiz_next_btn")
                    )
                }
            } else {
                val pct = (correctCount * 100) / quizItems.size
                val passed = pct >= CoursePathEngine.QUIZ_PASS_THRESHOLD_PERCENT
                val xpGain = CoursePathEngine.XP_PER_LESSON + if (pct >= 90) CoursePathEngine.XP_EXCELLENCE_BONUS else 0

                if (passed) {
                    SuccessFeedback(
                        title = "Chapitre validé — $pct %",
                        message = "Score : $correctCount / ${quizItems.size} (+$xpGain XP). L'étape suivante de votre parcours est désormais débloquée !"
                    )
                    Surface(
                        shape = RoundedCornerShape(FrancaisProDimens.RadiusCard),
                        color = semantic.successContainer,
                        border = BorderStroke(1.dp, semantic.success.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = semantic.success)
                            Text(
                                text = "Progression enregistrée localement · +$xpGain XP gagnés",
                                style = MaterialTheme.typography.labelLarge,
                                color = semantic.onSuccessContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    PrimaryButton(
                        text = "Retourner au cours",
                        onClick = onBack,
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    EditorialCard(
                        containerColor = semantic.warningContainer,
                        borderColor = semantic.warning.copy(alpha = 0.4f)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = semantic.warning)
                            Column {
                                Text(
                                    text = "Score insuffisant ($pct %)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = semantic.onWarningContainer
                                )
                                Text(
                                    text = "Il faut au moins ${CoursePathEngine.QUIZ_PASS_THRESHOLD_PERCENT} % pour valider ce chapitre et débloquer la suite. Révisez la règle et réessayez !",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = semantic.onWarningContainer
                                )
                            }
                        }
                    }
                    PrimaryButton(
                        text = "Réessayer le quiz",
                        icon = Icons.Filled.Replay,
                        onClick = {
                            index = 0
                            selected = ""
                            checked = false
                            correctCount = 0
                            finished = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quiz_retry_btn")
                    )
                    SecondaryButton(
                        text = "Relire le cours",
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
