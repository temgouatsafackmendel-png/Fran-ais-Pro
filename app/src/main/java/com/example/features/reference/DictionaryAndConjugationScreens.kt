package com.example.features.reference

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.example.core.database.DictionaryEntry
import com.example.core.database.Favorite
import com.example.core.database.Verb
import com.example.core.design.AppIconButton
import com.example.core.design.AppSearchBar
import com.example.core.design.AppTopBar
import com.example.core.design.CategoryBadge
import com.example.core.design.CefrLevelPill
import com.example.core.design.EditorialCard
import com.example.core.design.EmptyState
import com.example.core.design.PrimaryButton
import com.example.core.design.SecondaryButton
import com.example.linguistics.conjugation.IdentifiedVerbForm
import com.example.linguistics.conjugation.VerbConjugationTable
import com.example.linguistics.model.FrenchNormalizer
import com.example.ui.theme.FrancaisProDimens
import com.example.ui.theme.FrancaisProTextStyles
import com.example.ui.theme.LocalSemanticColors

/**
 * Écran 11 : Dictionnaire — AppSearchBar rapide, suggestions instantanées en direct, catégories et favoris.
 */
@Composable
fun DictionaryScreen(
    entries: List<DictionaryEntry>,
    recentEntries: List<DictionaryEntry>,
    favorites: List<Favorite>,
    onOpenWord: (String) -> Unit,
    onBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tous") }
    val categories = remember(entries) { listOf("Tous") + entries.map { it.partOfSpeech }.distinct() }
    val favWordIds = remember(favorites) { favorites.filter { it.itemType == "WORD" }.map { it.itemId }.toSet() }
    val semantic = LocalSemanticColors.current

    val filtered = remember(entries, query, selectedCategory) {
        val normQ = FrenchNormalizer.stripAccents(query.trim())
        entries.filter { e ->
            val catOk = selectedCategory == "Tous" || e.partOfSpeech == selectedCategory
            if (!catOk) return@filter false
            if (normQ.isEmpty()) return@filter true
            e.normalizedWord.contains(normQ) ||
                e.definition.lowercase().contains(query.trim().lowercase()) ||
                e.synonymsPipe.lowercase().contains(query.trim().lowercase()) ||
                levenshteinQuick(normQ, e.normalizedWord) <= 2
        }
    }

    val instantSuggestions = remember(query, filtered) {
        if (query.trim().length >= 2) filtered.take(5) else emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(
            title = "Dictionnaire",
            subtitle = "${entries.size} entrées · recherche instantanée",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = FrancaisProDimens.MaxContentWidth)
                .align(Alignment.CenterHorizontally)
        ) {
            AppSearchBar(
                query = query,
                onQueryChange = { query = it },
                placeholder = "Rechercher un mot, un synonyme…",
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("dictionary_search_input")
            )

            // Suggestions instantanées pendant la frappe
            if (instantSuggestions.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(instantSuggestions, key = { "sug_${it.id}" }) { sug ->
                        AssistChip(
                            onClick = { onOpenWord(sug.id) },
                            label = { Text(sug.word, fontWeight = FontWeight.SemiBold) },
                            shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall)
                        )
                    }
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (query.isBlank() && recentEntries.isNotEmpty()) {
                    item {
                        Text(
                            "Consultés récemment",
                            style = MaterialTheme.typography.labelLarge,
                            color = semantic.textSecondary
                        )
                        Spacer(Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(recentEntries) { rec ->
                                AssistChip(
                                    onClick = { onOpenWord(rec.id) },
                                    label = { Text(rec.word) }
                                )
                            }
                        }
                    }
                }

                if (filtered.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            title = "Aucun mot trouvé",
                            message = "Essayez une autre orthographe ou réinitialisez le filtre.",
                            actionLabel = "Tout afficher",
                            onAction = {
                                query = ""
                                selectedCategory = "Tous"
                            }
                        )
                    }
                } else {
                    items(filtered, key = { it.id }) { item ->
                        EditorialCard(
                            onClick = { onOpenWord(item.id) },
                            modifier = Modifier.testTag("dict_item_${item.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item.word, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(item.pronunciation, style = FrancaisProTextStyles.MonoMetric, color = semantic.textSecondary)
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (item.id in favWordIds) {
                                        Icon(Icons.Filled.Favorite, contentDescription = "Favori", tint = semantic.danger, modifier = Modifier.size(16.dp))
                                    }
                                    CategoryBadge(
                                        label = item.partOfSpeech,
                                        color = MaterialTheme.colorScheme.primary,
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = item.definition,
                                style = MaterialTheme.typography.bodySmall,
                                color = semantic.textSecondary,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Écran 12 : Fiche détaillée d'un mot — éditorial, aéré, audio + favori.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordDetailScreen(
    entry: DictionaryEntry?,
    favorites: List<Favorite>,
    onToggleFavorite: (DictionaryEntry) -> Unit,
    onSpeakWord: (String) -> Unit,
    onBack: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    val isFav = entry != null && favorites.any { it.itemType == "WORD" && it.itemId == entry.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(
            title = entry?.word ?: "Fiche de mot",
            subtitle = entry?.pronunciation,
            onBack = onBack,
            actions = {
                if (entry != null) {
                    AppIconButton(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Écouter la prononciation",
                        onClick = { onSpeakWord(entry.word) }
                    )
                    AppIconButton(
                        icon = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFav) "Retirer des favoris" else "Ajouter aux favoris",
                        tint = if (isFav) semantic.danger else MaterialTheme.colorScheme.onSurface,
                        onClick = { onToggleFavorite(entry) },
                        modifier = Modifier.testTag("word_fav_btn")
                    )
                }
            }
        )

        if (entry == null) {
            EmptyState(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                title = "Mot introuvable",
                message = "Revenez au dictionnaire pour sélectionner une entrée.",
                actionLabel = "Retour",
                onAction = onBack
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
            EditorialCard(contentPadding = 20.dp) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CefrLevelPill(level = entry.level)
                    CategoryBadge(
                        label = entry.partOfSpeech,
                        color = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    Text(
                        text = "${entry.gender} · Pluriel : ${entry.pluralForm}",
                        style = MaterialTheme.typography.bodySmall,
                        color = semantic.textSecondary
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(entry.word, style = FrancaisProTextStyles.Display)
                Text(entry.pronunciation, style = FrancaisProTextStyles.MonoMetric, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(12.dp))
                Text(entry.definition, style = MaterialTheme.typography.bodyLarge)
            }

            val synonyms = entry.synonymsPipe.split("|").filter { it.isNotBlank() }
            val antonyms = entry.antonymsPipe.split("|").filter { it.isNotBlank() }
            if (synonyms.isNotEmpty() || antonyms.isNotEmpty()) {
                EditorialCard {
                    if (synonyms.isNotEmpty()) {
                        Text("Synonymes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            synonyms.forEach { syn -> CategoryBadge(syn, semantic.success, semantic.successContainer) }
                        }
                    }
                    if (antonyms.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        Text("Antonymes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            antonyms.forEach { ant -> CategoryBadge(ant, semantic.danger, semantic.dangerContainer) }
                        }
                    }
                }
            }

            val examples = entry.examplesPipe.split("|").filter { it.isNotBlank() }
            if (examples.isNotEmpty()) {
                EditorialCard {
                    Text("Exemples d'emploi", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        examples.forEach { ex ->
                            Surface(
                                shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                                color = semantic.surfaceSubtle,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(ex, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp))
                            }
                        }
                    }
                }
            }

            val expressions = entry.expressionsPipe.split("|").filter { it.isNotBlank() }
            if (expressions.isNotEmpty()) {
                EditorialCard {
                    Text("Expressions associées", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    expressions.forEach { expr ->
                        Text("« $expr »", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 3.dp))
                    }
                }
            }
        }
    }
}

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

private fun levenshteinQuick(a: String, b: String): Int {
    if (kotlin.math.abs(a.length - b.length) > 2) return 3
    val dp = IntArray(b.length + 1) { it }
    for (i in 1..a.length) {
        var prev = dp[0]
        dp[0] = i
        for (j in 1..b.length) {
            val temp = dp[j]
            dp[j] = minOf(dp[j] + 1, dp[j - 1] + 1, prev + if (a[i - 1] == b[j - 1]) 0 else 1)
            prev = temp
        }
    }
    return dp[b.length]
}
