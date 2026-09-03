package com.example.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VocabDatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "vocab_database.db"
        private const val DATABASE_VERSION = 2

        // Vocabulary Table
        const val TABLE_VOCAB = "vocabulary"
        const val COL_ID = "id"
        const val COL_WORD = "word"
        const val COL_PRONUNCIATION = "pronunciation"
        const val COL_ENGLISH_MEANING = "english_meaning"
        const val COL_BENGALI_MEANING = "bengali_meaning"
        const val COL_EXAMPLE_SENTENCE = "example_sentence"
        const val COL_SYNONYMS = "synonyms"
        const val COL_ANTONYMS = "antonyms"
        const val COL_IS_FAVORITE = "is_favorite"
        const val COL_IS_MASTERED = "is_mastered"
        const val COL_REVIEW_COUNT = "review_count"
        const val COL_LAST_REVIEWED = "last_reviewed"

        // User Progress Table
        const val TABLE_USER_PROGRESS = "user_progress"
        const val COL_UP_ID = "id"
        const val COL_UNLOCKED_DAY = "unlocked_day"
        const val COL_DAILY_WORD_COUNT = "daily_word_count"
        const val COL_CURRENT_STREAK = "current_streak"
        const val COL_LAST_ACTIVE_DATE = "last_active_date"
        const val COL_IS_REST_DAY = "is_rest_day"
        const val COL_REST_DAY_DATE = "rest_day_date"
        const val COL_THEME_MODE = "theme_mode"

        // Day Progress Table
        const val TABLE_DAY_PROGRESS = "day_progress"
        const val COL_DAY_NUM = "day_number"
        const val COL_DAY_COMPLETED = "is_completed"
        const val COL_DAY_BEST_SCORE = "best_score"
        const val COL_DAY_PASSED_DATE = "passed_date"
        const val COL_DAY_ATTEMPTS = "attempts_count"

        // Milestone Progress Table
        const val TABLE_MILESTONE_PROGRESS = "milestone_progress"
        const val COL_MILESTONE_IDX = "milestone_index"
        const val COL_MILESTONE_COMPLETED = "is_completed"
        const val COL_MILESTONE_BEST_SCORE = "best_score"
        const val COL_MILESTONE_PASSED_DATE = "passed_date"
        const val COL_MILESTONE_ATTEMPTS = "attempts_count"

        // Test Attempts Table
        const val TABLE_TEST_ATTEMPTS = "test_attempts"
        const val COL_TA_ID = "id"
        const val COL_TA_TYPE = "test_type"
        const val COL_TA_DAY = "target_day"
        const val COL_TA_TOTAL = "total_questions"
        const val COL_TA_CORRECT = "correct_count"
        const val COL_TA_SCORE = "score_percent"
        const val COL_TA_PASSED = "passed"
        const val COL_TA_TIMESTAMP = "timestamp"

        // Mistake Bank Table
        const val TABLE_MISTAKE_BANK = "mistake_bank"
        const val COL_MB_WORD_ID = "word_id"
        const val COL_MB_ERROR_COUNT = "error_count"
        const val COL_MB_LAST_ERROR = "last_error_time"
        const val COL_MB_RESOLVED = "is_resolved"
    }

    override fun onCreate(db: SQLiteDatabase) {
        createAllTables(db)
        populateInitialData(db)
        initUserProgress(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        createAllTables(db)
        initUserProgress(db)
    }

    private fun createAllTables(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_VOCAB (
                $COL_ID INTEGER PRIMARY KEY,
                $COL_WORD TEXT NOT NULL,
                $COL_PRONUNCIATION TEXT,
                $COL_ENGLISH_MEANING TEXT,
                $COL_BENGALI_MEANING TEXT,
                $COL_EXAMPLE_SENTENCE TEXT,
                $COL_SYNONYMS TEXT,
                $COL_ANTONYMS TEXT,
                $COL_IS_FAVORITE INTEGER DEFAULT 0,
                $COL_IS_MASTERED INTEGER DEFAULT 0,
                $COL_REVIEW_COUNT INTEGER DEFAULT 0,
                $COL_LAST_REVIEWED INTEGER DEFAULT 0
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_word ON $TABLE_VOCAB ($COL_WORD)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_favorite ON $TABLE_VOCAB ($COL_IS_FAVORITE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_mastered ON $TABLE_VOCAB ($COL_IS_MASTERED)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_USER_PROGRESS (
                $COL_UP_ID INTEGER PRIMARY KEY,
                $COL_UNLOCKED_DAY INTEGER DEFAULT 1,
                $COL_DAILY_WORD_COUNT INTEGER DEFAULT 20,
                $COL_CURRENT_STREAK INTEGER DEFAULT 1,
                $COL_LAST_ACTIVE_DATE TEXT,
                $COL_IS_REST_DAY INTEGER DEFAULT 0,
                $COL_REST_DAY_DATE TEXT,
                $COL_THEME_MODE TEXT DEFAULT 'SYSTEM'
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_DAY_PROGRESS (
                $COL_DAY_NUM INTEGER PRIMARY KEY,
                $COL_DAY_COMPLETED INTEGER DEFAULT 0,
                $COL_DAY_BEST_SCORE INTEGER DEFAULT 0,
                $COL_DAY_PASSED_DATE INTEGER DEFAULT 0,
                $COL_DAY_ATTEMPTS INTEGER DEFAULT 0
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_MILESTONE_PROGRESS (
                $COL_MILESTONE_IDX INTEGER PRIMARY KEY,
                $COL_MILESTONE_COMPLETED INTEGER DEFAULT 0,
                $COL_MILESTONE_BEST_SCORE INTEGER DEFAULT 0,
                $COL_MILESTONE_PASSED_DATE INTEGER DEFAULT 0,
                $COL_MILESTONE_ATTEMPTS INTEGER DEFAULT 0
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_TEST_ATTEMPTS (
                $COL_TA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TA_TYPE TEXT NOT NULL,
                $COL_TA_DAY INTEGER DEFAULT 0,
                $COL_TA_TOTAL INTEGER NOT NULL,
                $COL_TA_CORRECT INTEGER NOT NULL,
                $COL_TA_SCORE INTEGER NOT NULL,
                $COL_TA_PASSED INTEGER NOT NULL,
                $COL_TA_TIMESTAMP INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_MISTAKE_BANK (
                $COL_MB_WORD_ID INTEGER PRIMARY KEY,
                $COL_MB_ERROR_COUNT INTEGER DEFAULT 1,
                $COL_MB_LAST_ERROR INTEGER NOT NULL,
                $COL_MB_RESOLVED INTEGER DEFAULT 0
            )
        """.trimIndent())
    }

    private fun initUserProgress(db: SQLiteDatabase) {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_USER_PROGRESS", null)
        val count = cursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
        if (count == 0) {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val values = ContentValues().apply {
                put(COL_UP_ID, 1)
                put(COL_UNLOCKED_DAY, 1)
                put(COL_DAILY_WORD_COUNT, 20)
                put(COL_CURRENT_STREAK, 1)
                put(COL_LAST_ACTIVE_DATE, todayStr)
                put(COL_IS_REST_DAY, 0)
                put(COL_REST_DAY_DATE, "")
                put(COL_THEME_MODE, "SYSTEM")
            }
            db.insert(TABLE_USER_PROGRESS, null, values)
        }
    }

    private fun populateInitialData(db: SQLiteDatabase) {
        try {
            val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_VOCAB", null)
            val count = cursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
            if (count > 0) return

            val inputStream = context.assets.open("vocabulary.json")
            val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
            val jsonString = reader.use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            db.beginTransaction()
            try {
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val values = ContentValues().apply {
                        put(COL_ID, obj.getInt("id"))
                        put(COL_WORD, obj.getString("word"))
                        put(COL_PRONUNCIATION, obj.optString("pronunciation", ""))
                        put(COL_ENGLISH_MEANING, obj.optString("englishMeaning", ""))
                        put(COL_BENGALI_MEANING, obj.optString("bengaliMeaning", ""))
                        put(COL_EXAMPLE_SENTENCE, obj.optString("exampleSentence", ""))
                        put(COL_SYNONYMS, obj.optString("synonyms", ""))
                        put(COL_ANTONYMS, obj.optString("antonyms", ""))
                        put(COL_IS_FAVORITE, 0)
                        put(COL_IS_MASTERED, 0)
                        put(COL_REVIEW_COUNT, 0)
                        put(COL_LAST_REVIEWED, 0L)
                    }
                    db.insert(TABLE_VOCAB, null, values)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- User Progress Queries ---

    fun getUserProgress(): UserProgress {
        val db = readableDatabase
        val cursor = db.query(TABLE_USER_PROGRESS, null, "$COL_UP_ID = 1", null, null, null, null)
        cursor.use {
            if (it.moveToFirst()) {
                return UserProgress(
                    unlockedDay = it.getInt(it.getColumnIndexOrThrow(COL_UNLOCKED_DAY)),
                    dailyWordCount = it.getInt(it.getColumnIndexOrThrow(COL_DAILY_WORD_COUNT)),
                    currentStreak = it.getInt(it.getColumnIndexOrThrow(COL_CURRENT_STREAK)),
                    lastActiveDate = it.getString(it.getColumnIndexOrThrow(COL_LAST_ACTIVE_DATE)) ?: "",
                    isRestDayActive = it.getInt(it.getColumnIndexOrThrow(COL_IS_REST_DAY)) == 1,
                    restDayDate = it.getString(it.getColumnIndexOrThrow(COL_REST_DAY_DATE)) ?: "",
                    themeMode = it.getString(it.getColumnIndexOrThrow(COL_THEME_MODE)) ?: "SYSTEM"
                )
            }
        }
        return UserProgress()
    }

    fun updateDailyWordCount(count: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_DAILY_WORD_COUNT, count)
        }
        db.update(TABLE_USER_PROGRESS, values, "$COL_UP_ID = 1", null)
    }

    fun setRestDayActive(active: Boolean) {
        val db = writableDatabase
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val values = ContentValues().apply {
            put(COL_IS_REST_DAY, if (active) 1 else 0)
            put(COL_REST_DAY_DATE, if (active) todayStr else "")
        }
        db.update(TABLE_USER_PROGRESS, values, "$COL_UP_ID = 1", null)
    }

    fun setThemeMode(mode: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_THEME_MODE, mode)
        }
        db.update(TABLE_USER_PROGRESS, values, "$COL_UP_ID = 1", null)
    }

    fun updateStreakOnActivity() {
        val db = writableDatabase
        val progress = getUserProgress()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        if (progress.lastActiveDate == todayStr) {
            // Already active today
            return
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        var newStreak = progress.currentStreak

        try {
            val lastDate = sdf.parse(progress.lastActiveDate)
            val today = sdf.parse(todayStr)
            if (lastDate != null && today != null) {
                val diffDays = ((today.time - lastDate.time) / (1000 * 60 * 60 * 24)).toInt()
                if (diffDays == 1) {
                    newStreak += 1
                } else if (diffDays > 1) {
                    // Check if rest day was active on the missed day
                    if (!progress.isRestDayActive) {
                        newStreak = 1
                    }
                }
            } else {
                newStreak = 1
            }
        } catch (e: Exception) {
            newStreak = 1
        }

        val values = ContentValues().apply {
            put(COL_CURRENT_STREAK, newStreak)
            put(COL_LAST_ACTIVE_DATE, todayStr)
            // If rest day was active for yesterday, deactivate it for today
            if (progress.isRestDayActive && progress.restDayDate != todayStr) {
                put(COL_IS_REST_DAY, 0)
            }
        }
        db.update(TABLE_USER_PROGRESS, values, "$COL_UP_ID = 1", null)
    }

    // --- Day Progress Queries ---

    fun getAllDayProgress(): Map<Int, DayProgress> {
        val map = mutableMapOf<Int, DayProgress>()
        val db = readableDatabase
        val cursor = db.query(TABLE_DAY_PROGRESS, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                val dayNum = it.getInt(it.getColumnIndexOrThrow(COL_DAY_NUM))
                map[dayNum] = DayProgress(
                    dayNumber = dayNum,
                    isCompleted = it.getInt(it.getColumnIndexOrThrow(COL_DAY_COMPLETED)) == 1,
                    bestScore = it.getInt(it.getColumnIndexOrThrow(COL_DAY_BEST_SCORE)),
                    passedDate = it.getLong(it.getColumnIndexOrThrow(COL_DAY_PASSED_DATE)),
                    attemptsCount = it.getInt(it.getColumnIndexOrThrow(COL_DAY_ATTEMPTS))
                )
            }
        }
        return map
    }

    fun getAllMilestoneProgress(): Map<Int, MilestoneProgress> {
        val map = mutableMapOf<Int, MilestoneProgress>()
        val db = readableDatabase
        val cursor = db.query(TABLE_MILESTONE_PROGRESS, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                val idx = it.getInt(it.getColumnIndexOrThrow(COL_MILESTONE_IDX))
                val startDay = (idx - 1) * 5 + 1
                val endDay = idx * 5
                map[idx] = MilestoneProgress(
                    milestoneIndex = idx,
                    startDay = startDay,
                    endDay = endDay,
                    isCompleted = it.getInt(it.getColumnIndexOrThrow(COL_MILESTONE_COMPLETED)) == 1,
                    bestScore = it.getInt(it.getColumnIndexOrThrow(COL_MILESTONE_BEST_SCORE)),
                    passedDate = it.getLong(it.getColumnIndexOrThrow(COL_MILESTONE_PASSED_DATE)),
                    attemptsCount = it.getInt(it.getColumnIndexOrThrow(COL_MILESTONE_ATTEMPTS))
                )
            }
        }
        return map
    }

    // Record a Daily Test Attempt
    fun recordDayTestAttempt(day: Int, correct: Int, total: Int, passed: Boolean): Boolean {
        val db = writableDatabase
        val scorePercent = if (total > 0) ((correct.toFloat() / total) * 100).toInt() else 0
        val timestamp = System.currentTimeMillis()

        // 1. Insert into test attempts
        val attemptValues = ContentValues().apply {
            put(COL_TA_TYPE, TestType.DAILY.name)
            put(COL_TA_DAY, day)
            put(COL_TA_TOTAL, total)
            put(COL_TA_CORRECT, correct)
            put(COL_TA_SCORE, scorePercent)
            put(COL_TA_PASSED, if (passed) 1 else 0)
            put(COL_TA_TIMESTAMP, timestamp)
        }
        db.insert(TABLE_TEST_ATTEMPTS, null, attemptValues)

        // 2. Update Day Progress
        var existingAttempts = 0
        var existingBestScore = 0
        var alreadyCompleted = false
        val checkCursor = db.query(TABLE_DAY_PROGRESS, null, "$COL_DAY_NUM = ?", arrayOf(day.toString()), null, null, null)
        checkCursor.use {
            if (it.moveToFirst()) {
                existingAttempts = it.getInt(it.getColumnIndexOrThrow(COL_DAY_ATTEMPTS))
                existingBestScore = it.getInt(it.getColumnIndexOrThrow(COL_DAY_BEST_SCORE))
                alreadyCompleted = it.getInt(it.getColumnIndexOrThrow(COL_DAY_COMPLETED)) == 1
            }
        }

        val newBestScore = maxOf(existingBestScore, scorePercent)
        val isNowCompleted = alreadyCompleted || passed

        val dayValues = ContentValues().apply {
            put(COL_DAY_NUM, day)
            put(COL_DAY_COMPLETED, if (isNowCompleted) 1 else 0)
            put(COL_DAY_BEST_SCORE, newBestScore)
            if (passed && !alreadyCompleted) {
                put(COL_DAY_PASSED_DATE, timestamp)
            }
            put(COL_DAY_ATTEMPTS, existingAttempts + 1)
        }
        db.insertWithOnConflict(TABLE_DAY_PROGRESS, null, dayValues, SQLiteDatabase.CONFLICT_REPLACE)

        // 3. Check unlocked day progress
        val userProgress = getUserProgress()
        var newlyUnlocked = false
        if (passed) {
            updateStreakOnActivity()
            // If this is the current unlocked day, check whether unlocking next day is allowed
            // (If day is a multiple of 5, milestone review must be passed before unlocking day + 1)
            if (day == userProgress.unlockedDay) {
                if (day % 5 != 0) {
                    val nextDay = day + 1
                    val totalDays = calculateTotalDays(userProgress.dailyWordCount)
                    if (nextDay <= totalDays) {
                        val upValues = ContentValues().apply {
                            put(COL_UNLOCKED_DAY, nextDay)
                        }
                        db.update(TABLE_USER_PROGRESS, upValues, "$COL_UP_ID = 1", null)
                        newlyUnlocked = true
                    }
                }
            }
        }

        return newlyUnlocked
    }

    // Record a 5-Day Milestone Test Attempt
    fun recordMilestoneTestAttempt(milestoneIndex: Int, correct: Int, total: Int, passed: Boolean): Boolean {
        val db = writableDatabase
        val scorePercent = if (total > 0) ((correct.toFloat() / total) * 100).toInt() else 0
        val timestamp = System.currentTimeMillis()

        // 1. Insert into test attempts
        val attemptValues = ContentValues().apply {
            put(COL_TA_TYPE, TestType.MILESTONE.name)
            put(COL_TA_DAY, milestoneIndex * 5)
            put(COL_TA_TOTAL, total)
            put(COL_TA_CORRECT, correct)
            put(COL_TA_SCORE, scorePercent)
            put(COL_TA_PASSED, if (passed) 1 else 0)
            put(COL_TA_TIMESTAMP, timestamp)
        }
        db.insert(TABLE_TEST_ATTEMPTS, null, attemptValues)

        // 2. Update Milestone Progress
        var existingAttempts = 0
        var existingBestScore = 0
        var alreadyCompleted = false
        val checkCursor = db.query(TABLE_MILESTONE_PROGRESS, null, "$COL_MILESTONE_IDX = ?", arrayOf(milestoneIndex.toString()), null, null, null)
        checkCursor.use {
            if (it.moveToFirst()) {
                existingAttempts = it.getInt(it.getColumnIndexOrThrow(COL_MILESTONE_ATTEMPTS))
                existingBestScore = it.getInt(it.getColumnIndexOrThrow(COL_MILESTONE_BEST_SCORE))
                alreadyCompleted = it.getInt(it.getColumnIndexOrThrow(COL_MILESTONE_COMPLETED)) == 1
            }
        }

        val newBestScore = maxOf(existingBestScore, scorePercent)
        val isNowCompleted = alreadyCompleted || passed

        val milestoneValues = ContentValues().apply {
            put(COL_MILESTONE_IDX, milestoneIndex)
            put(COL_MILESTONE_COMPLETED, if (isNowCompleted) 1 else 0)
            put(COL_MILESTONE_BEST_SCORE, newBestScore)
            if (passed && !alreadyCompleted) {
                put(COL_MILESTONE_PASSED_DATE, timestamp)
            }
            put(COL_MILESTONE_ATTEMPTS, existingAttempts + 1)
        }
        db.insertWithOnConflict(TABLE_MILESTONE_PROGRESS, null, milestoneValues, SQLiteDatabase.CONFLICT_REPLACE)

        // 3. If passed, unlock day (milestoneIndex * 5 + 1)
        var newlyUnlocked = false
        if (passed) {
            updateStreakOnActivity()
            val userProgress = getUserProgress()
            val nextDay = milestoneIndex * 5 + 1
            val totalDays = calculateTotalDays(userProgress.dailyWordCount)
            if (userProgress.unlockedDay <= milestoneIndex * 5 && nextDay <= totalDays) {
                val upValues = ContentValues().apply {
                    put(COL_UNLOCKED_DAY, nextDay)
                }
                db.update(TABLE_USER_PROGRESS, upValues, "$COL_UP_ID = 1", null)
                newlyUnlocked = true
            }
        }

        return newlyUnlocked
    }

    // Record Mistake Bank Test Attempt
    fun recordMistakeBankTestAttempt(correct: Int, total: Int, passed: Boolean) {
        val db = writableDatabase
        val scorePercent = if (total > 0) ((correct.toFloat() / total) * 100).toInt() else 0
        val attemptValues = ContentValues().apply {
            put(COL_TA_TYPE, TestType.MISTAKE_BANK.name)
            put(COL_TA_DAY, 0)
            put(COL_TA_TOTAL, total)
            put(COL_TA_CORRECT, correct)
            put(COL_TA_SCORE, scorePercent)
            put(COL_TA_PASSED, if (passed) 1 else 0)
            put(COL_TA_TIMESTAMP, System.currentTimeMillis())
        }
        db.insert(TABLE_TEST_ATTEMPTS, null, attemptValues)
        if (passed) {
            updateStreakOnActivity()
        }
    }

    // --- Mistake Bank Operations ---

    fun recordMistake(wordId: Int) {
        val db = writableDatabase
        val cursor = db.query(TABLE_MISTAKE_BANK, arrayOf(COL_MB_ERROR_COUNT), "$COL_MB_WORD_ID = ?", arrayOf(wordId.toString()), null, null, null)
        val existingCount = cursor.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }

        val values = ContentValues().apply {
            put(COL_MB_WORD_ID, wordId)
            put(COL_MB_ERROR_COUNT, existingCount + 1)
            put(COL_MB_LAST_ERROR, System.currentTimeMillis())
            put(COL_MB_RESOLVED, 0)
        }
        db.insertWithOnConflict(TABLE_MISTAKE_BANK, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun resolveMistake(wordId: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_MB_RESOLVED, 1)
        }
        db.update(TABLE_MISTAKE_BANK, values, "$COL_MB_WORD_ID = ?", arrayOf(wordId.toString()))
    }

    fun removeMistake(wordId: Int) {
        val db = writableDatabase
        db.delete(TABLE_MISTAKE_BANK, "$COL_MB_WORD_ID = ?", arrayOf(wordId.toString()))
    }

    fun getMistakeWords(includeResolved: Boolean = false): List<MistakeWord> {
        val list = mutableListOf<MistakeWord>()
        val db = readableDatabase
        val query = """
            SELECT 
                mb.$COL_MB_WORD_ID,
                mb.$COL_MB_ERROR_COUNT,
                mb.$COL_MB_LAST_ERROR,
                mb.$COL_MB_RESOLVED,
                v.*
            FROM $TABLE_MISTAKE_BANK mb
            INNER JOIN $TABLE_VOCAB v ON mb.$COL_MB_WORD_ID = v.$COL_ID
            ${if (!includeResolved) "WHERE mb.$COL_MB_RESOLVED = 0" else ""}
            ORDER BY mb.$COL_MB_ERROR_COUNT DESC, mb.$COL_MB_LAST_ERROR DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        cursor.use {
            while (it.moveToNext()) {
                val wordId = it.getInt(it.getColumnIndexOrThrow(COL_MB_WORD_ID))
                val errorCount = it.getInt(it.getColumnIndexOrThrow(COL_MB_ERROR_COUNT))
                val lastError = it.getLong(it.getColumnIndexOrThrow(COL_MB_LAST_ERROR))
                val resolved = it.getInt(it.getColumnIndexOrThrow(COL_MB_RESOLVED)) == 1
                val vocabWord = cursorToWord(it)
                list.add(MistakeWord(vocabWord, errorCount, lastError, resolved))
            }
        }
        return list
    }

    // --- Word Chunks & Lists ---

    fun getTotalWordCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_VOCAB", null)
        return cursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    fun calculateTotalDays(dailyWordCount: Int): Int {
        val total = getTotalWordCount().coerceAtLeast(1)
        val perDay = dailyWordCount.coerceAtLeast(15)
        return Math.ceil(total.toDouble() / perDay).toInt()
    }

    fun getWordsForDay(day: Int, dailyWordCount: Int): List<VocabWord> {
        val list = mutableListOf<VocabWord>()
        val db = readableDatabase
        val offset = ((day - 1) * dailyWordCount).coerceAtLeast(0)
        val cursor = db.query(TABLE_VOCAB, null, null, null, null, null, "$COL_ID ASC", "$offset, $dailyWordCount")
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToWord(it))
            }
        }
        return list
    }

    fun getWordsForMilestone(milestoneIndex: Int, dailyWordCount: Int): List<VocabWord> {
        val list = mutableListOf<VocabWord>()
        val db = readableDatabase
        val startDay = (milestoneIndex - 1) * 5 + 1
        val offset = ((startDay - 1) * dailyWordCount).coerceAtLeast(0)
        val limit = dailyWordCount * 5
        val cursor = db.query(TABLE_VOCAB, null, null, null, null, null, "$COL_ID ASC", "$offset, $limit")
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToWord(it))
            }
        }
        return list
    }

    // Lifetime Accuracy & Test Attempt Summary
    fun getTestStatsSummary(): Pair<Int, Float> {
        val db = readableDatabase
        var totalAttempts = 0
        var totalQuestions = 0
        var totalCorrect = 0

        val cursor = db.rawQuery("SELECT COUNT(*), SUM($COL_TA_TOTAL), SUM($COL_TA_CORRECT) FROM $TABLE_TEST_ATTEMPTS", null)
        cursor.use {
            if (it.moveToFirst()) {
                totalAttempts = it.getInt(0)
                totalQuestions = it.getInt(1)
                totalCorrect = it.getInt(2)
            }
        }

        val accuracy = if (totalQuestions > 0) {
            (totalCorrect.toFloat() / totalQuestions) * 100f
        } else {
            0f
        }

        return Pair(totalAttempts, accuracy)
    }

    // --- Standard Vocabulary Queries ---

    fun getAllWords(): List<VocabWord> {
        val list = mutableListOf<VocabWord>()
        val db = readableDatabase
        val cursor = db.query(TABLE_VOCAB, null, null, null, null, null, "$COL_WORD ASC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToWord(it))
            }
        }
        return list
    }

    fun searchWords(query: String, filter: WordFilter, letterPrefix: String? = null): List<VocabWord> {
        val list = mutableListOf<VocabWord>()
        val db = readableDatabase

        val selectionList = mutableListOf<String>()
        val argsList = mutableListOf<String>()

        if (query.isNotBlank()) {
            selectionList.add("($COL_WORD LIKE ? OR $COL_ENGLISH_MEANING LIKE ? OR $COL_BENGALI_MEANING LIKE ?)")
            val param = "%${query.trim()}%"
            argsList.add(param)
            argsList.add(param)
            argsList.add(param)
        }

        when (filter) {
            WordFilter.ALL -> {}
            WordFilter.FAVORITES -> selectionList.add("$COL_IS_FAVORITE = 1")
            WordFilter.MASTERED -> selectionList.add("$COL_IS_MASTERED = 1")
            WordFilter.LEARNING -> selectionList.add("$COL_IS_MASTERED = 0")
        }

        if (!letterPrefix.isNullOrBlank()) {
            selectionList.add("$COL_WORD LIKE ?")
            argsList.add("${letterPrefix.uppercase()}%")
        }

        val selection = if (selectionList.isEmpty()) null else selectionList.joinToString(" AND ")
        val args = if (argsList.isEmpty()) null else argsList.toTypedArray()

        val cursor = db.query(TABLE_VOCAB, null, selection, args, null, null, "$COL_WORD ASC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToWord(it))
            }
        }
        return list
    }

    fun getWordById(id: Int): VocabWord? {
        val db = readableDatabase
        val cursor = db.query(TABLE_VOCAB, null, "$COL_ID = ?", arrayOf(id.toString()), null, null, null)
        return cursor.use {
            if (it.moveToFirst()) cursorToWord(it) else null
        }
    }

    fun toggleFavorite(id: Int, isFav: Boolean) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_IS_FAVORITE, if (isFav) 1 else 0)
        }
        db.update(TABLE_VOCAB, values, "$COL_ID = ?", arrayOf(id.toString()))
    }

    fun toggleMastered(id: Int, isMastered: Boolean) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_IS_MASTERED, if (isMastered) 1 else 0)
            if (isMastered) {
                put(COL_LAST_REVIEWED, System.currentTimeMillis())
            }
        }
        db.update(TABLE_VOCAB, values, "$COL_ID = ?", arrayOf(id.toString()))
    }

    fun recordReview(id: Int) {
        val db = writableDatabase
        db.execSQL(
            "UPDATE $TABLE_VOCAB SET $COL_REVIEW_COUNT = $COL_REVIEW_COUNT + 1, $COL_LAST_REVIEWED = ? WHERE $COL_ID = ?",
            arrayOf(System.currentTimeMillis(), id)
        )
    }

    fun getRandomWords(count: Int, excludeId: Int? = null): List<VocabWord> {
        val list = mutableListOf<VocabWord>()
        val db = readableDatabase
        val query = if (excludeId != null) {
            "SELECT * FROM $TABLE_VOCAB WHERE $COL_ID != $excludeId ORDER BY RANDOM() LIMIT $count"
        } else {
            "SELECT * FROM $TABLE_VOCAB ORDER BY RANDOM() LIMIT $count"
        }
        val cursor = db.rawQuery(query, null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToWord(it))
            }
        }
        return list
    }

    fun getStats(): Stats {
        val db = readableDatabase
        var total = 0
        var mastered = 0
        var favorites = 0
        var reviewed = 0

        val cursor = db.rawQuery(
            """
            SELECT 
                COUNT(*),
                SUM(CASE WHEN $COL_IS_MASTERED = 1 THEN 1 ELSE 0 END),
                SUM(CASE WHEN $COL_IS_FAVORITE = 1 THEN 1 ELSE 0 END),
                SUM(CASE WHEN $COL_REVIEW_COUNT > 0 THEN 1 ELSE 0 END)
            FROM $TABLE_VOCAB
            """.trimIndent(), null
        )

        cursor.use {
            if (it.moveToFirst()) {
                total = it.getInt(0)
                mastered = it.getInt(1)
                favorites = it.getInt(2)
                reviewed = it.getInt(3)
            }
        }

        return Stats(total = total, mastered = mastered, favorites = favorites, reviewed = reviewed)
    }

    fun resetAllProgress() {
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_VOCAB SET $COL_IS_MASTERED = 0, $COL_REVIEW_COUNT = 0, $COL_LAST_REVIEWED = 0")
        db.execSQL("DELETE FROM $TABLE_DAY_PROGRESS")
        db.execSQL("DELETE FROM $TABLE_MILESTONE_PROGRESS")
        db.execSQL("DELETE FROM $TABLE_TEST_ATTEMPTS")
        db.execSQL("DELETE FROM $TABLE_MISTAKE_BANK")

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val values = ContentValues().apply {
            put(COL_UNLOCKED_DAY, 1)
            put(COL_CURRENT_STREAK, 1)
            put(COL_LAST_ACTIVE_DATE, todayStr)
            put(COL_IS_REST_DAY, 0)
            put(COL_REST_DAY_DATE, "")
        }
        db.update(TABLE_USER_PROGRESS, values, "$COL_UP_ID = 1", null)
    }

    private fun cursorToWord(cursor: Cursor): VocabWord {
        return VocabWord(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
            word = cursor.getString(cursor.getColumnIndexOrThrow(COL_WORD)),
            pronunciation = cursor.getString(cursor.getColumnIndexOrThrow(COL_PRONUNCIATION)) ?: "",
            englishMeaning = cursor.getString(cursor.getColumnIndexOrThrow(COL_ENGLISH_MEANING)) ?: "",
            bengaliMeaning = cursor.getString(cursor.getColumnIndexOrThrow(COL_BENGALI_MEANING)) ?: "",
            exampleSentence = cursor.getString(cursor.getColumnIndexOrThrow(COL_EXAMPLE_SENTENCE)) ?: "",
            synonyms = cursor.getString(cursor.getColumnIndexOrThrow(COL_SYNONYMS)) ?: "",
            antonyms = cursor.getString(cursor.getColumnIndexOrThrow(COL_ANTONYMS)) ?: "",
            isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_FAVORITE)) == 1,
            isMastered = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_MASTERED)) == 1,
            reviewCount = cursor.getInt(cursor.getColumnIndexOrThrow(COL_REVIEW_COUNT)),
            lastReviewedTime = cursor.getLong(cursor.getColumnIndexOrThrow(COL_LAST_REVIEWED))
        )
    }
}

data class Stats(
    val total: Int,
    val mastered: Int,
    val favorites: Int,
    val reviewed: Int
)
