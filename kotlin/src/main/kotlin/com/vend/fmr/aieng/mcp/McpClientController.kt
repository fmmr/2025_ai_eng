package com.vend.fmr.aieng.mcp

import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class McpClientController {

    // We'll create a new MCP client for each request
    // In production, you might want to manage client lifecycle differently

    @GetMapping("/demo/mcp-client")
    fun mcpClientDemo(model: Model): String {
        model.addAttribute("pageTitle", "MCP Client Demo")
        model.addAttribute("activeTab", "mcp-client")
        return "mcp-client-demo"
    }

    @PostMapping("/demo/mcp-client/ai-assist")
    @ResponseBody
    fun aiAssistedToolSelection(@RequestBody request: AiAssistRequest): AiAssistResponse = runBlocking {
        return@runBlocking try {
            val serverUrl = "http://localhost:8080/mcp/"  // Could be made configurable
            val mcpClient = McpClient(serverUrl)
            
            try {
                // Step 1: Connect to MCP server
                val connectionResult = mcpClient.initialize()
                if (!connectionResult.success) {
                    return@runBlocking AiAssistResponse(
                        status = "error",
                        error = "Failed to connect to MCP server: ${connectionResult.error}"
                    )
                }
                
                // Step 2: Discover available tools
                val toolsResult = mcpClient.discoverTools()
                if (!toolsResult.success) {
                    return@runBlocking AiAssistResponse(
                        status = "error", 
                        error = "Failed to discover tools: ${toolsResult.error}"
                    )
                }
                
                // Step 3: Let AI process the query with discovered tools
                val aiResult = mcpClient.processWithAI(request.query)
                
                if (aiResult.success) {
                    if (aiResult.toolsUsed.isNotEmpty()) {
                        // AI used tools
                        AiAssistResponse(
                            status = "success",
                            response = aiResult.response,
                            selectedTool = aiResult.toolsUsed.firstOrNull(),
                            reasoning = aiResult.reasoning ?: "AI selected tools to answer your question"
                        )
                    } else {
                        // AI answered directly
                        AiAssistResponse(
                            status = "no_tool_needed",
                            response = aiResult.response,
                            reasoning = aiResult.reasoning ?: "AI answered without using tools"
                        )
                    }
                } else {
                    AiAssistResponse(
                        status = "error",
                        error = aiResult.error ?: "AI processing failed"
                    )
                }
                
            } finally {
                mcpClient.close()
            }
            
        } catch (e: Exception) {
            AiAssistResponse(
                status = "error",
                error = "MCP client failed: ${e.message}"
            )
        }
    }
}