package com.example.features.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.design.AppSearchBar
import com.example.core.design.AppTopBar
import com.example.core.design.CategoryBadge
import com.example.core.design.EditorialCard
import com.example.core.design.ErrorDetailBottomSheet
import com.example.core.design.ErrorHighlightText
import com.example.core.design.PrimaryButton
import com.example.core.design.SecondaryButton
import com.example.core.design.SectionHeader
import com.example.linguistics.analysis.HybridCorrectionEngine
import com.example.linguistics.model.CorrectionIssue
import com.example.navigation.Routes
import com.example.ui.theme.FrancaisProDimens
import com.example.ui.theme.FrancaisProTextStyles
import com.example.ui.theme.LocalReducedMotion
import com.example.ui.theme.LocalSemanticColors

private data class DemoScenario(
    val title: String,
    val badge: String,
    val sampleText: String
)

private data class TourStep(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String
)

private data class GuideSection(
    val title: String,
    val tag: String,
    val summary: String,
    val bulletPoints: List<String>
)

private val demoScenarios = listOf(
    DemoScenario(
        title = "E-mail professionnel",
        badge = "Accords & Style",
        sampleText = "Bonjour, les rapport est pret malgré que nous avons oublier la piece jointe."
    ),
    DemoScenario(
        title = "Orthographe & Accords",
        badge = "Classique",
        sampleText = "Les etudiants apprend le francais chaque jour pour ecrire sans faute."
    ),
    DemoScenario(
        title = "Syntaxe courante",
        badge = "Perfectionnement",
        sampleText = "Je vais au coiffeur aujourd'hui malgré que le temps soit pluvieux."
    )
)

private val appTourSteps = listOf(
    TourStep(
        title = "1. Accueil",
        description = "Visualisez votre niveau CECRL, votre score global et lancez votre entraînement du jour en un geste.",
        icon = Icons.Filled.Home,
        route = Routes.DASHBOARD
    ),
    TourStep(
        title = "2. Corriger",
        description = "Saisissez ou collez un texte : chaque faute est surlignée par couleur avec explication détaillée et comparaison Avant/Après.",
        icon = Icons.Filled.Spellcheck,
        route = Routes.CORRECTOR
    ),
    TourStep(
        title = "3. Apprendre",
        description = "Suivez les leçons structurées A1 à C2 et consultez le Dictionnaire, le Conjugueur inversé et le Vocabulaire SM-2.",
        icon = Icons.Filled.School,
        route = Routes.ACADEMY
    ),
    TourStep(
        title = "4. Exercices",
        description = "Pratiquez 10 formats variés, réalisez des dictées audio hors ligne (ou lecture visuelle) et travaillez votre expression orale.",
        icon = Icons.Filled.FitnessCenter,
        route = Routes.EXERCISES
    ),
    TourStep(
        title = "5. Profil & Statistiques",
        description = "Inspectez vos graphiques hebdomadaires, personnalisez le thème clair/sombre et sauvegardez vos progrès en JSON.",
        icon = Icons.Filled.Person,
        route = Routes.PROFILE
    )
)

private val guideSections = listOf(
    GuideSection(
        title = "Démarrage & Autonomie 100 % hors ligne",
        tag = "Essentiel",
        summary = "Utilisez toute l'application en mode avion, sans compte ni connexion Internet.",
        bulletPoints = listOf(
            "Toutes les données (leçons, dictionnaire, conjugueur, exercices et moteur linguistique) sont stockées localement sur votre appareil.",
            "Aucune permission Internet n'est demandée : vos textes et statistiques restent strictement privés.",
            "Vous pouvez refaire l'évaluation initiale à tout moment depuis Profil → Refaire l'évaluation."
        )
    ),
    GuideSection(
        title = "Maîtriser le Correcteur interactif",
        tag = "Correcteur",
        summary = "Comprenez instantanément vos erreurs grâce au code couleur 4 catégories.",
        bulletPoints = listOf(
            "Rouge : Orthographe · Orange : Grammaire & Conjugaison · Violet : Syntaxe & Ponctuation · Bleu : Style.",
            "Touchez un mot surligné pour ouvrir la fiche explicative complète (règle, exemple correct, Accepter ou Ignorer).",
            "Utilisez le bouton « Tout corriger » pour appliquer les corrections sûres, et « Avant / Après » pour comparer."
        )
    ),
    GuideSection(
        title = "Progresser dans l'Académie CECRL (A1–C2)",
        tag = "Apprendre",
        summary = "Chaque leçon suit une méthode claire en 8 sections pédagogiques.",
        bulletPoints = listOf(
            "Choisissez votre niveau (A1 Découverte à C2 Maîtrise) dans l'onglet Apprendre.",
            "Lisez l'objectif, la règle à retenir, les exemples commentés et les pièges fréquents.",
            "Validez le mini-exercice intégré et le quiz final pour enregistrer votre maîtrise."
        )
    ),
    GuideSection(
        title = "Exercices, Dictées audio & Repli visuel",
        tag = "Entraînement",
        summary = "Dix formats d'exercices adaptatifs et des dictées accessibles même sans voix installée.",
        bulletPoints = listOf(
            "Filtrez les exercices par catégorie (Orthographe, Accords, Conjugaison, Homophones, Ponctuation, Syntaxe).",
            "Dans Dictée, la synthèse vocale locale lit le texte en français ; si votre appareil n'a pas de voix FR, le mode lecture visuelle rythmée prend le relais automatiquement.",
            "Dans Expression orale, écoutez la phrase modèle et entraînez votre articulation ou votre lecture guidée."
        )
    ),
    GuideSection(
        title = "Dictionnaire, Conjugueur & Vocabulaire SM-2",
        tag = "Référence",
        summary = "Des outils de consultation immédiats et une mémorisation scientifiquement espacée.",
        bulletPoints = listOf(
            "Dictionnaire : recherche instantanée avec définitions, synonymes, antonymes et mise en favoris.",
            "Conjugueur : tableaux complets par mode/temps et recherche inverse pour identifier une forme conjuguée (ex. « firent »).",
            "Vocabulaire SM-2 : évaluez chaque carte (À revoir, Difficile, Bien, Facile) pour planifier automatiquement la prochaine révision."
        )
    ),
    GuideSection(
        title = "Statistiques interactives & Sauvegarde JSON",
        tag = "Suivi",
        summary = "Suivez vos progrès en détail et protégez votre historique lors d'un changement d'appareil.",
        bulletPoints = listOf(
            "Dans Profil → Statistiques détaillées, touchez chaque barre hebdomadaire ou chaque catégorie du Donut pour afficher les détails exacts.",
            "Dans Profil → Paramètres, basculez entre thème Clair, Sombre ou Système et ajustez votre objectif quotidien.",
            "Dans Profil → Sauvegarde & restauration, exportez un fichier JSON portable via le sélecteur Android (SAF) et réimportez-le à volonté."
        )
    )
)

/**
 * Écran 25 : Démo interactive (moteur NLP réel) & Guide d'utilisation hors ligne.
 */
@Composable
fun DemoAndGuideScreen(
    correctionEngine: HybridCorrectionEngine,
    onOpenInCorrector: (String) -> Unit,
    onNavigateToTab: (String) -> Unit,
    onBack: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    val reducedMotion = LocalReducedMotion.current
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Démo, 1 = Guide

    var selectedScenarioIdx by remember { mutableIntStateOf(0) }
    var demoText by remember { mutableStateOf(demoScenarios.first().sampleText) }
    var inspectedIssue by remember { mutableStateOf<CorrectionIssue?>(null) }
    val demoReport = remember(demoText) { correctionEngine.analyze(demoText) }

    var guideQuery by remember { mutableStateOf("") }
    var expandedGuideIdx by remember { mutableStateOf<Int?>(0) }

    inspectedIssue?.let { issue ->
        ErrorDetailBottomSheet(
            issue = issue,
            onAccept = {
                demoText = correctionEngine.applySingleIssue(demoText, issue)
                inspectedIssue = null
            },
            onDismissIssue = { inspectedIssue = null },
            onClose = { inspectedIssue = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(
            title = "Démo & Guide",
            subtitle = "Essai interactif et mode d'emploi hors ligne",
            onBack = onBack
        )

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val hPadding = FrancaisProDimens.screenHorizontalPadding(maxWidth)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = FrancaisProDimens.MaxContentWidth)
            ) {
                // Sélecteur 2 onglets épuré
                Surface(
                    shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                    color = semantic.surfaceSubtle,
                    border = BorderStroke(1.dp, semantic.border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = hPadding, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        GuideSegmentChip(
                            label = "Démo interactive",
                            icon = Icons.Filled.PlayArrow,
                            selected = activeTab == 0,
                            testTag = "guide_tab_demo",
                            onClick = { activeTab = 0 },
                            modifier = Modifier.weight(1f)
                        )
                        GuideSegmentChip(
                            label = "Guide d'utilisation",
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            selected = activeTab == 1,
                            testTag = "guide_tab_manual",
                            onClick = { activeTab = 1 },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        val ms = if (reducedMotion) 0 else 200
                        fadeIn(tween(ms, easing = FastOutSlowInEasing)) togetherWith
                            fadeOut(tween(ms, easing = FastOutSlowInEasing))
                    },
                    label = "demo_guide_tab_content",
                    modifier = Modifier.weight(1f)
                ) { tab ->
                    if (tab == 0) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("demo_list"),
                            contentPadding = PaddingValues(horizontal = hPadding, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Bac à sable interactif du correcteur hors ligne
                            item {
                                EditorialCard(contentPadding = 16.dp) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "Essai en direct du Correcteur",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "Choisissez un exemple puis touchez un mot souligné",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = semantic.textSecondary
                                            )
                                        }
                                        CategoryBadge(
                                            label = "${demoReport.qualityScore}/100",
                                            color = if (demoReport.qualityScore >= 85) semantic.success else MaterialTheme.colorScheme.primary,
                                            containerColor = if (demoReport.qualityScore >= 85) semantic.successContainer else MaterialTheme.colorScheme.primaryContainer
                                        )
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    // Sélecteur de scénarios
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        demoScenarios.forEachIndexed { idx, scenario ->
                                            val isSelected = idx == selectedScenarioIdx
                                            Surface(
                                                shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else semantic.surfaceSubtle,
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (isSelected) MaterialTheme.colorScheme.primary else semantic.border
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 48.dp)
                                                    .clip(RoundedCornerShape(FrancaisProDimens.RadiusSmall))
                                                    .clickable(role = Role.Button) {
                                                        selectedScenarioIdx = idx
                                                        demoText = scenario.sampleText
                                                    }
                                                    .testTag("demo_scenario_$idx")
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        text = scenario.title,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1
                                                    )
                                                    Text(
                                                        text = scenario.badge,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = semantic.textSecondary,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(14.dp))

                                    // Zone surlignée interactive
                                    Surface(
                                        shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                                        color = semantic.surfaceSubtle,
                                        border = BorderStroke(1.dp, semantic.border),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            ErrorHighlightText(
                                                text = demoText,
                                                issues = demoReport.issues,
                                                onIssueClick = { inspectedIssue = it }
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                text = if (demoReport.issues.isEmpty()) {
                                                    "Texte corrigé sans anomalie détectée."
                                                } else {
                                                    "${demoReport.issues.size} point(s) détecté(s) · touchez un fragment coloré pour voir la règle"
                                                },
                                                style = FrancaisProTextStyles.MonoMetric,
                                                color = if (demoReport.issues.isEmpty()) semantic.success else semantic.textSecondary
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (demoReport.issues.isNotEmpty()) {
                                            PrimaryButton(
                                                text = "Tout corriger",
                                                icon = Icons.Filled.AutoFixHigh,
                                                onClick = { demoText = correctionEngine.applySafeCorrections(demoText, demoReport.issues) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("demo_apply_all_btn")
                                            )
                                        } else {
                                            SecondaryButton(
                                                text = "Réinitialiser",
                                                icon = Icons.Filled.Refresh,
                                                onClick = { demoText = demoScenarios[selectedScenarioIdx].sampleText },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("demo_reset_btn")
                                            )
                                        }
                                        SecondaryButton(
                                            text = "Correcteur",
                                            icon = Icons.AutoMirrored.Filled.ArrowForward,
                                            onClick = { onOpenInCorrector(demoText) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("demo_open_in_corrector_btn")
                                        )
                                    }
                                }
                            }

                            // Visite rapide des 5 onglets principaux
                            item {
                                SectionHeader(title = "Visite guidée des 5 espaces")
                            }

                            itemsIndexed(appTourSteps) { idx, step ->
                                EditorialCard(
                                    onClick = { onNavigateToTab(step.route) },
                                    contentPadding = 14.dp,
                                    modifier = Modifier.testTag("demo_tour_step_$idx")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = step.icon,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(9.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(step.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            Spacer(Modifier.height(2.dp))
                                            Text(step.description, style = MaterialTheme.typography.bodySmall, color = semantic.textSecondary)
                                        }
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Ouvrir ${step.title}",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            item { Spacer(Modifier.height(12.dp)) }
                        }
                    } else {
                        val filteredGuides = remember(guideQuery) {
                            val q = guideQuery.trim()
                            if (q.isEmpty()) guideSections
                            else guideSections.filter { sec ->
                                sec.title.contains(q, ignoreCase = true) ||
                                    sec.tag.contains(q, ignoreCase = true) ||
                                    sec.summary.contains(q, ignoreCase = true) ||
                                    sec.bulletPoints.any { it.contains(q, ignoreCase = true) }
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("guide_manual_list"),
                            contentPadding = PaddingValues(horizontal = hPadding, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                AppSearchBar(
                                    query = guideQuery,
                                    onQueryChange = { guideQuery = it },
                                    placeholder = "Rechercher dans le guide (ex. dictée, JSON, SM-2)…"
                                )
                            }

                            itemsIndexed(filteredGuides) { idx, section ->
                                val isExpanded = expandedGuideIdx == idx
                                EditorialCard(
                                    onClick = { expandedGuideIdx = if (isExpanded) null else idx },
                                    contentPadding = 16.dp,
                                    modifier = Modifier.testTag("guide_section_$idx")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            CategoryBadge(
                                                label = section.tag,
                                                color = MaterialTheme.colorScheme.primary,
                                                containerColor = MaterialTheme.colorScheme.primaryContainer
                                            )
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                text = section.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = section.summary,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = semantic.textSecondary
                                            )
                                        }
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                            contentDescription = if (isExpanded) "Réduire" else "Déplier",
                                            tint = semantic.textSecondary
                                        )
                                    }

                                    AnimatedVisibility(visible = isExpanded) {
                                        Column(
                                            modifier = Modifier.padding(top = 12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            section.bulletPoints.forEach { point ->
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.Top
                                                ) {
                                                    Text(
                                                        "•",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = point,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item { Spacer(Modifier.height(12.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideSegmentChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    Surface(
        shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
        color = if (selected) MaterialTheme.colorScheme.surface else androidx.compose.ui.graphics.Color.Transparent,
        border = if (selected) BorderStroke(1.dp, semantic.border) else null,
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(FrancaisProDimens.RadiusSmall))
            .clickable(role = Role.Tab, onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else semantic.textSecondary,
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.size(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.primary else semantic.textSecondary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
