package com.vend.fmr.aieng.impl.openai

import com.vend.fmr.aieng.EMBEDDING_MODEL
import com.vend.fmr.aieng.OPEN_AI_MODEL
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


@Suppress("unused")
class OpenAI(private val openaiApiKey: String) : Closeable {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging){
            level = LogLevel.NONE
        }
    }

    suspend fun createChatCompletion(
        prompt: String,
        systemMessage: String? = null,
        model: String = OPEN_AI_MODEL,
        maxTokens: Int = 300,
        temperature: Double = 0.7,
        debug: Boolean = false
    ): ChatCompletionResponse {
        val messages = buildList {
            systemMessage?.let { add(Message(role = "system", content = systemMessage)) }
            add(Message(role = "user", content = prompt))
        }

        return createChatCompletionWithMessages(messages, model, maxTokens, temperature, debug)
    }

    suspend fun createChatCompletionWithMessages(
        messages: List<Message>,
        model: String = OPEN_AI_MODEL,
        maxTokens: Int = 300,
        temperature: Double = 0.7,
        debug: Boolean = false
    ): ChatCompletionResponse {
        val request = ChatCompletionRequest(
            model = model,
            messages = messages,
            maxTokens = maxTokens,
            temperature = temperature
        )
        val response = client.post("https://api.openai.com/v1/chat/completions") {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $openaiApiKey")
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

    suspend fun createEmbedding(text: String): List<Double> {
        val request = EmbeddingRequest(
            input = text,
            model = EMBEDDING_MODEL
        )

        val response = client.post("https://api.openai.com/v1/embeddings") {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $openaiApiKey")
            }
            setBody(request)
        }

        val responseText = response.bodyAsText()

        if (!response.status.isSuccess()) {
            throw Exception("API request failed with status ${response.status}: $responseText")
        }

        val embeddingResponse = json.decodeFromString<EmbeddingResponse>(responseText)
        return embeddingResponse.data.first().embedding
    }

    suspend fun createEmbeddings(texts: List<String>): List<List<Double>> {
        val request = BatchEmbeddingRequest(
            input = texts,
            model = EMBEDDING_MODEL
        )

        val response = client.post("https://api.openai.com/v1/embeddings") {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $openaiApiKey")
            }
            setBody(request)
        }

        val responseText = response.bodyAsText()

        if (!response.status.isSuccess()) {
            throw Exception("API request failed with status ${response.status}: $responseText")
        }

        val embeddingResponse = json.decodeFromString<EmbeddingResponse>(responseText)
        return embeddingResponse.data.sortedBy { it.index }.map { it.embedding }
    }

    override fun close() {
        client.close()
    }
}


