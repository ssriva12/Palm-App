package com.palmlens.core.network

import com.palmlens.core.network.model.ChatMessage
import com.palmlens.core.network.model.ChatRequest
import com.palmlens.core.network.model.ContentPart
import com.palmlens.core.network.model.ImageUrl
import com.palmlens.core.network.model.JsonSchemaSpec
import com.palmlens.core.network.model.ResponseFormat
import com.palmlens.core.serialization.PalmlensJson
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/** Guards the exact OpenAI Chat Completions wire shape — the `type` discriminator on each
 *  content part, and that null request fields are omitted. */
class ChatRequestSerializationTest {

    private val json = PalmlensJson

    @Test
    fun `content parts carry the type discriminator OpenAI expects`() {
        val request = ChatRequest(
            model = "m",
            messages = listOf(
                ChatMessage.system("be nice"),
                ChatMessage.user(
                    ContentPart.Text("read this"),
                    ContentPart.Image(ImageUrl("data:image/jpeg;base64,AAA")),
                ),
            ),
            responseFormat = ResponseFormat(
                jsonSchema = JsonSchemaSpec("palm", buildJsonObject { put("type", "object") }),
            ),
        )

        val tree = json.parseToJsonElement(json.encodeToString(ChatRequest.serializer(), request)).jsonObject
        val userParts = tree["messages"]!!.jsonArray[1].jsonObject["content"]!!.jsonArray

        assertEquals("text", userParts[0].jsonObject["type"]!!.jsonPrimitive.content)
        assertEquals("read this", userParts[0].jsonObject["text"]!!.jsonPrimitive.content)
        assertEquals("image_url", userParts[1].jsonObject["type"]!!.jsonPrimitive.content)
        assertEquals(
            "data:image/jpeg;base64,AAA",
            userParts[1].jsonObject["image_url"]!!.jsonObject["url"]!!.jsonPrimitive.content,
        )
        val responseFormat = tree["response_format"]!!.jsonObject
        assertEquals("json_schema", responseFormat["type"]!!.jsonPrimitive.content)
        assertEquals(true, responseFormat["json_schema"]!!.jsonObject["strict"]!!.jsonPrimitive.content.toBoolean())
        // null fields (temperature, max_completion_tokens) must not be sent
        assertFalse(tree.containsKey("temperature"))
    }
}
