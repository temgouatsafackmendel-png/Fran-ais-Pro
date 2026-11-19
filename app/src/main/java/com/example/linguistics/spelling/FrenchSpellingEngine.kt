package com.example.linguistics.spelling

import com.example.linguistics.lexicon.FrenchLexicon
import com.example.linguistics.model.CorrectionCategory
import com.example.linguistics.model.CorrectionIssue
import com.example.linguistics.model.CorrectionRuleModule
import com.example.linguistics.model.FrenchNormalizer
import com.example.linguistics.model.FrenchToken
import com.example.linguistics.model.TokenType

class FrenchSpellingEngine(
    private val lexicon: FrenchLexicon
) : CorrectionRuleModule {

    override val moduleId: String = "spelling_engine_v1"

    // Mots courts ambigus gérés exclusivement par les règles contextuelles d'homophones
    private val homophoneManaged = setOf(
        "a", "à", "ou", "où", "et", "est", "son", "sont", "on", "ont",
        "ce", "se", "ces", "ses", "sa", "ça", "ca", "leur", "leurs", "la", "là", "du", "dû", "sur", "sûr"
    )

    override fun analyze(text: String, tokens: List<FrenchToken>): List<CorrectionIssue> {
        val issues = mutableListOf<CorrectionIssue>()

        for (token in tokens) {
            if (token.type != TokenType.WORD) continue
            val lower = token.lower
            if (lower.length < 2) continue
            if (lower in homophoneManaged) continue

            // Ignorer les acronymes tout en majuscules (ex: CECRL, CV, PDF, ONG)
            if (token.text.length in 2..6 && token.text.all { it.isUpperCase() }) continue

            // 1. Table dédiée des fautes fréquentes d'orthographe / accents / doubles consonnes
            val knownError = lexicon.commonMisspellings[lower]
            if (knownError != null) {
                val (replacement, reason) = knownError
                val cased = FrenchNormalizer.preserveCase(token.text, replacement)
                if (cased != token.text) {
                    issues += CorrectionIssue(
                        id = "spell_${token.start}_${token.end}",
                        originalFragment = token.text,
                        suggestion = cased,
                        category = CorrectionCategory.ORTHOGRAPHE,
                        ruleCode = "ORTH_FREQUENT",
                        explanation = reason,
                        example = "Forme correcte : « $cased ».",
                        confidence = 0.96f,
                        startIndex = token.start,
                        endIndex = token.end
                    )
                }
                continue
            }

            // Si le mot est connu dans le lexique local, aucune erreur orthographique
            if (lexicon.isKnownWord(lower)) continue

            // Ignorer les noms propres probables en milieu de phrase (majuscule hors début de phrase)
            if (token.indexInSentence > 0 && token.text.first().isUpperCase()) continue

            // 2. Accent oublié sur un mot du lexique
            val accentMatch = lexicon.findAccentMatch(lower)
            if (accentMatch != null && accentMatch != lower) {
                val cased = FrenchNormalizer.preserveCase(token.text, accentMatch)
                issues += CorrectionIssue(
                    id = "acc_${token.start}_${token.end}",
                    originalFragment = token.text,
                    suggestion = cased,
                    category = CorrectionCategory.ORTHOGRAPHE,
                    ruleCode = "ORTH_ACCENT",
                    explanation = "Un accent ou une cédille semble manquer dans le mot « ${token.text} ».",
                    example = "Écrivez : « $cased ».",
                    confidence = 0.93f,
                    startIndex = token.start,
                    endIndex = token.end
                )
                continue
            }

            // 3. Faute de frappe manifeste (distance Damerau-Levenshtein <= 1 sur mots >= 5 lettres)
            // Limité à distance 1 pour éviter les faux positifs sur des mots rares absents du lexique embarqué
            if (lower.length >= 5) {
                val suggestions = lexicon.findClosestSuggestions(lower, maxDistance = 1, limit = 3)
                if (suggestions.isNotEmpty() && !looksLikeRegularFrenchDerivation(lower)) {
                    val primary = FrenchNormalizer.preserveCase(token.text, suggestions.first())
                    issues += CorrectionIssue(
                        id = "typo_${token.start}_${token.end}",
                        originalFragment = token.text,
                        suggestion = primary,
                        alternativeSuggestions = suggestions.drop(1).map {
                            FrenchNormalizer.preserveCase(token.text, it)
                        },
                        category = CorrectionCategory.ORTHOGRAPHE,
                        ruleCode = "ORTH_PROXIMITE",
                        explanation = "Orthographe inhabituelle détectée pour « ${token.text} ». Vérifiez s'il s'agit d'une faute de frappe.",
                        example = "Suggestion principale : « $primary ».",
                        confidence = 0.78f,
                        startIndex = token.start,
                        endIndex = token.end,
                        isAmbiguous = suggestions.size > 1
                    )
                }
            }
        }
        return issues
    }

    private fun looksLikeRegularFrenchDerivation(word: String): Boolean {
        val suffixes = listOf(
            "ment", "tion", "sion", "ique", "isme", "iste", "able", "ible", "eur", "euse",
            "eurs", "euses", "age", "ages", "ence", "ance", "ité", "ités", "aire", "aires",
            "er", "ir", "ons", "ez", "ais", "ait", "aient", "era", "eront", "ées", "és", "ant"
        )
        return suffixes.any { word.endsWith(it) && word.length > it.length + 3 }
    }
}
