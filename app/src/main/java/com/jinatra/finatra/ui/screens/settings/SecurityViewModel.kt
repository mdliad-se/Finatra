package com.jinatra.finatra.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.prefs.SecurePrefs
import com.jinatra.finatra.data.prefs.SettingsRepository
import com.jinatra.finatra.data.prefs.UserSettings
import com.jinatra.finatra.util.PinHasher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Security screen.
 *
 * Owns app-lock configuration backed by two stores:
 *  - [SecurePrefs] for the secret material: the real PIN hash and the optional decoy PIN
 *    hash. PINs are never stored in plaintext — only their [PinHasher] hashes.
 *  - [SettingsRepository] for non-secret toggles: biometric unlock, auto-lock delay and
 *    the usage-lock schedule, exposed reactively via [state].
 */
@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val secure: SecurePrefs,
) : ViewModel() {

    val state = settings.settings.stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    /** Whether a real app-lock PIN is configured. */
    fun hasPin() = secure.hasPin()
    /** Stores the salted hash of [pin] as the real PIN. */
    fun setPin(pin: String) { secure.pinHash = PinHasher.hash(pin) }
    /** Removes the real PIN; also clears the decoy, which has no meaning without a real PIN. */
    fun clearPin() { secure.pinHash = null; secure.decoyPinHash = null }

    // Decoy PIN (PRD 6.13) — must differ from the real PIN.
    /** Whether a decoy PIN is configured. */
    fun hasDecoyPin() = secure.hasDecoyPin()
    /** True if [pin] matches the real PIN — used to forbid setting a decoy equal to the real one. */
    fun decoyMatchesReal(pin: String) = PinHasher.verify(pin, secure.pinHash)
    /** Stores the salted hash of [pin] as the decoy PIN. */
    fun setDecoyPin(pin: String) { secure.decoyPinHash = PinHasher.hash(pin) }
    /** Removes the decoy PIN. */
    fun clearDecoyPin() { secure.decoyPinHash = null }
    /** Enables/disables biometric unlock. */
    fun setBiometric(on: Boolean) = viewModelScope.launch { settings.setBiometric(on) }
    /** Sets the idle minutes before auto-lock (0 = immediately, -1 = never). */
    fun setAutoLock(minutes: Int) = viewModelScope.launch { settings.setAutoLock(minutes) }

    // Usage lock schedule (PRD 6.13) — keeps the app locked during a set range of hours.
    /** Enables/disables the scheduled usage lock. */
    fun setUsageLock(on: Boolean) = viewModelScope.launch { settings.setUsageLock(on) }
    /** Sets the start hour (0–23) of the usage-lock window. */
    fun setUsageLockStart(hour: Int) = viewModelScope.launch { settings.setUsageLockStart(hour) }
    /** Sets the end hour (0–23) of the usage-lock window. */
    fun setUsageLockEnd(hour: Int) = viewModelScope.launch { settings.setUsageLockEnd(hour) }
}
