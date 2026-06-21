package com.jinatra.finatra.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.prefs.SettingsRepository
import com.jinatra.finatra.data.prefs.ThemeMode
import com.jinatra.finatra.data.prefs.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the main Settings screen.
 *
 * Exposes the persisted [UserSettings] as reactive [settings] state and provides one setter
 * per user-toggleable preference (appearance, locale, notifications, screenshot blocking).
 * Each setter delegates to [SettingsRepository] on the view-model scope; security PINs and
 * data backup live on dedicated sub-screens, not here.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository,
) : ViewModel() {

    val settings = repo.settings.stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { repo.setThemeMode(mode) }
    fun setDynamic(on: Boolean) = viewModelScope.launch { repo.setDynamicColor(on) }
    fun setCurrency(code: String) = viewModelScope.launch { repo.setBaseCurrency(code) }
    fun setLanguage(code: String) = viewModelScope.launch { repo.setLanguage(code) }
    fun setScreenshotPrevention(on: Boolean) = viewModelScope.launch { repo.setScreenshotPrevention(on) }
    fun setNotifBudget(on: Boolean) = viewModelScope.launch { repo.setNotifBudget(on) }
    fun setNotifRecurring(on: Boolean) = viewModelScope.launch { repo.setNotifRecurring(on) }
    fun setNotifLowBalance(on: Boolean) = viewModelScope.launch { repo.setNotifLowBalance(on) }
    fun setNotifWeekly(on: Boolean) = viewModelScope.launch { repo.setNotifWeekly(on) }
    fun setNotifMonthly(on: Boolean) = viewModelScope.launch { repo.setNotifMonthly(on) }
}
