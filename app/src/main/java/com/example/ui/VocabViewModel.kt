package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DayProgress
import com.example.data.MilestoneProgress
import com.example.data.MistakeWord
import com.example.data.Stats
import com.example.data.TestQuestion
import com.example.data.TestType
import com.example.data.UserProgress
import com.example.data.VocabRepository
import com.example.data.VocabWord
import com.example.data.WordFilter
import com.example.ui.theme.AppThemeMode
import com.example.util.Motivations
import com.example.util.TestGenerator
import com.example.util.TtsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VocabViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VocabRepository(application)
    private val ttsManager = TtsManager(application)

    // User Progress & Settings
    val userProgress: StateFlow<UserProgress> = repository.userProgress
    val dayProgressMap: StateFlow<Map<Int, DayProgress>> = repository.dayProgressMap
    val milestoneProgressMap: StateFlow<Map<Int, MilestoneProgress>> = repository.milestoneProgressMap
    val mistakeWords: StateFlow<List<MistakeWord>> = repository.mistakeWords
    val testStats: StateFlow<Pair<Int, Float>> = repository.testStats
    val overallStats: StateFlow<Stats> = repository.stats

    // Current Theme Mode
    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    // Daily Reading Section
    private val _selectedReadingDay = MutableStateFlow(1)
    val selectedReadingDay: StateFlow<Int> = _selectedReadingDay.asStateFlow()

    private val _readingWords = MutableStateFlow<List<VocabWord>>(emptyList())
    val readingWords: StateFlow<List<VocabWord>> = _readingWords.asStateFlow()

    private val _readingWordIndex = MutableStateFlow(0)
    val readingWordIndex: StateFlow<Int> = _readingWordIndex.asStateFlow()

    // Test / Challenge Session
    private val _activeTestType = MutableStateFlow(TestType.DAILY)
    val activeTestType: StateFlow<TestType> = _activeTestType.asStateFlow()

    private val _activeTestDay = MutableStateFlow(1) // Day num for Daily, Milestone index for Milestone
    val activeTestDay: StateFlow<Int> = _activeTestDay.asStateFlow()

    private val _testQuestions = MutableStateFlow<List<TestQuestion>>(emptyList())
    val testQuestions: StateFlow<List<TestQuestion>> = _testQuestions.asStateFlow()

    private val _testQuestionIndex = MutableStateFlow(0)
    val testQuestionIndex: StateFlow<Int> = _testQuestionIndex.asStateFlow()

    private val _selectedOption = MutableStateFlow<String?>(null)
    val selectedOption: StateFlow<String?> = _selectedOption.asStateFlow()

    private val _isAnswerSubmitted = MutableStateFlow(false)
    val isAnswerSubmitted: StateFlow<Boolean> = _isAnswerSubmitted.asStateFlow()

    private val _isAnswerCorrect = MutableStateFlow(false)
    val isAnswerCorrect: StateFlow<Boolean> = _isAnswerCorrect.asStateFlow()

    private val _testCorrectCount = MutableStateFlow(0)
    val testCorrectCount: StateFlow<Int> = _testCorrectCount.asStateFlow()

    private val _isTestCompleted = MutableStateFlow(false)
    val isTestCompleted: StateFlow<Boolean> = _isTestCompleted.asStateFlow()

    private val _testPassed = MutableStateFlow(false)
    val testPassed: StateFlow<Boolean> = _testPassed.asStateFlow()

    private val _motivationMessage = MutableStateFlow("")
    val motivationMessage: StateFlow<String> = _motivationMessage.asStateFlow()

    private val _newlyUnlockedDay = MutableStateFlow(false)
    val newlyUnlockedDay: StateFlow<Boolean> = _newlyUnlockedDay.asStateFlow()

    // Dictionary / Lexicon Browser
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentFilter = MutableStateFlow(WordFilter.ALL)
    val currentFilter: StateFlow<WordFilter> = _currentFilter.asStateFlow()

    private val _selectedLetter = MutableStateFlow<String?>(null)
    val selectedLetter: StateFlow<String?> = _selectedLetter.asStateFlow()

    val wordsList: StateFlow<List<VocabWord>> = repository.wordsList

    private val _selectedWordDetail = MutableStateFlow<VocabWord?>(null)
    val selectedWordDetail: StateFlow<VocabWord?> = _selectedWordDetail.asStateFlow()

    // Flashcards State
    private val _flashcards = MutableStateFlow<List<VocabWord>>(emptyList())
    val flashcards: StateFlow<List<VocabWord>> = _flashcards.asStateFlow()

    private val _flashcardIndex = MutableStateFlow(0)
    val flashcardIndex: StateFlow<Int> = _flashcardIndex.asStateFlow()

    private val _isCardFlipped = MutableStateFlow(false)
    val isCardFlipped: StateFlow<Boolean> = _isCardFlipped.asStateFlow()

    init {
        viewModelScope.launch {
            repository.refreshAll()
            repository.refreshWords()
            val up = repository.userProgress.value
            _selectedReadingDay.value = up.unlockedDay
            loadReadingWords(up.unlockedDay)
            _themeMode.value = when (up.themeMode) {
                "LIGHT" -> AppThemeMode.LIGHT
                "DARK" -> AppThemeMode.DARK
                else -> AppThemeMode.SYSTEM
            }
            loadFlashcards()
        }
    }

    // --- Progression & Settings ---

    fun setDailyWordCount(count: Int) {
        viewModelScope.launch {
            repository.setDailyWordCount(count)
            loadReadingWords(_selectedReadingDay.value)
        }
    }

    fun setRestDayActive(active: Boolean) {
        viewModelScope.launch {
            repository.setRestDayActive(active)
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        viewModelScope.launch {
            repository.setThemeMode(mode.name)
        }
    }

    fun getTotalDays(): Int {
        val perDay = userProgress.value.dailyWordCount
        return repository.calculateTotalDays(perDay)
    }

    fun isDayUnlocked(day: Int): Boolean {
        return day <= userProgress.value.unlockedDay
    }

    fun isMilestoneUnlocked(milestoneIndex: Int): Boolean {
        // Milestone X spans Day ((X-1)*5 + 1) to (X*5).
        // It is unlocked once Day (X*5) is completed with >= 80%.
        val endDay = milestoneIndex * 5
        val dayProg = dayProgressMap.value[endDay]
        return dayProg?.isCompleted == true
    }

    // --- Daily Reading Section ---

    fun selectReadingDay(day: Int) {
        _selectedReadingDay.value = day
        _readingWordIndex.value = 0
        loadReadingWords(day)
    }

    private fun loadReadingWords(day: Int) {
        viewModelScope.launch {
            val perDay = userProgress.value.dailyWordCount
            val words = repository.getWordsForDay(day, perDay)
            _readingWords.value = words
            _readingWordIndex.value = 0
        }
    }

    fun nextReadingWord() {
        val list = _readingWords.value
        if (list.isNotEmpty()) {
            _readingWordIndex.value = (_readingWordIndex.value + 1) % list.size
        }
    }

    fun prevReadingWord() {
        val list = _readingWords.value
        if (list.isNotEmpty()) {
            val prev = _readingWordIndex.value - 1
            _readingWordIndex.value = if (prev < 0) list.size - 1 else prev
        }
    }

    fun selectReadingWordIndex(index: Int) {
        if (index in _readingWords.value.indices) {
            _readingWordIndex.value = index
        }
    }

    // --- Test / Challenge Engine ---

    fun startDailyTest(day: Int) {
        viewModelScope.launch {
            _activeTestType.value = TestType.DAILY
            _activeTestDay.value = day
            val perDay = userProgress.value.dailyWordCount
            val targetWords = repository.getWordsForDay(day, perDay)
            val allWords = repository.getRandomWords(30)
            val questions = TestGenerator.generateTest(targetWords, allWords, maxQuestions = 10)

            _testQuestions.value = questions
            _testQuestionIndex.value = 0
            _selectedOption.value = null
            _isAnswerSubmitted.value = false
            _isAnswerCorrect.value = false
            _testCorrectCount.value = 0
            _isTestCompleted.value = false
            _testPassed.value = false
            _newlyUnlockedDay.value = false
            _motivationMessage.value = ""
        }
    }

    fun startMilestoneTest(milestoneIndex: Int) {
        viewModelScope.launch {
            _activeTestType.value = TestType.MILESTONE
            _activeTestDay.value = milestoneIndex
            val perDay = userProgress.value.dailyWordCount
            val targetWords = repository.getWordsForMilestone(milestoneIndex, perDay)
            val allWords = repository.getRandomWords(40)
            val questions = TestGenerator.generateTest(targetWords, allWords, maxQuestions = 15)

            _testQuestions.value = questions
            _testQuestionIndex.value = 0
            _selectedOption.value = null
            _isAnswerSubmitted.value = false
            _isAnswerCorrect.value = false
            _testCorrectCount.value = 0
            _isTestCompleted.value = false
            _testPassed.value = false
            _newlyUnlockedDay.value = false
            _motivationMessage.value = ""
        }
    }

    fun startMistakeBankTest() {
        viewModelScope.launch {
            val mistakes = repository.mistakeWords.value
            if (mistakes.isEmpty()) return@launch

            _activeTestType.value = TestType.MISTAKE_BANK
            _activeTestDay.value = 0
            val targetWords = mistakes.map { it.word }
            val allWords = repository.getRandomWords(30)
            val questions = TestGenerator.generateTest(targetWords, allWords, maxQuestions = minOf(12, targetWords.size))

            _testQuestions.value = questions
            _testQuestionIndex.value = 0
            _selectedOption.value = null
            _isAnswerSubmitted.value = false
            _isAnswerCorrect.value = false
            _testCorrectCount.value = 0
            _isTestCompleted.value = false
            _testPassed.value = false
            _newlyUnlockedDay.value = false
            _motivationMessage.value = ""
        }
    }

    fun retakeCurrentTest() {
        when (_activeTestType.value) {
            TestType.DAILY -> startDailyTest(_activeTestDay.value)
            TestType.MILESTONE -> startMilestoneTest(_activeTestDay.value)
            TestType.MISTAKE_BANK -> startMistakeBankTest()
        }
    }

    fun selectOption(option: String) {
        if (!_isAnswerSubmitted.value) {
            _selectedOption.value = option
        }
    }

    fun submitCurrentAnswer() {
        if (_isAnswerSubmitted.value) return
        val option = _selectedOption.value ?: return
        val questions = _testQuestions.value
        val index = _testQuestionIndex.value
        if (index !in questions.indices) return

        val currentQ = questions[index]
        val isCorrect = (option == currentQ.correctAnswer)

        _isAnswerSubmitted.value = true
        _isAnswerCorrect.value = isCorrect

        viewModelScope.launch {
            if (isCorrect) {
                _testCorrectCount.value += 1
                repository.recordReview(currentQ.targetWord.id)
                if (_activeTestType.value == TestType.MISTAKE_BANK) {
                    repository.resolveMistake(currentQ.targetWord.id)
                }
            } else {
                // Log mistake in Mistake Bank immediately
                repository.recordMistake(currentQ.targetWord.id)
            }
        }
    }

    fun nextTestQuestion() {
        val questions = _testQuestions.value
        val nextIdx = _testQuestionIndex.value + 1

        if (nextIdx < questions.size) {
            _testQuestionIndex.value = nextIdx
            _selectedOption.value = null
            _isAnswerSubmitted.value = false
            _isAnswerCorrect.value = false
        } else {
            // Test Completed! Calculate score and check 80% Gatekeeper
            val total = questions.size
            val correct = _testCorrectCount.value
            val percent = if (total > 0) ((correct.toFloat() / total) * 100).toInt() else 0
            val passed = percent >= 80

            _isTestCompleted.value = true
            _testPassed.value = passed
            _motivationMessage.value = if (passed) {
                Motivations.getRandomPassMessage()
            } else {
                Motivations.getRandomRetryMessage()
            }

            viewModelScope.launch {
                when (_activeTestType.value) {
                    TestType.DAILY -> {
                        val unlocked = repository.recordDayTestAttempt(_activeTestDay.value, correct, total, passed)
                        _newlyUnlockedDay.value = unlocked
                    }
                    TestType.MILESTONE -> {
                        val unlocked = repository.recordMilestoneTestAttempt(_activeTestDay.value, correct, total, passed)
                        _newlyUnlockedDay.value = unlocked
                    }
                    TestType.MISTAKE_BANK -> {
                        repository.recordMistakeBankTestAttempt(correct, total, passed)
                    }
                }
            }
        }
    }

    // --- Mistake Bank Operations ---

    fun resolveMistake(wordId: Int) {
        viewModelScope.launch {
            repository.resolveMistake(wordId)
        }
    }

    fun removeMistake(wordId: Int) {
        viewModelScope.launch {
            repository.removeMistake(wordId)
        }
    }

    // --- Dictionary / Lexicon Browser ---

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        loadFilteredWords()
    }

    fun onFilterChange(filter: WordFilter) {
        _currentFilter.value = filter
        loadFilteredWords()
    }

    fun onLetterSelect(letter: String?) {
        _selectedLetter.value = if (_selectedLetter.value == letter) null else letter
        loadFilteredWords()
    }

    private fun loadFilteredWords() {
        viewModelScope.launch {
            repository.refreshWords(
                query = _searchQuery.value,
                filter = _currentFilter.value,
                letter = _selectedLetter.value
            )
        }
    }

    fun selectWordDetail(word: VocabWord?) {
        _selectedWordDetail.value = word
    }

    fun toggleFavorite(word: VocabWord) {
        viewModelScope.launch {
            val newFav = !word.isFavorite
            repository.toggleFavorite(word.id, newFav)
            if (_selectedWordDetail.value?.id == word.id) {
                _selectedWordDetail.value = _selectedWordDetail.value?.copy(isFavorite = newFav)
            }
            // Update in reading list if present
            _readingWords.value = _readingWords.value.map {
                if (it.id == word.id) it.copy(isFavorite = newFav) else it
            }
        }
    }

    fun toggleMastered(word: VocabWord) {
        viewModelScope.launch {
            val newMastered = !word.isMastered
            repository.toggleMastered(word.id, newMastered)
            if (_selectedWordDetail.value?.id == word.id) {
                _selectedWordDetail.value = _selectedWordDetail.value?.copy(isMastered = newMastered)
            }
            // Update in reading list if present
            _readingWords.value = _readingWords.value.map {
                if (it.id == word.id) it.copy(isMastered = newMastered) else it
            }
        }
    }

    fun speak(text: String) {
        ttsManager.speak(text)
    }

    // --- Flashcards ---

    fun loadFlashcards() {
        viewModelScope.launch {
            val words = repository.wordsList.value.ifEmpty {
                repository.getRandomWords(20)
            }
            _flashcards.value = words
            _flashcardIndex.value = 0
            _isCardFlipped.value = false
        }
    }

    fun flipFlashcard() {
        _isCardFlipped.value = !_isCardFlipped.value
    }

    fun nextFlashcard() {
        if (_flashcards.value.isNotEmpty()) {
            _flashcardIndex.value = (_flashcardIndex.value + 1) % _flashcards.value.size
            _isCardFlipped.value = false
        }
    }

    fun prevFlashcard() {
        if (_flashcards.value.isNotEmpty()) {
            val prev = _flashcardIndex.value - 1
            _flashcardIndex.value = if (prev < 0) _flashcards.value.size - 1 else prev
            _isCardFlipped.value = false
        }
    }

    fun shuffleFlashcards() {
        _flashcards.value = _flashcards.value.shuffled()
        _flashcardIndex.value = 0
        _isCardFlipped.value = false
    }

    // Reset All Progress
    fun resetAllProgress() {
        viewModelScope.launch {
            repository.resetAllProgress()
            _selectedReadingDay.value = 1
            loadReadingWords(1)
            loadFilteredWords()
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
