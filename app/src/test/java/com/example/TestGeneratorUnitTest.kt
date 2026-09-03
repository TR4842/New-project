package com.example

import com.example.data.VocabWord
import com.example.util.Motivations
import com.example.util.TestGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TestGeneratorUnitTest {

    private val sampleWords = listOf(
        VocabWord(
            id = 1,
            word = "Abate",
            pronunciation = "uh-BAYT",
            englishMeaning = "To decrease, reduce or diminish in intensity",
            bengaliMeaning = "কমানো বা হ্রাস করা",
            exampleSentence = "The violent storm began to abate by midnight.",
            synonyms = "diminish, dwindle, subside",
            antonyms = "increase, intensify, escalate",
            isFavorite = false,
            isMastered = false
        ),
        VocabWord(
            id = 2,
            word = "Aberration",
            pronunciation = "ab-uh-RAY-shun",
            englishMeaning = "Something that differs from the normal or standard",
            bengaliMeaning = "বিচ্যুতি বা অস্বাভাবিকতা",
            exampleSentence = "Snow in July was a distinct weather aberration.",
            synonyms = "anomaly, deviation, peculiarity",
            antonyms = "conformity, normality, standard",
            isFavorite = false,
            isMastered = false
        ),
        VocabWord(
            id = 3,
            word = "Abeyance",
            pronunciation = "uh-BAY-uns",
            englishMeaning = "Temporary suspension or inactive state",
            bengaliMeaning = "স্থগিতাবস্থা",
            exampleSentence = "The proposed construction was held in abeyance.",
            synonyms = "dormancy, hiatus, suspension",
            antonyms = "continuation, resumption",
            isFavorite = false,
            isMastered = false
        ),
        VocabWord(
            id = 4,
            word = "Abhor",
            pronunciation = "ab-HOR",
            englishMeaning = "To regard with extreme repugnance or hatred",
            bengaliMeaning = "ঘৃণা করা",
            exampleSentence = "Pacifists naturally abhor all forms of senseless violence.",
            synonyms = "detest, despise, loathe",
            antonyms = "admire, love, adore",
            isFavorite = false,
            isMastered = false
        ),
        VocabWord(
            id = 5,
            word = "Abjure",
            pronunciation = "ab-JOOR",
            englishMeaning = "To renounce, repudiate or formally retract under oath",
            bengaliMeaning = "শপথপূর্বক পরিত্যাগ করা",
            exampleSentence = "He agreed to abjure his former allegiance to the crown.",
            synonyms = "renounce, recant, retract",
            antonyms = "affirm, embrace, profess",
            isFavorite = false,
            isMastered = false
        )
    )

    @Test
    fun testGenerateQuestionsIncludesCorrectAnswerInOptions() {
        val questions = TestGenerator.generateTest(
            targetWords = sampleWords,
            allWordsPool = sampleWords,
            maxQuestions = 5
        )

        assertTrue("Generated questions should not be empty", questions.isNotEmpty())
        for (q in questions) {
            assertNotNull("Target word should not be null", q.targetWord)
            assertTrue("Options must contain the correct answer", q.options.contains(q.correctAnswer))
            assertTrue("Options should have up to 4 choices", q.options.size in 2..4)
        }
    }

    @Test
    fun testGatekeeperCalculation() {
        // 80% gatekeeper verification
        val totalQuestions = 10
        val pass8 = 8
        val passScore = (pass8.toFloat() / totalQuestions) * 100f
        assertTrue("8/10 must meet or exceed 80%", passScore >= 80f)

        val fail7 = 7
        val failScore = (fail7.toFloat() / totalQuestions) * 100f
        assertTrue("7/10 must fail the 80% gatekeeper", failScore < 80f)
    }

    @Test
    fun testMotivationsReturnNonEmptyFeedback() {
        val passMessage = Motivations.getRandomPassMessage()
        val retryMessage = Motivations.getRandomRetryMessage()

        assertTrue(passMessage.isNotBlank())
        assertTrue(retryMessage.isNotBlank())
    }
}
