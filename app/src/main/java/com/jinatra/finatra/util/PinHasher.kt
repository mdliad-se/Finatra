package com.jinatra.finatra.util

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * PIN hashing for the app lock.
 *
 * A PIN is short and numeric, so a plain digest is brute-forceable in milliseconds. The stored value
 * is therefore derived with PBKDF2-HMAC-SHA256 over a random per-PIN [salt] and a high iteration
 * count, encoded as `pbkdf2$<iterations>$<saltB64>$<hashB64>`. The hash is kept only in
 * EncryptedSharedPreferences and never transmitted.
 *
 * Legacy unsalted SHA-256 hex hashes (written by older builds) are still accepted by [verify] for
 * backward compatibility; callers should re-hash with [hash] after a successful legacy verify
 * (see [isLegacy]).
 */
object PinHasher {
    private const val PREFIX = "pbkdf2"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256

    /** Hash [pin] with a fresh random salt; returns the encoded `pbkdf2$iter$salt$hash` string. */
    fun hash(pin: String): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val derived = pbkdf2(pin, salt, ITERATIONS)
        return listOf(PREFIX, ITERATIONS.toString(), b64(salt), b64(derived)).joinToString("$")
    }

    /** True if [pin] matches [stored]. Handles both the PBKDF2 format and legacy SHA-256 hex. */
    fun verify(pin: String, stored: String?): Boolean {
        if (stored == null) return false
        if (!stored.startsWith("$PREFIX$")) {
            // Legacy unsalted SHA-256 hex digest.
            return MessageDigest.isEqual(legacySha256(pin).toByteArray(), stored.toByteArray())
        }
        val parts = stored.split("$")
        if (parts.size != 4) return false
        val iterations = parts[1].toIntOrNull() ?: return false
        val salt = unb64(parts[2]) ?: return false
        val expected = unb64(parts[3]) ?: return false
        // MessageDigest.isEqual is constant-time, avoiding a timing side channel on the compare.
        return MessageDigest.isEqual(pbkdf2(pin, salt, iterations), expected)
    }

    /** True when [stored] is a legacy (non-PBKDF2) hash that should be upgraded after verifying. */
    fun isLegacy(stored: String?): Boolean = stored != null && !stored.startsWith("$PREFIX$")

    private fun pbkdf2(pin: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, iterations, KEY_LENGTH)
        return SecretKeyFactory.getInstance("PBKDF2withHmacSHA256").generateSecret(spec).encoded
    }

    private fun legacySha256(pin: String): String =
        MessageDigest.getInstance("SHA-256").digest(pin.toByteArray()).joinToString("") { "%02x".format(it) }

    private fun b64(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
    private fun unb64(s: String): ByteArray? = runCatching { Base64.decode(s, Base64.NO_WRAP) }.getOrNull()
}
