package com.jinatra.finatra.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.local.entity.AccountEntity
import com.jinatra.finatra.data.local.entity.AccountType
import com.jinatra.finatra.data.prefs.SecurePrefs
import com.jinatra.finatra.data.prefs.SettingsRepository
import com.jinatra.finatra.data.repository.FinanceRepository
import com.jinatra.finatra.util.PinHasher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for [OnboardingScreen]. Persists the user's initial setup across the settings
 * store, the secure (encrypted) prefs, and the finance repository, then marks onboarding
 * complete. Validates inputs along the way so that skipped/blank fields are handled safely.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repo: FinanceRepository,
    private val settings: SettingsRepository,
    private val secure: SecurePrefs,
) : ViewModel() {

    /**
     * Applies the onboarding choices in one coroutine: stores the (defaulted) user name and
     * base currency, hashes and saves the PIN only if it is 4-6 digits, saves AI credentials
     * only when a key is provided, creates the first account when named, flags onboarding as
     * done, and finally calls [onComplete].
     */
    fun finish(
        userName: String,
        baseCurrency: String,
        accountName: String,
        accountType: AccountType,
        openingBalance: Double,
        colorHex: Long,
        pin: String = "",
        aiProvider: String = "",
        aiApiKey: String = "",
        onComplete: () -> Unit,
    ) {
        viewModelScope.launch {
            val trimmedName = userName.trim()
            if (trimmedName.isNotBlank()) {
                settings.setUserName(trimmedName)
            } else {
                settings.setUserName("User")
            }
            // Only set a PIN if it is a valid 4-6 digit code; store its hash, never the raw PIN.
            val trimmedPin = pin.trim()
            if (trimmedPin.length in 4..6 && trimmedPin.all { it.isDigit() }) {
                secure.pinHash = PinHasher.hash(trimmedPin)
            }
            // Persist AI credentials only when the user actually entered a key.
            if (aiApiKey.isNotBlank()) {
                secure.aiProvider = aiProvider
                secure.aiApiKey = aiApiKey.trim()
            }
            settings.setBaseCurrency(baseCurrency)
            if (accountName.isNotBlank()) {
                repo.upsertAccount(
                    AccountEntity(
                        name = accountName.trim(),
                        type = accountType,
                        currency = baseCurrency,
                        openingBalance = openingBalance,
                        colorHex = colorHex,
                        createdAt = System.currentTimeMillis(),
                    )
                )
            }
            settings.setOnboardingDone(true)
            onComplete()
        }
    }
}
