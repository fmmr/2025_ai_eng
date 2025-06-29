package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.utils.Demo
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.DisabledChatModel
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.UserMessage
import dev.langchain4j.service.V
import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/demo/langchain4j")
class Langchain4jController : BaseController(Demo.LANGCHAIN4J) {

    @Autowired
    @Qualifier("langchain4jChatModel")
    private lateinit var chatModel: ChatModel

    // AI Service interface using LangChain4j annotations
    interface AssistantService {
        @UserMessage("Analyze this text and provide insights: {{text}}")
        fun analyzeText(@V("text") text: String): String
        
        @UserMessage("Summarize this text in {{maxWords}} words or less: {{text}}")
        fun summarizeText(@V("text") text: String, @V("maxWords") maxWords: Int): String
        
        @UserMessage("Classify the sentiment of this text as positive, negative, or neutral. Respond with just the classification and a confidence score: {{text}}")
        fun classifySentiment(@V("text") text: String): String
    }

    override fun addDefaultModel(model: Model, session: HttpSession) {
        if (chatModel is DisabledChatModel) {
            model.addAttribute("error", "LangChain4j is not available - OPENAI_API_KEY environment variable is required")
        }
        
        model.addAttribute("defaultText", "LangChain4j provides a fantastic abstraction layer for AI applications. It simplifies integration with multiple AI providers, offers type-safe interfaces, and includes powerful tools for building production-ready AI systems with minimal boilerplate code.")
        model.addAttribute("defaultMaxWords", 50)
        model.addAttribute("explanation", "Compare LangChain4j's declarative approach vs. our custom HTTP implementations")
        
        // Initialize empty formData for GET request
        val formData = mutableMapOf<String, String>()
        formData["text"] = ""
        formData["operation"] = "analyze"
        formData["maxWords"] = "50"
        model.addAttribute("formData", formData)
    }

    @PostMapping
    fun processLangChain4j(
        @RequestParam("text", defaultValue = "") text: String,
        @RequestParam("operation", defaultValue = "analyze") operation: String,
        @RequestParam("maxWords", defaultValue = "50") maxWords: String,
        model: Model
    ): String {
        val defaultText = "LangChain4j provides a fantastic abstraction layer for AI applications. It simplifies integration with multiple AI providers, offers type-safe interfaces, and includes powerful tools for building production-ready AI systems with minimal boilerplate code."
        val inputText = if (text.isBlank()) defaultText else text.trim()
        val maxWordsInt = maxWords.toIntOrNull() ?: 50
        
        val formData = mutableMapOf<String, String>()
        formData["text"] = inputText
        formData["operation"] = operation
        formData["maxWords"] = maxWordsInt.toString()
        model.addAttribute("formData", formData)
        
        if (chatModel is DisabledChatModel) {
            model.addAttribute("error", "LangChain4j is not available - OPENAI_API_KEY environment variable is required")
            return demo.id
        }
        
        try {
            // Create AI Service using LangChain4j
            val assistant = AiServices.builder(AssistantService::class.java)
                .chatModel(chatModel)
                .build()
            
            val result = when (operation) {
                "summarize" -> assistant.summarizeText(inputText, maxWordsInt)
                "sentiment" -> assistant.classifySentiment(inputText)
                "analyze" -> assistant.analyzeText(inputText)
                else -> assistant.analyzeText(inputText)
            }
            
            model.addAttribute("inputText", inputText)
            model.addAttribute("operation", operation)
            model.addAttribute("maxWords", maxWordsInt)
            model.addAttribute("result", result)
            model.addAttribute("modelUsed", chatModel.javaClass.simpleName)
            model.addAttribute("results", true)
            model.addAttribute("success", true)
            
        } catch (e: Exception) {
            model.addAttribute("error", "LangChain4j operation failed: ${e.message}")
            model.addAttribute("inputText", inputText)
            model.addAttribute("operation", operation)
            model.addAttribute("maxWords", maxWordsInt)
        }
        
        model.addAttribute("defaultText", defaultText)
        model.addAttribute("defaultMaxWords", 50)
        model.addAttribute("explanation", "Compare LangChain4j's declarative approach vs. our custom HTTP implementations")
        
        return demo.id
    }
}