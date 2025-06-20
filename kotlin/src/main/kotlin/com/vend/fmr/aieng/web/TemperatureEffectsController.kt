package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.apis.openai.OpenAI
import com.vend.fmr.aieng.dtos.ParameterSet
import com.vend.fmr.aieng.utils.Demo
import com.vend.fmr.aieng.utils.Prompts
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/demo/temperature-effects")
class TemperatureEffectsController(
    private val openAI: OpenAI
) : BaseController(Demo.TEMPERATURE_EFFECTS) {
    companion object {
        const val WILD = 1.9
        const val FIXED_TOP_P = 0.9
    }

    override fun addDefaultModel(model: Model, session: HttpSession) {
        model.addAttribute("prompt", Prompts.Defaults.CHAT_PARAMETERS_PROMPT)
        model.addAttribute("fixedParam", "top_p = $FIXED_TOP_P")
        model.addAttribute("variableParam", "temperature (0.1 → $WILD)")
        model.addAttribute("explanation", "How temperature affects creativity vs consistency")
    }

    @PostMapping
    suspend fun runTemperatureComparison(
        @RequestParam("customPrompt", defaultValue = "") customPrompt: String,
        model: Model
    ): String {
        val fixedTopP = 0.9
        val prompt = if (customPrompt.isBlank()) {
            Prompts.Defaults.CHAT_PARAMETERS_PROMPT
        } else {
            customPrompt.trim()
        }

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
                temperature = WILD,
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

            commonModelAttributes(model, prompt, temperatureSettings)
            model.addAttribute("results", true)
            model.addAttribute("success", true)

        } catch (e: Exception) {
            commonModelAttributes(model, prompt, temperatureSettings)
            model.addAttribute("error", "Failed to generate responses: ${e.message}")

            val formData = mutableMapOf<String, String>()
            formData["customPrompt"] = customPrompt
            model.addAttribute("formData", formData)
        }

        return demo.id
    }

    private fun commonModelAttributes(
        model: Model,
        prompt: String,
        temperatureSettings: MutableList<ParameterSet>
    ) {
        model.addAttribute("prompt", prompt)
        model.addAttribute("fixedParam", "top_p = $FIXED_TOP_P")
        model.addAttribute("variableParam", "temperature (0.1 → $WILD)")
        model.addAttribute("parameterSets", temperatureSettings)
        model.addAttribute("explanation", "How temperature affects creativity vs consistency")
    }
}