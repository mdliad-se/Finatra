package com.jinatra.finatra

import com.jinatra.finatra.data.ai.AiEndpoints
import com.jinatra.finatra.data.ai.AiHttp
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Verifies the cloud-AI HTTP layer end-to-end against a local MockWebServer (no real key). */
class AiHttpTest {
    private lateinit var server: MockWebServer
    private lateinit var http: AiHttp

    @Before fun setUp() {
        server = MockWebServer()
        server.start()
        val base = server.url("/").toString().trimEnd('/')
        http = AiHttp(
            OkHttpClient(),
            AiEndpoints(gemini = "$base/gemini", claude = "$base/claude", openRouter = "$base/openrouter"),
        )
    }

    @After fun tearDown() = server.shutdown()

    /**
     * Gemini provider: parses the text out of Gemini's `candidates/content/parts` response, and
     * sends the API key as a `key=` query param and the prompt in the request body.
     */
    @Test fun gemini_parsesText_andSendsKeyAndPrompt() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"candidates":[{"content":{"parts":[{"text":"Hello from Gemini"}]}}]}"""))
        val out = http.complete("Gemini", "spent 5 on coffee", "KEY123")
        assertEquals("Hello from Gemini", out)
        val req = server.takeRequest()
        // Gemini authenticates via a query-string key and carries the prompt in the body.
        assertTrue(req.path!!.contains("key=KEY123"))
        assertTrue(req.body.readUtf8().contains("spent 5 on coffee"))
    }

    /**
     * Claude provider: parses the text out of Claude's `content` array, and authenticates with the
     * `x-api-key` header plus the required `anthropic-version` header.
     */
    @Test fun claude_parsesText_andSendsApiKeyHeader() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"content":[{"text":"Hi from Claude"}]}"""))
        val out = http.complete("Claude", "lunch 12", "SK-CLAUDE")
        assertEquals("Hi from Claude", out)
        val req = server.takeRequest()
        // Claude expects the key in x-api-key and a pinned API version header.
        assertEquals("SK-CLAUDE", req.getHeader("x-api-key"))
        assertEquals("2023-06-01", req.getHeader("anthropic-version"))
    }

    /**
     * OpenRouter provider: parses the text out of the OpenAI-style `choices/message/content`
     * response and authenticates with a `Bearer` Authorization header.
     */
    @Test fun openRouter_parsesText_andSendsBearer() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"choices":[{"message":{"content":"OR reply"}}]}"""))
        val out = http.complete("OpenRouter", "taxi 8", "OR-KEY")
        assertEquals("OR reply", out)
        assertEquals("Bearer OR-KEY", server.takeRequest().getHeader("Authorization"))
    }

    /** A non-2xx HTTP response (here 500) yields a null result rather than throwing. */
    @Test fun serverError_returnsNull() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("nope"))
        assertNull(http.complete("Gemini", "x", "k"))
    }
}
