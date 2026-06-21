package com.jinatra.finatra

import android.content.ContextWrapper
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
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
import androidx.compose.runtime.Composable
import dagger.hilt.android.AndroidEntryPoint

/**
 * Override the in-app locale (PRD 6.20) so stringResource resolves to the chosen language.
 *
 * @param language ISO language code to apply to the wrapped subtree.
 * @param content composable subtree rendered under the localized configuration/context.
 */
@Composable
private fun Localized(language: String, content: @Composable () -> Unit) {
    val base = LocalContext.current
    val config = remember(language) {
        Configuration(base.resources.configuration).apply { setLocale(Locale(language)) }
    }
    // Wrap the original context (an Activity) rather than replacing it with a bare
    // createConfigurationContext() result. hiltViewModel() unwraps LocalContext's
    // ContextWrapper chain to find the Activity; a raw ContextImpl breaks that and
    // throws "Expected an activity context for creating a HiltViewModelFactory".
    val localizedCtx = remember(language) {
        val configCtx = base.createConfigurationContext(config)
        object : ContextWrapper(base) {
            override fun getResources(): Resources = configCtx.resources
            override fun getAssets(): AssetManager = configCtx.assets
        }
    }
    CompositionLocalProvider(
        LocalConfiguration provides config,
        LocalContext provides localizedCtx,
    ) { content() }
}

/**
 * Single activity hosting the entire Compose UI.
 *
 * Wires global app behaviours to settings/lifecycle: optional screenshot blocking,
 * activity-lifecycle-driven auto-lock, theme (light/dark/dynamic), locale, and the
 * top-level lock vs. main-content branch.
 */
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
                Localized(settings.language) {
                    // Keep the main UI composed and overlay the lock gate on top while locked, so the
                    // navigation back stack (the screen the user was on) survives a background re-lock
                    // instead of being torn down and rebuilt at the Home start destination on unlock.
                    // The lock screen is a full-screen opaque Surface, so it fully hides the content;
                    // FLAG_SECURE additionally blocks it from screenshots/recents.
                    Box {
                        FinatraRoot(onboardingDone = settings.onboardingDone, quizDone = settings.quizDone)
                        if (locked) {
                            LockScreen(
                                hasPin = vm.hasPin(),
                                biometricEnabled = vm.biometricEnabled(),
                                onSubmitPin = vm::submitPin,
                                onUnlock = vm::unlock,
                            )
                        }
                    }
                }
            }
        }
    }
}
