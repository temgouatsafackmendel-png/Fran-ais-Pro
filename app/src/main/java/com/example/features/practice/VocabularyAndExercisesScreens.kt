package com.example.features.practice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.database.Exercise
import com.example.core.database.ExerciseAttempt
import com.example.core.database.LearningStatistics
import com.example.core.database.UserProfile
import com.example.core.database.VocabularyProgress
import com.example.core.database.VocabularyWord
import com.example.core.design.AppIconButton
import com.example.core.design.AppTopBar
import com.example.core.design.CategoryBadge
import com.example.core.design.CefrLevelPill
import com.example.core.design.EditorialCard
import com.example.core.design.EmptyState
import com.example.core.design.ExerciseCard
import com.example.core.design.PrimaryButton
import com.example.core.design.SecondaryButton
import com.example.core.design.SectionHeader
import com.example.core.design.SuccessFeedback
import com.example.ui.theme.FrancaisProDimens
import com.example.ui.theme.FrancaisProTextStyles
import com.example.ui.theme.LocalReducedMotion
import com.example.ui.theme.LocalSemanticColors

private data class SessionRecord(
    val prompt: String,
    val expected: String,
    val explanation: String,
    val isCorrect: Boolean
)

/**
 * Onglet Exercices : Gamification adulte (XP, série, défi du jour), filtres, feedback animé et Résultat de session.
 */
@Composable
fun ExercisesScreen(
    profile: UserProfile?,
    exercises: List<Exercise>,
    exerciseAttempts: List<ExerciseAttempt>,
    statistics: List<LearningStatistics>,
    initialCategoryFilter: String?,
    onClearInitialCategoryFilter: () -> Unit,
    onSubmitAnswer: (Exercise, String, Boolean) -> Unit,
    onOpenDictation: () -> Unit,
    onOpenSpeaking: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    var selectedLevel by remember { mutableStateOf("Tous") }
    var selectedCategory by remember { mutableStateOf("Toutes") }
    var activeExerciseId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(initialCategoryFilter) {
        if (!initialCategoryFilter.isNullOrBlank()) {
            selectedCategory = initialCategoryFilter
            activeExerciseId = null
            onClearInitialCategoryFilter()
        }
    }

    // Suivi de la session en cours (résumé de session après 3 exercices ou à la demande)
    val sessionHistory = remember { mutableStateListOf<SessionRecord>() }
    var showSessionResult by remember { mutableStateOf(false) }

    val levels = listOf("Tous", "A1", "A2", "B1", "B2", "C1", "C2")
    val categories = remember(exercises) { listOf("Toutes") + exercises.map { it.category }.distinct() }
    val weakCategory = remember(statistics) {
        statistics.filter { it.totalAttempts > 0 }.minByOrNull { it.masteryPercent }?.skillCategory
    }
    val totalXp = remember(exerciseAttempts) {
        exerciseAttempts.count { it.isCorrect } * 15
    }

    val filtered = remember(exercises, selectedLevel, selectedCategory, weakCategory) {
        exercises
            .filter {
                (selectedLevel == "Tous" || it.level == selectedLevel) &&
                    (selectedCategory == "Toutes" || it.category.equals(selectedCategory, ignoreCase = true))
            }
            .sortedByDescending { if (weakCategory != null && it.category == weakCategory) 1 else 0 }
    }

    val activeExercise = filtered.firstOrNull { it.id == activeExerciseId } ?: filtered.firstOrNull()

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
            // En-tête + Gamification adulte sobre (XP, Série, Défi)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Exercices", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            "Entraînement ciblé sur 10 formats",
                            style = MaterialTheme.typography.bodySmall,
                            color = semantic.textSecondary
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CategoryBadge(
                            label = "$totalXp XP",
                            color = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            icon = Icons.Filled.Star
                        )
                        CategoryBadge(
                            label = "${profile?.streakDays ?: 0} j",
                            color = semantic.warning,
                            containerColor = semantic.warningContainer,
                            icon = Icons.Filled.LocalFireDepartment
                        )
                    }
                }
            }

            // Accès Dictée & Expression orale
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EditorialCard(
                        onClick = onOpenDictation,
                        contentPadding = 14.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("exercises_open_dictation")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Filled.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("Dictées", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Audio & note /20", style = MaterialTheme.typography.bodySmall, color = semantic.textSecondary)
                            }
                        }
                    }
                    EditorialCard(
                        onClick = onOpenSpeaking,
                        contentPadding = 14.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("exercises_open_speaking")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("Oral", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Prononciation", style = MaterialTheme.typography.bodySmall, color = semantic.textSecondary)
                            }
                        }
                    }
                }
            }

            // Défi du jour sobre
            if (sessionHistory.isNotEmpty() && !showSessionResult) {
                item {
                    EditorialCard(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        contentPadding = 12.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text(
                                    "Session en cours : ${sessionHistory.count { it.isCorrect }}/${sessionHistory.size} réussite(s)",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                            SecondaryButton(
                                text = "Bilan",
                                onClick = { showSessionResult = true },
                                modifier = Modifier.testTag("exercise_session_summary_btn")
                            )
                        }
                    }
                }
            }

            if (showSessionResult && sessionHistory.isNotEmpty()) {
                // Vue dédiée de résultat de session
                item {
                    val correctCount = sessionHistory.count { it.isCorrect }
                    val wrongCount = sessionHistory.size - correctCount
                    val pct = (correctCount * 100) / sessionHistory.size
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SuccessFeedback(
                            title = if (pct >= 70) "Belle régularité — $pct % de réussite" else "Session terminée — $pct %",
                            message = "$correctCount bonne(s) réponse(s) · $wrongCount à revoir · +${correctCount * 15} XP."
                        )
                        EditorialCard {
                            Text("Explications de la session", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            sessionHistory.forEachIndexed { idx, rec ->
                                if (idx > 0) Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "${if (rec.isCorrect) "✓" else "✕"} ${rec.prompt}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (rec.isCorrect) semantic.success else semantic.danger
                                )
                                Text(
                                    text = "Réponse : ${rec.expected} — ${rec.explanation}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = semantic.textSecondary
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            SecondaryButton(
                                text = "Recommencer",
                                icon = Icons.Filled.Refresh,
                                onClick = {
                                    sessionHistory.clear()
                                    showSessionResult = false
                                },
                                modifier = Modifier.weight(1f)
                            )
                            PrimaryButton(
                                text = "Continuer",
                                onClick = { showSessionResult = false },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            } else {
                // Filtres rapides
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(levels) { lvl ->
                                FilterChip(
                                    selected = selectedLevel == lvl,
                                    onClick = {
                                        selectedLevel = lvl
                                        activeExerciseId = null
                                    },
                                    label = { Text(lvl) }
                                )
                            }
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories) { cat ->
                                FilterChip(
                                    selected = selectedCategory.equals(cat, ignoreCase = true),
                                    onClick = {
                                        selectedCategory = cat
                                        activeExerciseId = null
                                    },
                                    label = { Text(cat) }
                                )
                            }
                        }
                    }
                }

                if (activeExercise == null) {
                    item {
                        EmptyState(
                            icon = Icons.Filled.FitnessCenter,
                            title = "Aucun exercice pour ce filtre",
                            message = "Essayez un autre niveau ou réinitialisez les filtres.",
                            actionLabel = "Réinitialiser",
                            onAction = {
                                selectedLevel = "Tous"
                                selectedCategory = "Toutes"
                            }
                        )
                    }
                } else {
                    item(key = activeExercise.id) {
                        ExerciseRunnerCard(
                            exercise = activeExercise,
                            onSubmit = { ans, ok ->
                                onSubmitAnswer(activeExercise, ans, ok)
                                sessionHistory.add(
                                    SessionRecord(
                                        prompt = activeExercise.promptText,
                                        expected = activeExercise.expectedAnswer,
                                        explanation = activeExercise.explanation,
                                        isCorrect = ok
                                    )
                                )
                            },
                            onNext = {
                                if (sessionHistory.size >= 3 && sessionHistory.size % 3 == 0) {
                                    showSessionResult = true
                                }
                                val idx = filtered.indexOfFirst { it.id == activeExercise.id }
                                val next = filtered.getOrNull((idx + 1) % filtered.size)
                                activeExerciseId = next?.id
                            }
                        )
                    }

                    item {
                        SectionHeader(title = "Exercices disponibles (${filtered.size})")
                    }
                    items(filtered, key = { "list_${it.id}" }) { ex ->
                        ExerciseCard(
                            level = ex.level,
                            typeLabel = ex.type.replace('_', ' '),
                            category = ex.category,
                            promptPreview = ex.promptText,
                            points = ex.points,
                            isSelected = ex.id == activeExercise.id,
                            onClick = { activeExerciseId = ex.id }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseRunnerCard(
    exercise: Exercise,
    onSubmit: (String, Boolean) -> Unit,
    onNext: () -> Unit
) {
    val options = remember(exercise.id) { exercise.optionsPipe.split("|").filter { it.isNotBlank() } }
    var userInput by remember(exercise.id) { mutableStateOf("") }
    var validated by remember(exercise.id) { mutableStateOf(false) }
    var isCorrect by remember(exercise.id) { mutableStateOf(false) }
    val semantic = LocalSemanticColors.current
    val reducedMotion = LocalReducedMotion.current

    EditorialCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                CefrLevelPill(level = exercise.level)
                CategoryBadge(
                    label = exercise.category,
                    color = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
            Text(
                text = "+${exercise.points} XP",
                style = FrancaisProTextStyles.MonoMetric,
                color = semantic.textSecondary
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(exercise.instruction, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(6.dp))
        Text(exercise.promptText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))

        if (options.isNotEmpty()) {
            options.forEachIndexed { idx, opt ->
                val isSelected = userInput == opt
                val bg = when {
                    !validated -> if (isSelected) MaterialTheme.colorScheme.primaryContainer else semantic.surfaceSubtle
                    opt.equals(exercise.expectedAnswer, ignoreCase = true) -> semantic.successContainer
                    isSelected -> semantic.dangerContainer
                    else -> semantic.surfaceSubtle
                }
                Surface(
                    onClick = { if (!validated) userInput = opt },
                    shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                    color = bg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("exercise_option_$idx")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isSelected, onClick = { if (!validated) userInput = opt })
                        Text(opt, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = userInput,
                onValueChange = { if (!validated) userInput = it },
                label = { Text("Votre réponse") },
                placeholder = { Text("Saisissez la réponse exacte…") },
                shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("exercise_text_input")
            )
        }

        AnimatedVisibility(
            visible = validated,
            enter = fadeIn(tween(if (reducedMotion) 0 else 200)),
            exit = fadeOut(tween(if (reducedMotion) 0 else 150))
        ) {
            Column {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                    color = if (isCorrect) semantic.successContainer else semantic.dangerContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (isCorrect) "Bonne réponse !" else "Réponse attendue : « ${exercise.expectedAnswer} »",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCorrect) semantic.onSuccessContainer else semantic.onDangerContainer
                        )
                        Text(
                            text = exercise.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isCorrect) semantic.onSuccessContainer else semantic.onDangerContainer
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        if (!validated) {
            PrimaryButton(
                text = "Valider",
                onClick = {
                    val trimmed = userInput.trim()
                    val accepted = buildSet {
                        add(exercise.expectedAnswer.trim().lowercase())
                        exercise.acceptedAnswersPipe.split("|").filter { it.isNotBlank() }.forEach {
                            add(it.trim().lowercase())
                        }
                    }
                    isCorrect = trimmed.lowercase() in accepted
                    validated = true
                    onSubmit(trimmed, isCorrect)
                },
                enabled = userInput.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("exercise_submit_btn")
            )
        } else {
            PrimaryButton(
                text = "Exercice suivant",
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("exercise_next_btn")
            )
        }
    }
}

/**
 * Écran 16 : Vocabulaire élégant (mot, prononciation audio, définition, exemple, synonymes, SM-2).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VocabularyScreen(
    words: List<VocabularyWord>,
    progress: List<VocabularyProgress>,
    onSpeakWord: (String) -> Unit,
    onReviewQuality: (wordId: String, quality: Int) -> Unit,
    onBack: () -> Unit
) {
    val now = System.currentTimeMillis()
    val progressMap = remember(progress) { progress.associateBy { it.wordId } }
    val dueWords = remember(words, progress) {
        words.sortedBy { progressMap[it.id]?.nextReviewAt ?: 0L }
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var showDefinition by remember { mutableStateOf(false) }
    var quizAnswerChosen by remember { mutableStateOf<String?>(null) }
    val semantic = LocalSemanticColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val masteredCount = progress.count { it.mastered }
        val dueCount = words.count { (progressMap[it.id]?.nextReviewAt ?: 0L) <= now }
        AppTopBar(
            title = "Vocabulaire",
            subtitle = "$dueCount à réviser · $masteredCount maîtrisé(s)",
            onBack = onBack
        )

        if (dueWords.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Bookmark,
                title = "Aucun mot chargé",
                message = "La banque de vocabulaire s'initialise localement."
            )
            return
        }

        val currentWord = dueWords[currentIndex % dueWords.size]
        val wordProg = progressMap[currentWord.id]

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = FrancaisProDimens.MaxContentWidth)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                EditorialCard(contentPadding = 20.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            CefrLevelPill(level = currentWord.level)
                            CategoryBadge(
                                label = currentWord.theme,
                                color = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        }
                        AppIconButton(
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Écouter la prononciation",
                            tint = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            onClick = { onSpeakWord(currentWord.word) },
                            modifier = Modifier.testTag("vocab_speak_btn")
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(currentWord.word, style = FrancaisProTextStyles.Display)
                    Text(
                        text = if (wordProg != null) "Intervalle SM-2 : ${wordProg.intervalDays} j" else "Nouveau mot à mémoriser",
                        style = MaterialTheme.typography.labelSmall,
                        color = semantic.textSecondary
                    )
                    Spacer(Modifier.height(12.dp))

                    if (!showDefinition) {
                        Text(
                            "Formulez mentalement la définition et un synonyme avant de révéler la fiche.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = semantic.textSecondary
                        )
                        Spacer(Modifier.height(16.dp))
                        PrimaryButton(
                            text = "Révéler la fiche",
                            onClick = { showDefinition = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("vocab_reveal_btn")
                        )
                    } else {
                        Text(currentWord.definition, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                            color = semantic.surfaceSubtle,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "« ${currentWord.exampleSentence} »",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(14.dp)
                            )
                        }

                        Spacer(Modifier.height(12.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            currentWord.synonymsPipe.split("|").filter { it.isNotBlank() }.forEach { syn ->
                                CategoryBadge("Syn. $syn", semantic.success, semantic.successContainer)
                            }
                            currentWord.antonymsPipe.split("|").filter { it.isNotBlank() }.forEach { ant ->
                                CategoryBadge("Ant. $ant", semantic.danger, semantic.dangerContainer)
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Text("Vérification rapide : ${currentWord.quizQuestion}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        currentWord.quizOptionsPipe.split("|").filter { it.isNotBlank() }.forEach { opt ->
                            val chosen = quizAnswerChosen == opt
                            val bg = when {
                                quizAnswerChosen == null -> semantic.surfaceSubtle
                                opt == currentWord.quizAnswer -> semantic.successContainer
                                chosen -> semantic.dangerContainer
                                else -> semantic.surfaceSubtle
                            }
                            Surface(
                                onClick = { if (quizAnswerChosen == null) quizAnswerChosen = opt },
                                shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                                color = bg,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Text(opt, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp))
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Text("Mémoriser (répétition espacée)", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(8.dp))
                        val advance: (Int) -> Unit = { q ->
                            onReviewQuality(currentWord.id, q)
                            currentIndex = (currentIndex + 1) % dueWords.size
                            showDefinition = false
                            quizAnswerChosen = null
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SecondaryButton(
                                text = "À revoir",
                                onClick = { advance(1) },
                                modifier = Modifier.weight(1f).testTag("vocab_rate_again")
                            )
                            SecondaryButton(
                                text = "Difficile",
                                onClick = { advance(3) },
                                modifier = Modifier.weight(1f).testTag("vocab_rate_hard")
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PrimaryButton(
                                text = "Bien",
                                containerColor = semantic.success,
                                onClick = { advance(4) },
                                modifier = Modifier.weight(1f).testTag("vocab_rate_good")
                            )
                            PrimaryButton(
                                text = "Facile",
                                onClick = { advance(5) },
                                modifier = Modifier.weight(1f).testTag("vocab_rate_easy")
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Tous les mots (${words.size})")
            }
            items(words, key = { it.id }) { w ->
                val p = progressMap[w.id]
                EditorialCard(
                    onClick = {
                        currentIndex = dueWords.indexOfFirst { it.id == w.id }.coerceAtLeast(0)
                        showDefinition = false
                        quizAnswerChosen = null
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(w.word, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(w.definition, style = MaterialTheme.typography.bodySmall, color = semantic.textSecondary, maxLines = 1)
                        }
                        if (p?.mastered == true) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = "Maîtrisé", tint = semantic.success, modifier = Modifier.size(20.dp))
                        } else {
                            CefrLevelPill(level = w.level)
                        }
                    }
                }
            }
        }
    }
}
