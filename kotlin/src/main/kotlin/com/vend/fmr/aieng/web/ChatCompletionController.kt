package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.apis.openai.OpenAI
import com.vend.fmr.aieng.utils.Demo
import com.vend.fmr.aieng.utils.Models
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/demo/chat-completion")
class ChatCompletionController(
    private val openAI: OpenAI
) : BaseController(Demo.CHAT_COMPLETION) {

    @PostMapping
    suspend fun processChatCompletion(
        @RequestParam(defaultValue = "") userPrompt: String,
        @RequestParam(defaultValue = "") systemMessage: String,
        @RequestParam(defaultValue = Models.Defaults.CHAT_COMPLETION) modelName: String,
        @RequestParam(defaultValue = "300") maxTokens: Int,
        @RequestParam(defaultValue = "0.7") temperature: Double,
        model: Model
    ): String {
        if (userPrompt.isNotBlank()) {
            try {
                val response = openAI.createChatCompletion(
                    prompt = userPrompt,
                    systemMessage = systemMessage.takeIf { it.isNotBlank() },
                    model = modelName,
                    maxTokens = maxTokens,
                    temperature = temperature
                )
                
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
        
        return demo.id
    }
}
