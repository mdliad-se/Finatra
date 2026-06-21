package com.jinatra.finatra.ui

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/** Walks the [ContextWrapper] chain to find the host [FragmentActivity] (required by BiometricPrompt). */
private fun Context.findActivity(): FragmentActivity? {
    var c = this
    while (c is ContextWrapper) {
        if (c is FragmentActivity) return c
        c = c.baseContext
    }
    return null
}

/**
 * Full-screen lock gate (PRD 6.11). Prompts biometric first (when [biometricEnabled]) and falls
 * back to a PIN field (when [hasPin]). Renders a lock icon, headline, optional PIN entry, and an
 * optional "Use biometric" button.
 *
 * @param hasPin whether a PIN is configured (shows the PIN field and gates lockout behaviour).
 * @param biometricEnabled whether biometric unlock is enabled in settings.
 * @param onSubmitPin verifies an entered PIN; returns true on success. The caller handles unlocking.
 * @param onUnlock invoked when biometric authentication succeeds (or to avoid permanent lockout).
 */
@Composable
fun LockScreen(
    hasPin: Boolean,
    biometricEnabled: Boolean,
    onSubmitPin: (String) -> Boolean,
    onUnlock: () -> Unit,
) {
    val context = LocalContextActivity()
    // Hoisted PIN entry state and an error flag toggled on an incorrect PIN.
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    fun promptBiometric() {
        val activity = context ?: return
        if (!biometricEnabled) return
        val manager = BiometricManager.from(activity)
        val authenticators = BIOMETRIC_STRONG or BIOMETRIC_WEAK
        if (manager.canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
            // No usable biometric. If there's no PIN either, don't lock the user out.
            if (!hasPin) onUnlock()
            return
        }
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onUnlock()
                override fun onAuthenticationError(code: Int, msg: CharSequence) {
                    if (!hasPin) onUnlock() // avoid permanent lockout when PIN isn't set
                }
            },
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Finatra")
            .setSubtitle("Confirm your identity")
            .setAllowedAuthenticators(authenticators)
            .setNegativeButtonText(if (hasPin) "Use PIN" else "Cancel")
            .build()
        prompt.authenticate(info)
    }

    // Auto-trigger the biometric prompt once when the lock screen first appears.
    LaunchedEffect(Unit) { promptBiometric() }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            Text("Finatra is locked", style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(
                "Confirm your identity to continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))

            if (hasPin) {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.filter { c -> c.isDigit() }.take(6); error = false },
                    label = { Text("PIN") },
                    isError = error,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (error) Text("Incorrect PIN", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(12.dp))
                Button(
                    // On a wrong PIN, flag the error and clear the field; success unlocks via the caller.
                    onClick = { if (!onSubmitPin(pin)) { error = true; pin = "" } },
                    enabled = pin.length in 4..6,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Unlock") }
            }

            if (biometricEnabled) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { promptBiometric() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Fingerprint, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Use biometric")
                }
            }
        }
    }
}

/** Resolves the hosting [FragmentActivity] from the composition's local context, or null. */
@Composable
private fun LocalContextActivity(): FragmentActivity? =
    androidx.compose.ui.platform.LocalContext.current.findActivity()
