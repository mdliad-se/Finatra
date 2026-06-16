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

data class AiProvider(val name: String, val keyUrl: String)

sealed interface DownloadState {
    data object Idle : DownloadState
    data class Running(val progress: Float) : DownloadState   // -1f = unknown size
    data object Done : DownloadState
    data class Error(val message: String) : DownloadState
}

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

    fun savedProvider(): String = secure.aiProvider ?: providers.first().name
    fun savedKey(): String = secure.aiApiKey ?: ""

    fun save(provider: String, key: String) {
        secure.aiProvider = provider
        secure.aiApiKey = key.ifBlank { null }
    }

    // On-device Gemma model management
    fun gemmaAvailable(): Boolean = gemma.isAvailable()
    fun importGemma(input: InputStream): Boolean = gemma.importModel(input)
    fun deleteGemma() = gemma.deleteModel()

    private val _download = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val download = _download.asStateFlow()
    private var dlJob: Job? = null

    fun downloadGemma(url: String) {
        if (url.isBlank()) { _download.value = DownloadState.Error("Enter a model URL"); return }
        dlJob = viewModelScope.launch {
            _download.value = DownloadState.Running(0f)
            val ok = gemma.download(url) { p -> _download.value = DownloadState.Running(p) }
            _download.value = if (ok) DownloadState.Done else DownloadState.Error("Download failed (URL/access?)")
        }
    }

    fun cancelDownload() {
        dlJob?.cancel()
        _download.value = DownloadState.Idle
    }
}
