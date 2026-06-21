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

/** App theme preference; SYSTEM follows the OS light/dark setting. */
enum class ThemeMode { LIGHT, DARK, SYSTEM }

/** Spending style from the onboarding quiz (PRD 6.17); drives default budgets + AI coaching. */
enum class SpendingPersonality { SAVER, BALANCED, SPENDER, IMPULSIVE, UNKNOWN }

/** Immutable snapshot of all user preferences; the defaults here are applied when a key is unset. */
data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,   // default off: show the fixed Finatra brand palette
    val baseCurrency: String = "USD",
    val language: String = "en",   // BCP-47 base: en | bn (PRD 6.20)
    val onboardingDone: Boolean = false,
    val quizDone: Boolean = false,
    val personality: SpendingPersonality = SpendingPersonality.UNKNOWN,
    val screenshotPrevention: Boolean = true,
    val autoLockMinutes: Int = 0,   // -1 = never, 0 = immediately, >0 = minutes
    val biometricEnabled: Boolean = false,
    // App usage lock schedule (PRD 6.13) — lock the app during [start, end) hour window.
    val usageLockEnabled: Boolean = false,
    val usageLockStartHour: Int = 22,
    val usageLockEndHour: Int = 6,
    // Notification preferences (PRD 6.10) — per-type toggles
    val notifBudget: Boolean = true,
    val notifRecurring: Boolean = true,
    val notifLowBalance: Boolean = true,
    val notifWeekly: Boolean = true,
    val notifMonthly: Boolean = true,
    val userName: String = "User",
)

// Single process-wide DataStore instance ("finatra_settings.preferences_pb") for non-sensitive prefs.
private val Context.dataStore by preferencesDataStore(name = "finatra_settings")

/**
 * Repository over Preferences DataStore for non-sensitive user settings.
 *
 * Exposes preferences reactively via [settings] (a [Flow] that re-emits on every change) and provides
 * `suspend` setters that persist atomically. Sensitive values (PIN hash, API key) live in
 * [com.jinatra.finatra.data.prefs.SecurePrefs] instead, not here.
 */
@Singleton
class SettingsRepository @Inject constructor(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val DYNAMIC = booleanPreferencesKey("dynamic_color")
        val CURRENCY = stringPreferencesKey("base_currency")
        val LANGUAGE = stringPreferencesKey("language")
        val ONBOARDED = booleanPreferencesKey("onboarding_done")
        val QUIZ_DONE = booleanPreferencesKey("quiz_done")
        val PERSONALITY = stringPreferencesKey("personality")
        val SCREENSHOT = booleanPreferencesKey("screenshot_prevention")
        val AUTOLOCK = intPreferencesKey("auto_lock_minutes")
        val BIOMETRIC = booleanPreferencesKey("biometric_enabled")
        val USAGE_LOCK = booleanPreferencesKey("usage_lock_enabled")
        val USAGE_LOCK_START = intPreferencesKey("usage_lock_start")
        val USAGE_LOCK_END = intPreferencesKey("usage_lock_end")
        val N_BUDGET = booleanPreferencesKey("notif_budget")
        val N_RECURRING = booleanPreferencesKey("notif_recurring")
        val N_LOWBAL = booleanPreferencesKey("notif_low_balance")
        val N_WEEKLY = booleanPreferencesKey("notif_weekly")
        val N_MONTHLY = booleanPreferencesKey("notif_monthly")
        val USER_NAME = stringPreferencesKey("user_name")
    }

    /** Reactive stream of the current [UserSettings]; emits the full snapshot on each preference change. */
    val settings: Flow<UserSettings> = context.dataStore.data.map { p ->
        UserSettings(
            themeMode = ThemeMode.valueOf(p[Keys.THEME] ?: ThemeMode.SYSTEM.name),
            dynamicColor = p[Keys.DYNAMIC] ?: false,
            baseCurrency = p[Keys.CURRENCY] ?: "USD",
            language = p[Keys.LANGUAGE] ?: "en",
            onboardingDone = p[Keys.ONBOARDED] ?: false,
            quizDone = p[Keys.QUIZ_DONE] ?: false,
            personality = SpendingPersonality.valueOf(p[Keys.PERSONALITY] ?: SpendingPersonality.UNKNOWN.name),
            screenshotPrevention = p[Keys.SCREENSHOT] ?: true,
            autoLockMinutes = p[Keys.AUTOLOCK] ?: 0,
            biometricEnabled = p[Keys.BIOMETRIC] ?: false,
            usageLockEnabled = p[Keys.USAGE_LOCK] ?: false,
            usageLockStartHour = p[Keys.USAGE_LOCK_START] ?: 22,
            usageLockEndHour = p[Keys.USAGE_LOCK_END] ?: 6,
            notifBudget = p[Keys.N_BUDGET] ?: true,
            notifRecurring = p[Keys.N_RECURRING] ?: true,
            notifLowBalance = p[Keys.N_LOWBAL] ?: true,
            notifWeekly = p[Keys.N_WEEKLY] ?: true,
            notifMonthly = p[Keys.N_MONTHLY] ?: true,
            userName = p[Keys.USER_NAME] ?: "User",
        )
    }

    // Persisting setters: each updates one key (enums stored by name) and suspends until written.
    suspend fun setThemeMode(mode: ThemeMode) = edit { it[Keys.THEME] = mode.name }
    suspend fun setDynamicColor(on: Boolean) = edit { it[Keys.DYNAMIC] = on }
    suspend fun setBaseCurrency(code: String) = edit { it[Keys.CURRENCY] = code }
    suspend fun setLanguage(code: String) = edit { it[Keys.LANGUAGE] = code }
    suspend fun setOnboardingDone(done: Boolean) = edit { it[Keys.ONBOARDED] = done }
    /** Stores the quiz [personality] and also marks the onboarding quiz complete in the same write. */
    suspend fun setPersonality(p: SpendingPersonality) = edit {
        it[Keys.PERSONALITY] = p.name
        it[Keys.QUIZ_DONE] = true
    }
    suspend fun setQuizDone(done: Boolean) = edit { it[Keys.QUIZ_DONE] = done }
    suspend fun setScreenshotPrevention(on: Boolean) = edit { it[Keys.SCREENSHOT] = on }
    suspend fun setAutoLock(minutes: Int) = edit { it[Keys.AUTOLOCK] = minutes }
    suspend fun setBiometric(on: Boolean) = edit { it[Keys.BIOMETRIC] = on }
    suspend fun setUsageLock(on: Boolean) = edit { it[Keys.USAGE_LOCK] = on }
    suspend fun setUsageLockStart(hour: Int) = edit { it[Keys.USAGE_LOCK_START] = hour }
    suspend fun setUsageLockEnd(hour: Int) = edit { it[Keys.USAGE_LOCK_END] = hour }
    suspend fun setNotifBudget(on: Boolean) = edit { it[Keys.N_BUDGET] = on }
    suspend fun setNotifRecurring(on: Boolean) = edit { it[Keys.N_RECURRING] = on }
    suspend fun setNotifLowBalance(on: Boolean) = edit { it[Keys.N_LOWBAL] = on }
    suspend fun setNotifWeekly(on: Boolean) = edit { it[Keys.N_WEEKLY] = on }
    suspend fun setNotifMonthly(on: Boolean) = edit { it[Keys.N_MONTHLY] = on }
    suspend fun setUserName(name: String) = edit { it[Keys.USER_NAME] = name }

    /** Shared transactional editor: applies [block] to the mutable preferences in a single atomic write. */
    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }
}
