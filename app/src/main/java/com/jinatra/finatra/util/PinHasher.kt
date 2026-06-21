package com.jinatra.finatra.util

import java.security.MessageDigest

/**
 * Simple SHA-256 PIN hashing. The resulting hash is stored only in EncryptedSharedPreferences.
 *
 * This is an unsalted, single-round digest — adequate here only because the input space is a short
 * numeric PIN guarded behind an encrypted store and OS-level access control, not a general password.
 */
object PinHasher {
    /** Hash [pin] (UTF-8 bytes) with SHA-256 and return it as a lowercase hex string. */
    fun hash(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        // Render each byte as two lowercase hex chars so equal hashes compare as equal strings.
        return digest.joinToString("") { "%02x".format(it) }
    }
    /** True if [pin] hashes to the stored [hash]; false when no PIN has been set ([hash] is null). */
    fun verify(pin: String, hash: String?): Boolean = hash != null && hash == hash(pin)
}
