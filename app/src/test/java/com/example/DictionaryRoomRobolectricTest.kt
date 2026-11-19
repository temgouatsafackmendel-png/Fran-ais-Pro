package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.backup.BackupManager
import com.example.core.database.DatabaseSeeder
import com.example.core.database.FrancaisProDatabase
import com.example.core.datastore.PreferencesRepository
import com.example.data.DictionarySearchScope
import com.example.data.FrancaisProRepository
import com.example.linguistics.analysis.HybridCorrectionEngine
import com.example.linguistics.conjugation.FrenchConjugatorEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DictionaryRoomRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: FrancaisProDatabase
    private lateinit var repository: FrancaisProRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, FrancaisProDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val conjugator = FrenchConjugatorEngine()
        repository = FrancaisProRepository(
            db = db,
            prefs = PreferencesRepository(context),
            correctionEngine = HybridCorrectionEngine(),
            conjugatorEngine = conjugator,
            backupManager = BackupManager(db)
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `room dictionary searches instantly across words definitions synonyms and antonyms offline`() = runTest {
        DatabaseSeeder(db, FrenchConjugatorEngine()).seedIfNeeded()

        val total = repository.dictionaryTotalCount.first()
        assertEquals(48, total)

        // 1. Recherche insensible aux accents sur le mot-vedette
        val byAccentlessWord = repository.searchDictionary("eloquence").first()
        assertTrue(byAccentlessWord.isNotEmpty())
        assertEquals("Éloquence", byAccentlessWord.first().word)

        // 2. Recherche dans les définitions via Room SQLite
        val byDefinition = repository.searchDictionary(
            query = "redondants",
            scope = DictionarySearchScope.DEFINITIONS
        ).first()
        assertTrue(byDefinition.any { it.word == "Pléonasme" })

        // 3. Recherche directe dans les synonymes via Room SQLite
        val bySynonym = repository.searchDictionary(
            query = "sagace",
            scope = DictionarySearchScope.SYNONYMS
        ).first()
        assertTrue(bySynonym.any { it.word == "Perspicace" })

        // 4. Recherche directe dans les antonymes via Room SQLite
        val byAntonym = repository.searchDictionary(
            query = "prolixe",
            scope = DictionarySearchScope.ANTONYMS
        ).first()
        assertTrue(byAntonym.any { it.word == "Concis" })

        // 5. Filtre combiné par catégorie grammaticale (Verbe)
        val verbsOnly = repository.searchDictionary(
            query = "",
            category = "Verbe",
            scope = DictionarySearchScope.ALL
        ).first()
        assertTrue(verbsOnly.isNotEmpty())
        assertTrue(verbsOnly.all { it.partOfSpeech == "Verbe" })
    }

    @Test
    fun `room dictionary supports fuzzy suggestions recent tracking related lookup and custom entries`() = runTest {
        DatabaseSeeder(db, FrenchConjugatorEngine()).seedIfNeeded()

        // Suggestions floues hors ligne sur faute de frappe
        val fuzzy = repository.fuzzyDictionarySuggestions("sintaxe")
        assertTrue(fuzzy.any { it.word == "Syntaxe" })

        // Suivi des consultations récentes dans Room
        val entry = repository.getDictionaryEntry("dic_05")
        assertNotNull(entry)
        assertEquals("Concis", entry?.word)
        val recents = repository.recentDictionaryEntries.first()
        assertTrue(recents.any { it.id == "dic_05" })

        // Résolution directe d'un synonyme présent dans Room
        val linked = repository.findDictionaryEntryByWord("harmonie")
        assertNotNull(linked)
        assertEquals("Harmonie", linked?.word)

        // Ajout et suppression d'une entrée personnelle dans Room
        val custom = repository.addCustomDictionaryEntry(
            word = "Éphémère",
            partOfSpeech = "Adjectif",
            definition = "Qui ne dure qu'un temps très court.",
            synonyms = listOf("passager", "fugace"),
            antonyms = listOf("durable", "éternel"),
            example = "Une gloire éphémère.",
            level = "B2"
        )
        val foundBySyn = repository.searchDictionary("fugace", scope = DictionarySearchScope.SYNONYMS).first()
        assertTrue(foundBySyn.any { it.id == custom.id })
        val foundByAnt = repository.searchDictionary("éternel", scope = DictionarySearchScope.ANTONYMS).first()
        assertTrue(foundByAnt.any { it.id == custom.id })

        repository.deleteCustomDictionaryEntry(custom.id)
        assertNull(db.dictionaryDao().getById(custom.id))
    }
}
