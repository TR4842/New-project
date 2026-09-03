package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VocabViewModel
import com.example.ui.screens.ChallengeTestScreen
import com.example.ui.screens.DailyReadingScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MistakeBankScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.WordDetailSheet
import com.example.ui.screens.WordListScreen
import com.example.ui.theme.WordSmartTheme

enum class AppNavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("Home", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    READING("Read", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    CHALLENGE("Challenge", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter),
    MISTAKES("Mistakes", Icons.Filled.Psychology, Icons.Outlined.Psychology),
    LEXICON("Lexicon", Icons.Filled.AutoStories, Icons.Outlined.AutoStories)
}

class MainActivity : ComponentActivity() {
    private val viewModel: VocabViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()

            WordSmartTheme(themeMode = themeMode) {
                var currentTab by remember { mutableStateOf(AppNavTab.DASHBOARD) }
                var showSettingsDialog by remember { mutableStateOf(false) }

                val userProgress by viewModel.userProgress.collectAsState()
                val dayProgressMap by viewModel.dayProgressMap.collectAsState()
                val milestoneProgressMap by viewModel.milestoneProgressMap.collectAsState()
                val mistakeWords by viewModel.mistakeWords.collectAsState()
                val testStats by viewModel.testStats.collectAsState()
                val overallStats by viewModel.overallStats.collectAsState()
                val totalDays = viewModel.getTotalDays()

                // Reading states
                val readingWords by viewModel.readingWords.collectAsState()
                val readingIndex by viewModel.readingWordIndex.collectAsState()
                val selectedReadingDay by viewModel.selectedReadingDay.collectAsState()

                // Test states
                val activeTestType by viewModel.activeTestType.collectAsState()
                val activeTestDay by viewModel.activeTestDay.collectAsState()
                val testQuestions by viewModel.testQuestions.collectAsState()
                val testQuestionIndex by viewModel.testQuestionIndex.collectAsState()
                val selectedOption by viewModel.selectedOption.collectAsState()
                val isAnswerSubmitted by viewModel.isAnswerSubmitted.collectAsState()
                val isAnswerCorrect by viewModel.isAnswerCorrect.collectAsState()
                val testCorrectCount by viewModel.testCorrectCount.collectAsState()
                val isTestCompleted by viewModel.isTestCompleted.collectAsState()
                val testPassed by viewModel.testPassed.collectAsState()
                val motivationMessage by viewModel.motivationMessage.collectAsState()
                val newlyUnlockedDay by viewModel.newlyUnlockedDay.collectAsState()

                // Lexicon states
                val wordsList by viewModel.wordsList.collectAsState()
                val searchQuery by viewModel.searchQuery.collectAsState()
                val currentFilter by viewModel.currentFilter.collectAsState()
                val selectedLetter by viewModel.selectedLetter.collectAsState()
                val selectedWordDetail by viewModel.selectedWordDetail.collectAsState()

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = "Word Smart",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            actions = {
                                IconButton(
                                    onClick = { showSettingsDialog = true },
                                    modifier = Modifier.testTag("app_bar_settings_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Preferences",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {
                            AppNavTab.entries.forEach { tab ->
                                val isSelected = currentTab == tab
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title
                                        )
                                    },
                                    label = { Text(tab.title) },
                                    modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            AppNavTab.DASHBOARD -> {
                                DashboardScreen(
                                    userProgress = userProgress,
                                    dayProgressMap = dayProgressMap,
                                    milestoneProgressMap = milestoneProgressMap,
                                    mistakeWords = mistakeWords,
                                    testStats = testStats,
                                    overallStats = overallStats,
                                    totalDays = totalDays,
                                    onStartReadingDay = { day ->
                                        viewModel.selectReadingDay(day)
                                        currentTab = AppNavTab.READING
                                    },
                                    onStartDailyTest = { day ->
                                        viewModel.startDailyTest(day)
                                        currentTab = AppNavTab.CHALLENGE
                                    },
                                    onStartMilestoneTest = { mIdx ->
                                        viewModel.startMilestoneTest(mIdx)
                                        currentTab = AppNavTab.CHALLENGE
                                    },
                                    onNavigateToMistakeBank = {
                                        currentTab = AppNavTab.MISTAKES
                                    },
                                    onDailyGoalSelected = { goal ->
                                        viewModel.setDailyWordCount(goal)
                                    },
                                    onToggleRestDay = { active ->
                                        viewModel.setRestDayActive(active)
                                    },
                                    onOpenSettings = {
                                        showSettingsDialog = true
                                    }
                                )
                            }

                            AppNavTab.READING -> {
                                DailyReadingScreen(
                                    dayNumber = selectedReadingDay,
                                    words = readingWords,
                                    currentIndex = readingIndex,
                                    onSelectIndex = { viewModel.selectReadingWordIndex(it) },
                                    onNext = { viewModel.nextReadingWord() },
                                    onPrev = { viewModel.prevReadingWord() },
                                    onSpeak = { viewModel.speak(it) },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    onToggleMastered = { viewModel.toggleMastered(it) },
                                    onStartTest = { day ->
                                        viewModel.startDailyTest(day)
                                        currentTab = AppNavTab.CHALLENGE
                                    }
                                )
                            }

                            AppNavTab.CHALLENGE -> {
                                ChallengeTestScreen(
                                    testType = activeTestType,
                                    targetDay = activeTestDay,
                                    questions = testQuestions,
                                    currentIndex = testQuestionIndex,
                                    selectedOption = selectedOption,
                                    isAnswerSubmitted = isAnswerSubmitted,
                                    isAnswerCorrect = isAnswerCorrect,
                                    correctCount = testCorrectCount,
                                    isCompleted = isTestCompleted,
                                    isPassed = testPassed,
                                    motivationMessage = motivationMessage,
                                    newlyUnlocked = newlyUnlockedDay,
                                    onOptionSelected = { viewModel.selectOption(it) },
                                    onSubmitAnswer = { viewModel.submitCurrentAnswer() },
                                    onNextQuestion = { viewModel.nextTestQuestion() },
                                    onRetakeTest = { viewModel.retakeCurrentTest() },
                                    onNavigateToMistakes = { currentTab = AppNavTab.MISTAKES },
                                    onBackToDashboard = { currentTab = AppNavTab.DASHBOARD },
                                    onSpeak = { viewModel.speak(it) }
                                )
                            }

                            AppNavTab.MISTAKES -> {
                                MistakeBankScreen(
                                    mistakes = mistakeWords,
                                    onStartMistakeTest = {
                                        viewModel.startMistakeBankTest()
                                        currentTab = AppNavTab.CHALLENGE
                                    },
                                    onResolveMistake = { viewModel.resolveMistake(it) },
                                    onRemoveMistake = { viewModel.removeMistake(it) },
                                    onSpeak = { viewModel.speak(it) }
                                )
                            }

                            AppNavTab.LEXICON -> {
                                WordListScreen(
                                    words = wordsList,
                                    stats = overallStats,
                                    searchQuery = searchQuery,
                                    currentFilter = currentFilter,
                                    selectedLetter = selectedLetter,
                                    onSearchChange = { viewModel.onSearchQueryChange(it) },
                                    onFilterChange = { viewModel.onFilterChange(it) },
                                    onLetterSelect = { viewModel.onLetterSelect(it) },
                                    onWordClick = { viewModel.selectWordDetail(it) },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    onToggleMastered = { viewModel.toggleMastered(it) },
                                    onSpeak = { viewModel.speak(it) }
                                )
                            }
                        }

                        // Word Detail Modal Sheet
                        selectedWordDetail?.let { word ->
                            WordDetailSheet(
                                word = word,
                                onDismiss = { viewModel.selectWordDetail(null) },
                                onToggleFavorite = { viewModel.toggleFavorite(word) },
                                onToggleMastered = { viewModel.toggleMastered(word) },
                                onSpeak = { viewModel.speak(word.word) },
                                onPrevWord = {},
                                onNextWord = {}
                            )
                        }

                        // Settings Dialog
                        if (showSettingsDialog) {
                            SettingsDialog(
                                userProgress = userProgress,
                                currentThemeMode = themeMode,
                                onDismiss = { showSettingsDialog = false },
                                onDailyGoalSelected = { viewModel.setDailyWordCount(it) },
                                onToggleRestDay = { viewModel.setRestDayActive(it) },
                                onThemeModeSelected = { viewModel.setThemeMode(it) },
                                onResetAllProgress = { viewModel.resetAllProgress() }
                            )
                        }
                    }
                }
            }
        }
    }
}
