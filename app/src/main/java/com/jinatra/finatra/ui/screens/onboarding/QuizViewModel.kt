package com.jinatra.finatra.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.prefs.SettingsRepository
import com.jinatra.finatra.data.prefs.SpendingPersonality
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for [QuizScreen]. Persists the quiz outcome to settings and signals completion.
 */
@HiltViewModel
class QuizViewModel @Inject constructor(
    private val settings: SettingsRepository,
) : ViewModel() {

    /** Saves the computed spending personality, then invokes [onDone]. */
    fun finish(result: SpendingPersonality, onDone: () -> Unit) {
        viewModelScope.launch { settings.setPersonality(result); onDone() }
    }

    /** Marks the quiz as done without recording a personality, then invokes [onDone]. */
    fun skip(onDone: () -> Unit) {
        viewModelScope.launch { settings.setQuizDone(true); onDone() }
    }
}
