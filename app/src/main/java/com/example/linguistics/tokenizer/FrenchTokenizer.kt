package com.example.linguistics.tokenizer

import com.example.linguistics.model.FrenchNormalizer
import com.example.linguistics.model.FrenchToken
import com.example.linguistics.model.TokenType

class FrenchTokenizer {

    private val tokenRegex = Regex(
        """([A-Za-zÀ-ÖØ-öø-ÿœŒæÆ]+['’])|([A-Za-zÀ-ÖØ-öø-ÿœŒæÆ]+(?:-[A-Za-zÀ-ÖØ-öø-ÿœŒæÆ]+)*)|(\d+(?:[.,]\d+)?)|([.,;:!?()«»"…—–])"""
    )

    fun tokenize(text: String): List<FrenchToken> {
        val tokens = mutableListOf<FrenchToken>()
        var sentenceIdx = 0
        var indexInSentence = 0

        for (match in tokenRegex.findAll(text)) {
            val raw = match.value
            val start = match.range.first
            val end = match.range.last + 1

            val type = when {
                match.groups[1] != null -> TokenType.ELISION_PREFIX
                match.groups[2] != null -> TokenType.WORD
                match.groups[3] != null -> TokenType.NUMBER
                else -> TokenType.PUNCTUATION
            }

            val lower = raw.lowercase().replace('’', '\'')
            val normalized = FrenchNormalizer.stripAccents(lower)

            tokens += FrenchToken(
                text = raw,
                lower = lower,
                normalized = normalized,
                start = start,
                end = end,
                type = type,
                sentenceIndex = sentenceIdx,
                indexInSentence = indexInSentence
            )
            indexInSentence++

            if (type == TokenType.PUNCTUATION && raw in setOf(".", "!", "?", "…")) {
                sentenceIdx++
                indexInSentence = 0
            }
        }
        return tokens
    }
}
