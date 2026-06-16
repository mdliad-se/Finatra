package com.jinatra.finatra.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.inject.Inject
import javax.inject.Singleton

/** EncryptedSharedPreferences for sensitive values: AI API keys, PIN hash. Never transmitted. */
@Singleton
class SecurePrefs @Inject constructor(context: Context) {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "finatra_secure",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    var aiProvider: String?
        get() = prefs.getString(KEY_PROVIDER, null)
        set(v) = prefs.edit().putString(KEY_PROVIDER, v).apply()

    var aiApiKey: String?
        get() = prefs.getString(KEY_API_KEY, null)
        set(v) = prefs.edit().putString(KEY_API_KEY, v).apply()

    var pinHash: String?
        get() = prefs.getString(KEY_PIN, null)
        set(v) = prefs.edit().putString(KEY_PIN, v).apply()

    fun hasPin(): Boolean = pinHash != null

    companion object {
        private const val KEY_PROVIDER = "ai_provider"
        private const val KEY_API_KEY = "ai_api_key"
        private const val KEY_PIN = "pin_hash"
    }
}
