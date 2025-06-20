package com.vend.fmr.aieng.mcp

import com.vend.fmr.aieng.apis.openai.OpenAI
import com.vend.fmr.aieng.utils.AgentTool
import com.vend.fmr.aieng.utils.Demo
import com.vend.fmr.aieng.utils.getClientIpAddress
import com.vend.fmr.aieng.web.BaseController
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

data class StreamRequest(
    val query: String,
    val verbosity: String = "function_calls" // "minimal", "function_calls", "debug"
)

data class StreamEvent(
    val type: String, // "progress", "result", "error", "complete"
    val message: String,
    val data: Any? = null,
    val timestamp: String = java.time.LocalTime.now().toString()
)

@Controller
@RequestMapping("/demo/mcp-assistant")
class McpAssistantController(private val openAI: OpenAI) : BaseController(Demo.MCP_ASSISTANT) {

    override fun addDefaultModel(model: Model, session: HttpSession) {
        // Pass available tools for display
        model.addAttribute("availableTools", AgentTool.entries)
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
    }


    private fun sendStreamEvent(sessionId: String, event: StreamEvent) {
        sendSseEvent(sessionId, event.type, event.message, event.data)
    }

    @PostMapping("/process")
    @ResponseBody
    fun processMcpRequest(@RequestBody request: Map<String, String>, session: HttpSession, httpRequest: HttpServletRequest): Map<String, String> = runBlocking {
        val query = request["query"] ?: return@runBlocking mapOf("error" to "Missing query")
        val verbosity = request["verbosity"] ?: "function_calls"
        val sessionId = request["sessionId"] ?: return@runBlocking mapOf("error" to "Missing sessionId")

        val streamRequest = StreamRequest(query, verbosity)
        val clientIp = getClientIpAddress(httpRequest)

        // Process in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                sendStreamEvent(
                    sessionId, StreamEvent(
                        type = "progress",
                        message = "🤖 AI analyzing request: \"${streamRequest.query}\""
                    )
                )

                processMcpStreamRequest(streamRequest, session, clientIp, sessionId)
            } catch (e: Exception) {
                sendStreamEvent(
                    sessionId, StreamEvent(
                        type = "error",
                        message = "❌ Processing failed: ${e.message}"
                    )
                )
            }
        }

        mapOf("status" to "started")
    }

    private suspend fun processMcpStreamRequest(request: StreamRequest, session: HttpSession, clientIp: String, streamId: String) {
        @Suppress("UNCHECKED_CAST")
        val mcpConversation = session.getAttribute("mcpConversation") as? MutableList<McpChatMessage>
            ?: mutableListOf<McpChatMessage>().also { session.setAttribute("mcpConversation", it) }

        @Suppress("UNCHECKED_CAST")
        var cachedTools = session.getAttribute("mcpToolsCache") as? List<Tool> ?: emptyList()

        @Suppress("UNCHECKED_CAST")
        var cachedOpenAITools = session.getAttribute("mcpOpenAIToolsCache") as? List<com.vend.fmr.aieng.apis.openai.Tool> ?: emptyList()

        // Add user message to conversation
        mcpConversation.add(McpChatMessage("user", request.query))

        try {
            val serverUrl = "http://localhost:8080/mcp/"
            val mcpClient = McpClient(serverUrl, openAI, clientIp)

            try {
                // Connection phase
                if (cachedTools.isEmpty()) {
                    if (request.verbosity == "debug") {
                        sendStreamEvent(
                            streamId, StreamEvent(
                                type = "progress",
                                message = "🔌 Connecting to MCP server..."
                            )
                        )
                    }

                    val connectionResult = mcpClient.initialize()
                    if (!connectionResult.success) {
                        sendStreamEvent(
                            streamId, StreamEvent(
                                type = "error",
                                message = "❌ Failed to connect to MCP server: ${connectionResult.error}"
                            )
                        )
                        return
                    }

                    if (request.verbosity == "debug") {
                        sendStreamEvent(
                            streamId, StreamEvent(
                                type = "progress",
                                message = "🔍 Discovering available tools..."
                            )
                        )
                    }

                    val toolsResult = mcpClient.discoverTools()
                    if (!toolsResult.success) {
                        sendStreamEvent(
                            streamId, StreamEvent(
                                type = "error",
                                message = "❌ Failed to discover tools: ${toolsResult.error}"
                            )
                        )
                        return
                    }

                    cachedTools = toolsResult.tools
                    session.setAttribute("mcpToolsCache", cachedTools)

                    cachedOpenAITools = mcpClient.convertMcpToolsToOpenAI(cachedTools)
                    session.setAttribute("mcpOpenAIToolsCache", cachedOpenAITools)

                    if (request.verbosity == "debug") {
                        sendStreamEvent(
                            streamId, StreamEvent(
                                type = "progress",
                                message = "✅ Found ${cachedTools.size} tools, converted to OpenAI format"
                            )
                        )
                    }
                } else {
                    mcpClient.initialize()
                    mcpClient.setAvailableTools(cachedTools)
                    mcpClient.setConvertedTools(cachedOpenAITools)

                    if (request.verbosity == "debug") {
                        sendStreamEvent(
                            streamId, StreamEvent(
                                type = "progress",
                                message = "♻️ Using cached tools (${cachedTools.size} available)"
                            )
                        )
                    }
                }

                // AI Processing phase
                val aiResult = mcpClient.process(mcpConversation) { progressMessage ->
                    if (request.verbosity in listOf("function_calls", "debug")) {
                        sendStreamEvent(
                            streamId, StreamEvent(
                                type = "progress",
                                message = progressMessage
                            )
                        )
                    }
                }

                if (aiResult.success) {
                    if (aiResult.toolsUsed.isNotEmpty()) {
                        // AI used tools
                        mcpConversation.add(McpChatMessage("assistant", aiResult.response ?: "", aiResult.toolsUsed.firstOrNull()))

                        if (request.verbosity in listOf("function_calls", "debug")) {
                            sendStreamEvent(
                                streamId, StreamEvent(
                                    type = "progress",
                                    message = "🎯 ${aiResult.reasoning ?: "AI selected tools to answer your question"}"
                                )
                            )

                            if (aiResult.toolsUsed.size > 1) {
                                sendStreamEvent(
                                    streamId, StreamEvent(
                                        type = "progress",
                                        message = "🔄 Multi-step execution: ${aiResult.toolsUsed.size} tools used"
                                    )
                                )

                                aiResult.toolsUsed.forEachIndexed { index, tool ->
                                    sendStreamEvent(
                                        streamId, StreamEvent(
                                            type = "progress",
                                            message = "  Step ${index + 1}: $tool"
                                        )
                                    )
                                }
                            } else {
                                sendStreamEvent(
                                    streamId, StreamEvent(
                                        type = "progress",
                                        message = "🛠️ Tool used: ${aiResult.toolsUsed.first()}"
                                    )
                                )
                            }
                        }

                        sendStreamEvent(
                            streamId, StreamEvent(
                                type = "result",
                                message = "${aiResult.response}",
                                data = mapOf(
                                    "response" to aiResult.response,
                                    "toolsUsed" to aiResult.toolsUsed,
                                    "reasoning" to aiResult.reasoning
                                )
                            )
                        )
                    } else {
                        // AI answered directly
                        mcpConversation.add(McpChatMessage("assistant", aiResult.response ?: ""))

                        sendStreamEvent(
                            streamId, StreamEvent(
                                type = "result",
                                message = "${aiResult.response}",
                                data = mapOf(
                                    "response" to aiResult.response,
                                    "reasoning" to aiResult.reasoning
                                )
                            )
                        )
                    }

                    // Keep conversation history manageable
                    if (mcpConversation.size > 20) {
                        mcpConversation.removeAt(0)
                    }

                    sendStreamEvent(
                        streamId, StreamEvent(
                            type = "progress",
                            message = "💬 Session: Conversation context maintained (${mcpConversation.size} messages)"
                        )
                    )
                } else {
                    sendStreamEvent(
                        streamId, StreamEvent(
                            type = "error",
                            message = "❌ AI processing failed: ${aiResult.error ?: "Unknown error"}"
                        )
                    )
                }

            } finally {
                mcpClient.close()
            }

        } catch (e: Exception) {
            sendStreamEvent(
                streamId, StreamEvent(
                    type = "error",
                    message = "❌ MCP client failed: ${e.message}"
                )
            )
        } finally {
            sendStreamEvent(
                streamId, StreamEvent(
                    type = "complete",
                    message = "✅ Processing complete"
                )
            )
            // Clean up SSE connection after processing
            activeEmitters.remove(streamId)
        }
    }


    @PostMapping("/reset")
    @ResponseBody
    fun resetMcpSession(session: HttpSession): Map<String, String> {
        session.removeAttribute("mcpToolsCache")
        session.removeAttribute("mcpOpenAIToolsCache")
        session.removeAttribute("mcpConversation")
        return mapOf("status" to "reset", "message" to "Session cleared - next request will re-discover tools")
    }
}