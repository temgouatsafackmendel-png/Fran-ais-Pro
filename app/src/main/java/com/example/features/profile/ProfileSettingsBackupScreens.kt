package com.example.features.profile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.BuildConfig
import com.example.core.database.Achievement
import com.example.core.database.UserProfile
import com.example.core.datastore.LocalUserPreferences
import com.example.core.design.AppTopBar
import com.example.core.design.CategoryBadge
import com.example.core.design.CefrLevelPill
import com.example.core.design.DiscreetOfflineBadge
import com.example.core.design.EditorialCard
import com.example.core.design.PrimaryButton
import com.example.core.design.SecondaryButton
import com.example.core.design.SectionHeader
import com.example.core.design.StatisticsCard
import com.example.core.design.UserInitialsAvatar
import com.example.core.work.DailyReminderWorker
import com.example.learning.progression.LinguisticProfileSummary
import com.example.navigation.Routes
import com.example.ui.theme.FrancaisProDimens
import com.example.ui.theme.LocalSemanticColors
import com.example.ui.theme.LocalSpacing

/**
 * Écran 22 : Profil personnel — Niveau, progression, temps d'étude, réalisations, paramètres & sauvegarde.
 */
@Composable
fun ProfileScreen(
    profile: UserProfile?,
    adaptiveSummary: LinguisticProfileSummary,
    achievements: List<Achievement>,
    onUpdateName: (String) -> Unit,
    onNavigate: (String) -> Unit
) {
    var nameDraft by remember(profile?.displayName) { mutableStateOf(profile?.displayName ?: "Apprenant") }
    val semantic = LocalSemanticColors.current

    val spacing = LocalSpacing.current
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        val hPadding = spacing.screenHorizontalPadding(maxWidth)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = spacing.maxContentWidth),
            contentPadding = PaddingValues(horizontal = hPadding, vertical = spacing.space16),
            verticalArrangement = Arrangement.spacedBy(spacing.space16)
        ) {
            item {
                Text("Profil", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Votre espace personnel d'apprentissage",
                    style = MaterialTheme.typography.bodySmall,
                    color = semantic.textSecondary
                )
            }

            // Identité locale + Niveau actuel
            item {
                EditorialCard(contentPadding = 18.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            UserInitialsAvatar(displayName = profile?.displayName ?: "Apprenant", size = 54.dp)
                            Column {
                                Text(profile?.displayName ?: "Apprenant", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(
                                    "${adaptiveSummary.levelLabelFr} · Score ${adaptiveSummary.globalScore}/100",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = semantic.textSecondary
                                )
                            }
                        }
                        CefrLevelPill(level = adaptiveSummary.estimatedLevel)
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = nameDraft,
                            onValueChange = { nameDraft = it },
                            label = { Text("Nom affiché") },
                            singleLine = true,
                            shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                            modifier = Modifier.weight(1f)
                        )
                        PrimaryButton(
                            text = "Enregistrer",
                            onClick = { onUpdateName(nameDraft) }
                        )
                    }
                }
            }

            // Résumé rapide Temps / Série / Réalisations
            item {
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
                        value = "${profile?.streakDays ?: 0} j",
                        label = "Série active",
                        icon = Icons.Filled.LocalFireDepartment,
                        accentColor = semantic.warning,
                        accentContainer = semantic.warningContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(10.dp))
                StatisticsCard(
                    value = "${achievements.count { it.isUnlocked }} / ${achievements.size}",
                    label = "Réalisations débloquées",
                    icon = Icons.Filled.EmojiEvents,
                    accentColor = semantic.success,
                    accentContainer = semantic.successContainer,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Compétences maîtrisées / à renforcer
            item {
                SectionHeader(
                    title = "Bilan des compétences",
                    subtitle = "Points forts et axes de progression",
                    icon = Icons.Filled.EmojiEvents
                )
                Spacer(Modifier.height(10.dp))
                EditorialCard {
                    Text("Compétences maîtrisées (${adaptiveSummary.masteredSkills.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    if (adaptiveSummary.masteredSkills.isEmpty()) {
                        Text(
                            "Complétez quelques exercices pour révéler vos points forts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = semantic.textSecondary
                        )
                    } else {
                        adaptiveSummary.masteredSkills.forEach { s ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(s.skillCategory, style = MaterialTheme.typography.bodyMedium)
                                CategoryBadge("${s.masteryPercent} %", semantic.success, semantic.successContainer)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("À renforcer (${adaptiveSummary.weakSkills.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    if (adaptiveSummary.weakSkills.isEmpty()) {
                        Text(
                            "Aucun point faible critique relevé pour le moment.",
                            style = MaterialTheme.typography.bodySmall,
                            color = semantic.textSecondary
                        )
                    } else {
                        adaptiveSummary.weakSkills.forEach { s ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(s.skillCategory, style = MaterialTheme.typography.bodyMedium)
                                CategoryBadge("${s.masteryPercent} %", semantic.danger, semantic.dangerContainer)
                            }
                        }
                    }
                }
            }

            // Navigation secondaire claire
            item {
                SectionHeader(
                    title = "Espace & réglages",
                    subtitle = "Outils, statistiques, préférences et sauvegarde",
                    icon = Icons.Filled.Settings
                )
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProfileNavRow(Icons.AutoMirrored.Filled.MenuBook, "Démo & Guide d'utilisation", "Essai interactif du correcteur et mode d'emploi", "profile_nav_guide") {
                        onNavigate(Routes.USER_GUIDE)
                    }
                    ProfileNavRow(Icons.AutoMirrored.Filled.ShowChart, "Aperçu des performances (30 j)", "Tendances Grammaire, Vocabulaire et Écriture", "profile_nav_performance") {
                        onNavigate(Routes.PERFORMANCE_OVERVIEW)
                    }
                    ProfileNavRow(Icons.Filled.BarChart, "Statistiques détaillées", "Graphiques interactifs de progression et badges", "profile_nav_stats") {
                        onNavigate(Routes.STATISTICS)
                    }
                    ProfileNavRow(Icons.Filled.History, "Historique", "Corrections et dictées enregistrées", "profile_nav_history") {
                        onNavigate(Routes.HISTORY)
                    }
                    ProfileNavRow(Icons.Filled.Favorite, "Favoris", "Mots, leçons et verbes", "profile_nav_favorites") {
                        onNavigate(Routes.FAVORITES)
                    }
                    ProfileNavRow(Icons.Filled.Settings, "Paramètres", "Thème clair/sombre, objectif, voix et rappels", "profile_nav_settings") {
                        onNavigate(Routes.SETTINGS)
                    }
                    ProfileNavRow(Icons.Filled.Backup, "Sauvegarde & restauration", "Exporter ou importer vos données JSON", "profile_nav_backup") {
                        onNavigate(Routes.BACKUP)
                    }
                }
            }

            item {
                SecondaryButton(
                    text = "Refaire l'évaluation de niveau",
                    onClick = { onNavigate(Routes.ASSESSMENT) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_retake_assessment_btn")
                )
            }

            // Encart Version & Nouveautés (v1.3.0)
            item {
                EditorialCard(
                    contentPadding = spacing.space16,
                    modifier = Modifier.testTag("profile_release_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FRANÇAIS PRO v${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        CategoryBadge(
                            label = "v${BuildConfig.VERSION_NAME}",
                            color = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Nouveautés v1.3.0 : Design System Premium unifié (tokens clair/sombre, grille 4 dp, typographie Fraunces, Plus Jakarta Sans & JetBrains Mono), Aperçu des performances sur 30 jours (Grammaire, Vocabulaire, Écriture), prononciation audio directe dans le Dictionnaire, précision & filtre d'erreurs en Exercices, et studio Correcteur raffiné de bout en bout.",
                        style = MaterialTheme.typography.bodySmall,
                        color = semantic.textSecondary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Réalisé par Temgoua Mendel · 100 % hors ligne",
                        style = MaterialTheme.typography.labelSmall,
                        color = semantic.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tag: String,
    onClick: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    EditorialCard(onClick = onClick, contentPadding = 14.dp, modifier = Modifier.testTag(tag)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = semantic.textSecondary)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = semantic.textSecondary)
        }
    }
}

/**
 * Écran 23 : Paramètres (thème, objectif quotidien, voix, rappels, indicateur discret « Fonctionne hors ligne »).
 */
@Composable
fun SettingsScreen(
    prefs: LocalUserPreferences,
    onThemeModeChange: (String) -> Unit,
    onDailyGoalChange: (Int) -> Unit,
    onSpeechRateChange: (Float) -> Unit,
    onAutoAnalyzeChange: (Boolean) -> Unit,
    onDailyReminderChange: (Boolean, Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val semantic = LocalSemanticColors.current
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onDailyReminderChange(true, prefs.reminderHour)
            DailyReminderWorker.schedule(context, true, prefs.reminderHour)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(
            title = "Paramètres",
            subtitle = "Préférences locales",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = FrancaisProDimens.MaxContentWidth)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                EditorialCard {
                    Text("Apparence", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("SYSTEM" to "Système", "LIGHT" to "Clair", "DARK" to "Sombre").forEach { (key, label) ->
                            FilterChip(
                                selected = prefs.themeMode == key,
                                onClick = { onThemeModeChange(key) },
                                label = { Text(label) },
                                modifier = Modifier.testTag("theme_chip_$key")
                            )
                        }
                    }
                }
            }

            item {
                EditorialCard {
                    Text("Objectif quotidien : ${prefs.dailyGoalMinutes} minutes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Slider(
                        value = prefs.dailyGoalMinutes.toFloat(),
                        onValueChange = { onDailyGoalChange(it.toInt()) },
                        valueRange = 5f..60f,
                        steps = 10
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("Vitesse de lecture vocale : ${"%.2f".format(prefs.ttsSpeechRate)}x", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Slider(
                        value = prefs.ttsSpeechRate,
                        onValueChange = onSpeechRateChange,
                        valueRange = 0.7f..1.3f
                    )
                }
            }

            item {
                EditorialCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Analyse automatique", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Analyser immédiatement au chargement d'un exemple", style = MaterialTheme.typography.bodySmall, color = semantic.textSecondary)
                        }
                        Switch(checked = prefs.autoAnalyze, onCheckedChange = onAutoAnalyzeChange)
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Rappel quotidien", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Notification locale programmée sur cet appareil", style = MaterialTheme.typography.bodySmall, color = semantic.textSecondary)
                        }
                        Switch(
                            checked = prefs.dailyReminderEnabled,
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
                                onDailyReminderChange(enable, prefs.reminderHour)
                                DailyReminderWorker.schedule(context, enable, prefs.reminderHour)
                            }
                        )
                    }
                }
            }

            // Indicateur discret « Fonctionne hors ligne » uniquement ici et dans l'onboarding (§14)
            item {
                EditorialCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Confidentialité", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        DiscreetOfflineBadge()
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Français Pro n'exige aucun compte, ne contient aucune publicité et ne demande aucune permission Internet. Tous vos écrits et progrès restent stockés localement sur cet appareil.",
                        style = MaterialTheme.typography.bodySmall,
                        color = semantic.textSecondary
                    )
                }
            }
        }
    }
}

/**
 * Écran 24 : Sauvegarde & Restauration JSON locale (SAF).
 */
@Composable
fun BackupScreen(
    includeHistory: Boolean,
    backupStatus: String?,
    onToggleIncludeHistory: (Boolean) -> Unit,
    onPrepareExport: (includeHistory: Boolean, onJsonReady: (String) -> Unit) -> Unit,
    onImportJson: (String) -> Unit,
    onBackupFeedback: (String) -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val semantic = LocalSemanticColors.current
    var pendingJsonToWrite by remember { mutableStateOf<String?>(null) }
    var manualJsonInput by remember { mutableStateOf("") }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val json = pendingJsonToWrite
        pendingJsonToWrite = null
        if (uri == null) {
            onBackupFeedback("Export annulé : aucun fichier n'a été enregistré.")
            return@rememberLauncherForActivityResult
        }
        if (json == null) {
            onBackupFeedback("Erreur : contenu de sauvegarde indisponible.")
            return@rememberLauncherForActivityResult
        }
        try {
            val stream = context.contentResolver.openOutputStream(uri)
            if (stream == null) {
                onBackupFeedback("Impossible d'ouvrir le fichier sélectionné en écriture.")
            } else {
                stream.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                }
                onBackupFeedback("Sauvegarde enregistrée avec succès dans le fichier JSON.")
            }
        } catch (e: Exception) {
            onBackupFeedback("Échec de l'enregistrement du fichier : ${e.message ?: "erreur d'écriture"}")
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val content = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
            if (content.isNullOrBlank()) {
                onBackupFeedback("Le fichier sélectionné est vide ou illisible.")
            } else {
                onImportJson(content)
            }
        } catch (e: Exception) {
            onBackupFeedback("Impossible de lire le fichier sélectionné : ${e.message ?: "erreur de lecture"}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(
            title = "Sauvegarde locale",
            subtitle = "Export et restauration JSON",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = FrancaisProDimens.MaxContentWidth)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (backupStatus != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = backupStatus,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            item {
                EditorialCard {
                    Text("Exporter vos données", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Enregistre votre profil, vos leçons, exercices, vocabulaire SM-2, dictées, statistiques et favoris dans un fichier JSON portable.",
                        style = MaterialTheme.typography.bodySmall,
                        color = semantic.textSecondary
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Inclure l'historique du correcteur", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = includeHistory, onCheckedChange = onToggleIncludeHistory)
                    }
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton(
                        text = "Exporter en fichier JSON",
                        icon = Icons.Filled.Upload,
                        onClick = {
                            onPrepareExport(includeHistory) { json ->
                                pendingJsonToWrite = json
                                createDocumentLauncher.launch("francais_pro_sauvegarde.json")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("backup_export_btn")
                    )
                }
            }

            item {
                EditorialCard {
                    Text("Restaurer une sauvegarde", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Le fichier est intégralement validé avant l'écriture en base dans une transaction atomique.",
                        style = MaterialTheme.typography.bodySmall,
                        color = semantic.textSecondary
                    )
                    Spacer(Modifier.height(12.dp))
                    SecondaryButton(
                        text = "Choisir un fichier JSON",
                        icon = Icons.Filled.Download,
                        onClick = { openDocumentLauncher.launch(arrayOf("application/json", "text/plain")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("backup_import_file_btn")
                    )

                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = manualJsonInput,
                        onValueChange = { manualJsonInput = it },
                        label = { Text("Ou coller le JSON de sauvegarde") },
                        shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    PrimaryButton(
                        text = "Valider et restaurer",
                        enabled = manualJsonInput.isNotBlank(),
                        onClick = { onImportJson(manualJsonInput) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
