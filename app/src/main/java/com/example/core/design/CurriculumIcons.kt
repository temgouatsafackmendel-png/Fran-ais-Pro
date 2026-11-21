package com.example.core.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Segment
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalSemanticColors
import com.example.ui.theme.SemanticColors

/**
 * Résolveur centralisé d'icônes modernes et palettes sémantiques pour les niveaux CECRL
 * et les domaines linguistiques du parcours d'apprentissage.
 */
object CurriculumIcons {

    fun forLevel(level: String): ImageVector = when (level) {
        "A1" -> Icons.Filled.Explore
        "A2" -> Icons.Filled.AutoStories
        "B1" -> Icons.Filled.Psychology
        "B2" -> Icons.Filled.WorkspacePremium
        "C1" -> Icons.Filled.MilitaryTech
        "C2" -> Icons.Filled.EmojiEvents
        else -> Icons.AutoMirrored.Filled.MenuBook
    }

    fun forTheme(theme: String): ImageVector = when {
        theme.contains("Orthographe", ignoreCase = true) -> Icons.Filled.Spellcheck
        theme.contains("Grammaire", ignoreCase = true) -> Icons.Filled.AccountTree
        theme.contains("Conjugaison", ignoreCase = true) -> Icons.Filled.Schedule
        theme.contains("Syntaxe", ignoreCase = true) -> Icons.Filled.Segment
        theme.contains("Ponctuation", ignoreCase = true) -> Icons.Filled.FormatQuote
        theme.contains("Compréhension", ignoreCase = true) -> Icons.Filled.Visibility
        theme.contains("Expression écrite", ignoreCase = true) -> Icons.Filled.EditNote
        theme.contains("professionnel", ignoreCase = true) -> Icons.Filled.BusinessCenter
        theme.contains("Vocabulaire", ignoreCase = true) -> Icons.Filled.AutoAwesome
        theme.contains("Expression orale", ignoreCase = true) -> Icons.Filled.RecordVoiceOver
        theme.contains("Style", ignoreCase = true) -> Icons.Filled.Brush
        else -> Icons.AutoMirrored.Filled.MenuBook
    }

    fun forAchievement(iconName: String): ImageVector = when (iconName) {
        "edit" -> Icons.Filled.Edit
        "school" -> Icons.Filled.School
        "account_tree" -> Icons.Filled.AccountTree
        "book" -> Icons.AutoMirrored.Filled.MenuBook
        "spellcheck" -> Icons.Filled.Spellcheck
        "schedule" -> Icons.Filled.Schedule
        "check_circle" -> Icons.Filled.CheckCircle
        "fitness_center" -> Icons.Filled.FitnessCenter
        "verified" -> Icons.Filled.Verified
        "workspace_premium" -> Icons.Filled.WorkspacePremium
        "mic" -> Icons.Filled.Mic
        "local_fire_department" -> Icons.Filled.LocalFireDepartment
        else -> Icons.Filled.EmojiEvents
    }

    fun themeAccent(theme: String, semantic: SemanticColors, primary: Color, primaryContainer: Color): Pair<Color, Color> = when {
        theme.contains("Orthographe", ignoreCase = true) -> semantic.spellingColor to semantic.spellingContainer
        theme.contains("Grammaire", ignoreCase = true) || theme.contains("Conjugaison", ignoreCase = true) ->
            semantic.grammarColor to semantic.grammarContainer
        theme.contains("Syntaxe", ignoreCase = true) || theme.contains("Ponctuation", ignoreCase = true) ->
            semantic.syntaxColor to semantic.syntaxContainer
        theme.contains("Vocabulaire", ignoreCase = true) || theme.contains("Compréhension", ignoreCase = true) ->
            semantic.success to semantic.successContainer
        else -> primary to primaryContainer
    }
}

/**
 * En-tête de section de cours structuré avec badge d'icône moderne et numéro d'étape optionnel.
 */
@Composable
fun StructuredSectionTitle(
    title: String,
    icon: ImageVector,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    stepLabel: String? = null,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
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
        Column(modifier = Modifier.weight(1f)) {
            if (!stepLabel.isNullOrBlank()) {
                Text(
                    text = stepLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
