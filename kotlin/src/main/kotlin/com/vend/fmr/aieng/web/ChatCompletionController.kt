package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.OPEN_AI_KEY
import com.vend.fmr.aieng.apis.openai.OpenAI
import com.vend.fmr.aieng.utils.Models
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/demo/chat-completion")
class ChatCompletionController {

    @GetMapping
    fun chatCompletionDemo(model: Model): String {
        model.addAttribute("pageTitle", "OpenAI Chat Completion")
        model.addAttribute("activeTab", "chat-completion")
        return "chat-completion-demo"
    }

    @PostMapping
    suspend fun processChatCompletion(
        @RequestParam(defaultValue = "") userPrompt: String,
        @RequestParam(defaultValue = "") systemMessage: String,
        @RequestParam(defaultValue = Models.Defaults.CHAT_COMPLETION) modelName: String,
        @RequestParam(defaultValue = "300") maxTokens: Int,
        @RequestParam(defaultValue = "0.7") temperature: Double,
        model: Model
    ): String {
        model.addAttribute("pageTitle", "OpenAI Chat Completion")
        model.addAttribute("activeTab", "chat-completion")
        
        if (userPrompt.isNotBlank()) {
            try {
                val apiKey = OPEN_AI_KEY

                val openAI = OpenAI(apiKey)
                val response = openAI.createChatCompletion(
                    prompt = userPrompt,
                    systemMessage = systemMessage.takeIf { it.isNotBlank() },
                    model = modelName,
                    maxTokens = maxTokens,
                    temperature = temperature
                )
                openAI.close()
                
                model.addAttribute("chatResult", ChatResult(
                    userPrompt = userPrompt,
                    systemMessage = systemMessage.takeIf { it.isNotBlank() } ?: "No system message",
                    response = response.text(),
                    usage = response.usage(),
                    model = response.model
                ))
                model.addAttribute("formData", ChatFormData(userPrompt, systemMessage, modelName, maxTokens, temperature))
                
            } catch (e: Exception) {
                model.addAttribute("error", "Error: ${e.message}")
                model.addAttribute("formData", ChatFormData(userPrompt, systemMessage, modelName, maxTokens, temperature))
            }
        }
        
        return "chat-completion-demo"
    }
}
