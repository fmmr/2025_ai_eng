package com.vend.fmr.aieng.mcp

import com.vend.fmr.aieng.OPEN_AI_KEY
import com.vend.fmr.aieng.apis.openai.OpenAI
import com.vend.fmr.aieng.apis.openai.FunctionDefinition
import com.vend.fmr.aieng.apis.openai.FunctionParameters
import com.vend.fmr.aieng.apis.openai.PropertyDefinition
import com.vend.fmr.aieng.apis.openai.Tool as OpenAITool
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

class McpClient(private val serverUrl: String) : Closeable {
    
    private val openai = OpenAI(OPEN_AI_KEY)
    private var availableTools: List<Tool> = emptyList()
    private var isConnected = false
    private var requestId = 1
    
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
     * Step 3: AI-powered tool selection and execution
     * This is the real MCP client magic - OpenAI decides which tools to use!
     */
    suspend fun processWithAI(userQuery: String): McpAiResult {
        if (availableTools.isEmpty()) {
            return McpAiResult(
                success = false,
                error = "No tools available. Run discoverTools() first."
            )
        }
        
        return try {
            // Convert MCP tools to OpenAI function calling format
            val openaiTools = convertMcpToolsToOpenAI(availableTools)
            
            // Let OpenAI decide which tools to call
            val systemMessage = """
                You are an AI assistant with access to external tools via MCP (Model Context Protocol).
                
                Available tools:
                ${availableTools.joinToString("\n") { tool ->
                    val params = tool.inputSchema.properties.entries.joinToString(", ") { (name, prop) ->
                        "$name (${prop.type}): ${prop.description}"
                    }
                    "- ${tool.name}: ${tool.description}" + if (params.isNotEmpty()) "\n  Parameters: $params" else ""
                }}
                
                Guidelines:
                - Use tools when users ask for specific data or actions that match the tool descriptions
                - Always provide all required parameters when calling tools
                - For geographic locations, you can use your knowledge to provide coordinates
                - For stock symbols, extract them from the user's query (e.g., "AAPL", "MSFT")
                - When no tools are needed, respond directly with your knowledge
                
                Example parameter extraction:
                - "Tell me about AAPL" → symbol="AAPL"
                - "Weather in Oslo" → latitude=59.9139, longitude=10.7522
                - "What's my IP location for 8.8.8.8" → ip="8.8.8.8"
            """.trimIndent()
            
            val aiResponse = openai.createChatCompletion(
                systemMessage = systemMessage,
                prompt = userQuery,
                tools = openaiTools,
                toolChoice = "auto",
                maxTokens = 300,
                temperature = 0.1
            )
            
            val choice = aiResponse.choices.firstOrNull()
            val toolCalls = choice?.message?.toolCalls
            
            println("🤖 OpenAI Response:")
            println("📝 Message content: ${choice?.message?.content}")
            println("🛠️ Tool calls: ${toolCalls?.size ?: 0}")
            
            if (toolCalls?.isNotEmpty() == true) {
                // AI wants to use tools
                val results = mutableListOf<String>()
                
                for (toolCall in toolCalls) {
                    println("🎯 Tool call: ${toolCall.function.name}")
                    println("📋 Arguments: '${toolCall.function.arguments}'")
                    val toolResult = executeToolCall(toolCall.function.name, toolCall.function.arguments)
                    results.add(toolResult)
                }
                
                // Let AI format the final response with tool results
                val finalResponse = openai.createChatCompletion(
                    systemMessage = "You are a helpful assistant. Format the tool results into a natural response for the user.",
                    prompt = "User asked: '$userQuery'\n\nTool results: ${results.joinToString("\n")}\n\nProvide a helpful response:",
                    maxTokens = 200,
                    temperature = 0.3
                )
                
                McpAiResult(
                    success = true,
                    response = finalResponse.choices.firstOrNull()?.message?.content?.toString() ?: "Tool executed successfully",
                    toolsUsed = toolCalls.map { it.function.name },
                    reasoning = "AI selected and executed ${toolCalls.size} tool(s) to answer your question"
                )
            } else {
                // AI answered directly without tools
                McpAiResult(
                    success = true,
                    response = choice?.message?.content?.toString() ?: "I'm not sure how to help with that.",
                    toolsUsed = emptyList(),
                    reasoning = "AI answered directly without needing external tools"
                )
            }
            
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
            println("🔧 Executing tool: $toolName")
            println("📝 Raw arguments JSON: '$argumentsJson'")
            
            val arguments = if (argumentsJson.isBlank() || argumentsJson == "{}" || argumentsJson == "null") {
                emptyMap()
            } else {
                try {
                    val argsJson = json.parseToJsonElement(argumentsJson).jsonObject
                    argsJson.mapValues { it.value.jsonPrimitive.content }
                } catch (e: Exception) {
                    println("❌ Failed to parse arguments JSON: ${e.message}")
                    emptyMap()
                }
            }
            
            println("🎯 Parsed arguments: $arguments")
            
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
    private fun convertMcpToolsToOpenAI(mcpTools: List<Tool>): List<OpenAITool> {
        return mcpTools.map { mcpTool ->
            println("🔄 Converting MCP tool: ${mcpTool.name}")
            println("📋 Description: ${mcpTool.description}")
            println("📋 Properties: ${mcpTool.inputSchema.properties}")
            
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
            
            println("✅ Converted to OpenAI format: ${openaiTool.function.name}")
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