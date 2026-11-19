package com.example.features.reference

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.database.DictionaryEntry
import com.example.core.database.Favorite
import com.example.core.design.AppIconButton
import com.example.core.design.AppSearchBar
import com.example.core.design.AppTopBar
import com.example.core.design.CategoryBadge
import com.example.core.design.CefrLevelPill
import com.example.core.design.EditorialCard
import com.example.core.design.EmptyState
import com.example.data.DictionarySearchScope
import com.example.features.DictionaryUiState
import com.example.ui.theme.FrancaisProDimens
import com.example.ui.theme.FrancaisProTextStyles
import com.example.ui.theme.LocalSemanticColors

/**
 * Écran 11 : Dictionnaire local (Room SQLite 100 % hors ligne) —
 * Recherche instantanée dans les mots, définitions, synonymes et antonymes,
 * suggestions floues, catégories, favoris et ajout d'entrées personnelles.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DictionaryScreen(
    uiState: DictionaryUiState,
    favorites: List<Favorite>,
    onQueryChange: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onScopeSelected: (DictionarySearchScope) -> Unit,
    onToggleFavoritesOnly: () -> Unit,
    onClearFilters: () -> Unit,
    onOpenWord: (String) -> Unit,
    onAddCustomWord: (
        word: String,
        partOfSpeech: String,
        definition: String,
        synonymsCsv: String,
        antonymsCsv: String,
        example: String,
        level: String
    ) -> Unit = { _, _, _, _, _, _, _ -> },
    onBack: () -> Unit
) {
    val favWordIds = remember(favorites) {
        favorites.filter { it.itemType == "WORD" }.map { it.itemId }.toSet()
    }
    val semantic = LocalSemanticColors.current
    var showAddDialog by remember { mutableStateOf(false) }

    val instantSuggestions = remember(uiState.query, uiState.results) {
        if (uiState.query.trim().length >= 2) uiState.results.take(5) else emptyList()
    }
    val hasActiveFilter = uiState.query.isNotBlank() ||
        uiState.selectedCategory != "Tous" ||
        uiState.selectedScope != DictionarySearchScope.ALL ||
        uiState.favoritesOnly

    if (showAddDialog) {
        AddDictionaryEntryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { word, pos, def, syn, ant, ex, lvl ->
                onAddCustomWord(word, pos, def, syn, ant, ex, lvl)
                showAddDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(
            title = "Dictionnaire",
            subtitle = "${uiState.totalCount} entrées locales · 100 % hors ligne",
            onBack = onBack,
            actions = {
                AppIconButton(
                    icon = Icons.Filled.Add,
                    contentDescription = "Ajouter un mot personnel au dictionnaire",
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("dict_add_word_btn")
                )
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = FrancaisProDimens.MaxContentWidth)
                .align(Alignment.CenterHorizontally)
        ) {
            AppSearchBar(
                query = uiState.query,
                onQueryChange = onQueryChange,
                placeholder = "Mot, définition, synonyme ou antonyme…",
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("dictionary_search_input")
            )

            // Filtres de portée SQL Room (Tout, Définitions, Synonymes, Antonymes, Favoris)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(DictionarySearchScope.entries) { scope ->
                    FilterChip(
                        selected = uiState.selectedScope == scope,
                        onClick = { onScopeSelected(scope) },
                        label = { Text(scope.label) },
                        modifier = Modifier.testTag("dict_scope_${scope.name}")
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.favoritesOnly,
                        onClick = onToggleFavoritesOnly,
                        label = { Text("Favoris") },
                        leadingIcon = {
                            Icon(
                                imageVector = if (uiState.favoritesOnly) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("dict_scope_FAVORITES")
                    )
                }
            }

            // Suggestions instantanées pendant la frappe
            if (instantSuggestions.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(instantSuggestions, key = { "sug_${it.id}" }) { sug ->
                        AssistChip(
                            onClick = { onOpenWord(sug.id) },
                            label = { Text(sug.word, fontWeight = FontWeight.SemiBold) },
                            shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
                            modifier = Modifier.testTag("dict_suggestion_${sug.id}")
                        )
                    }
                }
            }

            // Catégories grammaticales issues de Room
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.categories) { cat ->
                    FilterChip(
                        selected = uiState.selectedCategory == cat,
                        onClick = { onCategorySelected(cat) },
                        label = { Text(cat) },
                        modifier = Modifier.testTag("dict_cat_$cat")
                    )
                }
            }

            // Bandeau de statut hors ligne + compteur de résultats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.OfflinePin,
                        contentDescription = null,
                        tint = semantic.success,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "${uiState.results.size} résultat(s)",
                        style = FrancaisProTextStyles.MonoMetric,
                        color = semantic.textSecondary,
                        modifier = Modifier.testTag("dict_result_count")
                    )
                }
                if (hasActiveFilter) {
                    TextButton(
                        onClick = onClearFilters,
                        modifier = Modifier.testTag("dict_clear_filters_btn")
                    ) {
                        Text("Réinitialiser")
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("dictionary_results_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (uiState.query.isBlank() && uiState.recentEntries.isNotEmpty() && !uiState.favoritesOnly) {
                    item(key = "recent_header") {
                        Column {
                            Text(
                                "Consultés récemment",
                                style = MaterialTheme.typography.labelLarge,
                                color = semantic.textSecondary
                            )
                            Spacer(Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(uiState.recentEntries, key = { "rec_${it.id}" }) { rec ->
                                    AssistChip(
                                        onClick = { onOpenWord(rec.id) },
                                        label = { Text(rec.word) },
                                        modifier = Modifier.testTag("dict_recent_${rec.id}")
                                    )
                                }
                            }
                        }
                    }
                }

                if (uiState.results.isEmpty()) {
                    if (uiState.fuzzySuggestions.isNotEmpty()) {
                        item(key = "fuzzy_suggestions") {
                            EditorialCard(contentPadding = 16.dp) {
                                Text(
                                    text = "Vous vouliez peut-être dire…",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(8.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    uiState.fuzzySuggestions.forEach { sug ->
                                        AssistChip(
                                            onClick = { onOpenWord(sug.id) },
                                            label = { Text(sug.word, fontWeight = FontWeight.SemiBold) },
                                            modifier = Modifier.testTag("dict_fuzzy_${sug.id}")
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item(key = "empty_state") {
                        EmptyState(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            title = "Aucun mot trouvé",
                            message = "Essayez une autre orthographe, un synonyme ou réinitialisez les filtres.",
                            actionLabel = "Tout afficher",
                            onAction = onClearFilters
                        )
                    }
                } else {
                    items(uiState.results, key = { it.id }) { item ->
                        DictionaryEntryCard(
                            item = item,
                            isFavorite = item.id in favWordIds,
                            onClick = { onOpenWord(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DictionaryEntryCard(
    item: DictionaryEntry,
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    val synonyms = remember(item.synonymsPipe) {
        item.synonymsPipe.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    }
    val antonyms = remember(item.antonymsPipe) {
        item.antonymsPipe.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    }

    EditorialCard(
        onClick = onClick,
        modifier = Modifier.testTag("dict_item_${item.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.word,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.pronunciation,
                    style = FrancaisProTextStyles.MonoMetric,
                    color = semantic.textSecondary,
                    maxLines = 1
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isFavorite) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Favori",
                        tint = semantic.danger,
                        modifier = Modifier.size(16.dp)
                    )
                }
                CefrLevelPill(level = item.level)
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
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (synonyms.isNotEmpty() || antonyms.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                synonyms.take(2).forEach { syn ->
                    CategoryBadge(
                        label = "≈ $syn",
                        color = semantic.success,
                        containerColor = semantic.successContainer
                    )
                }
                antonyms.take(2).forEach { ant ->
                    CategoryBadge(
                        label = "≠ $ant",
                        color = semantic.danger,
                        containerColor = semantic.dangerContainer
                    )
                }
            }
        }
    }
}

/**
 * Écran 12 : Fiche détaillée d'un mot — définition, synonymes/antonymes interactifs reliés à Room,
 * prononciation audio locale, exemples, expressions et favoris.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordDetailScreen(
    entry: DictionaryEntry?,
    favorites: List<Favorite>,
    onToggleFavorite: (DictionaryEntry) -> Unit,
    onSpeakWord: (String) -> Unit,
    onRelatedWordClick: (String) -> Unit = {},
    onDeleteCustomEntry: ((String) -> Unit)? = null,
    onBack: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    val isFav = entry != null && favorites.any { it.itemType == "WORD" && it.itemId == entry.id }
    val isCustom = entry?.id?.startsWith("custom_") == true

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
                        onClick = { onSpeakWord(entry.word) },
                        modifier = Modifier.testTag("word_speak_btn")
                    )
                    AppIconButton(
                        icon = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFav) "Retirer des favoris" else "Ajouter aux favoris",
                        tint = if (isFav) semantic.danger else MaterialTheme.colorScheme.onSurface,
                        onClick = { onToggleFavorite(entry) },
                        modifier = Modifier.testTag("word_fav_btn")
                    )
                    if (isCustom && onDeleteCustomEntry != null) {
                        AppIconButton(
                            icon = Icons.Filled.DeleteOutline,
                            contentDescription = "Supprimer cette entrée personnelle",
                            tint = semantic.danger,
                            onClick = { onDeleteCustomEntry(entry.id) },
                            modifier = Modifier.testTag("word_delete_btn")
                        )
                    }
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
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CefrLevelPill(level = entry.level)
                    CategoryBadge(
                        label = entry.partOfSpeech,
                        color = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    if (isCustom) {
                        CategoryBadge(
                            label = "Personnel",
                            color = semantic.warning,
                            containerColor = semantic.warningContainer
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${entry.gender} · Pluriel : ${entry.pluralForm}",
                    style = MaterialTheme.typography.bodySmall,
                    color = semantic.textSecondary
                )
                Spacer(Modifier.height(10.dp))
                Text(entry.word, style = FrancaisProTextStyles.Display)
                Text(entry.pronunciation, style = FrancaisProTextStyles.MonoMetric, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(12.dp))
                Text(entry.definition, style = MaterialTheme.typography.bodyLarge)
            }

            val synonyms = entry.synonymsPipe.split("|").map { it.trim() }.filter { it.isNotBlank() }
            val antonyms = entry.antonymsPipe.split("|").map { it.trim() }.filter { it.isNotBlank() }
            if (synonyms.isNotEmpty() || antonyms.isNotEmpty()) {
                EditorialCard {
                    if (synonyms.isNotEmpty()) {
                        Text("Synonymes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Touchez un terme pour le consulter ou le rechercher hors ligne.",
                            style = MaterialTheme.typography.labelSmall,
                            color = semantic.textSecondary
                        )
                        Spacer(Modifier.height(10.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            synonyms.forEach { syn ->
                                RelatedWordChip(
                                    label = syn,
                                    accentColor = semantic.success,
                                    containerColor = semantic.successContainer,
                                    onClick = { onRelatedWordClick(syn) },
                                    modifier = Modifier.testTag("word_synonym_$syn")
                                )
                            }
                        }
                    }
                    if (antonyms.isNotEmpty()) {
                        if (synonyms.isNotEmpty()) Spacer(Modifier.height(16.dp))
                        Text("Antonymes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(10.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            antonyms.forEach { ant ->
                                RelatedWordChip(
                                    label = ant,
                                    accentColor = semantic.danger,
                                    containerColor = semantic.dangerContainer,
                                    onClick = { onRelatedWordClick(ant) },
                                    modifier = Modifier.testTag("word_antonym_$ant")
                                )
                            }
                        }
                    }
                }
            }

            val examples = entry.examplesPipe.split("|").map { it.trim() }.filter { it.isNotBlank() }
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

            val expressions = entry.expressionsPipe.split("|").map { it.trim() }.filter { it.isNotBlank() }
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

@Composable
private fun RelatedWordChip(
    label: String,
    accentColor: Color,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
        color = containerColor,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
        modifier = modifier
            .heightIn(min = 38.dp)
            .clip(RoundedCornerShape(FrancaisProDimens.RadiusSmall))
            .clickable(role = Role.Button, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = accentColor
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Rechercher $label",
                tint = accentColor,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
private fun AddDictionaryEntryDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        word: String,
        partOfSpeech: String,
        definition: String,
        synonymsCsv: String,
        antonymsCsv: String,
        example: String,
        level: String
    ) -> Unit
) {
    var word by remember { mutableStateOf("") }
    var pos by remember { mutableStateOf("Nom") }
    var definition by remember { mutableStateOf("") }
    var synonyms by remember { mutableStateOf("") }
    var antonyms by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("B1") }
    val posOptions = listOf("Nom", "Verbe", "Adjectif", "Adverbe")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un mot hors ligne") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = word,
                    onValueChange = { word = it },
                    label = { Text("Mot *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_word_input")
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(posOptions) { option ->
                        FilterChip(
                            selected = pos == option,
                            onClick = { pos = option },
                            label = { Text(option) }
                        )
                    }
                }
                OutlinedTextField(
                    value = definition,
                    onValueChange = { definition = it },
                    label = { Text("Définition *") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_definition_input")
                )
                OutlinedTextField(
                    value = synonyms,
                    onValueChange = { synonyms = it },
                    label = { Text("Synonymes (séparés par virgule)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_synonyms_input")
                )
                OutlinedTextField(
                    value = antonyms,
                    onValueChange = { antonyms = it },
                    label = { Text("Antonymes (séparés par virgule)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_antonyms_input")
                )
                OutlinedTextField(
                    value = example,
                    onValueChange = { example = it },
                    label = { Text("Exemple d'emploi") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(word, pos, definition, synonyms, antonyms, example, level) },
                enabled = word.isNotBlank() && definition.isNotBlank(),
                modifier = Modifier.testTag("add_word_confirm_btn")
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
