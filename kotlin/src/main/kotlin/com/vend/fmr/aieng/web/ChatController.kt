package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.impl.openai.Message
import com.vend.fmr.aieng.impl.openai.TextContent
import com.vend.fmr.aieng.openAI
import com.vend.fmr.aieng.utils.Prompts
import jakarta.servlet.http.HttpSession
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

data class ChatMessage(
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Controller
@RequestMapping("/demo/chat")
class ChatController {

    @GetMapping
    fun chatDemo(model: Model, session: HttpSession): String {
        model.addAttribute("pageTitle", "Interactive Chat")
        model.addAttribute("activeTab", "chat")
        
        // Get chat history from session
        val chatHistory = session.getAttribute("chatHistory") as? MutableList<ChatMessage> ?: mutableListOf()
        model.addAttribute("chatHistory", chatHistory)
        
        return "chat-demo"
    }

    @PostMapping
    fun processMessage(
        @RequestParam userMessage: String,
        model: Model,
        session: HttpSession
    ): String = runBlocking {
        model.addAttribute("pageTitle", "Interactive Chat")
        model.addAttribute("activeTab", "chat")
        
        // Get or create chat history
        val chatHistory = session.getAttribute("chatHistory") as? MutableList<ChatMessage> 
            ?: mutableListOf<ChatMessage>().also { session.setAttribute("chatHistory", it) }
        
        // Add user message to history
        chatHistory.add(ChatMessage("user", userMessage))
        
        try {
            // Build conversation for OpenAI API
            val conversationMessages = mutableListOf<Message>()
            
            // Add system message
            conversationMessages.add(Message("system", TextContent(Prompts.CHAT_ASSISTANT)))
            
            // Add conversation history (convert to OpenAI format)
            chatHistory.forEach { 
                conversationMessages.add(Message(it.role, TextContent(it.content)))
            }
            
            // Get AI response
            val response = openAI.createChatCompletion(
                messages = conversationMessages,
                maxTokens = 300,
                temperature = 0.7
            )
            
            val assistantReply = response.text()
            
            // Add assistant response to history
            chatHistory.add(ChatMessage(Prompts.Roles.ASSISTANT, assistantReply))
            
            // Keep conversation manageable (last 20 messages)
            if (chatHistory.size > 20) {
                chatHistory.removeAt(0)
            }
            
        } catch (e: Exception) {
            chatHistory.add(ChatMessage(Prompts.Roles.ASSISTANT, "Sorry, I encountered an error: ${e.message}"))
        }
        
        model.addAttribute("chatHistory", chatHistory)
        return@runBlocking "chat-demo"
    }
}