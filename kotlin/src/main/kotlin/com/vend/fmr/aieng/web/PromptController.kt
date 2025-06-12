package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.apis.openai.OpenAI
import com.vend.fmr.aieng.dtos.PromptComparison
import com.vend.fmr.aieng.utils.Demo
import com.vend.fmr.aieng.utils.Prompts
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/demo/prompt-engineering")
class PromptController(
    private val openAI: OpenAI
) : BaseController(Demo.PROMPT_ENGINEERING) {

    @GetMapping
    fun promptDemo(model: Model): String {
        val scenarios = listOf(
            PromptComparison(
                scenario = "Recipe Generation",
                vague = "Make pasta",
                better = "Give me an Italian pasta recipe",
                excellent = "Create an authentic Italian carbonara recipe for 4 people, including: exact ingredient quantities, prep time, cooking steps, and tips for perfect texture"
            ),
            PromptComparison(
                scenario = "Travel Planning", 
                vague = "Plan a trip",
                better = "Plan a 3-day trip to Paris",
                excellent = "Create a detailed 3-day budget-friendly Paris itinerary for a couple in spring, focusing on art museums and local cuisine, with daily schedules, transportation, and cost estimates"
            )
        )
        
        model.addAttribute("scenarios", scenarios)
        return "prompt-demo"
    }

    @PostMapping
    suspend fun runComparison(
        @RequestParam scenarioIndex: Int,
        model: Model
    ): String {
        val scenarios = mutableListOf(
            PromptComparison(
                scenario = "Recipe Generation",
                vague = "Make pasta",
                better = "Give me an Italian pasta recipe",
                excellent = "Create an authentic Italian carbonara recipe for 4 people, including: exact ingredient quantities, prep time, cooking steps, and tips for perfect texture"
            ),
            PromptComparison(
                scenario = "Travel Planning", 
                vague = "Plan a trip",
                better = "Plan a 3-day trip to Paris",
                excellent = "Create a detailed 3-day budget-friendly Paris itinerary for a couple in spring, focusing on art museums and local cuisine, with daily schedules, transportation, and cost estimates"
            )
        )

        if (scenarioIndex in scenarios.indices) {
            val scenario = scenarios[scenarioIndex]
            
            try {
                val vagueResponse = openAI.createChatCompletion(scenario.vague, Prompts.CONCISE_ASSISTANT)
                val betterResponse = openAI.createChatCompletion(scenario.better, Prompts.CONCISE_ASSISTANT)
                val excellentResponse = openAI.createChatCompletion(scenario.excellent, Prompts.CONCISE_ASSISTANT)
                
                val updatedScenario = scenario.copy(
                    vagueResponse = vagueResponse.text(),
                    betterResponse = betterResponse.text(),
                    excellentResponse = excellentResponse.text()
                )
                
                scenarios[scenarioIndex] = updatedScenario
                
                model.addAttribute("scenarios", scenarios)
                model.addAttribute("selectedScenario", scenarioIndex)
                model.addAttribute("success", true)
                
            } catch (e: Exception) {
                model.addAttribute("scenarios", scenarios)
                model.addAttribute("error", "Failed to generate responses: ${e.message}")
            }
        } else {
            model.addAttribute("scenarios", scenarios)
            model.addAttribute("error", "Invalid scenario selected")
        }

        return "prompt-demo"
    }
}