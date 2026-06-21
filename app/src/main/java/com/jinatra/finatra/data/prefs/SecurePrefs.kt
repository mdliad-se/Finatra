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

    // Built lazily so the Android Keystore master key is created on first access, not at injection.
    private val prefs: SharedPreferences by lazy {
        // AES-256-GCM master key in the Android Keystore; keys are SIV-encrypted, values GCM-encrypted.
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

    /** Selected AI provider identifier; null until the user configures one. */
    var aiProvider: String?
        get() = prefs.getString(KEY_PROVIDER, null)
        set(v) = prefs.edit().putString(KEY_PROVIDER, v).apply()

    /** Cloud AI API key, encrypted at rest; null when unset (e.g. on-device-only mode). */
    var aiApiKey: String?
        get() = prefs.getString(KEY_API_KEY, null)
        set(v) = prefs.edit().putString(KEY_API_KEY, v).apply()

    /** SHA-256 hash of the primary unlock PIN (see [com.jinatra.finatra.util.PinHasher]); null if no PIN set. */
    var pinHash: String?
        get() = prefs.getString(KEY_PIN, null)
        set(v) = prefs.edit().putString(KEY_PIN, v).apply()

    /** Alternate "decoy" PIN (PRD 6.13) — unlocks into a clean, empty app state. */
    var decoyPinHash: String?
        get() = prefs.getString(KEY_DECOY_PIN, null)
        set(v) = prefs.edit().putString(KEY_DECOY_PIN, v).apply()

    /** True once a primary unlock PIN has been set. */
    fun hasPin(): Boolean = pinHash != null
    /** True once a decoy PIN has been set. */
    fun hasDecoyPin(): Boolean = decoyPinHash != null

    companion object {
        private const val KEY_PROVIDER = "ai_provider"
        private const val KEY_API_KEY = "ai_api_key"
        private const val KEY_PIN = "pin_hash"
        private const val KEY_DECOY_PIN = "decoy_pin_hash"
    }
}
