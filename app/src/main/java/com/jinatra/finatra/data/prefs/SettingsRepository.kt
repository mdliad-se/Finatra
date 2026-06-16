package com.jinatra.finatra.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode { LIGHT, DARK, SYSTEM }

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,   // default off: show the fixed Finatra brand palette
    val baseCurrency: String = "USD",
    val onboardingDone: Boolean = false,
    val screenshotPrevention: Boolean = true,
    val autoLockMinutes: Int = 5,   // 0 = never
    val biometricEnabled: Boolean = false,
    // Notification preferences (PRD 6.10) — per-type toggles
    val notifBudget: Boolean = true,
    val notifRecurring: Boolean = true,
    val notifLowBalance: Boolean = true,
    val notifWeekly: Boolean = true,
    val notifMonthly: Boolean = true,
)

private val Context.dataStore by preferencesDataStore(name = "finatra_settings")

@Singleton
class SettingsRepository @Inject constructor(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val DYNAMIC = booleanPreferencesKey("dynamic_color")
        val CURRENCY = stringPreferencesKey("base_currency")
        val ONBOARDED = booleanPreferencesKey("onboarding_done")
        val SCREENSHOT = booleanPreferencesKey("screenshot_prevention")
        val AUTOLOCK = intPreferencesKey("auto_lock_minutes")
        val BIOMETRIC = booleanPreferencesKey("biometric_enabled")
        val N_BUDGET = booleanPreferencesKey("notif_budget")
        val N_RECURRING = booleanPreferencesKey("notif_recurring")
        val N_LOWBAL = booleanPreferencesKey("notif_low_balance")
        val N_WEEKLY = booleanPreferencesKey("notif_weekly")
        val N_MONTHLY = booleanPreferencesKey("notif_monthly")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { p ->
        UserSettings(
            themeMode = ThemeMode.valueOf(p[Keys.THEME] ?: ThemeMode.SYSTEM.name),
            dynamicColor = p[Keys.DYNAMIC] ?: false,
            baseCurrency = p[Keys.CURRENCY] ?: "USD",
            onboardingDone = p[Keys.ONBOARDED] ?: false,
            screenshotPrevention = p[Keys.SCREENSHOT] ?: true,
            autoLockMinutes = p[Keys.AUTOLOCK] ?: 5,
            biometricEnabled = p[Keys.BIOMETRIC] ?: false,
            notifBudget = p[Keys.N_BUDGET] ?: true,
            notifRecurring = p[Keys.N_RECURRING] ?: true,
            notifLowBalance = p[Keys.N_LOWBAL] ?: true,
            notifWeekly = p[Keys.N_WEEKLY] ?: true,
            notifMonthly = p[Keys.N_MONTHLY] ?: true,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) = edit { it[Keys.THEME] = mode.name }
    suspend fun setDynamicColor(on: Boolean) = edit { it[Keys.DYNAMIC] = on }
    suspend fun setBaseCurrency(code: String) = edit { it[Keys.CURRENCY] = code }
    suspend fun setOnboardingDone(done: Boolean) = edit { it[Keys.ONBOARDED] = done }
    suspend fun setScreenshotPrevention(on: Boolean) = edit { it[Keys.SCREENSHOT] = on }
    suspend fun setAutoLock(minutes: Int) = edit { it[Keys.AUTOLOCK] = minutes }
    suspend fun setBiometric(on: Boolean) = edit { it[Keys.BIOMETRIC] = on }
    suspend fun setNotifBudget(on: Boolean) = edit { it[Keys.N_BUDGET] = on }
    suspend fun setNotifRecurring(on: Boolean) = edit { it[Keys.N_RECURRING] = on }
    suspend fun setNotifLowBalance(on: Boolean) = edit { it[Keys.N_LOWBAL] = on }
    suspend fun setNotifWeekly(on: Boolean) = edit { it[Keys.N_WEEKLY] = on }
    suspend fun setNotifMonthly(on: Boolean) = edit { it[Keys.N_MONTHLY] = on }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }
}
