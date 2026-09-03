package com.example.data

data class VocabWord(
    val id: Int,
    val word: String,
    val pronunciation: String,
    val englishMeaning: String,
    val bengaliMeaning: String,
    val exampleSentence: String,
    val synonyms: String,
    val antonyms: String,
    val isFavorite: Boolean = false,
    val isMastered: Boolean = false,
    val reviewCount: Int = 0,
    val lastReviewedTime: Long = 0L
)

enum class WordFilter {
    ALL,
    FAVORITES,
    MASTERED,
    LEARNING
}

data class UserProgress(
    val unlockedDay: Int = 1,
    val dailyWordCount: Int = 20, // 15, 20, 25, 30
    val currentStreak: Int = 1,
    val lastActiveDate: String = "",
    val isRestDayActive: Boolean = false,
    val restDayDate: String = "",
    val themeMode: String = "SYSTEM" // SYSTEM, LIGHT, DARK
)

data class DayProgress(
    val dayNumber: Int,
    val isCompleted: Boolean = false,
    val bestScore: Int = 0,
    val passedDate: Long = 0L,
    val attemptsCount: Int = 0
)

data class MilestoneProgress(
    val milestoneIndex: Int, // 1 for Days 1-5, 2 for Days 6-10, etc.
    val startDay: Int,
    val endDay: Int,
    val isCompleted: Boolean = false,
    val bestScore: Int = 0,
    val passedDate: Long = 0L,
    val attemptsCount: Int = 0
)

data class MistakeWord(
    val word: VocabWord,
    val errorCount: Int,
    val lastErrorTime: Long,
    val isResolved: Boolean = false
)

enum class TestType {
    DAILY,
    MILESTONE,
    MISTAKE_BANK
}

data class TestAttempt(
    val id: Long = 0,
    val testType: TestType,
    val targetDay: Int = 0,
    val totalQuestions: Int,
    val correctCount: Int,
    val scorePercent: Int,
    val passed: Boolean,
    val timestamp: Long
)

enum class QuestionType {
    WORD_MEANING,
    SENTENCE_COMPLETION,
    SYNONYM,
    ANTONYM
}

data class TestQuestion(
    val id: String,
    val type: QuestionType,
    val targetWord: VocabWord,
    val prompt: String,
    val clozeSentence: String? = null,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
)
