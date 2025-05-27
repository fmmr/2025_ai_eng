package openai

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.json.Json
import utils.getEnv

val OPENAI_API_KEY = getEnv("OPENAI_API_KEY")

class OpenAIClient() : Closeable {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }

    suspend fun createChatCompletion(
        prompt: String,
        systemMessage: String? = null,
        model: String = "gpt-3.5-turbo",
        maxTokens: Int = 300,
        temperature: Double = 0.7,
        debug: Boolean = false
    ): ChatCompletionResponse {
        val messages = buildList {
            systemMessage?.let { add(Message(role = "system", content = systemMessage)) }
            add(Message(role = "user", content = prompt))
        }

        val request = ChatCompletionRequest(
            model = model,
            messages = messages,
            maxTokens = maxTokens,
            temperature = temperature
        )
        val response = client.post("https://api.openai.com/v1/chat/completions") {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $OPENAI_API_KEY")
            }
            setBody(request)
        }

        // Debug: print the raw response
        val responseText = response.bodyAsText()
        if (debug) {
            println("Status: ${response.status}")
            println("Raw API response: $responseText")
        }

        // Check if the response is successful
        if (!response.status.isSuccess()) {
            throw Exception("API request failed with status ${response.status}: $responseText")
        }

        // Parse the response
        return json.decodeFromString<ChatCompletionResponse>(responseText)
    }

    suspend fun simple(prompt: String) {
        try {
            val response = createChatCompletion(prompt)
            println("Generated text:")
            println(response.text())
            println("Usage:")
            println(response.usage())
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }

    override fun close() {
        client.close()
    }
}