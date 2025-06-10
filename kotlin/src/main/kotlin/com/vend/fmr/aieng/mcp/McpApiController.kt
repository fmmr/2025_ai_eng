package com.vend.fmr.aieng.mcp

import com.vend.fmr.aieng.utils.Tools
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mcp")
@CrossOrigin(origins = ["*"])
class McpApiController {

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
            encodeDefaults = true
        }
    }


    /**
     * Main MCP endpoint - handles JSON-RPC 2.0 requests
     * MCP (Model Context Protocol) uses JSON-RPC 2.0 over HTTP
     */
    @PostMapping("/", 
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun handleMcpRequest(@RequestBody request: String, httpRequest: HttpServletRequest): String {
        
        return try {
            val mcpRequest = json.decodeFromString<McpRequest>(request)
            
            when (mcpRequest.method) {
                "initialize" -> handleInitialize(mcpRequest.id)
                "tools/list" -> handleToolsList(mcpRequest.id)
                "tools/call" -> handleToolsCall(mcpRequest, mcpRequest.id, httpRequest)
                else -> createErrorResponse(mcpRequest.id, -32601, "Method not found: ${mcpRequest.method}")
            }
        } catch (e: Exception) {
            println("❌ MCP Error: ${e.message}")
            createErrorResponse(null, -32700, "Parse error: ${e.message}")
        }
    }

    private fun handleInitialize(id: Int?): String {
        val response = McpResponse(
            id = id,
            result = McpResult(
                protocolVersion = "2024-11-05",
                serverInfo = ServerInfo(
                    name = "Kotlin AI Engineering MCP Server",
                    version = "1.0.0"
                ),
                capabilities = Capabilities()
            )
        )
        return json.encodeToString(McpResponse.serializer(), response)
    }

    private fun handleToolsList(id: Int?): String {
        val tools = Tools.entries.map { it.toMcpTool() }
        
        val response = McpResponse(
            id = id,
            result = McpResult(tools = tools)
        )
        return json.encodeToString(McpResponse.serializer(), response)
    }

    private fun handleToolsCall(mcpRequest: McpRequest, id: Int?, httpRequest: HttpServletRequest): String {
        val toolName = mcpRequest.params?.name ?: return createErrorResponse(id, -32602, "Missing tool name")
        val arguments = mcpRequest.params.arguments ?: emptyMap()
        
        return try {
            runBlocking {
                val argumentsJson = if (arguments.isEmpty()) "{}" else "{" + arguments.entries.joinToString(",") { "\"${it.key}\":\"${it.value}\"" } + "}"
                val result = Tools.execute(toolName, argumentsJson, httpRequest)
                createSuccessResponse(id, result)
            }
        } catch (e: Exception) {
            createErrorResponse(id, -32603, "Tool execution failed: ${e.message}")
        }
    }

    private fun createSuccessResponse(id: Int?, text: String): String {
        val response = McpResponse(
            id = id,
            result = McpResult(
                content = listOf(Content(text = text))
            )
        )
        return json.encodeToString(McpResponse.serializer(), response)
    }

    private fun createErrorResponse(id: Int?, code: Int, message: String): String {
        val response = McpResponse(
            id = id,
            error = McpError(code = code, message = message)
        )
        return json.encodeToString(McpResponse.serializer(), response)
    }
}