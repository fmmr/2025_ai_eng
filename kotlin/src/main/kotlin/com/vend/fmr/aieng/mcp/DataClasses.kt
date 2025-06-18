package com.vend.fmr.aieng.mcp

import kotlinx.serialization.Serializable

@Serializable
data class McpRequest(
    val jsonrpc: String = "2.0",
    val id: Int? = null,
    val method: String,
    val params: McpParams? = null
)

@Serializable
data class McpParams(
    val protocolVersion: String? = null,
    val clientInfo: ClientInfo? = null,
    val name: String? = null,
    val arguments: Map<String, String>? = null
)

@Serializable
data class ClientInfo(
    val name: String,
    val version: String
)

@Serializable
data class McpResponse(
    val jsonrpc: String = "2.0",
    val id: Int?,
    val result: McpResult? = null,
    val error: McpError? = null
)

@Serializable
data class McpResult(
    val protocolVersion: String? = null,
    val serverInfo: ServerInfo? = null,
    val capabilities: Capabilities? = null,
    val tools: List<Tool>? = null,
    val content: List<Content>? = null,
    val resources: List<Resource>? = null,
    val prompts: List<Prompt>? = null
)

@Serializable
data class ServerInfo(
    val name: String,
    val version: String
)

@Serializable
data class Capabilities(
    val tools: Map<String, String> = emptyMap()
)

@Serializable
data class Tool(
    val name: String,
    val description: String,
    val inputSchema: InputSchema
)

@Serializable
data class InputSchema(
    val type: String = "object",
    val properties: Map<String, PropertySchema>
)

@Serializable
data class PropertySchema(
    val type: String,
    val description: String
)

@Serializable
data class Content(
    val type: String = "text",
    val text: String
)

@Serializable
data class Resource(
    val uri: String,
    val name: String,
    val description: String? = null,
    val mimeType: String? = null
)

@Serializable
data class Prompt(
    val name: String,
    val description: String,
    val arguments: List<PromptArgument>? = null
)

@Serializable
data class PromptArgument(
    val name: String,
    val description: String,
    val required: Boolean = false
)

@Serializable
data class McpError(
    val code: Int,
    val message: String
)


// Session conversation data class
@Serializable
data class McpChatMessage(
    val role: String,
    val content: String,
    val toolUsed: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)