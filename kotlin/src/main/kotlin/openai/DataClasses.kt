package openai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Message(
    val role: String,
    val content: String
)

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<Message>,
    @SerialName("max_tokens") val maxTokens: Int = 100,
    val temperature: Double = 0.7,
    val stream: Boolean = false
)

@Serializable
data class ChatChoice(
    val index: Int,
    val message: Message,
    @SerialName("finish_reason") val finishReason: String?
)

@Serializable
data class ChatCompletionResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<ChatChoice>,
    val usage: Usage?
) {
    fun text(): String {
        return choices.joinToString(" ") { it.message.content.trim() }
    }

    fun usage(): String {
        return usage?.let { "Tokens used: ${usage.totalTokens} (${usage.promptTokens} prompt + ${usage.completionTokens} completion)" } ?: "No usage data available."
    }


}

@Serializable
data class Usage(
    @SerialName("prompt_tokens") val promptTokens: Int,
    @SerialName("completion_tokens") val completionTokens: Int,
    @SerialName("total_tokens") val totalTokens: Int
)

@Serializable
data class EmbeddingRequest(
    val input: String,
    val model: String
)

@Serializable
data class BatchEmbeddingRequest(
    val input: List<String>,
    val model: String
)

@Serializable
data class EmbeddingData(
    val embedding: List<Double>,
    val index: Int,
    @SerialName("object") val objectType: String
)

@Serializable
data class EmbeddingUsage(
    @SerialName("prompt_tokens") val promptTokens: Int,
    @SerialName("total_tokens") val totalTokens: Int
)

@Serializable
data class EmbeddingResponse(
    val data: List<EmbeddingData>,
    val model: String,
    @SerialName("object") val objectType: String,
    val usage: EmbeddingUsage
)