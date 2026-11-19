package com.example.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val displayName: String = "Apprenant",
    val estimatedLevel: String = "A1",
    val globalScore: Int = 0,
    val streakDays: Int = 0,
    val lastActiveEpochDay: Long = 0L,
    val totalLearningMinutes: Int = 0,
    val totalCorrectionsCount: Int = 0,
    val onboardingCompleted: Boolean = false,
    val initialAssessmentCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "lessons",
    indices = [Index("level"), Index("theme")]
)
data class Lesson(
    @PrimaryKey val id: String,
    val level: String, // A1..C2
    val moduleTitle: String,
    val chapterOrder: Int,
    val theme: String, // Orthographe, Grammaire, Conjugaison, Syntaxe, Vocabulaire, Ponctuation, Expression écrite, Expression orale, Compréhension, Français professionnel
    val title: String,
    val summary: String,
    val ruleContent: String,
    val examplesJson: String, // JSON list of examples separated by ||
    val pitfallsContent: String,
    val estimatedMinutes: Int = 8
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "lesson_progress",
    foreignKeys = [
        ForeignKey(
            entity = Lesson::class,
            parentColumns = ["id"],
            childColumns = ["lessonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("lessonId", unique = true)]
)
data class LessonProgress(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lessonId: String,
    val isCompleted: Boolean = false,
    val quizScorePercent: Int = 0,
    val completedAt: Long = 0L,
    val reviewCount: Int = 0
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "exercises",
    indices = [Index("level"), Index("category"), Index("type"), Index("lessonId")]
)
data class Exercise(
    @PrimaryKey val id: String,
    val lessonId: String? = null,
    val level: String,
    val category: String,
    val type: String, // QCM, TEXTE_A_TROUS, VRAI_FAUX, ASSOCIATION, CORRECTION_PHRASE, CONJUGAISON, DICTEE, REECRITURE, COMPREHENSION, IDENTIFICATION_ERREUR
    val instruction: String,
    val promptText: String,
    val optionsPipe: String, // pipe-separated options for QCM/VraiFaux/Association
    val expectedAnswer: String,
    val acceptedAnswersPipe: String = "", // additional exact valid variants
    val explanation: String,
    val points: Int = 10,
    val difficulty: Int = 1 // 1..5
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "exercise_attempts",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exerciseId"), Index("category"), Index("attemptedAt")]
)
data class ExerciseAttempt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: String,
    val category: String,
    val userAnswer: String,
    val isCorrect: Boolean,
    val scoreEarned: Int,
    val attemptedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "dictionary_entries",
    indices = [Index("normalizedWord"), Index("partOfSpeech"), Index("word")]
)
data class DictionaryEntry(
    @PrimaryKey val id: String,
    val word: String,
    val normalizedWord: String,
    val definition: String,
    val partOfSpeech: String,
    val gender: String, // Masculin, Féminin, Invariable, Variable
    val pluralForm: String,
    val pronunciation: String,
    val synonymsPipe: String,
    val antonymsPipe: String,
    val examplesPipe: String,
    val expressionsPipe: String,
    val level: String = "A1",
    val lastViewedAt: Long = 0L
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "recent_dictionary_searches",
    indices = [Index("searchedAt")]
)
data class RecentDictionarySearch(
    @PrimaryKey val normalizedQuery: String,
    val queryText: String,
    val searchedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "verbs",
    indices = [Index("normalizedInfinitive")]
)
data class Verb(
    @PrimaryKey val infinitive: String,
    val normalizedInfinitive: String,
    val groupNumber: Int, // 1, 2, 3
    val auxiliary: String, // avoir, être
    val presentParticiple: String,
    val pastParticiple: String,
    val isIrregular: Boolean,
    val modelInfo: String
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "conjugations",
    foreignKeys = [
        ForeignKey(
            entity = Verb::class,
            parentColumns = ["infinitive"],
            childColumns = ["verbInfinitive"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("verbInfinitive"), Index("normalizedForm")]
)
data class Conjugation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val verbInfinitive: String,
    val mood: String,
    val tense: String,
    val personIndex: Int, // 0..5
    val pronoun: String,
    val conjugatedForm: String,
    val normalizedForm: String
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "correction_history",
    indices = [Index("createdAt")]
)
data class CorrectionHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originalText: String,
    val correctedText: String,
    val errorsFoundCount: Int,
    val errorsAppliedCount: Int,
    val sourceModule: String = "CORRECTEUR", // CORRECTEUR, REDACTION
    val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "correction_errors",
    foreignKeys = [
        ForeignKey(
            entity = CorrectionHistory::class,
            parentColumns = ["id"],
            childColumns = ["historyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("historyId"), Index("category")]
)
data class CorrectionError(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val historyId: Long,
    val originalFragment: String,
    val suggestion: String,
    val category: String, // Orthographe, Grammaire, Conjugaison, Syntaxe, Style, Ponctuation
    val explanation: String,
    val example: String,
    val confidence: Float,
    val startIndex: Int,
    val endIndex: Int,
    val wasAccepted: Boolean = false
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "vocabulary_words",
    indices = [Index("level"), Index("theme")]
)
data class VocabularyWord(
    @PrimaryKey val id: String,
    val word: String,
    val level: String,
    val theme: String,
    val definition: String,
    val synonymsPipe: String,
    val antonymsPipe: String,
    val exampleSentence: String,
    val quizQuestion: String,
    val quizOptionsPipe: String,
    val quizAnswer: String
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "vocabulary_progress",
    foreignKeys = [
        ForeignKey(
            entity = VocabularyWord::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("wordId", unique = true), Index("nextReviewAt")]
)
data class VocabularyProgress(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val wordId: String,
    val repetitions: Int = 0,
    val intervalDays: Int = 1,
    val easeFactor: Float = 2.5f,
    val nextReviewAt: Long = 0L,
    val lastQuality: Int = 0,
    val lapsesCount: Int = 0,
    val mastered: Boolean = false
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "dictations",
    indices = [Index("level"), Index("theme")]
)
data class Dictation(
    @PrimaryKey val id: String,
    val title: String,
    val level: String,
    val theme: String,
    val referenceText: String,
    val keyRulesPipe: String,
    val wordCount: Int
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "dictation_attempts",
    foreignKeys = [
        ForeignKey(
            entity = Dictation::class,
            parentColumns = ["id"],
            childColumns = ["dictationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dictationId"), Index("attemptedAt")]
)
data class DictationAttempt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dictationId: String,
    val userText: String,
    val scoreOutOf20: Float,
    val mistakesCount: Int,
    val summaryFeedback: String,
    val attemptedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "daily_goals")
data class DailyGoal(
    @PrimaryKey val epochDay: Long,
    val targetMinutes: Int = 15,
    val completedMinutes: Int = 0,
    val exercisesDone: Int = 0,
    val correctionsDone: Int = 0,
    val goalReached: Boolean = false
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0L
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "learning_statistics",
    indices = [Index("skillCategory", unique = true)]
)
data class LearningStatistics(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val skillCategory: String,
    val totalAttempts: Int = 0,
    val successCount: Int = 0,
    val errorCount: Int = 0,
    val masteryPercent: Int = 0,
    val lastUpdatedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "favorites",
    indices = [Index(value = ["itemType", "itemId"], unique = true)]
)
data class Favorite(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemType: String, // WORD, LESSON, VERB, VOCAB
    val itemId: String,
    val title: String,
    val subtitle: String,
    val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    val dailyGoalMinutes: Int = 15,
    val ttsSpeechRate: Float = 0.92f,
    val autoAnalyzeCorrection: Boolean = false,
    val includeHistoryInBackup: Boolean = true,
    val dailyReminderEnabled: Boolean = false,
    val reminderHour: Int = 19
)
