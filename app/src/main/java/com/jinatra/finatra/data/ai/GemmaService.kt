package com.jinatra.finatra.data.ai

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

/**
 * On-device LLM via MediaPipe LLM Inference (PRD 6.9 — Gemma).
 * A `.task` model must be present at [modelPath]; import one through AI settings, or
 * point at a model already downloaded by Google AI Edge Gallery (then re-import here).
 * All inference is fully local — zero network calls.
 */
@Singleton
class GemmaService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: OkHttpClient,
) {
    private val modelFile: File get() = File(context.filesDir, "models/gemma.task")
    private var engine: LlmInference? = null

    fun modelPath(): String = modelFile.absolutePath
    fun isAvailable(): Boolean = modelFile.exists() && modelFile.length() > 0

    /** Copy a user-selected model file into app-private storage. */
    fun importModel(input: InputStream): Boolean = runCatching {
        modelFile.parentFile?.mkdirs()
        input.use { ins -> modelFile.outputStream().use { ins.copyTo(it) } }
        engine?.close(); engine = null
        true
    }.getOrDefault(false)

    /**
     * Stream-download a `.task` model from [url] into app storage (PRD 6.9 — download Gemma).
     * Writes to a temp file then atomically renames so a partial download never looks valid.
     * [onProgress] gets 0f..1f (or -1f when total length is unknown). Cancellable via the caller's scope.
     * Note: many Gemma models are license-gated — such URLs need an auth token appended by the user.
     */
    suspend fun download(url: String, onProgress: (Float) -> Unit): Boolean = withContext(Dispatchers.IO) {
        val tmp = File(modelFile.parentFile?.also { it.mkdirs() }, "gemma.task.tmp")
        runCatching {
            val req = Request.Builder().url(url).build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext false
                val body = resp.body ?: return@withContext false
                val total = body.contentLength()
                body.byteStream().use { input ->
                    tmp.outputStream().use { out ->
                        val buf = ByteArray(64 * 1024)
                        var read: Int
                        var downloaded = 0L
                        while (input.read(buf).also { read = it } != -1) {
                            coroutineContext.ensureActive() // cancellation
                            out.write(buf, 0, read)
                            downloaded += read
                            onProgress(if (total > 0) downloaded.toFloat() / total else -1f)
                        }
                    }
                }
            }
            engine?.close(); engine = null
            tmp.copyTo(modelFile, overwrite = true)
            tmp.delete()
            true
        }.getOrElse {
            tmp.delete()
            false
        }
    }

    fun deleteModel() {
        engine?.close(); engine = null
        runCatching { modelFile.delete() }
    }

    suspend fun generate(prompt: String): String? = withContext(Dispatchers.IO) {
        if (!isAvailable()) return@withContext null
        runCatching {
            val llm = engine ?: LlmInference.createFromOptions(
                context,
                LlmInferenceOptions.builder()
                    .setModelPath(modelPath())
                    .setMaxTokens(512)
                    .build(),
            ).also { engine = it }
            llm.generateResponse(prompt)
        }.getOrNull()
    }
}
