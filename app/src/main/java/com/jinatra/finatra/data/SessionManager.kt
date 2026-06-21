package com.jinatra.finatra.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Process-wide session state. [decoy] is set when the user unlocks with the decoy PIN
 * (PRD 6.13): while active, the repository hides all real data and discards writes, so the
 * app presents a clean, empty state. Reset to false on every re-lock.
 */
@Singleton
class SessionManager @Inject constructor() {
    private val _decoy = MutableStateFlow(false)

    /** Observable decoy-mode state for reactive consumers. */
    val decoy = _decoy.asStateFlow()

    /** Current decoy-mode flag, read synchronously. */
    val isDecoy: Boolean get() = _decoy.value

    /** Toggles decoy mode on or off. */
    fun setDecoy(on: Boolean) { _decoy.value = on }
}
