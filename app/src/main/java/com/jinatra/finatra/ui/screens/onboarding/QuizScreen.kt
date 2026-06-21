package com.jinatra.finatra.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jinatra.finatra.data.prefs.SpendingPersonality
import com.jinatra.finatra.ui.components.ExpressiveCard
import com.jinatra.finatra.ui.components.OverlineLabel

/** One answer choice; [vote] is the spending personality this option counts toward. */
private data class QuizOption(val text: String, val vote: SpendingPersonality)
/** A quiz question with its prompt and the list of selectable [QuizOption]s. */
private data class QuizQuestion(val prompt: String, val options: List<QuizOption>)

// The fixed financial-personality questionnaire. Each question offers one option per
// personality, so every answer casts a single vote toward that personality.
private val QUESTIONS = listOf(
    QuizQuestion(
        "When money comes in, you usually…",
        listOf(
            QuizOption("Save most of it first", SpendingPersonality.SAVER),
            QuizOption("Split between saving and spending", SpendingPersonality.BALANCED),
            QuizOption("Spend on what I enjoy", SpendingPersonality.SPENDER),
            QuizOption("Buy something before I plan", SpendingPersonality.IMPULSIVE),
        ),
    ),
    QuizQuestion(
        "How do you feel about budgets?",
        listOf(
            QuizOption("I love a tight, detailed budget", SpendingPersonality.SAVER),
            QuizOption("A loose plan works for me", SpendingPersonality.BALANCED),
            QuizOption("Budgets feel restrictive", SpendingPersonality.SPENDER),
            QuizOption("I rarely stick to one", SpendingPersonality.IMPULSIVE),
        ),
    ),
    QuizQuestion(
        "An unexpected sale on something you want…",
        listOf(
            QuizOption("Skip it — not in the plan", SpendingPersonality.SAVER),
            QuizOption("Consider if it fits my budget", SpendingPersonality.BALANCED),
            QuizOption("Probably treat myself", SpendingPersonality.SPENDER),
            QuizOption("Buy it right away", SpendingPersonality.IMPULSIVE),
        ),
    ),
    QuizQuestion(
        "At month end, your balance is usually…",
        listOf(
            QuizOption("Comfortably positive", SpendingPersonality.SAVER),
            QuizOption("Roughly on track", SpendingPersonality.BALANCED),
            QuizOption("Most of it spent", SpendingPersonality.SPENDER),
            QuizOption("A surprise either way", SpendingPersonality.IMPULSIVE),
        ),
    ),
)

/**
 * Financial-personality quiz shown during onboarding. Walks the user through the fixed
 * [QUESTIONS] one at a time with progress dots and single-choice answers. On the last
 * question it tallies the votes into a [SpendingPersonality] and saves it via
 * [QuizViewModel]; the user can also skip at any point. [onDone] advances out of the quiz.
 */
@Composable
fun QuizScreen(onDone: () -> Unit, vm: QuizViewModel = hiltViewModel()) {
    var index by remember { mutableIntStateOf(0) }
    val answers = remember { mutableStateMapOf<Int, Int>() }   // questionIndex -> optionIndex
    val q = QUESTIONS[index]
    val selected = answers[index]
    val isLast = index == QUESTIONS.lastIndex

    // Tally each answer's vote and return the most-voted personality (ties broken by map
    // iteration order); defaults to BALANCED when nothing has been answered.
    fun computeResult(): SpendingPersonality {
        val tally = HashMap<SpendingPersonality, Int>()
        answers.forEach { (qi, oi) ->
            val vote = QUESTIONS[qi].options[oi].vote
            tally[vote] = (tally[vote] ?: 0) + 1
        }
        return tally.maxByOrNull { it.value }?.key ?: SpendingPersonality.BALANCED
    }

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        Text("Quick quiz", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Text("4 questions to tune Finatra to your style. Skip anytime.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        // Progress dots: every question up to and including the current one is "active".
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            QUESTIONS.indices.forEach { i ->
                val active = i <= index
                androidx.compose.foundation.layout.Box(
                    Modifier.height(6.dp).clip(CircleShape)
                        .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .weight(1f),
                )
            }
        }

        ExpressiveCard(Modifier.fillMaxWidth()) {
            OverlineLabel("Question ${index + 1} of ${QUESTIONS.size}")
            Spacer(Modifier.height(8.dp))
            Text(q.prompt, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            q.options.forEachIndexed { oi, opt ->
                Row(
                    Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).clickable { answers[index] = oi }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = selected == oi, onClick = { answers[index] = oi })
                    Text(opt.text, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Advances to the next question, or on the last one scores the quiz and finishes.
        Button(
            onClick = { if (isLast) vm.finish(computeResult(), onDone) else index++ },
            enabled = selected != null,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (isLast) "See my style" else "Next") }

        TextButton(onClick = { vm.skip(onDone) }, modifier = Modifier.fillMaxWidth()) { Text("Skip") }
    }
}
