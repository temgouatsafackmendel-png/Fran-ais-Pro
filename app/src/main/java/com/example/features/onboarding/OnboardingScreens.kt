package com.example.features.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.database.UserProfile
import com.example.core.design.AppTopBar
import com.example.core.design.CategoryBadge
import com.example.core.design.CefrLevelPill
import com.example.core.design.DiscreetOfflineBadge
import com.example.core.design.EditorialCard
import com.example.core.design.PrimaryButton
import com.example.core.design.SecondaryButton
import com.example.core.design.SuccessFeedback
import com.example.core.design.UserInitialsAvatar
import com.example.learning.content.AcademyLessonsSeed
import com.example.learning.progression.AdaptiveProgressionEngine
import com.example.ui.theme.FrancaisProDimens
import com.example.ui.theme.FrancaisProTextStyles
import com.example.ui.theme.LocalReducedMotion
import com.example.ui.theme.LocalSemanticColors
import kotlinx.coroutines.delay

/**
 * Écran 1 : Splash screen minimaliste et instantané.
 * Attend la fin de l'initialisation locale Room avant de décider Onboarding vs Accueil
 * afin d'éviter toute redirection prématurée sur appareil lent.
 */
@Composable
fun SplashScreen(
    profile: UserProfile?,
    isSeedReady: Boolean = true,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    var minDelayElapsed by remember { mutableStateOf(false) }
    var timedOut by remember { mutableStateOf(false) }
    var hasNavigated by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(550L)
        minDelayElapsed = true
        delay(3000L)
        timedOut = true
    }

    LaunchedEffect(minDelayElapsed, isSeedReady, profile?.onboardingCompleted, timedOut) {
        if (hasNavigated || !minDelayElapsed) return@LaunchedEffect
        val ready = (isSeedReady && profile != null) || timedOut
        if (!ready) return@LaunchedEffect
        hasNavigated = true
        if (profile?.onboardingCompleted == true) {
            onNavigateToDashboard()
        } else {
            onNavigateToOnboarding()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(semantic.primaryNavy)
            .safeDrawingPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_app_icon_emblem),
                contentDescription = "Emblème Français Pro",
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(FrancaisProDimens.RadiusSurface)),
                contentScale = ContentScale.Crop
            )
            Text(
                text = "FRANÇAIS PRO",
                style = FrancaisProTextStyles.Display,
                color = Color.White
            )
            Text(
                text = "Écrivez mieux. Parlez juste. Progressez chaque jour.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.78f),
                textAlign = TextAlign.Center
            )
            if (minDelayElapsed && (!isSeedReady || profile == null) && !timedOut) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator(
                    color = Color.White.copy(alpha = 0.85f),
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Écran 2 : Onboarding Premium en 7 étapes (ignorable à tout moment).
 * 1. Présentation · 2. Correcteur · 3. Académie · 4. Hors ligne
 * 5. Évaluation initiale · 6. Profil local facultatif · 7. Objectif quotidien
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onFinishOnboarding: (displayName: String, dailyGoalMinutes: Int, placementCorrect: Int?, placementTotal: Int?) -> Unit,
    onSkipToDashboard: (displayName: String) -> Unit,
    onReminderOptIn: (enabled: Boolean, hour: Int) -> Unit = { _, _ -> }
) {
    val stepTitles = listOf("Bienvenue", "Correcteur", "Académie", "Hors ligne", "Niveau", "Profil", "Objectif")
    val totalSteps = stepTitles.size
    var step by rememberSaveable { mutableIntStateOf(0) } // 0..6
    var displayName by rememberSaveable { mutableStateOf("") }
    var selectedGoalMinutes by rememberSaveable { mutableIntStateOf(15) }
    var reminderOptIn by rememberSaveable { mutableStateOf(false) }
    var demoCorrectionApplied by rememberSaveable { mutableStateOf(false) }

    // État interne de l'évaluation initiale (étape 5 = index 4)
    val questions = remember { AcademyLessonsSeed.placementQuestions() }
    var qIndex by rememberSaveable { mutableIntStateOf(0) }
    var qSelected by rememberSaveable { mutableIntStateOf(-1) }
    var qValidated by rememberSaveable { mutableStateOf(false) }
    var qCorrectCount by rememberSaveable { mutableIntStateOf(0) }
    var assessmentDone by rememberSaveable { mutableStateOf(false) }
    var assessmentTaken by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        reminderOptIn = granted
    }

    val semantic = LocalSemanticColors.current
    val reducedMotion = LocalReducedMotion.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(step) {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Barre supérieure d'onboarding : Retour + progression + Ignorer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > 0) {
                IconButton(
                    onClick = { step-- },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("onboarding_back_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Étape précédente")
                }
            } else {
                Spacer(Modifier.size(48.dp))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Étape ${step + 1} sur $totalSteps · ${stepTitles[step]}",
                    style = MaterialTheme.typography.labelMedium,
                    color = semantic.textSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onSkipToDashboard(displayName)
                },
                modifier = Modifier.testTag("onboarding_skip_button")
            ) {
                Text("Ignorer", style = MaterialTheme.typography.labelLarge, color = semantic.textSecondary)
            }
        }

        // Indicateur segmenté en 7 paliers visuels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in 0 until totalSteps) {
                val active = i == step
                val passed = i < step
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            when {
                                active -> MaterialTheme.colorScheme.primary
                                passed -> semantic.success
                                else -> semantic.surfaceSubtle
                            }
                        )
                )
            }
        }

        AnimatedContent(
            targetState = step,
            transitionSpec = {
                val dur = if (reducedMotion) 0 else 200
                fadeIn(tween(dur)) togetherWith fadeOut(tween(dur))
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .widthIn(max = 640.dp),
            label = "onboarding_step_content"
        ) { currentStep ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                when (currentStep) {
                    // Étape 1 — Présentation
                    0 -> {
                        OnboardingHeroIcon(Icons.Filled.AutoAwesome, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                        Text(
                            text = "Votre français mérite le meilleur.",
                            style = FrancaisProTextStyles.Display,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Un atelier complet pour corriger vos écrits, comprendre chaque règle et progresser chaque jour à votre rythme.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = semantic.textSecondary
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(164.dp)
                                .clip(RoundedCornerShape(FrancaisProDimens.RadiusCard))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_editorial_hero),
                                contentDescription = "Illustration éditoriale Français Pro",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        EditorialCard {
                            Text("Gratuit, sobre et sans publicité", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Aucun compte à créer, aucun abonnement : commencez immédiatement en toute confidentialité.",
                                style = MaterialTheme.typography.bodySmall,
                                color = semantic.textSecondary
                            )
                        }
                    }

                    // Étape 2 — Correcteur intelligent avec mini-essai interactif
                    1 -> {
                        OnboardingHeroIcon(Icons.Filled.Spellcheck, semantic.primaryBlue, MaterialTheme.colorScheme.primaryContainer)
                        Text(
                            text = "Comprenez et éliminez vos erreurs.",
                            style = FrancaisProTextStyles.Display,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Chaque faute est identifiée par couleur, expliquée clairement et corrigeable en un geste sans jamais modifier votre texte sans accord.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = semantic.textSecondary
                        )
                        EditorialCard {
                            Text("Code couleur clair et pédagogique", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(10.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CategoryBadge("Orthographe", semantic.spellingColor, semantic.spellingContainer)
                                CategoryBadge("Grammaire", semantic.grammarColor, semantic.grammarContainer)
                                CategoryBadge("Syntaxe", semantic.syntaxColor, semantic.syntaxContainer)
                                CategoryBadge("Style", semantic.styleColor, semantic.styleContainer)
                            }
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                                color = if (demoCorrectionApplied) semantic.successContainer else semantic.surfaceSubtle,
                                border = BorderStroke(
                                    1.dp,
                                    if (demoCorrectionApplied) semantic.success.copy(alpha = 0.45f) else semantic.border
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (demoCorrectionApplied) {
                                            "« Ils sont partis tôt. »"
                                        } else {
                                            "« Ils son parti tôt. » (2 points détectés)"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (demoCorrectionApplied) {
                                            "✓ « sont » (auxiliaire être au présent) + « partis » (accord avec le sujet « Ils »)."
                                        } else {
                                            "Touchez ci-dessous pour tester la correction locale en direct."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (demoCorrectionApplied) semantic.onSuccessContainer else semantic.textSecondary
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            SecondaryButton(
                                text = if (demoCorrectionApplied) "Réinitialiser l'exemple" else "Tester la correction",
                                onClick = { demoCorrectionApplied = !demoCorrectionApplied },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("onboarding_try_corrector_btn")
                            )
                        }
                    }

                    // Étape 3 — Académie + Badges & Réussites
                    2 -> {
                        OnboardingHeroIcon(Icons.Filled.School, semantic.success, semantic.successContainer)
                        Text(
                            text = "Progressez du débutant à l'expert.",
                            style = FrancaisProTextStyles.Display,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Suivez un parcours structuré de A1 à C2 avec cours progressifs, exercices variés, badges de réussite, conjugueur et dictionnaire intégrés.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = semantic.textSecondary
                        )
                        EditorialCard {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("A1", "A2", "B1", "B2", "C1", "C2").forEach { lvl ->
                                    CefrLevelPill(level = lvl)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Débloquez chaque palier à votre rythme et remportez des badges dédiés à mesure que vous progressez :",
                                style = MaterialTheme.typography.bodySmall,
                                color = semantic.textSecondary
                            )
                            Spacer(Modifier.height(10.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CategoryBadge(
                                    label = "Gourou de la grammaire",
                                    color = semantic.warning,
                                    containerColor = semantic.warningContainer,
                                    icon = Icons.Filled.EmojiEvents
                                )
                                CategoryBadge(
                                    label = "Maître du vocabulaire",
                                    color = semantic.success,
                                    containerColor = semantic.successContainer,
                                    icon = Icons.Filled.Verified
                                )
                            }
                        }
                    }

                    // Étape 4 — Hors ligne (indicateur discret autorisé ici §14)
                    3 -> {
                        OnboardingHeroIcon(Icons.Filled.CloudOff, semantic.success, semantic.successContainer)
                        DiscreetOfflineBadge()
                        Text(
                            text = "Votre apprentissage, partout, même sans Internet.",
                            style = FrancaisProTextStyles.Display,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Toutes vos leçons, votre dictionnaire, votre conjugueur, vos progrès et le correcteur fonctionnent localement sur votre téléphone.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = semantic.textSecondary
                        )
                        EditorialCard {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Prêt à l'emploi et 100 % confidentiel", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        "• Correcteur linguistique, 24 cours et exercices inclus sur l'appareil\n• Dictionnaire local, recherches récentes et conjugueur immédiats\n• Sauvegarde / restauration JSON à la demande sans connexion",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = semantic.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Étape 5 — Évaluation initiale (intégrée, facultative)
                    4 -> {
                        Text(
                            text = "Évaluez votre niveau",
                            style = FrancaisProTextStyles.Heading1,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "7 questions rapides pour personnaliser vos recommandations dès le premier jour (facultatif).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = semantic.textSecondary
                        )

                        if (!assessmentDone) {
                            val q = questions[qIndex]
                            EditorialCard {
                                LinearProgressIndicator(
                                    progress = { (qIndex + 1).toFloat() / questions.size },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = semantic.surfaceSubtle
                                )
                                Spacer(Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        CefrLevelPill(level = q.level)
                                        Text(q.skill, style = MaterialTheme.typography.labelMedium, color = semantic.textSecondary)
                                    }
                                    Text(
                                        "${qIndex + 1} / ${questions.size}",
                                        style = FrancaisProTextStyles.MonoMetric,
                                        color = semantic.textSecondary
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(q.question, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(10.dp))

                                q.options.forEachIndexed { idx, opt ->
                                    val isSel = qSelected == idx
                                    val bg = when {
                                        !qValidated -> if (isSel) MaterialTheme.colorScheme.primaryContainer else semantic.surfaceSubtle
                                        idx == q.correctIndex -> semantic.successContainer
                                        isSel -> semantic.dangerContainer
                                        else -> semantic.surfaceSubtle
                                    }
                                    Surface(
                                        onClick = { if (!qValidated) qSelected = idx },
                                        shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                                        color = bg,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .testTag("onboarding_placement_opt_$idx")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = isSel, onClick = { if (!qValidated) qSelected = idx })
                                            Text(opt, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }

                                if (qValidated) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(q.explanation, style = MaterialTheme.typography.bodySmall, color = semantic.textSecondary)
                                }

                                Spacer(Modifier.height(12.dp))
                                if (!qValidated) {
                                    PrimaryButton(
                                        text = "Valider",
                                        enabled = qSelected >= 0,
                                        onClick = {
                                            assessmentTaken = true
                                            if (qSelected == q.correctIndex) qCorrectCount++
                                            qValidated = true
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("onboarding_placement_validate")
                                    )
                                } else {
                                    PrimaryButton(
                                        text = if (qIndex + 1 < questions.size) "Question suivante" else "Voir mon estimation",
                                        onClick = {
                                            if (qIndex + 1 < questions.size) {
                                                qIndex++
                                                qSelected = -1
                                                qValidated = false
                                            } else {
                                                assessmentDone = true
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("onboarding_placement_next")
                                    )
                                }
                            }
                        } else {
                            val est = AdaptiveProgressionEngine.estimateLevelFromPlacement(qCorrectCount, questions.size)
                            val lbl = AdaptiveProgressionEngine.cefrLabels[est] ?: "Débutant"
                            SuccessFeedback(
                                title = "Niveau estimé : $est — $lbl",
                                message = "$qCorrectCount / ${questions.size} bonnes réponses. Estimation interne pour adapter vos cours et exercices."
                            )
                        }
                    }

                    // Étape 6 — Profil local facultatif
                    5 -> {
                        OnboardingHeroIcon(Icons.Filled.Person, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                        Text(
                            text = "Personnalisez votre espace",
                            style = FrancaisProTextStyles.Heading1,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Choisissez un prénom ou un pseudonyme local. Aucun e-mail ni compte n'est demandé.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = semantic.textSecondary
                        )
                        EditorialCard {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                UserInitialsAvatar(
                                    displayName = displayName.ifBlank { "Apprenant" },
                                    size = 54.dp
                                )
                                Column {
                                    Text(
                                        text = displayName.ifBlank { "Apprenant" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Profil local sur cet appareil",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = semantic.textSecondary
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            OutlinedTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                label = { Text("Prénom ou pseudonyme (facultatif)") },
                                placeholder = { Text("Ex. Claire") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                    }
                                ),
                                trailingIcon = if (displayName.isNotEmpty()) {
                                    {
                                        IconButton(onClick = { displayName = "" }) {
                                            Icon(Icons.Filled.Close, contentDescription = "Effacer le prénom")
                                        }
                                    }
                                } else null,
                                shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("onboarding_name_input")
                            )
                        }
                    }

                    // Étape 7 — Objectif quotidien + Récapitulatif personnalisé
                    else -> {
                        OnboardingHeroIcon(Icons.Filled.Timer, semantic.primaryBlue, MaterialTheme.colorScheme.primaryContainer)
                        Text(
                            text = "Choisissez votre rythme quotidien",
                            style = FrancaisProTextStyles.Heading1,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Un objectif réaliste vous aide à progresser avec régularité. Vous pourrez le modifier à tout moment.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = semantic.textSecondary
                        )
                        val goals = listOf(
                            Triple(5, "5 min / jour", "Léger · 1 mini-session rapide"),
                            Triple(10, "10 min / jour", "Régulier · Idéal pour ancrer les bases"),
                            Triple(15, "15 min / jour", "Recommandé · 1 leçon + exercices"),
                            Triple(30, "30 min / jour", "Intensif · Maîtrise accélérée")
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            goals.forEach { (mins, title, subtitle) ->
                                val selected = selectedGoalMinutes == mins
                                Surface(
                                    shape = RoundedCornerShape(FrancaisProDimens.RadiusCard),
                                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        width = if (selected) 1.5.dp else 1.dp,
                                        color = if (selected) MaterialTheme.colorScheme.primary else semantic.border
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(FrancaisProDimens.RadiusCard))
                                        .clickable { selectedGoalMinutes = mins }
                                        .testTag("onboarding_goal_$mins")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = semantic.textSecondary)
                                        }
                                        RadioButton(selected = selected, onClick = { selectedGoalMinutes = mins })
                                    }
                                }
                            }
                        }
                        EditorialCard(
                            contentPadding = 14.dp
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
                                    Icon(
                                        imageVector = Icons.Filled.NotificationsActive,
                                        contentDescription = null,
                                        tint = if (reminderOptIn) semantic.success else MaterialTheme.colorScheme.primary
                                    )
                                    Column {
                                        Text(
                                            text = "Rappel de série à 19h00",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Uniquement si votre session Académie du jour n'est pas validée.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = semantic.textSecondary
                                        )
                                    }
                                }
                                Switch(
                                    checked = reminderOptIn,
                                    onCheckedChange = { enable ->
                                        if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            val granted = ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.POST_NOTIFICATIONS
                                            ) == PackageManager.PERMISSION_GRANTED
                                            if (!granted) {
                                                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                return@Switch
                                            }
                                        }
                                        reminderOptIn = enable
                                    },
                                    modifier = Modifier.testTag("onboarding_reminder_toggle")
                                )
                            }
                        }
                        val previewLevel = if (assessmentTaken) {
                            AdaptiveProgressionEngine.estimateLevelFromPlacement(qCorrectCount, questions.size)
                        } else "A1"
                        EditorialCard(
                            containerColor = semantic.surfaceSubtle,
                            contentPadding = 14.dp,
                            modifier = Modifier.testTag("onboarding_summary_card")
                        ) {
                            Text(
                                text = "Votre espace prêt sur cet appareil",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CategoryBadge(
                                    label = displayName.trim().ifBlank { "Apprenant" },
                                    color = MaterialTheme.colorScheme.primary,
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    icon = Icons.Filled.Person
                                )
                                CategoryBadge(
                                    label = "$selectedGoalMinutes min / jour",
                                    color = semantic.success,
                                    containerColor = semantic.successContainer,
                                    icon = Icons.Filled.Timer
                                )
                                if (reminderOptIn) {
                                    CategoryBadge(
                                        label = "Rappel 19h00",
                                        color = semantic.warning,
                                        containerColor = semantic.warningContainer,
                                        icon = Icons.Filled.NotificationsActive
                                    )
                                }
                                CefrLevelPill(level = previewLevel)
                            }
                        }
                    }
                }
            }
        }

        // Pied de page d'action clair
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (step == 4 && !assessmentDone) {
                SecondaryButton(
                    text = "Passer l'évaluation",
                    onClick = { step++ },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("onboarding_skip_assessment_btn")
                )
            } else if (step < totalSteps - 1) {
                PrimaryButton(
                    text = "Continuer",
                    onClick = { step++ },
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_next_btn")
                )
            } else {
                PrimaryButton(
                    text = "Commencer avec Français Pro",
                    onClick = {
                        onReminderOptIn(reminderOptIn, 19)
                        onFinishOnboarding(
                            displayName,
                            selectedGoalMinutes,
                            if (assessmentTaken) qCorrectCount else null,
                            if (assessmentTaken) questions.size else null
                        )
                    },
                    icon = Icons.Filled.CheckCircle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_finish_btn")
                )
            }
        }
    }
}

@Composable
private fun OnboardingHeroIcon(
    icon: ImageVector,
    tint: Color,
    containerColor: Color
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
    }
}

/**
 * Écran 3 : Évaluation initiale autonome (accessible depuis le Profil pour réévaluer son niveau).
 */
@Composable
fun InitialAssessmentScreen(
    onFinished: (correct: Int, total: Int) -> Unit,
    onBack: () -> Unit
) {
    val questions = remember { AcademyLessonsSeed.placementQuestions() }
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableIntStateOf(-1) }
    var validated by remember { mutableStateOf(false) }
    var correctCount by remember { mutableIntStateOf(0) }
    var completed by remember { mutableStateOf(false) }
    val semantic = LocalSemanticColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        AppTopBar(
            title = "Évaluation de niveau",
            subtitle = if (completed) "Bilan estimatif" else "Question ${currentIndex + 1} sur ${questions.size}",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!completed) {
                    val q = questions[currentIndex]
                    LinearProgressIndicator(
                        progress = { (currentIndex + 1).toFloat() / questions.size },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )

                    EditorialCard {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CefrLevelPill(level = q.level)
                            Text(q.skill, style = MaterialTheme.typography.labelMedium, color = semantic.textSecondary)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(q.question, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))

                        q.options.forEachIndexed { idx, opt ->
                            val isSelected = selectedOption == idx
                            val bg = when {
                                !validated -> if (isSelected) MaterialTheme.colorScheme.primaryContainer else semantic.surfaceSubtle
                                idx == q.correctIndex -> semantic.successContainer
                                isSelected && idx != q.correctIndex -> semantic.dangerContainer
                                else -> semantic.surfaceSubtle
                            }
                            Surface(
                                onClick = { if (!validated) selectedOption = idx },
                                shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                                color = bg,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .testTag("placement_option_$idx")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = isSelected, onClick = { if (!validated) selectedOption = idx })
                                    Text(opt, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 6.dp))
                                }
                            }
                        }

                        if (validated) {
                            Spacer(Modifier.height(10.dp))
                            val ok = selectedOption == q.correctIndex
                            Surface(
                                shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                                color = if (ok) semantic.successContainer else semantic.warningContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(q.explanation, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
                            }
                        }
                    }

                    if (!validated) {
                        PrimaryButton(
                            text = "Valider ma réponse",
                            onClick = {
                                if (selectedOption >= 0) {
                                    if (selectedOption == q.correctIndex) correctCount++
                                    validated = true
                                }
                            },
                            enabled = selectedOption >= 0,
                            modifier = Modifier.fillMaxWidth().testTag("placement_validate_button")
                        )
                    } else {
                        PrimaryButton(
                            text = if (currentIndex + 1 < questions.size) "Question suivante" else "Voir mon estimation",
                            onClick = {
                                if (currentIndex + 1 < questions.size) {
                                    currentIndex++
                                    selectedOption = -1
                                    validated = false
                                } else {
                                    completed = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("placement_next_button")
                        )
                    }
                } else {
                    val estimated = AdaptiveProgressionEngine.estimateLevelFromPlacement(correctCount, questions.size)
                    val label = AdaptiveProgressionEngine.cefrLabels[estimated] ?: "Débutant"
                    SuccessFeedback(
                        title = "Niveau estimé : $estimated — $label",
                        message = "$correctCount / ${questions.size} bonnes réponses. Estimation pédagogique interne inspirée du CECRL (non certifiante)."
                    )
                    PrimaryButton(
                        text = "Enregistrer et revenir à l'accueil",
                        onClick = { onFinished(correctCount, questions.size) },
                        modifier = Modifier.fillMaxWidth().testTag("placement_finish_button")
                    )
                    SecondaryButton(
                        text = "Refaire l'évaluation",
                        onClick = {
                            currentIndex = 0
                            selectedOption = -1
                            validated = false
                            correctCount = 0
                            completed = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
