package com.vend.fmr.aieng.mcp

import com.vend.fmr.aieng.OPEN_AI_KEY
import com.vend.fmr.aieng.apis.openai.*
import com.vend.fmr.aieng.utils.Prompts
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import com.vend.fmr.aieng.apis.openai.Tool as OpenAITool

class McpClient(private val serverUrl: String, private val originalClientIp: String? = null) : Closeable {

    private val openai = OpenAI(OPEN_AI_KEY)
    private var availableTools: List<Tool> = emptyList()
    private var convertedOpenAITools: List<OpenAITool> = emptyList()
    private var isConnected = false
    private var requestId = 1

    /**
     * Set available tools (for cached sessions)
     */
    fun setAvailableTools(tools: List<Tool>) {
        availableTools = tools
    }

    /**
     * Set pre-converted OpenAI tools (for cached sessions)
     */
    fun setConvertedTools(tools: List<OpenAITool>) {
        convertedOpenAITools = tools
    }

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
    }

    /**
     * Step 1: Initialize connection with MCP server
     */
    suspend fun initialize(): McpConnectionResult {
        return try {
            val initRequest = McpRequest(
                id = requestId++,
                method = "initialize",
                params = McpParams(
                    protocolVersion = "2024-11-05",
                    clientInfo = ClientInfo(
                        name = "Kotlin AI MCP Client",
                        version = "1.0.0"
                    )
                )
            )

            val response = sendRequest(initRequest)

            if (response.error != null) {
                McpConnectionResult(
                    success = false,
                    error = response.error.message
                )
            } else {
                isConnected = true
                McpConnectionResult(
                    success = true,
                    serverInfo = response.result?.serverInfo,
                    protocolVersion = response.result?.protocolVersion
                )
            }
        } catch (e: Exception) {
            McpConnectionResult(
                success = false,
                error = "Connection failed: ${e.message}"
            )
        }
    }

    /**
     * Step 2: Discover available tools from MCP server
     */
    suspend fun discoverTools(): McpToolsResult {
        if (!isConnected) {
            return McpToolsResult(
                success = false,
                error = "Must connect to server first"
            )
        }

        return try {
            val toolsRequest = McpRequest(
                id = requestId++,
                method = "tools/list"
            )

            val response = sendRequest(toolsRequest)

            if (response.error != null) {
                McpToolsResult(
                    success = false,
                    error = response.error.message
                )
            } else {
                availableTools = response.result?.tools ?: emptyList()
                McpToolsResult(
                    success = true,
                    tools = availableTools
                )
            }
        } catch (e: Exception) {
            McpToolsResult(
                success = false,
                error = "Tool discovery failed: ${e.message}"
            )
        }
    }

    /**
     * AI-powered tool selection with conversation context
     * Maintains session state and conversation history
     */
    suspend fun process(conversationHistory: List<McpChatMessage>): McpAiResult {
        if (availableTools.isEmpty()) {
            return McpAiResult(
                success = false,
                error = "No tools available. Run discoverTools() first."
            )
        }

        return try {
            // Use cached converted tools if available, otherwise convert
            val openaiTools = convertedOpenAITools.ifEmpty {
                convertMcpToolsToOpenAI(availableTools)
            }

            val conversationMessages = mutableListOf<Message>()
            val toolsDescription = formatToolsForSystemMessage(availableTools)
            val systemMessage = Prompts.mcpAssistantSystem(toolsDescription)

            conversationMessages.add(Message("system", TextContent(systemMessage)))

            conversationHistory.takeLast(10).forEach { msg ->
                conversationMessages.add(
                    Message(msg.role, TextContent(msg.content))
                )
            }

            val aiResponse = openai.createChatCompletion(
                messages = conversationMessages,
                tools = openaiTools,
                toolChoice = "auto",
                maxTokens = 300,
                temperature = 0.1
            )

            // Implement iterative tool execution for multi-step reasoning
            val maxIterations = 5
            var currentIteration = 0
            val allToolsUsed = mutableListOf<String>()

            while (currentIteration < maxIterations) {
                val currentResponse = if (currentIteration == 0) {
                    aiResponse
                } else {
                    // Make new AI call with updated conversation context
                    openai.createChatCompletion(
                        messages = conversationMessages,
                        tools = openaiTools,
                        toolChoice = "auto",
                        maxTokens = 300,
                        temperature = 0.1
                    )
                }

                val choice = currentResponse.choices.firstOrNull()
                val toolCalls = choice?.message?.toolCalls

                if (toolCalls?.isNotEmpty() == true) {
                    // Add assistant message with tool calls to conversation
                    conversationMessages.add(
                        Message(
                            role = "assistant",
                            content = choice.message.content,
                            toolCalls = toolCalls
                        )
                    )

                    // Execute tools and add results to conversation
                    for (toolCall in toolCalls) {
                        val toolResult = executeToolCall(toolCall.function.name, toolCall.function.arguments)
                        allToolsUsed.add(toolCall.function.name)

                        // Add tool result to conversation
                        conversationMessages.add(
                            Message(
                                role = "tool",
                                content = TextContent(toolResult),
                                toolCallId = toolCall.id
                            )
                        )
                    }

                    currentIteration++
                } else {
                    // AI is done with tool calls, return final response
                    val finalContent = choice?.message?.content?.toString() ?: "I've completed the requested analysis."

                    return McpAiResult(
                        success = true,
                        response = finalContent,
                        toolsUsed = allToolsUsed,
                        reasoning = if (allToolsUsed.isNotEmpty()) {
                            "AI used multi-step reasoning with ${allToolsUsed.size} tool call(s) across ${currentIteration} iteration(s)"
                        } else {
                            "AI answered directly without needing external tools"
                        }
                    )
                }
            }

            // Max iterations reached
            McpAiResult(
                success = true,
                response = "I've used multiple tools to gather information. Let me know if you need more specific details.",
                toolsUsed = allToolsUsed,
                reasoning = "AI completed multi-step analysis using ${allToolsUsed.size} tool(s) (max iterations reached)"
            )

        } catch (e: Exception) {
            McpAiResult(
                success = false,
                error = "AI processing failed: ${e.message}"
            )
        }
    }

    /**
     * Execute a specific tool call via MCP
     */
    private suspend fun executeToolCall(toolName: String, argumentsJson: String): String {
        return try {

            val arguments = if (argumentsJson.isBlank() || argumentsJson == "{}" || argumentsJson == "null") {
                emptyMap()
            } else {
                try {
                    val argsJson = json.parseToJsonElement(argumentsJson).jsonObject
                    argsJson.mapValues { it.value.jsonPrimitive.content }
                } catch (_: Exception) {
                    emptyMap()
                }
            }


            val toolRequest = McpRequest(
                id = requestId++,
                method = "tools/call",
                params = McpParams(
                    name = toolName,
                    arguments = arguments
                )
            )

            val response = sendRequest(toolRequest)

            if (response.error != null) {
                "Error calling $toolName: ${response.error.message}"
            } else {
                response.result?.content?.firstOrNull()?.text ?: "Tool executed but no result"
            }
        } catch (e: Exception) {
            "Error executing $toolName: ${e.message}"
        }
    }

    /**
     * Convert MCP tools to OpenAI function calling format
     * Uses the exact descriptions from MCP server - no hardcoding!
     */
    fun convertMcpToolsToOpenAI(mcpTools: List<Tool>): List<OpenAITool> {
        return mcpTools.map { mcpTool ->

            val openaiTool = OpenAITool(
                function = FunctionDefinition(
                    name = mcpTool.name,
                    description = mcpTool.description, // Use exact MCP description
                    parameters = FunctionParameters(
                        properties = mcpTool.inputSchema.properties.mapValues { (_, prop) ->
                            PropertyDefinition(
                                type = prop.type,
                                description = prop.description // Use exact MCP property descriptions
                            )
                        },
                        required = mcpTool.inputSchema.properties.keys.toList()
                    )
                )
            )

            return@map openaiTool
        }
    }

    /**
     * Send JSON-RPC request to MCP server
     */
    private suspend fun sendRequest(request: McpRequest): McpResponse {
        val response = client.post(serverUrl) {
            contentType(ContentType.Application.Json)
            setBody(request)
            // Forward the original client IP if available
            if (originalClientIp != null) {
                header("X-Original-Client-IP", originalClientIp)
            }
        }

        if (!response.status.isSuccess()) {
            throw Exception("HTTP ${response.status.value}: ${response.status.description}")
        }

        val responseText = response.bodyAsText()
        return json.decodeFromString<McpResponse>(responseText)
    }

    override fun close() {
        client.close()
    }

    /**
     * Format available tools for system message
     */
    private fun formatToolsForSystemMessage(tools: List<Tool>): String {
        return tools.joinToString("\n") { tool ->
            val params = tool.inputSchema.properties.entries.joinToString(", ") { (name, prop) ->
                "$name (${prop.type}): ${prop.description}"
            }
            "- ${tool.name}: ${tool.description}" + if (params.isNotEmpty()) "\n  Parameters: $params" else ""
        }
    }
}

// Result data classes for MCP operations
@kotlinx.serialization.Serializable
data class McpConnectionResult(
    val success: Boolean,
    val serverInfo: ServerInfo? = null,
    val protocolVersion: String? = null,
    val error: String? = null
)

@kotlinx.serialization.Serializable
data class McpToolsResult(
    val success: Boolean,
    val tools: List<Tool> = emptyList(),
    val error: String? = null
)

@kotlinx.serialization.Serializable
data class McpAiResult(
    val success: Boolean,
    val response: String? = null,
    val toolsUsed: List<String> = emptyList(),
    val reasoning: String? = null,
    val error: String? = null
)