package com.jinatra.finatra.util

import java.security.MessageDigest

/** Simple SHA-256 PIN hashing. Stored only in EncryptedSharedPreferences. */
object PinHasher {
    fun hash(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
    fun verify(pin: String, hash: String?): Boolean = hash != null && hash == hash(pin)
}
