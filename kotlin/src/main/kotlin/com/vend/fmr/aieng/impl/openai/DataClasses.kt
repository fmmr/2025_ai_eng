package com.vend.fmr.aieng.impl.openai

import com.vend.fmr.aieng.utils.Models
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@Serializable
sealed class MessageContent

@Serializable
data class TextContent(val text: String) : MessageContent()

@Serializable
data class VisionContent(val parts: List<ContentPart>) : MessageContent()

@OptIn(ExperimentalSerializationApi::class)
object MessageContentSerializer : KSerializer<MessageContent?> {
    override val descriptor = buildClassSerialDescriptor("MessageContent")
    
    override fun serialize(encoder: Encoder, value: MessageContent?) {
        when (value) {
            is TextContent -> encoder.encodeString(value.text)
            is VisionContent -> encoder.encodeSerializableValue(ListSerializer(ContentPart.serializer()), value.parts)
            null -> encoder.encodeNull()
        }
    }
    
    override fun deserialize(decoder: Decoder): MessageContent? {
        return try {
            val text = decoder.decodeString()
            TextContent(text)
        } catch (_: Exception) {
            null
        }
    }
}

@Serializable
data class Message(
    val role: String,
    @Serializable(with = MessageContentSerializer::class)
    val content: MessageContent? = null,
    @SerialName("tool_calls") val toolCalls: List<ToolCall>? = null,
    @SerialName("tool_call_id") val toolCallId: String? = null
)

@Serializable
data class ContentPart(
    val type: String,
    val text: String? = null,
    @SerialName("image_url") val imageUrl: ImageUrl? = null
)

@Serializable
data class ImageUrl(
    val url: String,
    val detail: String = "auto"
)

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<Message>,
    @SerialName("max_tokens") val maxTokens: Int = 100,
    val temperature: Double = 0.7,
    @SerialName("top_p") val topP: Double? = null,
    val stream: Boolean = false,
    val tools: List<Tool>? = null,
    @SerialName("tool_choice") val toolChoice: String? = null
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
        return choices.joinToString(" ") { 
            when (val content = it.message.content) {
                is TextContent -> content.text.trim()
                is VisionContent -> content.parts.find { part -> part.text != null }?.text?.trim() ?: ""
                null -> ""
            }
        }
    }
    
    fun hasToolCalls(): Boolean {
        return choices.any { it.message.toolCalls?.isNotEmpty() == true }
    }
    
    fun getToolCalls(): List<ToolCall> {
        return choices.flatMap { it.message.toolCalls ?: emptyList() }
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

@Serializable
data class Tool(
    val type: String = "function",
    val function: FunctionDefinition
)

@Serializable
data class FunctionDefinition(
    val name: String,
    val description: String,
    val parameters: FunctionParameters
)

@Serializable
data class FunctionParameters(
    val type: String = "object",
    val properties: Map<String, PropertyDefinition>,
    val required: List<String>? = null
)

@Serializable
data class PropertyDefinition(
    val type: String,
    val description: String,
    val enum: List<String>? = null
)

@Serializable
data class ToolCall(
    val id: String,
    val type: String,
    val function: FunctionCall
)

@Serializable
data class FunctionCall(
    val name: String,
    val arguments: String
)

@Serializable
data class ImageGenerationRequest(
    val prompt: String,
    val model: String = Models.Defaults.IMAGE_GENERATION,
    val n: Int = 1,
    val size: String = "1024x1024",
    val style: String? = null,
    val quality: String? = null,
    @SerialName("response_format") val responseFormat: String = "url"
)

@Serializable
data class ImageData(
    val url: String? = null,
    @SerialName("b64_json") val b64Json: String? = null,
    @SerialName("revised_prompt") val revisedPrompt: String? = null
)

@Serializable
data class ImageGenerationResponse(
    val created: Long,
    val data: List<ImageData>
)

@Serializable
data class ImageEditResponse(
    val created: Long,
    val data: List<ImageData>
)