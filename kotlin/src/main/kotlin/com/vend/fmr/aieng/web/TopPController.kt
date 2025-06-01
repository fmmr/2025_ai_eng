package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.openAI
import com.vend.fmr.aieng.utils.ParameterSet
import com.vend.fmr.aieng.utils.Prompts
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

@Controller
class TopPController {

    @GetMapping("/demo/top-p-effects")
    fun topPDemo(model: Model): String {
        model.addAttribute("pageTitle", "Top-P Effects")
        model.addAttribute("activeTab", "top-p-effects")
        
        val fixedTemperature = 0.7
        
        val topPSettings = listOf(
            ParameterSet(
                name = "Very Restricted",
                description = "Only Most Likely Words",
                temperature = fixedTemperature,
                topP = 0.1,
                color = "🔵"
            ),
            ParameterSet(
                name = "Somewhat Restricted", 
                description = "Limited Vocabulary",
                temperature = fixedTemperature,
                topP = 0.5,
                color = "🟢"
            ),
            ParameterSet(
                name = "Balanced",
                description = "Good Word Variety", 
                temperature = fixedTemperature,
                topP = 0.9,
                color = "🟡"
            ),
            ParameterSet(
                name = "Unrestricted",
                description = "Full Vocabulary Access",
                temperature = fixedTemperature,
                topP = 1.0,
                color = "🔴"
            )
        )
        
        model.addAttribute("prompt", Prompts.Defaults.CHAT_PARAMETERS_PROMPT)
        model.addAttribute("fixedParam", "temperature = $fixedTemperature")
        model.addAttribute("variableParam", "top_p (0.1 → 1.0)")
        model.addAttribute("parameterSets", topPSettings)
        model.addAttribute("explanation", "How top-p affects vocabulary restriction vs exploration")
        
        return "top-p-demo"
    }

    @PostMapping("/demo/top-p-effects")
    suspend fun runTopPComparison(model: Model): String {
        model.addAttribute("pageTitle", "Top-P Effects")
        model.addAttribute("activeTab", "top-p-effects")

        val fixedTemperature = 0.7
        val prompt = Prompts.Defaults.CHAT_PARAMETERS_PROMPT
        
        val topPSettings = mutableListOf(
            ParameterSet(
                name = "Very Restricted",
                description = "Only Most Likely Words",
                temperature = fixedTemperature,
                topP = 0.1,
                color = "🔵"
            ),
            ParameterSet(
                name = "Somewhat Restricted", 
                description = "Limited Vocabulary",
                temperature = fixedTemperature,
                topP = 0.5,
                color = "🟢"
            ),
            ParameterSet(
                name = "Balanced",
                description = "Good Word Variety", 
                temperature = fixedTemperature,
                topP = 0.9,
                color = "🟡"
            ),
            ParameterSet(
                name = "Unrestricted",
                description = "Full Vocabulary Access",
                temperature = fixedTemperature,
                topP = 1.0,
                color = "🔴"
            )
        )

        try {
            topPSettings.forEachIndexed { index, params ->
                val response = openAI.createChatCompletion(
                    prompt = prompt,
                    systemMessage = Prompts.BRIEF_ASSISTANT,
                    maxTokens = 100,
                    temperature = params.temperature,
                    topP = params.topP
                )
                
                topPSettings[index] = params.copy(response = response.text())
            }
            
            model.addAttribute("prompt", prompt)
            model.addAttribute("fixedParam", "temperature = $fixedTemperature")
            model.addAttribute("variableParam", "top_p (0.1 → 1.0)")
            model.addAttribute("parameterSets", topPSettings)
            model.addAttribute("explanation", "How top-p affects vocabulary restriction vs exploration")
            model.addAttribute("results", true)
            model.addAttribute("success", true)
            
        } catch (e: Exception) {
            model.addAttribute("prompt", prompt)
            model.addAttribute("fixedParam", "temperature = $fixedTemperature")
            model.addAttribute("variableParam", "top_p (0.1 → 1.0)")
            model.addAttribute("parameterSets", topPSettings)
            model.addAttribute("explanation", "How top-p affects vocabulary restriction vs exploration")
            model.addAttribute("error", "Failed to generate responses: ${e.message}")
        }

        return "top-p-demo"
    }
}