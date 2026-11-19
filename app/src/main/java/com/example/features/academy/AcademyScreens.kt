package com.example.features.academy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.FilterChip
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
import com.example.core.design.EditorialCard
import com.example.core.design.EmptyState
import com.example.core.design.LessonCard
import com.example.core.design.LevelCard
import com.example.core.design.PrimaryButton
import com.example.core.design.SecondaryButton
import com.example.core.design.SuccessFeedback
import com.example.learning.progression.AdaptiveProgressionEngine
import com.example.ui.theme.FrancaisProDimens
import com.example.ui.theme.FrancaisProTextStyles
import com.example.ui.theme.LocalSemanticColors

/**
 * Écran 7 : Académie du français (parcours visuels A1–C2 + Dictionnaire / Conjugueur / Vocabulaire).
 */
@Composable
fun AcademyScreen(
    lessons: List<Lesson>,
    progress: List<LessonProgress>,
    onOpenLevelLessons: (String) -> Unit,
    onOpenDictionary: () -> Unit,
    onOpenConjugation: () -> Unit,
    onOpenVocabulary: () -> Unit
) {
    val levels = listOf(
        "A1" to "Fondamentaux",
        "A2" to "Intermédiaire initial",
        "B1" to "Autonome",
        "B2" to "Avancé",
        "C1" to "Supérieur",
        "C2" to "Maîtrise experte"
    )
    val completedIds = remember(progress) { progress.filter { it.isCompleted }.map { it.lessonId }.toSet() }
    val semantic = LocalSemanticColors.current

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
                .widthIn(max = FrancaisProDimens.MaxContentWidth),
            contentPadding = PaddingValues(horizontal = hPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Apprendre", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Parcours structuré du niveau A1 au niveau C2",
                        style = MaterialTheme.typography.bodySmall,
                        color = semantic.textSecondary
                    )
                }
            }

            // Outils de référence rapides
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EditorialCard(
                        onClick = onOpenDictionary,
                        contentPadding = 12.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("academy_open_dictionary")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text("Dictionnaire", style = MaterialTheme.typography.labelLarge)
                    }
                    EditorialCard(
                        onClick = onOpenConjugation,
                        contentPadding = 12.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("academy_open_conjugation")
                    ) {
                        Icon(Icons.Filled.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text("Conjugueur", style = MaterialTheme.typography.labelLarge)
                    }
                    EditorialCard(
                        onClick = onOpenVocabulary,
                        contentPadding = 12.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("academy_open_vocabulary")
                    ) {
                        Icon(Icons.Filled.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text("Vocabulaire", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            items(levels, key = { it.first }) { (level, difficultyLabel) ->
                val levelLessons = lessons.filter { it.level == level }
                val done = levelLessons.count { it.id in completedIds }
                val nextLesson = levelLessons.firstOrNull { it.id !in completedIds } ?: levelLessons.firstOrNull()
                val skills = levelLessons.map { it.theme }.distinct().take(3)

                LevelCard(
                    level = level,
                    title = AdaptiveProgressionEngine.cefrLabels[level] ?: level,
                    difficultyLabel = difficultyLabel,
                    completedCount = done,
                    totalCount = levelLessons.size,
                    nextLessonTitle = nextLesson?.title,
                    skillsPreview = skills,
                    onClick = { onOpenLevelLessons(level) },
                    modifier = Modifier.testTag("academy_level_$level")
                )
            }
        }
    }
}

/**
 * Écran 8 : Liste des cours d'un niveau CECRL.
 */
@Composable
fun LessonListScreen(
    level: String,
    lessons: List<Lesson>,
    progress: List<LessonProgress>,
    onOpenLesson: (String) -> Unit,
    onBack: () -> Unit
) {
    val levelLessons = remember(lessons, level) {
        if (level == "ALL") lessons else lessons.filter { it.level == level }
    }
    val themes = remember(levelLessons) { listOf("Tous") + levelLessons.map { it.theme }.distinct() }
    var selectedTheme by remember { mutableStateOf("Tous") }
    val filtered = remember(levelLessons, selectedTheme) {
        if (selectedTheme == "Tous") levelLessons else levelLessons.filter { it.theme == selectedTheme }
    }
    val progressMap = remember(progress) { progress.associateBy { it.lessonId } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(
            title = if (level == "ALL") "Tous les cours" else "Parcours $level",
            subtitle = AdaptiveProgressionEngine.cefrLabels[level] ?: "Académie complète",
            onBack = onBack
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(themes) { theme ->
                FilterChip(
                    selected = selectedTheme == theme,
                    onClick = { selectedTheme = theme },
                    label = { Text(theme) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = FrancaisProDimens.MaxContentWidth)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtered, key = { it.id }) { lesson ->
                val lp = progressMap[lesson.id]
                LessonCard(
                    title = lesson.title,
                    summary = lesson.summary,
                    theme = lesson.theme,
                    level = lesson.level,
                    estimatedMinutes = lesson.estimatedMinutes,
                    isCompleted = lp?.isCompleted == true,
                    scorePercent = lp?.quizScorePercent,
                    onClick = { onOpenLesson(lesson.id) },
                    modifier = Modifier.testTag("lesson_card_${lesson.id}")
                )
            }
        }
    }
}

/**
 * Écran 9 : Leçon structurée en 8 sections claires avec barre de lecture et mini-exercice intégré.
 * 1. Titre · 2. Objectif · 3. Explication · 4. Exemples · 5. Règle à retenir
 * 6. Erreurs fréquentes · 7. Mini-exercice intégré · 8. Résultat & navigation Précédente/Suivante
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
    onBack: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    val scrollState = rememberScrollState()
    val readingProgress by remember {
        derivedStateOf {
            if (scrollState.maxValue <= 0) 0.25f
            else (scrollState.value.toFloat() / scrollState.maxValue).coerceIn(0.08f, 1f)
        }
    }

    // Mini-exercice intégré correspondant à cette leçon
    val miniExercise = remember(lesson?.id, allExercises) {
        allExercises.firstOrNull { it.lessonId == lesson?.id && it.optionsPipe.isNotBlank() }
            ?: allExercises.firstOrNull { it.level == lesson?.level && it.optionsPipe.isNotBlank() }
    }
    var miniChoice by remember(lesson?.id) { mutableStateOf("") }
    var miniChecked by remember(lesson?.id) { mutableStateOf(false) }
    var miniCorrect by remember(lesson?.id) { mutableStateOf(false) }

    val orderedLessons = remember(allLessons, lesson?.level) {
        allLessons.filter { it.level == lesson?.level }
    }
    val currentPos = orderedLessons.indexOfFirst { it.id == lesson?.id }
    val prevLesson = if (currentPos > 0) orderedLessons[currentPos - 1] else null
    val nextLesson = if (currentPos >= 0 && currentPos + 1 < orderedLessons.size) orderedLessons[currentPos + 1] else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isFav = lesson != null && favorites.any { it.itemType == "LESSON" && it.itemId == lesson.id }
        AppTopBar(
            title = lesson?.moduleTitle ?: "Leçon",
            subtitle = lesson?.let { "${it.level} · ${it.theme} · ${it.estimatedMinutes} min" },
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
                message = "Sélectionnez une leçon depuis l'Académie.",
                actionLabel = "Retour",
                onAction = onBack
            )
            return
        }

        // Indicateur de progression de lecture en haut
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
            // Section 1 : Titre & métadonnées
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CefrLevelPill(level = lesson.level)
                    CategoryBadge(
                        label = lesson.theme,
                        color = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    if (progress?.isCompleted == true) {
                        CategoryBadge(
                            label = "Validée",
                            color = semantic.success,
                            containerColor = semantic.successContainer,
                            icon = Icons.Filled.CheckCircle
                        )
                    }
                }
                Text(lesson.title, style = FrancaisProTextStyles.Heading1)
            }

            // Section 2 : Objectif pédagogique
            EditorialCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)) {
                Text("Objectif de la leçon", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(6.dp))
                Text(lesson.summary, style = MaterialTheme.typography.bodyLarge)
            }

            // Section 3 : Explication claire
            EditorialCard {
                Text("Explication", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(lesson.ruleContent, style = MaterialTheme.typography.bodyLarge)
            }

            // Section 4 : Exemples espacés
            val examples = lesson.examplesJson.split("||").filter { it.isNotBlank() }
            if (examples.isNotEmpty()) {
                EditorialCard {
                    Text("Exemples vérifiés", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        examples.forEach { ex ->
                            Surface(
                                shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                                color = semantic.surfaceSubtle,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = ex,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Section 5 : Encadré « Règle à retenir »
            val keyTakeaway = lesson.ruleContent.split(".").firstOrNull()?.trim().orEmpty().ifBlank { lesson.summary }
            Surface(
                shape = RoundedCornerShape(FrancaisProDimens.RadiusCard),
                color = semantic.successContainer,
                border = BorderStroke(1.dp, semantic.success.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = semantic.success)
                    Column {
                        Text("Règle à retenir", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = semantic.onSuccessContainer)
                        Spacer(Modifier.height(4.dp))
                        Text("$keyTakeaway.", style = MaterialTheme.typography.bodyMedium, color = semantic.onSuccessContainer)
                    }
                }
            }

            // Section 6 : Encadré « Erreurs fréquentes »
            Surface(
                shape = RoundedCornerShape(FrancaisProDimens.RadiusCard),
                color = semantic.warningContainer,
                border = BorderStroke(1.dp, semantic.warning.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = semantic.warning)
                    Column {
                        Text("Erreurs fréquentes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = semantic.onWarningContainer)
                        Spacer(Modifier.height(4.dp))
                        Text(lesson.pitfallsContent, style = MaterialTheme.typography.bodyMedium, color = semantic.onWarningContainer)
                    }
                }
            }

            // Section 7 : Mini-exercice intégré
            if (miniExercise != null) {
                val options = miniExercise.optionsPipe.split("|").filter { it.isNotBlank() }
                EditorialCard {
                    Text("Mini-exercice intégré", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(miniExercise.instruction, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
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
                            modifier = Modifier.fillMaxWidth().testTag("lesson_mini_check_btn")
                        )
                    } else {
                        // Section 8 : Résultat du mini-exercice
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

            // Validation & Quiz complet
            PrimaryButton(
                text = "Passer le quiz complet",
                onClick = { onStartQuiz(lesson.id) },
                icon = Icons.Filled.Quiz,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lesson_start_quiz_btn")
            )

            SecondaryButton(
                text = if (progress?.isCompleted == true) "Leçon déjà validée" else "Marquer cette leçon comme terminée",
                onClick = { onMarkCompleted(lesson.id, if (miniChecked && miniCorrect) 100 else 90) },
                icon = Icons.Filled.CheckCircle,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lesson_mark_complete_btn")
            )

            // Navigation fluide Leçon précédente / Leçon suivante
            if (prevLesson != null || nextLesson != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (prevLesson != null) {
                        SecondaryButton(
                            text = "Précédente",
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            onClick = { onOpenLesson(prevLesson.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (nextLesson != null) {
                        PrimaryButton(
                            text = "Leçon suivante",
                            icon = Icons.AutoMirrored.Filled.ArrowForward,
                            onClick = { onOpenLesson(nextLesson.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

/**
 * Écran 10 : Quiz de validation d'une leçon.
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
                message = "Aucun QCM spécifique associé. Vous pouvez valider la leçon directement.",
                actionLabel = "Valider la leçon",
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
                        modifier = Modifier.fillMaxWidth().testTag("quiz_validate_btn")
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
                                lesson?.let { onQuizCompleted(it.id, pct) }
                                finished = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("quiz_next_btn")
                    )
                }
            } else {
                val pct = (correctCount * 100) / quizItems.size
                SuccessFeedback(
                    title = "Quiz terminé — $pct %",
                    message = "Score obtenu : $correctCount / ${quizItems.size}. Votre progression est enregistrée localement."
                )
                PrimaryButton(
                    text = "Retourner à la leçon",
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
