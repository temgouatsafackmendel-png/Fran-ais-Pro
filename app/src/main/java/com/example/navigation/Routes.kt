package com.example.navigation

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val ASSESSMENT = "assessment"

    // Conteneur animé des 5 onglets principaux
    const val MAIN_TABS = "main_tabs"

    // 5 onglets principaux
    const val DASHBOARD = "dashboard"
    const val CORRECTOR = "corrector"
    const val ACADEMY = "academy"
    const val EXERCISES = "exercises"
    const val PROFILE = "profile"

    // Sous-écrans (total 24 écrans)
    const val CORRECTION_RESULT = "correction_result"
    const val LESSON_LIST = "lesson_list/{level}"
    const val LESSON_DETAIL = "lesson_detail/{lessonId}"
    const val LESSON_QUIZ = "lesson_quiz/{lessonId}"
    const val DICTIONARY = "dictionary"
    const val WORD_DETAIL = "word_detail/{wordId}"
    const val CONJUGATION = "conjugation"
    const val DICTATION = "dictation"
    const val DICTATION_RESULT = "dictation_result"
    const val VOCABULARY = "vocabulary"
    const val WRITING = "writing"
    const val SPEAKING = "speaking"
    const val STATISTICS = "statistics"
    const val HISTORY = "history"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"
    const val BACKUP = "backup"
    const val USER_GUIDE = "user_guide"

    fun lessonList(level: String) = "lesson_list/$level"
    fun lessonDetail(lessonId: String) = "lesson_detail/$lessonId"
    fun lessonQuiz(lessonId: String) = "lesson_quiz/$lessonId"
    fun wordDetail(wordId: String) = "word_detail/$wordId"

    val topLevelTabs = setOf(DASHBOARD, CORRECTOR, ACADEMY, EXERCISES, PROFILE)
}
