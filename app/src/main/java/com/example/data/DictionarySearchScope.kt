package com.example.data

/**
 * Filtre de recherche instantanée dans la base Room du Dictionnaire local.
 */
enum class DictionarySearchScope(val label: String) {
    ALL("Tout"),
    DEFINITIONS("Définitions"),
    SYNONYMS("Synonymes"),
    ANTONYMS("Antonymes")
}
