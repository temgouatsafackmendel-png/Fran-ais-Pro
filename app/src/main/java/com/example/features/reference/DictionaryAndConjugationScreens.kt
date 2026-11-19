package com.example.features.reference

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.database.Favorite
import com.example.core.database.Verb
import com.example.core.design.AppIconButton
import com.example.core.design.AppSearchBar
import com.example.core.design.AppTopBar
import com.example.core.design.CategoryBadge
import com.example.core.design.EditorialCard
import com.example.core.design.EmptyState
import com.example.core.design.PrimaryButton
import com.example.core.design.SecondaryButton
import com.example.linguistics.conjugation.IdentifiedVerbForm
import com.example.linguistics.conjugation.VerbConjugationTable
import com.example.ui.theme.FrancaisProDimens
import com.example.ui.theme.FrancaisProTextStyles
import com.example.ui.theme.LocalSemanticColors

/**
 * Écran 13 : Conjugueur + identification inverse + navigation fluide entre les temps.
 */
@Composable
fun ConjugationScreen(
    verbs: List<Verb>,
    selectedTable: VerbConjugationTable?,
    identifiedForms: List<IdentifiedVerbForm>,
    favorites: List<Favorite>,
    onSelectVerb: (String) -> Unit,
    onIdentifyForm: (String) -> Unit,
    onToggleFavoriteVerb: (VerbConjugationTable) -> Unit,
    onBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedTense by remember { mutableStateOf("Présent") }
    val semantic = LocalSemanticColors.current
    val isFav = selectedTable != null && favorites.any { it.itemType == "VERB" && it.itemId == selectedTable.infinitive }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(
            title = "Conjugaison",
            subtitle = "Infinitif ou forme conjuguée",
            onBack = onBack,
            actions = {
                if (selectedTable != null) {
                    AppIconButton(
                        icon = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFav) "Retirer des favoris" else "Ajouter aux favoris",
                        tint = if (isFav) semantic.danger else MaterialTheme.colorScheme.onSurface,
                        onClick = { onToggleFavoriteVerb(selectedTable) }
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = FrancaisProDimens.MaxContentWidth)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppSearchBar(
                        query = query,
                        onQueryChange = { query = it },
                        placeholder = "Ex. apprendre, viennes, serais…",
                        modifier = Modifier
                            .weight(1f)
                            .testTag("conjugation_search_input")
                    )
                    PrimaryButton(
                        text = "OK",
                        onClick = { if (query.isNotBlank()) onSelectVerb(query.trim()) },
                        icon = Icons.Filled.Translate,
                        modifier = Modifier.testTag("conjugation_search_btn")
                    )
                }
            }

            // Raccourcis de verbes populaires
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(verbs.take(18)) { v ->
                        AssistChip(
                            onClick = {
                                query = v.infinitive
                                onSelectVerb(v.infinitive)
                            },
                            label = { Text(v.infinitive) }
                        )
                    }
                }
            }

            if (identifiedForms.isNotEmpty()) {
                item {
                    EditorialCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
                        Text("Forme conjuguée identifiée", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        identifiedForms.take(4).forEach { m ->
                            Text(
                                text = "• « ${m.matchedForm} » → ${m.infinitive} · ${m.tense} (${m.pronoun})",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            if (selectedTable != null) {
                val tenseNames = selectedTable.tenses.keys.toList()
                val activeTenseIndex = tenseNames.indexOf(selectedTense).coerceAtLeast(0)
                val resolvedTense = tenseNames.getOrNull(activeTenseIndex) ?: selectedTense

                item {
                    EditorialCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedTable.infinitive.replaceFirstChar { it.titlecase() },
                                style = FrancaisProTextStyles.Heading1
                            )
                            CategoryBadge(
                                label = "${selectedTable.groupNumber}e groupe",
                                color = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(selectedTable.modelInfo, style = MaterialTheme.typography.bodySmall, color = semantic.textSecondary)
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Aux. ${selectedTable.auxiliary}", style = MaterialTheme.typography.labelMedium)
                            Text("P. prés. ${selectedTable.presentParticiple}", style = MaterialTheme.typography.labelMedium)
                            Text("P. passé ${selectedTable.pastParticiple}", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                // Sélecteur de temps + navigation Temps précédent / Temps suivant
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(tenseNames) { tense ->
                            FilterChip(
                                selected = resolvedTense == tense,
                                onClick = { selectedTense = tense },
                                label = { Text(tense) }
                            )
                        }
                    }
                }

                item {
                    val rows = selectedTable.tenses[resolvedTense].orEmpty()
                    EditorialCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(resolvedTense, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "${activeTenseIndex + 1}/${tenseNames.size}",
                                style = FrancaisProTextStyles.MonoMetric,
                                color = semantic.textSecondary
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        rows.forEachIndexed { idx, row ->
                            if (idx > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = semantic.border)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(row.pronoun, style = MaterialTheme.typography.bodyMedium, color = semantic.textSecondary)
                                Text(
                                    row.form,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SecondaryButton(
                                text = "Temps précédent",
                                icon = Icons.AutoMirrored.Filled.ArrowBack,
                                enabled = activeTenseIndex > 0,
                                onClick = {
                                    if (activeTenseIndex > 0) selectedTense = tenseNames[activeTenseIndex - 1]
                                },
                                modifier = Modifier.weight(1f)
                            )
                            SecondaryButton(
                                text = "Temps suivant",
                                icon = Icons.AutoMirrored.Filled.ArrowForward,
                                enabled = activeTenseIndex + 1 < tenseNames.size,
                                onClick = {
                                    if (activeTenseIndex + 1 < tenseNames.size) selectedTense = tenseNames[activeTenseIndex + 1]
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

