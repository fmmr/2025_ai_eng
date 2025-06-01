package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.impl.mocks.Mocks
import com.vend.fmr.aieng.impl.openai.Message
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

data class FunctionCallingStep(
    val stepNumber: Int,
    val type: String, // "user_query", "ai_response", "function_call", "function_result", "final_answer"
    val content: String,
    val functionName: String? = null,
    val functionArgs: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Controller
@RequestMapping("/demo/function-calling")
class FunctionCallingController {

    @GetMapping
    fun functionCallingDemo(model: Model): String {
        model.addAttribute("pageTitle", "Function Calling Agent")
        model.addAttribute("activeTab", "function-calling")
        model.addAttribute("defaultQuery", "Do you have any ideas for activities I can do at my location?")
        model.addAttribute("systemPrompt", Prompts.FUNCTION_CALLING_SYSTEM)
        model.addAttribute("systemPromptTruncated", Prompts.FUNCTION_CALLING_SYSTEM.truncate())
        return "function-calling-demo"
    }

    @PostMapping
    fun processFunctionCallingQuery(
        @RequestParam userQuery: String,
        model: Model
    ): String = runBlocking {
        model.addAttribute("pageTitle", "Function Calling Agent")
        model.addAttribute("activeTab", "function-calling")
        model.addAttribute("defaultQuery", userQuery)
        model.addAttribute("systemPrompt", Prompts.FUNCTION_CALLING_SYSTEM)
        model.addAttribute("systemPromptTruncated", Prompts.FUNCTION_CALLING_SYSTEM.truncate())

        try {
            val steps = runFunctionCallingAgent(userQuery)
            model.addAttribute("steps", steps)
            model.addAttribute("userQuery", userQuery)
            model.addAttribute("success", true)
        } catch (e: Exception) {
            model.addAttribute("error", "Error processing query: ${e.message}")
            model.addAttribute("success", false)
        }

        return@runBlocking "function-calling-demo"
    }

    private suspend fun runFunctionCallingAgent(userQuery: String, maxIterations: Int = 5): List<FunctionCallingStep> {
        val steps = mutableListOf<FunctionCallingStep>()
        val messages = mutableListOf<Message>()
        val tools = Mocks.getAvailableTools()
        var stepCounter = 1

        // Add system message
        messages.add(Message(
            role = "system", 
            content = Prompts.FUNCTION_CALLING_SYSTEM
        ))
        
        // Add initial user query
        messages.add(Message(role = "user", content = userQuery))
        
        // Add user query step
        steps.add(FunctionCallingStep(
            stepNumber = stepCounter++,
            type = "user_query",
            content = userQuery
        ))

        for (iteration in 0 until maxIterations) {
            // Call OpenAI with function definitions
            val response = openAI.createChatCompletionWithTools(
                messages = messages,
                tools = tools,
                temperature = 0.1,
                maxTokens = 400,
                toolChoice = "auto"
            )

            // Add assistant response to conversation
            val assistantMessage = response.choices.first().message
            messages.add(assistantMessage)

            // Check if OpenAI wants to call functions
            if (response.hasToolCalls()) {
                val toolCalls = response.getToolCalls()
                
                // Add AI response step (if it has content)
                if (!assistantMessage.content.isNullOrBlank()) {
                    steps.add(FunctionCallingStep(
                        stepNumber = stepCounter++,
                        type = "ai_response",
                        content = assistantMessage.content
                    ))
                }
                
                // Execute each function call
                for (toolCall in toolCalls) {
                    // Add function call step with formatted args
                    val formattedArgs = formatFunctionArgs(toolCall.function.arguments)
                    steps.add(FunctionCallingStep(
                        stepNumber = stepCounter++,
                        type = "function_call",
                        content = "Calling ${toolCall.function.name}",
                        functionName = toolCall.function.name,
                        functionArgs = formattedArgs
                    ))
                    
                    val result = Mocks.executeFunctionCall(toolCall.function)
                    
                    // Add function result step
                    steps.add(FunctionCallingStep(
                        stepNumber = stepCounter++,
                        type = "function_result",
                        content = result,
                        functionName = toolCall.function.name
                    ))
                    
                    // Add function result as a tool message
                    messages.add(Message(
                        role = "tool",
                        content = result,
                        toolCallId = toolCall.id
                    ))
                }
            } else {
                // No more function calls needed, AI has final answer
                val finalAnswer = response.text()
                steps.add(FunctionCallingStep(
                    stepNumber = stepCounter++,
                    type = "final_answer",
                    content = finalAnswer
                ))
                break
            }
        }

        return steps
    }

    private fun formatFunctionArgs(args: String): String {
        return when {
            args.isBlank() || args == "{}" -> ""
            else -> {
                // Remove extra whitespace and newlines, make it compact
                args.replace(Regex("\\s+"), " ").trim()
            }
        }
    }

}