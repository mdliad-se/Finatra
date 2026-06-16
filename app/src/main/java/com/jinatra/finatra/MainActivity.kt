package com.jinatra.finatra

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jinatra.finatra.data.prefs.ThemeMode
import com.jinatra.finatra.ui.FinatraRoot
import com.jinatra.finatra.ui.LockScreen
import com.jinatra.finatra.ui.RootViewModel
import com.jinatra.finatra.ui.theme.FinatraTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: RootViewModel = hiltViewModel()
            val settings by vm.settings.collectAsStateWithLifecycle()
            val locked by vm.locked.collectAsStateWithLifecycle()

            // Screenshot prevention (PRD 6.11) — disabled in debug so testers can capture.
            if (settings.screenshotPrevention && !BuildConfig.DEBUG) {
                window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }

            // Drive auto-lock from the activity lifecycle.
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_START -> vm.onStart()
                        Lifecycle.Event.ON_STOP -> vm.onStop()
                        else -> Unit
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            val dark = when (settings.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            FinatraTheme(darkTheme = dark, dynamicColor = settings.dynamicColor) {
                if (locked) {
                    LockScreen(
                        hasPin = vm.hasPin(),
                        biometricEnabled = vm.biometricEnabled(),
                        verifyPin = vm::verifyPin,
                        onUnlock = vm::unlock,
                    )
                } else {
                    FinatraRoot(onboardingDone = settings.onboardingDone)
                }
            }
        }
    }
}
