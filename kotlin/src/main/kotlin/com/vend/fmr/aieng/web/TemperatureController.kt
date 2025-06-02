package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.openAI
import com.vend.fmr.aieng.utils.ParameterSet
import com.vend.fmr.aieng.utils.Prompts
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class TemperatureController {

    @GetMapping("/demo/temperature-effects")
    fun temperatureDemo(model: Model): String {
        model.addAttribute("pageTitle", "Temperature Effects")
        model.addAttribute("activeTab", "temperature-effects")
        
        val fixedTopP = 0.9
        
        model.addAttribute("prompt", Prompts.Defaults.CHAT_PARAMETERS_PROMPT)
        model.addAttribute("fixedParam", "top_p = $fixedTopP")
        model.addAttribute("variableParam", "temperature (0.1 → 1.8)")
        model.addAttribute("explanation", "How temperature affects creativity vs consistency")
        
        return "temperature-demo"
    }

    @PostMapping("/demo/temperature-effects")
    suspend fun runTemperatureComparison(
        @RequestParam("customPrompt", defaultValue = "") customPrompt: String,
        model: Model
    ): String {
        model.addAttribute("pageTitle", "Temperature Effects")
        model.addAttribute("activeTab", "temperature-effects")

        val fixedTopP = 0.9
        val prompt = if (customPrompt.isBlank()) {
            Prompts.Defaults.CHAT_PARAMETERS_PROMPT
        } else {
            customPrompt.trim()
        }
        
        // Preserve form data for re-display
        val formData = mutableMapOf<String, String>()
        formData["customPrompt"] = prompt
        model.addAttribute("formData", formData)
        
        val temperatureSettings = mutableListOf(
            ParameterSet(
                name = "Very Conservative",
                description = "Extremely Predictable",
                temperature = 0.1,
                topP = fixedTopP,
color = "🔵"
            ),
            ParameterSet(
                name = "Moderate", 
                description = "Balanced Responses",
                temperature = 0.5,
                topP = fixedTopP,
color = "🟢"
            ),
            ParameterSet(
                name = "Creative",
                description = "More Adventurous", 
                temperature = 1.0,
                topP = fixedTopP,
color = "🟡"
            ),
            ParameterSet(
                name = "Wild",
                description = "Maximum Creativity",
                temperature = 1.8,
                topP = fixedTopP,
color = "🔴"
            )
        )

        try {
            temperatureSettings.forEachIndexed { index, params ->
                val response = openAI.createChatCompletion(
                    prompt = prompt,
                    systemMessage = Prompts.BRIEF_ASSISTANT,
                    maxTokens = 100,
                    temperature = params.temperature,
                    topP = params.topP
                )
                
                temperatureSettings[index] = params.copy(response = response.text())
            }
            
            model.addAttribute("prompt", prompt)
            model.addAttribute("fixedParam", "top_p = $fixedTopP")
            model.addAttribute("variableParam", "temperature (0.1 → 1.8)")
            model.addAttribute("parameterSets", temperatureSettings)
            model.addAttribute("explanation", "How temperature affects creativity vs consistency")
            model.addAttribute("results", true)
            model.addAttribute("success", true)
            
        } catch (e: Exception) {
            model.addAttribute("prompt", prompt)
            model.addAttribute("fixedParam", "top_p = $fixedTopP")
            model.addAttribute("variableParam", "temperature (0.1 → 1.8)")
            model.addAttribute("parameterSets", temperatureSettings)
            model.addAttribute("explanation", "How temperature affects creativity vs consistency")
            model.addAttribute("error", "Failed to generate responses: ${e.message}")
            
            // Preserve form data on error
            val formData = mutableMapOf<String, String>()
            formData["customPrompt"] = customPrompt
            model.addAttribute("formData", formData)
        }

        return "temperature-demo"
    }
}