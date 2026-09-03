package com.example.util

import com.example.data.QuestionType
import com.example.data.TestQuestion
import com.example.data.VocabWord
import java.util.UUID

object TestGenerator {

    /**
     * Generates a randomized test from the target pool of words,
     * using background pool for distractors.
     */
    fun generateTest(
        targetWords: List<VocabWord>,
        allWordsPool: List<VocabWord>,
        maxQuestions: Int = 10
    ): List<TestQuestion> {
        if (targetWords.isEmpty()) return emptyList()

        val shuffledTargets = targetWords.shuffled().take(maxQuestions.coerceAtMost(targetWords.size))
        val questions = mutableListOf<TestQuestion>()

        shuffledTargets.forEachIndexed { index, targetWord ->
            // Distractors pool excluding the target word
            val distractorsPool = allWordsPool.filter { it.id != targetWord.id }.shuffled()

            // Choose question type based on available word data
            val candidateTypes = mutableListOf<QuestionType>()
            candidateTypes.add(QuestionType.WORD_MEANING)

            if (targetWord.exampleSentence.isNotBlank() && targetWord.exampleSentence.contains(targetWord.word, ignoreCase = true)) {
                candidateTypes.add(QuestionType.SENTENCE_COMPLETION)
            }
            val synonymList = parseCommaList(targetWord.synonyms)
            if (synonymList.isNotEmpty()) {
                candidateTypes.add(QuestionType.SYNONYM)
            }
            val antonymList = parseCommaList(targetWord.antonyms)
            if (antonymList.isNotEmpty()) {
                candidateTypes.add(QuestionType.ANTONYM)
            }

            val chosenType = candidateTypes.random()

            when (chosenType) {
                QuestionType.SENTENCE_COMPLETION -> {
                    val regex = Regex("\\b${Regex.escape(targetWord.word)}\\w*", RegexOption.IGNORE_CASE)
                    val cloze = targetWord.exampleSentence.replace(regex, "_______")
                    val distractors = distractorsPool.take(3).map { it.word }
                    val options = (distractors + targetWord.word).shuffled()
                    questions.add(
                        TestQuestion(
                            id = UUID.randomUUID().toString(),
                            type = QuestionType.SENTENCE_COMPLETION,
                            targetWord = targetWord,
                            prompt = "Select the word that best completes the sentence:",
                            clozeSentence = cloze,
                            options = options,
                            correctAnswer = targetWord.word,
                            explanation = "\"${targetWord.word}\": ${targetWord.englishMeaning}"
                        )
                    )
                }
                QuestionType.SYNONYM -> {
                    val correctSynonym = synonymList.random().trim()
                    val distractorSynonyms = distractorsPool
                        .flatMap { parseCommaList(it.synonyms) }
                        .filter { it.isNotBlank() && !it.equals(correctSynonym, ignoreCase = true) }
                        .distinct()
                        .take(3)

                    val distractors = if (distractorSynonyms.size == 3) {
                        distractorSynonyms
                    } else {
                        distractorsPool.take(3).map { it.word }
                    }

                    val options = (distractors + correctSynonym).shuffled()
                    questions.add(
                        TestQuestion(
                            id = UUID.randomUUID().toString(),
                            type = QuestionType.SYNONYM,
                            targetWord = targetWord,
                            prompt = "Which of the following is a synonym of \"${targetWord.word}\"?",
                            clozeSentence = null,
                            options = options,
                            correctAnswer = correctSynonym,
                            explanation = "\"${targetWord.word}\" means ${targetWord.englishMeaning}. Synonym: $correctSynonym"
                        )
                    )
                }
                QuestionType.ANTONYM -> {
                    val correctAntonym = antonymList.random().trim()
                    val distractorAntonyms = distractorsPool
                        .flatMap { parseCommaList(it.antonyms) }
                        .filter { it.isNotBlank() && !it.equals(correctAntonym, ignoreCase = true) }
                        .distinct()
                        .take(3)

                    val distractors = if (distractorAntonyms.size == 3) {
                        distractorAntonyms
                    } else {
                        distractorsPool.take(3).map { it.word }
                    }

                    val options = (distractors + correctAntonym).shuffled()
                    questions.add(
                        TestQuestion(
                            id = UUID.randomUUID().toString(),
                            type = QuestionType.ANTONYM,
                            targetWord = targetWord,
                            prompt = "Which of the following is an antonym (opposite) of \"${targetWord.word}\"?",
                            clozeSentence = null,
                            options = options,
                            correctAnswer = correctAntonym,
                            explanation = "\"${targetWord.word}\" (${targetWord.englishMeaning}) is opposite in meaning to $correctAntonym"
                        )
                    )
                }
                QuestionType.WORD_MEANING -> {
                    val useBengali = targetWord.bengaliMeaning.isNotBlank() && (0..1).random() == 1
                    if (useBengali) {
                        val correct = targetWord.bengaliMeaning
                        val distractors = distractorsPool
                            .filter { it.bengaliMeaning.isNotBlank() && it.bengaliMeaning != correct }
                            .take(3)
                            .map { it.bengaliMeaning }

                        val fallbackDistractors = if (distractors.size < 3) {
                            distractorsPool.take(3).map { it.englishMeaning }
                        } else distractors

                        val options = (fallbackDistractors.take(3) + correct).shuffled()
                        questions.add(
                            TestQuestion(
                                id = UUID.randomUUID().toString(),
                                type = QuestionType.WORD_MEANING,
                                targetWord = targetWord,
                                prompt = "What is the Bengali meaning (বাংলা অর্থ) of \"${targetWord.word}\"?",
                                clozeSentence = null,
                                options = options,
                                correctAnswer = correct,
                                explanation = "\"${targetWord.word}\" (${targetWord.pronunciation}) = $correct\nEnglish: ${targetWord.englishMeaning}"
                            )
                        )
                    } else {
                        val correct = targetWord.englishMeaning
                        val distractors = distractorsPool
                            .filter { it.englishMeaning.isNotBlank() && it.englishMeaning != correct }
                            .take(3)
                            .map { it.englishMeaning }

                        val options = (distractors + correct).shuffled()
                        questions.add(
                            TestQuestion(
                                id = UUID.randomUUID().toString(),
                                type = QuestionType.WORD_MEANING,
                                targetWord = targetWord,
                                prompt = "What is the English meaning of \"${targetWord.word}\"?",
                                clozeSentence = null,
                                options = options,
                                correctAnswer = correct,
                                explanation = "\"${targetWord.word}\" (${targetWord.pronunciation}) means $correct"
                            )
                        )
                    }
                }
            }
        }

        return questions.shuffled()
    }

    private fun parseCommaList(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        return text.split(",", ";")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }
}
