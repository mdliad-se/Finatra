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

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val secure: SecurePrefs,
) : ViewModel() {

    val state = settings.settings.stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    fun hasPin() = secure.hasPin()
    fun setPin(pin: String) { secure.pinHash = PinHasher.hash(pin) }
    fun clearPin() { secure.pinHash = null }
    fun setBiometric(on: Boolean) = viewModelScope.launch { settings.setBiometric(on) }
    fun setAutoLock(minutes: Int) = viewModelScope.launch { settings.setAutoLock(minutes) }
}
