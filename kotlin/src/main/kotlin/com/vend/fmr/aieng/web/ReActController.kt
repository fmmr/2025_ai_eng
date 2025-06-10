package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.utils.Demo
import com.vend.fmr.aieng.apis.openai.*
import com.vend.fmr.aieng.utils.Prompts
import com.vend.fmr.aieng.utils.AgentTool
import com.vend.fmr.aieng.utils.truncate
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

data class ReActStep(
    val stepNumber: Int,
    val type: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Controller
@RequestMapping("/demo/react")
class ReActController(
    private val openAI: OpenAI
) : BaseController(Demo.REACT_AGENT) {

    @GetMapping
    fun reactDemo(model: Model): String {
        model.addAttribute("pageTitle", "ReAct Agent")
        model.addAttribute("activeTab", "react")
        model.addAttribute("defaultQuery", Prompts.Defaults.REACT_AGENT_QUERY)
        model.addAttribute("systemPrompt", Prompts.getReActSystemPrompt())
        model.addAttribute("systemPromptTruncated", Prompts.getReActSystemPrompt().truncate())
        model.addAttribute("availableTools", AgentTool.entries)
        return "react-demo"
    }

    @PostMapping
    fun processReActQuery(
        @RequestParam userQuery: String,
        model: Model
    ): String = runBlocking {
        model.addAttribute("pageTitle", "ReAct Agent")
        model.addAttribute("activeTab", "react")
        model.addAttribute("defaultQuery", userQuery)
        model.addAttribute("systemPrompt", Prompts.getReActSystemPrompt())
        model.addAttribute("systemPromptTruncated", Prompts.getReActSystemPrompt().truncate())
        model.addAttribute("availableTools", AgentTool.entries)

        try {
            val steps = runReActAgent(userQuery)
            model.addAttribute("steps", steps)
            model.addAttribute("userQuery", userQuery)
            model.addAttribute("success", true)
        } catch (e: Exception) {
            model.addAttribute("error", "Error processing query: ${e.message}")
            model.addAttribute("success", false)
        }

        return@runBlocking "react-demo"
    }

    private suspend fun runReActAgent(userQuery: String, maxIterations: Int = 10): List<ReActStep> {
        val steps = mutableListOf<ReActStep>()
        val messages = mutableListOf<Message>()
        var stepCounter = 1

        messages.add(Message(Prompts.Roles.SYSTEM, TextContent(Prompts.getReActSystemPrompt())))
        messages.add(Message(Prompts.Roles.USER, TextContent(userQuery)))

        for (iteration in 0 until maxIterations) {
            val aiResponse = openAI.createChatCompletion(
                messages = messages,
                temperature = 0.1,
                maxTokens = 400
            )

            val currentResponse = aiResponse.text()

            messages.add(Message(Prompts.Roles.ASSISTANT, TextContent(currentResponse)))

            if (currentResponse.contains("Final Answer:", ignoreCase = true)) {
                val finalAnswerRegex = Regex("Final Answer:\\s*(.*)", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
                val match = finalAnswerRegex.find(currentResponse)
                if (match != null) {
                    val finalAnswer = match.groupValues[1].trim()
                    steps.add(ReActStep(stepCounter, "final_answer", finalAnswer))
                    break
                }
            }

            parseThoughtAndAction(currentResponse, stepCounter, steps)
            val action = AgentTool.parseAction(currentResponse)
            if (action != null) {
                val (functionName, argumentsJson) = action
                stepCounter = steps.size + 1

                val result = AgentTool.execute(functionName, argumentsJson)
                val observation = "Observation: $result"

                steps.add(ReActStep(stepCounter++, "observation", result))

                messages.add(Message(Prompts.Roles.USER, TextContent(observation)))
            } else {
                break
            }
        }

        return steps
    }

    private fun parseThoughtAndAction(response: String, startingStepNumber: Int, steps: MutableList<ReActStep>) {
        var stepNumber = startingStepNumber

        val thoughtRegex = Regex("Thought:\\s*(.*?)(?=Action:|$)", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val thoughtMatch = thoughtRegex.find(response)
        if (thoughtMatch != null) {
            val thought = thoughtMatch.groupValues[1].trim()
            steps.add(ReActStep(stepNumber++, "thought", thought))
        }

        val actionRegex = Regex("Action:\\s*(.*?)(?=Observation:|$)", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val actionMatch = actionRegex.find(response)
        if (actionMatch != null) {
            val action = actionMatch.groupValues[1].trim()
            steps.add(ReActStep(stepNumber, "action", action))
        }
    }
}