package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.apis.mocks.Mocks
import com.vend.fmr.aieng.utils.Demo
import com.vend.fmr.aieng.apis.openai.Message
import com.vend.fmr.aieng.apis.openai.TextContent
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
    val type: String,
    val content: String,
    val functionName: String? = null,
    val functionArgs: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Controller
@RequestMapping("/demo/function-calling")
class FunctionCallingController : BaseController(Demo.FUNCTION_CALLING) {

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

        messages.add(Message(
            role = Prompts.Roles.SYSTEM, 
            content = TextContent(Prompts.FUNCTION_CALLING_SYSTEM)
        ))
        
        messages.add(Message(role = Prompts.Roles.USER, content = TextContent(userQuery)))
        
        steps.add(FunctionCallingStep(
            stepNumber = stepCounter++,
            type = "user_query",
            content = userQuery
        ))

        for (iteration in 0 until maxIterations) {
            val response = openAI.createChatCompletion(
                messages = messages,
                tools = tools,
                temperature = 0.1,
                maxTokens = 400,
                toolChoice = "auto"
            )

            val assistantMessage = response.choices.first().message
            messages.add(assistantMessage)

            if (response.hasToolCalls()) {
                val toolCalls = response.getToolCalls()
                
                val contentText = when (val content = assistantMessage.content) {
                    is TextContent -> content.text
                    else -> ""
                }
                if (contentText.isNotBlank()) {
                    steps.add(FunctionCallingStep(
                        stepNumber = stepCounter++,
                        type = "ai_response",
                        content = contentText
                    ))
                }
                
                for (toolCall in toolCalls) {
                    val formattedArgs = formatFunctionArgs(toolCall.function.arguments)
                    steps.add(FunctionCallingStep(
                        stepNumber = stepCounter++,
                        type = "function_call",
                        content = "Calling ${toolCall.function.name}",
                        functionName = toolCall.function.name,
                        functionArgs = formattedArgs
                    ))
                    
                    val result = Mocks.executeFunctionCall(toolCall.function)
                    
                    steps.add(FunctionCallingStep(
                        stepNumber = stepCounter++,
                        type = "function_result",
                        content = result,
                        functionName = toolCall.function.name
                    ))
                    
                    messages.add(Message(
                        role = Prompts.Roles.TOOL,
                        content = TextContent(result),
                        toolCallId = toolCall.id
                    ))
                }
            } else {
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
                args.replace(Regex("\\s+"), " ").trim()
            }
        }
    }

}