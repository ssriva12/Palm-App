package com.palmz.core.network

import com.palmz.core.network.model.ChatMessage
import com.palmz.core.network.model.ChatRequest
import com.palmz.core.network.model.ContentPart
import com.palmz.core.serialization.PalmlensJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
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
            io = Dispatchers.Unconfined, // real dispatcher: retry backoff runs in real (short) time
        )
    }

    @After
    fun tearDown() = server.shutdown()

    private fun request() = ChatRequest(
        model = "m-text",
        messages = listOf(ChatMessage.user(ContentPart.Text("hi"))),
    )

    private fun ok(content: String, finish: String = "stop"): MockResponse {
        val escaped = content.replace("\\", "\\\\").replace("\"", "\\\"")
        return MockResponse().setBody(
            """{"choices":[{"message":{"content":"$escaped"},"finish_reason":"$finish"}],
               "usage":{"prompt_tokens":10,"completion_tokens":5,"total_tokens":15}}""",
        )
    }

    @Test
    fun `complete returns content and usage`() = runTest {
        server.enqueue(ok("""{"score":80}"""))

        val result = client.complete(request())

        assertEquals("""{"score":80}""", result.content)
        assertEquals("stop", result.finishReason)
        assertEquals(15, result.usage?.totalTokens)
        assertEquals("Bearer sk-test", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `client error is not retried`() = runTest {
        server.enqueue(MockResponse().setResponseCode(400).setBody("""{"error":{"message":"bad request"}}"""))

        val ex = assertThrows(OpenAiClient.OpenAiException::class.java) {
            runBlocking { client.complete(request()) }
        }
        assertEquals(400, ex.statusCode)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `rate limit is retried then surfaces`() = runTest {
        repeat(3) {
            server.enqueue(
                MockResponse().setResponseCode(429)
                    .setHeader("Retry-After", "0")
                    .setBody("""{"error":{"message":"Rate limit reached"}}"""),
            )
        }

        val ex = assertThrows(OpenAiClient.OpenAiException::class.java) {
            runBlocking { client.complete(request()) }
        }
        assertEquals(429, ex.statusCode)
        assertTrue(ex.message!!.contains("Rate limit"))
        assertEquals(3, server.requestCount) // original + 2 retries
    }

    @Test
    fun `transient server error is retried then succeeds`() = runTest {
        server.enqueue(MockResponse().setResponseCode(503).setBody("{}"))
        server.enqueue(ok("""{"score":42}"""))

        val result = client.complete(request())

        assertEquals("""{"score":42}""", result.content)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `truncated response is retried`() = runTest {
        server.enqueue(ok("""{"score":1""", finish = "length"))
        server.enqueue(ok("""{"score":99}"""))

        val result = client.complete(request())

        assertEquals("""{"score":99}""", result.content)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `blank key is rejected before any network call`() = runTest {
        val unconfigured = OpenAiClient(
            OkHttpClient(),
            OpenAiConfig("", server.url("/v1").toString(), "m", "m"),
            PalmlensJson,
            Dispatchers.Unconfined,
        )
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { unconfigured.complete(request()) }
        }
    }
}
