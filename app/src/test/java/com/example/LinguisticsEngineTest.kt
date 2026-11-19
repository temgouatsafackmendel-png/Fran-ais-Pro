package com.example

import com.example.learning.dictation.DictationDiffEngine
import com.example.learning.spacedrepetition.Sm2Scheduler
import com.example.linguistics.analysis.HybridCorrectionEngine
import com.example.linguistics.conjugation.FrenchConjugatorEngine
import com.example.linguistics.model.CorrectionCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LinguisticsEngineTest {

    private val engine = HybridCorrectionEngine()
    private val conjugator = FrenchConjugatorEngine()

    @Test
    fun `corpus obligatoire - les 10 phrases correctes ne declenchent aucun faux positif`() {
        val mandatoryValidSentences = listOf(
            "Les étudiants apprennent le français.",
            "Les enfants jouent dans la cour.",
            "Elle a mangé.",
            "Ils sont partis.",
            "Je vais travailler.",
            "Il faut que tu viennes.",
            "Si j'avais su, je serais venu.",
            "Les fleurs que j'ai achetées.",
            "Leur maison est grande.",
            "Il travaille dans son entreprise."
        )

        for (sentence in mandatoryValidSentences) {
            val report = engine.analyze(sentence)
            val blockingIssues = report.issues.filter { it.category != CorrectionCategory.STYLE }
            assertTrue(
                "Faux positif détecté sur la phrase correcte « $sentence » : $blockingIssues",
                blockingIssues.isEmpty()
            )
        }
    }

    @Test
    fun `phrases ambigues et soutenues - aucun faux positif destructeur`() {
        val formalAndAmbiguous = listOf(
            "Une livre de farine suffit pour cette recette.",
            "Bien qu'il fasse froid, les étudiants apprennent le français.",
            "Les fleurs que j'ai achetées embellissent leur maison."
        )
        for (sentence in formalAndAmbiguous) {
            val report = engine.analyze(sentence)
            val safeApplied = engine.applySafeCorrections(sentence, report.issues)
            assertEquals("La phrase valide « $sentence » ne doit pas être altérée.", sentence, safeApplied)
        }
    }

    @Test
    fun `orthographe et accents - detection et correction fiables`() {
        val report = engine.analyze("Les etudiants apprennent le francais a l'ecole.")
        val fixed = engine.applySafeCorrections(report.originalText, report.issues)
        assertTrue(fixed.contains("étudiants"))
        assertTrue(fixed.contains("français"))
        assertTrue(fixed.contains("école"))
    }

    @Test
    fun `homophones grammaticaux - detection contextuelle`() {
        val r1 = engine.analyze("Ils son partis.")
        assertTrue(r1.issues.any { it.suggestion.equals("sont", ignoreCase = true) })

        val r2 = engine.analyze("Leurs maison est grande.")
        assertTrue(r2.issues.any { it.suggestion.equals("Leur", ignoreCase = true) })

        val r3 = engine.analyze("Il travaille dans sa entreprise.")
        assertTrue(r3.issues.any { it.suggestion.equals("son", ignoreCase = true) })

        val r4 = engine.analyze("Elle à mangé.")
        assertTrue(r4.issues.any { it.suggestion.equals("a", ignoreCase = true) })
    }

    @Test
    fun `accords sujet-verbe et participe passe`() {
        val r1 = engine.analyze("Les enfants joue dans la cour.")
        assertEquals("Les enfants jouent dans la cour.", engine.applySafeCorrections(r1.originalText, r1.issues))

        val r2 = engine.analyze("Elle a mangée.")
        assertEquals("Elle a mangé.", engine.applySafeCorrections(r2.originalText, r2.issues))

        val r3 = engine.analyze("Ils sont parti.")
        assertEquals("Ils sont partis.", engine.applySafeCorrections(r3.originalText, r3.issues))

        val r4 = engine.analyze("Les fleurs que j'ai acheté.")
        assertEquals("Les fleurs que j'ai achetées.", engine.applySafeCorrections(r4.originalText, r4.issues))
    }

    @Test
    fun `infinitif subjonctif et concordance si-imparfait`() {
        val r1 = engine.analyze("Je vais travaillé.")
        assertEquals("Je vais travailler.", engine.applySafeCorrections(r1.originalText, r1.issues))

        val r2 = engine.analyze("Il faut que tu viens.")
        assertEquals("Il faut que tu viennes.", engine.applySafeCorrections(r2.originalText, r2.issues))

        val r3 = engine.analyze("Si j'aurais su, je serais venu.")
        assertEquals("Si j'avais su, je serais venu.", engine.applySafeCorrections(r3.originalText, r3.issues))
    }

    @Test
    fun `syntaxe ponctuation et suggestions stylistiques non destructives`() {
        val r1 = engine.analyze("Malgré que il pleuve , au jour d'aujourd'hui du coup nous partons.")
        assertTrue(r1.issues.any { it.ruleCode == "SYN_MALGRE_QUE" })
        assertTrue(r1.issues.any { it.ruleCode == "PLEONASME_AUJOURDHUI" })
        assertTrue(r1.issues.any { it.ruleCode == "PONCT_ESPACE_AVANT" })

        val duCoup = r1.issues.firstOrNull { it.ruleCode == "STYLE_DU_COUP" }
        assertNotNull(duCoup)
        assertTrue("Les suggestions stylistiques ambiguës ne doivent pas être forcées silencieusement.", duCoup!!.isAmbiguous)
    }

    @Test
    fun `performance sur texte long hors ligne`() {
        val paragraph = "Les étudiants apprennent le français. Les enfants jouent dans la cour. Il travaille dans son entreprise. "
        val longText = paragraph.repeat(40) // > 600 mots
        val report = engine.analyze(longText)
        assertTrue("Analyse trop lente : ${report.analysisDurationMs} ms", report.analysisDurationMs < 2000L)
        assertTrue(report.wordCount >= 500)
    }

    @Test
    fun `conjugueur local et identification inverse`() {
        val venir = conjugator.conjugate("venir")
        assertNotNull(venir)
        assertEquals("être", venir!!.auxiliary)
        assertEquals("venu", venir.pastParticiple)
        assertTrue(venir.tenses.containsKey("Subjonctif présent"))
        assertEquals("viennes", venir.tenses["Subjonctif présent"]!![1].form)

        val identified = conjugator.identifyForm("viennes")
        assertTrue(identified.any { it.infinitive == "venir" && it.tense == "Subjonctif présent" })

        val regular = conjugator.conjugate("travailler")
        assertNotNull(regular)
        assertEquals("travaillent", regular!!.tenses["Présent"]!![5].form)
    }

    @Test
    fun `comparateur de dictee et algorithme SM-2`() {
        val perfect = DictationDiffEngine.evaluate("Elle a mangé.", "Elle a mangé.")
        assertEquals(20f, perfect.scoreOutOf20)
        assertEquals(0, perfect.mistakesCount)

        val withError = DictationDiffEngine.evaluate("Les fleurs que j'ai achetées.", "Les fleurs que j'ai acheté.")
        assertTrue(withError.mistakesCount >= 1)
        assertTrue(withError.scoreOutOf20 < 20f)

        val firstReview = Sm2Scheduler.schedule(null, "voc_01", quality = 4)
        assertEquals(1, firstReview.repetitions)
        assertEquals(1, firstReview.intervalDays)
        val secondReview = Sm2Scheduler.schedule(firstReview, "voc_01", quality = 5)
        assertEquals(2, secondReview.repetitions)
        assertEquals(6, secondReview.intervalDays)
        val lapse = Sm2Scheduler.schedule(secondReview, "voc_01", quality = 1)
        assertEquals(0, lapse.repetitions)
        assertEquals(1, lapse.lapsesCount)
        assertFalse(lapse.mastered)
    }
}
