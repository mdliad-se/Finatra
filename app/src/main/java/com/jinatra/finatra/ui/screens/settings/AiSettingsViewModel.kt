package com.jinatra.finatra.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.ai.GemmaService
import com.jinatra.finatra.data.prefs.SecurePrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject

/**
 * A selectable cloud AI provider.
 *
 * @property name display name and the key under which the user's API key is stored.
 * @property keyUrl deep link to where the user can obtain an API key for this provider.
 */
data class AiProvider(val name: String, val keyUrl: String)

/** State of the on-device Gemma model download. */
sealed interface DownloadState {
    /** No download in progress. */
    data object Idle : DownloadState
    /** Download running; [progress] is 0f..1f, or -1f when the total size is unknown. */
    data class Running(val progress: Float) : DownloadState   // -1f = unknown size
    /** Download finished and the model is installed. */
    data object Done : DownloadState
    /** Download failed; [message] is user-facing. */
    data class Error(val message: String) : DownloadState
}

/**
 * ViewModel for the AI settings screen.
 *
 * Manages two independent AI paths:
 *  - Cloud: persisting the chosen provider and its API key. The key is written to
 *    [SecurePrefs] (encrypted on-device) and only ever sent to the selected provider.
 *  - On-device: importing, downloading, removing and querying availability of a local
 *    Gemma `.task` model via [GemmaService] for fully offline inference.
 */
@HiltViewModel
class AiSettingsViewModel @Inject constructor(
    private val secure: SecurePrefs,
    private val gemma: GemmaService,
) : ViewModel() {

    val providers = listOf(
        AiProvider("Gemini", "https://aistudio.google.com/app/apikey"),
        AiProvider("Claude", "https://console.anthropic.com/settings/keys"),
        AiProvider("OpenRouter", "https://openrouter.ai/keys"),
    )

    /** A small, ungated default; users can paste any direct .task URL (gated ones need a token). */
    val defaultModelUrl = "https://huggingface.co/litert-community/Gemma3-1B-IT/resolve/main/gemma3-1b-it-int4.task"

    /** Previously selected provider name, defaulting to the first provider. */
    fun savedProvider(): String = secure.aiProvider ?: providers.first().name
    /** Previously saved API key, or empty string if none. */
    fun savedKey(): String = secure.aiApiKey ?: ""

    /** Persists the provider choice and key to encrypted prefs; blank keys are cleared (stored as null). */
    fun save(provider: String, key: String) {
        secure.aiProvider = provider
        secure.aiApiKey = key.ifBlank { null }
    }

    // On-device Gemma model management
    /** True if a local Gemma model is installed and usable. */
    fun gemmaAvailable(): Boolean = gemma.isAvailable()
    /** Imports a `.task` model from the given stream (e.g. a user-picked file); returns success. */
    fun importGemma(input: InputStream): Boolean = gemma.importModel(input)
    /** Deletes the installed local model. */
    fun deleteGemma() = gemma.deleteModel()

    private val _download = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val download = _download.asStateFlow()
    private var dlJob: Job? = null

    /**
     * Downloads a Gemma model from [url] into local storage, streaming progress into
     * [download]. The job is retained in [dlJob] so it can be cancelled. Gated URLs that
     * require an auth token will surface as a download failure.
     */
    fun downloadGemma(url: String) {
        if (url.isBlank()) { _download.value = DownloadState.Error("Enter a model URL"); return }
        dlJob = viewModelScope.launch {
            _download.value = DownloadState.Running(0f)
            // Progress callback pushes 0f..1f (or -1f for unknown-size) updates to the UI.
            val ok = gemma.download(url) { p -> _download.value = DownloadState.Running(p) }
            _download.value = if (ok) DownloadState.Done else DownloadState.Error("Download failed (URL/access?)")
        }
    }

    /** Cancels any in-flight download and resets the state to idle. */
    fun cancelDownload() {
        dlJob?.cancel()
        _download.value = DownloadState.Idle
    }
}
