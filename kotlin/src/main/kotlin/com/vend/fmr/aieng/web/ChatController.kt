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
        
        val chatHistory = session.getAttribute("chatHistory") as? MutableList<ChatMessage> ?: mutableListOf<ChatMessage>()
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
        
        val chatHistory = session.getAttribute("chatHistory") as? MutableList<ChatMessage>
            ?: mutableListOf<ChatMessage>().also { session.setAttribute("chatHistory", it) }
        
        chatHistory.add(ChatMessage("user", userMessage))
        
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
            
        } catch (e: Exception) {
            chatHistory.add(ChatMessage(Prompts.Roles.ASSISTANT, "Sorry, I encountered an error: ${e.message}"))
        }
        
        model.addAttribute("chatHistory", chatHistory)
        return@runBlocking "chat-demo"
    }
}