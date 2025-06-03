package com.vend.fmr.aieng.apis.openai

import com.vend.fmr.aieng.EMBEDDING_MODEL
import com.vend.fmr.aieng.OPEN_AI_MODEL
import com.vend.fmr.aieng.utils.Prompts
import com.vend.fmr.aieng.utils.Models
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.timeout
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
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
            encodeDefaults = true
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
        prompt: String? = null,
        systemMessage: String? = null,
        messages: List<Message>? = null,
        model: String = OPEN_AI_MODEL,
        maxTokens: Int = 300,
        temperature: Double = 0.7,
        topP: Double? = null,
        tools: List<Tool>? = null,
        toolChoice: String? = null,
        debug: Boolean = false,
        timeoutMs: Long = 120000
    ): ChatCompletionResponse {
        val finalMessages = messages ?: buildList {
            require(prompt != null) { "Either 'messages' or 'prompt' must be provided" }
            systemMessage?.let { add(Message(role = Prompts.Roles.SYSTEM, content = TextContent(it))) }
            add(Message(role = Prompts.Roles.USER, content = TextContent(prompt)))
        }
        
        val request = ChatCompletionRequest(
            model = model,
            messages = finalMessages,
            maxTokens = maxTokens,
            temperature = temperature,
            topP = topP,
            tools = tools,
            toolChoice = toolChoice
        )
        
        val response = client.post("https://api.openai.com/v1/chat/completions") {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $openaiApiKey")
            }
            setBody(request)
            timeout {
                requestTimeoutMillis = timeoutMs
            }
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("Status: ${response.status}")
        }

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

    suspend fun generateImage(
        prompt: String,
        model: String = Models.Defaults.IMAGE_GENERATION,
        size: String = "1024x1024",
        style: String? = null,
        quality: String? = null,
        debug: Boolean = false
    ): ImageGenerationResponse {
        val request = ImageGenerationRequest(
            prompt = prompt,
            model = model,
            size = size,
            style = style,
            quality = quality,
            responseFormat = "url"
        )

        val response = client.post("https://api.openai.com/v1/images/generations") {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $openaiApiKey")
            }
            setBody(request)
            timeout {
                requestTimeoutMillis = 300000 // 5 minutes for image generation
            }
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("Image Generation Status: ${response.status}")
            println("Response: $responseText")
        }

        if (!response.status.isSuccess()) {
            throw Exception("Image generation API request failed with status ${response.status}: $responseText")
        }

        return json.decodeFromString<ImageGenerationResponse>(responseText)
    }

    suspend fun editImage(
        prompt: String,
        imageFile: ByteArray,
        maskFile: ByteArray,
        model: String = Models.ImageGeneration.DALL_E_2, // Only DALL-E 2 supports editing
        size: String = "1024x1024",
        debug: Boolean = false
    ): ImageEditResponse {
        val response = client.submitFormWithBinaryData(
            url = "https://api.openai.com/v1/images/edits",
            formData = formData {
                append("prompt", prompt)
                append("model", model)
                append("size", size)
                append("response_format", "url")
                append("image", imageFile, Headers.build {
                    append(HttpHeaders.ContentType, "image/png")
                    append(HttpHeaders.ContentDisposition, "filename=\"image.png\"")
                })
                append("mask", maskFile, Headers.build {
                    append(HttpHeaders.ContentType, "image/png") 
                    append(HttpHeaders.ContentDisposition, "filename=\"mask.png\"")
                })
            }
        ) {
            headers {
                append("Authorization", "Bearer $openaiApiKey")
            }
            timeout {
                requestTimeoutMillis = 300000
            }
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("Image Edit Status: ${response.status}")
            println("Response: $responseText")
        }

        if (!response.status.isSuccess()) {
            throw Exception("Image editing API request failed with status ${response.status}: $responseText")
        }

        return json.decodeFromString<ImageEditResponse>(responseText)
    }

    suspend fun createVisionCompletion(
        prompt: String,
        imageUrl: String,
        model: String = Models.Defaults.VISION_ANALYSIS,
        maxTokens: Int = 300,
        detail: String = "auto",
        debug: Boolean = false
    ): ChatCompletionResponse {
        val messages = listOf(
            Message(
                role = Prompts.Roles.USER,
                content = VisionContent(
                    parts = listOf(
                        ContentPart(type = "text", text = prompt),
                        ContentPart(type = "image_url", imageUrl = ImageUrl(url = imageUrl, detail = detail))
                    )
                )
            )
        )

        return createChatCompletion(
            messages = messages,
            model = model,
            maxTokens = maxTokens,
            debug = debug,
            timeoutMs = 120000
        )
    }

    override fun close() {
        client.close()
    }
}


