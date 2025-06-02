package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.impl.mocks.Mocks
import com.vend.fmr.aieng.impl.openai.Message
import com.vend.fmr.aieng.impl.openai.TextContent
import com.vend.fmr.aieng.openAI
import com.vend.fmr.aieng.utils.Prompts
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
    val type: String, // "thought", "action", "observation", "final_answer"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Controller
@RequestMapping("/demo/react")
class ReActController {

    @GetMapping
    fun reactDemo(model: Model): String {
        model.addAttribute("pageTitle", "ReAct Agent")
        model.addAttribute("activeTab", "react")
        model.addAttribute("defaultQuery", Prompts.Defaults.REACT_AGENT_QUERY)
        model.addAttribute("systemPrompt", Prompts.REACT_AGENT_SYSTEM)
        model.addAttribute("systemPromptTruncated", Prompts.REACT_AGENT_SYSTEM.truncate())
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
        model.addAttribute("systemPrompt", Prompts.REACT_AGENT_SYSTEM)
        model.addAttribute("systemPromptTruncated", Prompts.REACT_AGENT_SYSTEM.truncate())

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

        // Add system message
        messages.add(Message(Prompts.Roles.SYSTEM, TextContent(Prompts.REACT_AGENT_SYSTEM)))
        
        // Add initial user query
        messages.add(Message(Prompts.Roles.USER, TextContent(userQuery)))

        for (iteration in 0 until maxIterations) {
            // Get AI response using message history
            val aiResponse = openAI.createChatCompletion(
                messages = messages,
                temperature = 0.1,
                maxTokens = 400
            )

            val currentResponse = aiResponse.text()

            // Add AI response to messages
            messages.add(Message(Prompts.Roles.ASSISTANT, TextContent(currentResponse)))

            // Check if AI provided final answer
            if (currentResponse.contains("Final Answer:", ignoreCase = true)) {
                val finalAnswerRegex = Regex("Final Answer:\\s*(.*)", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
                val match = finalAnswerRegex.find(currentResponse)
                if (match != null) {
                    val finalAnswer = match.groupValues[1].trim()
                    steps.add(ReActStep(stepCounter++, "final_answer", finalAnswer))
                    break
                }
            }

            // Parse thoughts and actions from response
            parseThoughtAndAction(currentResponse, stepCounter, steps)
            
            // Parse and execute action
            val action = Mocks.parseAction(currentResponse)
            if (action != null) {
                val (functionName, params) = action
                stepCounter = steps.size + 1

                val result = Mocks.executeFunction(functionName, params)
                val observation = "Observation: $result"

                steps.add(ReActStep(stepCounter++, "observation", result))

                // Add observation as a user message
                messages.add(Message(Prompts.Roles.USER, TextContent(observation)))
            } else {
                // No action found, AI might be done or confused
                break
            }
        }

        return steps
    }

    private fun parseThoughtAndAction(response: String, startingStepNumber: Int, steps: MutableList<ReActStep>) {
        var stepNumber = startingStepNumber

        // Extract thought
        val thoughtRegex = Regex("Thought:\\s*(.*?)(?=Action:|$)", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val thoughtMatch = thoughtRegex.find(response)
        if (thoughtMatch != null) {
            val thought = thoughtMatch.groupValues[1].trim()
            steps.add(ReActStep(stepNumber++, "thought", thought))
        }

        // Extract action
        val actionRegex = Regex("Action:\\s*(.*?)(?=Observation:|$)", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val actionMatch = actionRegex.find(response)
        if (actionMatch != null) {
            val action = actionMatch.groupValues[1].trim()
            steps.add(ReActStep(stepNumber, "action", action))
        }
    }
}