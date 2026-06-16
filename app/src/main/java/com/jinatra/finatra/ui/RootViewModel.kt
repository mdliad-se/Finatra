package com.jinatra.finatra.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.prefs.SecurePrefs
import com.jinatra.finatra.data.prefs.SettingsRepository
import com.jinatra.finatra.data.prefs.UserSettings
import com.jinatra.finatra.util.PinHasher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    settingsRepo: SettingsRepository,
    private val secure: SecurePrefs,
) : ViewModel() {

    val settings = settingsRepo.settings.stateIn(
        viewModelScope, SharingStarted.Eagerly, UserSettings()
    )

    // ── App lock (PRD 6.11) ──────────────────────────────────────────────
    private val _locked = MutableStateFlow(secure.hasPin())
    val locked = _locked.asStateFlow()
    private var authenticated = false
    private var pausedAt = 0L

    private val securityEnabled: Boolean
        get() = secure.hasPin() || settings.value.biometricEnabled

    init {
        // Lock on cold start once we know security is enabled (settings load async).
        viewModelScope.launch {
            settingsRepo.settings.collect { s ->
                if (!authenticated && (secure.hasPin() || s.biometricEnabled)) _locked.value = true
            }
        }
    }

    fun onStop() { pausedAt = System.currentTimeMillis() }

    /** Re-lock if the app was backgrounded longer than the auto-lock window. */
    fun onStart() {
        if (!securityEnabled) { _locked.value = false; return }
        val mins = settings.value.autoLockMinutes
        if (pausedAt > 0L && mins > 0 && System.currentTimeMillis() - pausedAt >= mins * 60_000L) {
            authenticated = false
            _locked.value = true
        }
    }

    fun unlock() { authenticated = true; _locked.value = false }

    fun hasPin(): Boolean = secure.hasPin()
    fun biometricEnabled(): Boolean = settings.value.biometricEnabled
    fun verifyPin(pin: String): Boolean = PinHasher.verify(pin, secure.pinHash)
}
