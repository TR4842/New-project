package com.example.util

object Motivations {

    private val PASS_MESSAGES = listOf(
        "Outstanding work! Your vocabulary is expanding exponentially.",
        "You're crushing it! Word mastery in pure action.",
        "Vocabulary master! You conquered the 80% gatekeeper with flying colors.",
        "Magnificent intellect! The next chapter of your lexicon awaits.",
        "Brilliant performance! Next day's words are now unlocked.",
        "Sharp as a razor! 80%+ recall precision achieved.",
        "Exemplary dedication! The path to lexical eloquence is yours.",
        "Phenomenal recall! Another milestone crossed in Word Smart.",
        "Astounding comprehension! Your intellectual lexicon grows deeper.",
        "Flawless discipline! Word Smart 1 has met its match today."
    )

    private val RETRY_MESSAGES = listOf(
        "Close effort! Review today's reading chunk and conquer the 80% bar on your retake.",
        "The 80% gatekeeper stands, but perseverance unlocks mastery. Check your weak words in the Mistake Bank and try again!",
        "Every master was once an apprentice. Review the day's definitions and retake with fresh eyes!",
        "Almost there! The questions have reshuffled. Sharpen your focus and seize the next day's unlock."
    )

    fun getRandomPassMessage(): String = PASS_MESSAGES.random()

    fun getRandomRetryMessage(): String = RETRY_MESSAGES.random()
}
