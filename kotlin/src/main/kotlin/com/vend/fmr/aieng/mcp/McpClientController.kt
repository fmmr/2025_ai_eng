package com.vend.fmr.aieng.mcp

import jakarta.servlet.http.HttpSession
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class McpClientController {

    @GetMapping("/demo/mcp-client")
    fun mcpClientDemo(model: Model, session: HttpSession): String {
        model.addAttribute("pageTitle", "MCP Client Demo")
        model.addAttribute("activeTab", "mcp-client")
        
        // Initialize session tools cache and conversation history
        if (session.getAttribute("mcpToolsCache") == null) {
            session.setAttribute("mcpToolsCache", emptyList<Tool>())
        }
        if (session.getAttribute("mcpOpenAIToolsCache") == null) {
            session.setAttribute("mcpOpenAIToolsCache", emptyList<com.vend.fmr.aieng.apis.openai.Tool>())
        }
        if (session.getAttribute("mcpConversation") == null) {
            session.setAttribute("mcpConversation", mutableListOf<McpChatMessage>())
        }
        
        return "mcp-client-demo"
    }

    @PostMapping("/demo/mcp-client/ai-assist")
    @ResponseBody
    fun aiAssistedToolSelection(@RequestBody request: AiAssistRequest, session: HttpSession): AiAssistResponse = runBlocking {
        @Suppress("UNCHECKED_CAST")
        val mcpConversation = session.getAttribute("mcpConversation") as? MutableList<McpChatMessage> 
            ?: mutableListOf<McpChatMessage>().also { session.setAttribute("mcpConversation", it) }
        
        @Suppress("UNCHECKED_CAST") 
        var cachedTools = session.getAttribute("mcpToolsCache") as? List<Tool> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        var cachedOpenAITools = session.getAttribute("mcpOpenAIToolsCache") as? List<com.vend.fmr.aieng.apis.openai.Tool> ?: emptyList()
        
        // Add user message to conversation
        mcpConversation.add(McpChatMessage("user", request.query))
        
        return@runBlocking try {
            val serverUrl = "http://localhost:8080/mcp/"
            val mcpClient = McpClient(serverUrl)
            
            try {
                // Only connect and discover tools if not cached
                if (cachedTools.isEmpty()) {
                    println("🔄 First request - initializing MCP connection")
                    val connectionResult = mcpClient.initialize()
                    if (!connectionResult.success) {
                        return@runBlocking AiAssistResponse(
                            status = "error",
                            error = "Failed to connect to MCP server: ${connectionResult.error}"
                        )
                    }
                    
                    val toolsResult = mcpClient.discoverTools()
                    if (!toolsResult.success) {
                        return@runBlocking AiAssistResponse(
                            status = "error", 
                            error = "Failed to discover tools: ${toolsResult.error}"
                        )
                    }
                    
                    cachedTools = toolsResult.tools
                    session.setAttribute("mcpToolsCache", cachedTools)
                    
                    // Convert and cache OpenAI tools too
                    cachedOpenAITools = mcpClient.convertMcpToolsToOpenAI(cachedTools)
                    session.setAttribute("mcpOpenAIToolsCache", cachedOpenAITools)
                    
                    println("✅ Cached ${cachedTools.size} MCP tools and ${cachedOpenAITools.size} OpenAI tools for session")
                } else {
                    // Quick connection for subsequent requests
                    println("⚡ Using cached tools (${cachedTools.size} MCP, ${cachedOpenAITools.size} OpenAI) - no conversion needed")
                    mcpClient.initialize()
                    mcpClient.setAvailableTools(cachedTools)
                    mcpClient.setConvertedTools(cachedOpenAITools)
                }
                
                // Process with conversation context
                val aiResult = mcpClient.processWithAIAndContext(request.query, mcpConversation)
                
                if (aiResult.success) {
                    val assistantMessage = if (aiResult.toolsUsed.isNotEmpty()) {
                        // AI used tools
                        mcpConversation.add(McpChatMessage("assistant", aiResult.response ?: "", aiResult.toolsUsed.firstOrNull()))
                        
                        AiAssistResponse(
                            status = "success",
                            response = aiResult.response,
                            selectedTool = aiResult.toolsUsed.firstOrNull(),
                            reasoning = aiResult.reasoning ?: "AI selected tools to answer your question"
                        )
                    } else {
                        // AI answered directly
                        mcpConversation.add(McpChatMessage("assistant", aiResult.response ?: ""))
                        
                        AiAssistResponse(
                            status = "no_tool_needed",
                            response = aiResult.response,
                            reasoning = aiResult.reasoning ?: "AI answered without using tools"
                        )
                    }
                    
                    // Keep conversation history manageable
                    if (mcpConversation.size > 20) {
                        mcpConversation.removeAt(0)
                    }
                    
                    assistantMessage
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
    
    @PostMapping("/demo/mcp-client/reset")
    @ResponseBody
    fun resetSession(session: HttpSession): Map<String, String> {
        session.removeAttribute("mcpToolsCache")
        session.removeAttribute("mcpOpenAIToolsCache")
        session.removeAttribute("mcpConversation")
        return mapOf("status" to "reset", "message" to "Session cleared - next request will re-discover tools")
    }
}