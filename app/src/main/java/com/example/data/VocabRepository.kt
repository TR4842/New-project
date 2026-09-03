package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class VocabRepository(context: Context) {
    private val dbHelper = VocabDatabaseHelper(context.applicationContext)

    private val _wordsList = MutableStateFlow<List<VocabWord>>(emptyList())
    val wordsList: StateFlow<List<VocabWord>> = _wordsList.asStateFlow()

    private val _stats = MutableStateFlow(Stats(total = 850, mastered = 0, favorites = 0, reviewed = 0))
    val stats: StateFlow<Stats> = _stats.asStateFlow()

    private val _userProgress = MutableStateFlow(UserProgress())
    val userProgress: StateFlow<UserProgress> = _userProgress.asStateFlow()

    private val _dayProgressMap = MutableStateFlow<Map<Int, DayProgress>>(emptyMap())
    val dayProgressMap: StateFlow<Map<Int, DayProgress>> = _dayProgressMap.asStateFlow()

    private val _milestoneProgressMap = MutableStateFlow<Map<Int, MilestoneProgress>>(emptyMap())
    val milestoneProgressMap: StateFlow<Map<Int, MilestoneProgress>> = _milestoneProgressMap.asStateFlow()

    private val _mistakeWords = MutableStateFlow<List<MistakeWord>>(emptyList())
    val mistakeWords: StateFlow<List<MistakeWord>> = _mistakeWords.asStateFlow()

    private val _testStats = MutableStateFlow(Pair(0, 0f)) // (totalAttempts, lifetimeAccuracy)
    val testStats: StateFlow<Pair<Int, Float>> = _testStats.asStateFlow()

    suspend fun refreshAll() {
        withContext(Dispatchers.IO) {
            _userProgress.value = dbHelper.getUserProgress()
            _dayProgressMap.value = dbHelper.getAllDayProgress()
            _milestoneProgressMap.value = dbHelper.getAllMilestoneProgress()
            _mistakeWords.value = dbHelper.getMistakeWords()
            _testStats.value = dbHelper.getTestStatsSummary()
            _stats.value = dbHelper.getStats()
        }
    }

    suspend fun refreshWords(query: String = "", filter: WordFilter = WordFilter.ALL, letter: String? = null) {
        withContext(Dispatchers.IO) {
            val words = dbHelper.searchWords(query, filter, letter)
            _wordsList.value = words
            _stats.value = dbHelper.getStats()
        }
    }

    suspend fun toggleFavorite(id: Int, isFav: Boolean) {
        withContext(Dispatchers.IO) {
            dbHelper.toggleFavorite(id, isFav)
            _wordsList.value = _wordsList.value.map {
                if (it.id == id) it.copy(isFavorite = isFav) else it
            }
            _stats.value = dbHelper.getStats()
        }
    }

    suspend fun toggleMastered(id: Int, isMastered: Boolean) {
        withContext(Dispatchers.IO) {
            dbHelper.toggleMastered(id, isMastered)
            _wordsList.value = _wordsList.value.map {
                if (it.id == id) it.copy(isMastered = isMastered) else it
            }
            _stats.value = dbHelper.getStats()
        }
    }

    suspend fun recordReview(id: Int) {
        withContext(Dispatchers.IO) {
            dbHelper.recordReview(id)
            _stats.value = dbHelper.getStats()
        }
    }

    suspend fun setDailyWordCount(count: Int) {
        withContext(Dispatchers.IO) {
            dbHelper.updateDailyWordCount(count)
            _userProgress.value = dbHelper.getUserProgress()
        }
    }

    suspend fun setRestDayActive(active: Boolean) {
        withContext(Dispatchers.IO) {
            dbHelper.setRestDayActive(active)
            _userProgress.value = dbHelper.getUserProgress()
        }
    }

    suspend fun setThemeMode(mode: String) {
        withContext(Dispatchers.IO) {
            dbHelper.setThemeMode(mode)
            _userProgress.value = dbHelper.getUserProgress()
        }
    }

    suspend fun updateStreakOnActivity() {
        withContext(Dispatchers.IO) {
            dbHelper.updateStreakOnActivity()
            _userProgress.value = dbHelper.getUserProgress()
        }
    }

    suspend fun getWordsForDay(day: Int, dailyWordCount: Int): List<VocabWord> {
        return withContext(Dispatchers.IO) {
            dbHelper.getWordsForDay(day, dailyWordCount)
        }
    }

    suspend fun getWordsForMilestone(milestoneIndex: Int, dailyWordCount: Int): List<VocabWord> {
        return withContext(Dispatchers.IO) {
            dbHelper.getWordsForMilestone(milestoneIndex, dailyWordCount)
        }
    }

    suspend fun getRandomWords(count: Int, excludeId: Int? = null): List<VocabWord> {
        return withContext(Dispatchers.IO) {
            dbHelper.getRandomWords(count, excludeId)
        }
    }

    suspend fun recordDayTestAttempt(day: Int, correct: Int, total: Int, passed: Boolean): Boolean {
        return withContext(Dispatchers.IO) {
            val newlyUnlocked = dbHelper.recordDayTestAttempt(day, correct, total, passed)
            refreshAll()
            newlyUnlocked
        }
    }

    suspend fun recordMilestoneTestAttempt(milestoneIndex: Int, correct: Int, total: Int, passed: Boolean): Boolean {
        return withContext(Dispatchers.IO) {
            val newlyUnlocked = dbHelper.recordMilestoneTestAttempt(milestoneIndex, correct, total, passed)
            refreshAll()
            newlyUnlocked
        }
    }

    suspend fun recordMistakeBankTestAttempt(correct: Int, total: Int, passed: Boolean) {
        withContext(Dispatchers.IO) {
            dbHelper.recordMistakeBankTestAttempt(correct, total, passed)
            refreshAll()
        }
    }

    suspend fun recordMistake(wordId: Int) {
        withContext(Dispatchers.IO) {
            dbHelper.recordMistake(wordId)
            _mistakeWords.value = dbHelper.getMistakeWords()
        }
    }

    suspend fun resolveMistake(wordId: Int) {
        withContext(Dispatchers.IO) {
            dbHelper.resolveMistake(wordId)
            _mistakeWords.value = dbHelper.getMistakeWords()
        }
    }

    suspend fun removeMistake(wordId: Int) {
        withContext(Dispatchers.IO) {
            dbHelper.removeMistake(wordId)
            _mistakeWords.value = dbHelper.getMistakeWords()
        }
    }

    fun calculateTotalDays(dailyWordCount: Int): Int {
        return dbHelper.calculateTotalDays(dailyWordCount)
    }

    fun getTotalWordCount(): Int {
        return dbHelper.getTotalWordCount()
    }

    suspend fun resetAllProgress() {
        withContext(Dispatchers.IO) {
            dbHelper.resetAllProgress()
            refreshAll()
            refreshWords()
        }
    }
}
