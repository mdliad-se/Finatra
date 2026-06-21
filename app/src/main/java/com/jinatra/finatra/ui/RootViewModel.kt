package com.jinatra.finatra.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.SessionManager
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

/**
 * Root-level ViewModel that owns app-wide settings and the security lock state.
 *
 * Exposes [settings] for the UI and drives the lock gate (PRD 6.11/6.13): cold-start lock,
 * background auto-lock, and the usage-lock time window. Lifecycle hooks [onStart]/[onStop] feed it
 * foreground/background transitions; PIN/biometric unlocks flow through [unlock]/[submitPin].
 */
@HiltViewModel
class RootViewModel @Inject constructor(
    settingsRepo: SettingsRepository,
    private val secure: SecurePrefs,
    private val sessionManager: SessionManager,
) : ViewModel() {

    /** App-wide user settings, kept hot for synchronous reads via [UserSettings] defaults. */
    val settings = settingsRepo.settings.stateIn(
        viewModelScope, SharingStarted.Eagerly, UserSettings()
    )

    // ── App lock (PRD 6.11) ──────────────────────────────────────────────
    // Start locked when a PIN exists; biometric-only locking is applied once settings load (init).
    private val _locked = MutableStateFlow(secure.hasPin())
    /** Whether the lock gate should be shown over the app content. */
    val locked = _locked.asStateFlow()
    private var authenticated = false  // true after a successful unlock this session
    private var pausedAt = 0L          // wall-clock time the app was last backgrounded

    /** Security is on when either a PIN is set or biometric unlock is enabled. */
    private val securityEnabled: Boolean
        get() = secure.hasPin() || settings.value.biometricEnabled

    /** True while the current hour falls inside the configured usage-lock window (PRD 6.13). */
    private fun usageLockActive(now: Long = System.currentTimeMillis()): Boolean {
        val s = settings.value
        if (!s.usageLockEnabled || !securityEnabled) return false
        val start = s.usageLockStartHour
        val end = s.usageLockEndHour
        if (start == end) return false
        val h = java.util.Calendar.getInstance().apply { timeInMillis = now }.get(java.util.Calendar.HOUR_OF_DAY)
        // Handle windows that wrap past midnight (start > end), e.g. 22 → 6.
        return if (start < end) h in start until end else h >= start || h < end
    }

    init {
        // Lock on cold start once we know security is enabled (settings load async).
        viewModelScope.launch {
            settingsRepo.settings.collect { s ->
                if (!authenticated && (secure.hasPin() || s.biometricEnabled)) _locked.value = true
            }
        }
    }

    /** Record when the app was backgrounded, used to compute the auto-lock timeout in [onStart]. */
    fun onStop() { pausedAt = System.currentTimeMillis() }

    /** Re-lock if backgrounded past auto-lock, or whenever inside the usage-lock window. */
    fun onStart() {
        if (!securityEnabled) { _locked.value = false; return }
        // Usage-lock window takes priority: always re-lock while inside it.
        if (usageLockActive()) {
            authenticated = false
            sessionManager.setDecoy(false)
            _locked.value = true
            return
        }
        // Otherwise re-lock only if backgrounded longer than the configured auto-lock timeout.
        val mins = settings.value.autoLockMinutes
        if (mins >= 0) {
            val timeoutMs = mins * 60_000L
            if (pausedAt > 0L && System.currentTimeMillis() - pausedAt >= timeoutMs) {
                authenticated = false
                sessionManager.setDecoy(false)
                _locked.value = true
            }
        }
    }

    /** Real unlock (biometric or correct PIN). Clears any decoy session. */
    fun unlock() { authenticated = true; sessionManager.setDecoy(false); _locked.value = false }

    /**
     * Verify [pin] against the real or decoy PIN (PRD 6.13). On the decoy PIN the app unlocks
     * into a clean, empty state. Returns true if either matched.
     */
    fun submitPin(pin: String): Boolean {
        if (PinHasher.verify(pin, secure.pinHash)) {
            // Transparently upgrade an old unsalted SHA-256 hash to the salted PBKDF2 format.
            if (PinHasher.isLegacy(secure.pinHash)) secure.pinHash = PinHasher.hash(pin)
            unlock(); return true
        }
        if (PinHasher.verify(pin, secure.decoyPinHash)) {
            sessionManager.setDecoy(true)
            authenticated = true
            _locked.value = false
            return true
        }
        return false
    }

    /** Whether a PIN is configured (drives whether the lock screen shows PIN entry). */
    fun hasPin(): Boolean = secure.hasPin()
    /** Whether biometric unlock is enabled in settings. */
    fun biometricEnabled(): Boolean = settings.value.biometricEnabled
}
