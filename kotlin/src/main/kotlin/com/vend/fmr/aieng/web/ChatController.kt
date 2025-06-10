package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.apis.openai.Message
import com.vend.fmr.aieng.apis.openai.TextContent
import com.vend.fmr.aieng.apis.openai.OpenAI
import com.vend.fmr.aieng.utils.Demo
import com.vend.fmr.aieng.utils.Prompts
import jakarta.servlet.http.HttpSession
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

data class ChatMessage(
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatRequest(
    val message: String
)

data class ChatResponse(
    val status: String,
    val response: String? = null,
    val error: String? = null
)

@Controller
@RequestMapping("/demo/chat")
class ChatController(
    private val openAI: OpenAI
) : BaseController(Demo.CHAT_INTERACTIVE) {

    @GetMapping
    fun chatDemo(model: Model, session: HttpSession): String {
        model.addAttribute("pageTitle", "Interactive Chat")
        model.addAttribute("activeTab", "chat")

        @Suppress("UNCHECKED_CAST")
        val chatHistory = session.getAttribute("chatHistory") as? MutableList<ChatMessage> ?: mutableListOf<ChatMessage>()
        model.addAttribute("chatHistory", chatHistory)

        return "chat-demo"
    }

    @PostMapping("/message")
    @ResponseBody
    fun processMessage(
        @RequestBody request: ChatRequest,
        session: HttpSession
    ): ChatResponse = runBlocking {
        @Suppress("UNCHECKED_CAST")
        val chatHistory = session.getAttribute("chatHistory") as? MutableList<ChatMessage>
            ?: mutableListOf<ChatMessage>().also { session.setAttribute("chatHistory", it) }

        chatHistory.add(ChatMessage("user", request.message))

        try {
            val conversationMessages = mutableListOf<Message>()

            conversationMessages.add(Message("system", TextContent(Prompts.CHAT_ASSISTANT)))

            chatHistory.forEach {
                conversationMessages.add(Message(it.role, TextContent(it.content)))
            }

            val response = openAI.createChatCompletion(
                messages = conversationMessages,
                maxTokens = 300,
                temperature = 0.7
            )

            val assistantReply = response.text()

            chatHistory.add(ChatMessage(Prompts.Roles.ASSISTANT, assistantReply))

            if (chatHistory.size > 20) {
                chatHistory.removeAt(0)
            }

            ChatResponse(
                status = "success",
                response = assistantReply
            )

        } catch (e: Exception) {
            val errorMessage = "Sorry, I encountered an error: ${e.message}"
            chatHistory.add(ChatMessage(Prompts.Roles.ASSISTANT, errorMessage))
            
            ChatResponse(
                status = "error",
                error = errorMessage
            )
        }
    }

    @PostMapping("/reset")
    @ResponseBody
    fun resetChatSession(session: HttpSession): Map<String, String> {
        session.removeAttribute("chatHistory")
        return mapOf("status" to "reset", "message" to "Chat history cleared")
    }
}