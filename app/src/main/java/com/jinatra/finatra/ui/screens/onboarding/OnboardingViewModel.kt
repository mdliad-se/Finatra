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

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repo: FinanceRepository,
    private val settings: SettingsRepository,
    private val secure: SecurePrefs,
) : ViewModel() {

    fun finish(
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
            val trimmedPin = pin.trim()
            if (trimmedPin.length in 4..6 && trimmedPin.all { it.isDigit() }) {
                secure.pinHash = PinHasher.hash(trimmedPin)
            }
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
