package com.palmlens.core.network

import com.palmlens.core.network.model.ChatMessage
import com.palmlens.core.network.model.ChatRequest
import com.palmlens.core.network.model.ContentPart
import com.palmlens.core.serialization.PalmlensJson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OpenAiClientTest {

    private lateinit var server: MockWebServer
    private lateinit var client: OpenAiClient

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
        client = OpenAiClient(
            okHttpClient = OkHttpClient(),
            config = OpenAiConfig(
                apiKey = "sk-test",
                baseUrl = server.url("/v1").toString(),
                visionModel = "m-vision",
                textModel = "m-text",
            ),
            json = PalmlensJson,
            io = UnconfinedTestDispatcher(),
        )
    }

    @After
    fun tearDown() = server.shutdown()

    private fun request() = ChatRequest(
        model = "m-text",
        messages = listOf(ChatMessage.user(ContentPart.Text("hi"))),
    )

    @Test
    fun `complete returns content and usage`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """{"choices":[{"message":{"content":"{\"score\":80}"},"finish_reason":"stop"}],
                   "usage":{"prompt_tokens":10,"completion_tokens":5,"total_tokens":15}}""",
            ),
        )

        val result = client.complete(request())

        assertEquals("""{"score":80}""", result.content)
        assertEquals("stop", result.finishReason)
        assertEquals(15, result.usage?.totalTokens)
        assertEquals("Bearer sk-test", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `non-2xx surfaces the OpenAI error message`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(429).setBody(
                """{"error":{"message":"Rate limit reached","type":"rate_limit_error"}}""",
            ),
        )

        val ex = assertThrows(OpenAiClient.OpenAiException::class.java) {
            kotlinx.coroutines.runBlocking { client.complete(request()) }
        }
        assertEquals(429, ex.statusCode)
        assertTrue(ex.message!!.contains("Rate limit"))
    }

    @Test
    fun `blank key is rejected before any network call`() = runTest {
        val unconfigured = OpenAiClient(
            OkHttpClient(),
            OpenAiConfig("", server.url("/v1").toString(), "m", "m"),
            PalmlensJson,
            UnconfinedTestDispatcher(),
        )
        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking { unconfigured.complete(request()) }
        }
    }
}
