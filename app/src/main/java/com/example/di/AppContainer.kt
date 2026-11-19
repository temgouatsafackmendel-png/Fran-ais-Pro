package com.example.di

import android.content.Context
import com.example.core.audio.LocalSpeechManager
import com.example.core.backup.BackupManager
import com.example.core.database.DatabaseSeeder
import com.example.core.database.FrancaisProDatabase
import com.example.core.datastore.PreferencesRepository
import com.example.data.FrancaisProRepository
import com.example.linguistics.analysis.HybridCorrectionEngine
import com.example.linguistics.conjugation.FrenchConjugatorEngine

/**
 * Conteneur d'injection de dépendances par constructeur (Clean Architecture).
 * Aucune dépendance réseau, aucun SDK tiers payant.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: FrancaisProDatabase by lazy { FrancaisProDatabase.getInstance(appContext) }
    val preferencesRepository: PreferencesRepository by lazy { PreferencesRepository(appContext) }
    val correctionEngine: HybridCorrectionEngine by lazy { HybridCorrectionEngine() }
    val conjugatorEngine: FrenchConjugatorEngine by lazy { FrenchConjugatorEngine() }
    val backupManager: BackupManager by lazy { BackupManager(database) }
    val seeder: DatabaseSeeder by lazy { DatabaseSeeder(database, conjugatorEngine) }
    val speechManager: LocalSpeechManager by lazy { LocalSpeechManager(appContext) }

    val repository: FrancaisProRepository by lazy {
        FrancaisProRepository(
            db = database,
            prefs = preferencesRepository,
            correctionEngine = correctionEngine,
            conjugatorEngine = conjugatorEngine,
            backupManager = backupManager
        )
    }
}
