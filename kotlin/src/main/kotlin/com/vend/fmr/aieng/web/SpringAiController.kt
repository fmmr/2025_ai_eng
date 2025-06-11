package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.utils.Demo
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class SpringAiController : BaseController(Demo.SPRING_AI) {

    @Autowired(required = false)
    private val chatModel: ChatModel? = null

    @GetMapping("/demo/spring-ai")
    fun springAiDemo(model: Model): String {
        if (chatModel == null) {
            model.addAttribute("error", "Spring AI is not available - OPENAI_API_KEY environment variable is required")
            return "spring-ai-demo"
        }

        model.addAttribute(
            "defaultText",
            "Spring AI provides a comprehensive framework for building AI applications. It offers abstractions for various AI providers, prompt templates, and seamless integration with the Spring ecosystem."
        )
        model.addAttribute("defaultMaxWords", 50)
        model.addAttribute("explanation", "Compare Spring AI's native framework approach vs. custom HTTP implementations")

        val formData = mutableMapOf<String, String>()
        formData["text"] = ""
        formData["operation"] = "analyze"
        formData["maxWords"] = "50"
        model.addAttribute("formData", formData)

        return "spring-ai-demo"
    }

    @PostMapping("/demo/spring-ai")
    fun processSpringAi(
        @RequestParam("text", defaultValue = "") text: String,
        @RequestParam("operation", defaultValue = "analyze") operation: String,
        @RequestParam("maxWords", defaultValue = "50") maxWords: String,
        model: Model
    ): String {
        val defaultText =
            "Spring AI provides a comprehensive framework for building AI applications. It offers abstractions for various AI providers, prompt templates, and seamless integration with the Spring ecosystem."
        val inputText = if (text.isBlank()) defaultText else text.trim()
        val maxWordsInt = maxWords.toIntOrNull() ?: 50

        val formData = mutableMapOf<String, String>()
        formData["text"] = inputText
        formData["operation"] = operation
        formData["maxWords"] = maxWordsInt.toString()
        model.addAttribute("formData", formData)

        if (chatModel == null) {
            model.addAttribute("error", "Spring AI is not available - OPENAI_API_KEY environment variable is required")
        } else {
            try {
                val promptText = when (operation) {
                    "summarize" -> "Summarize this text in $maxWordsInt words or less: $inputText"
                    "sentiment" -> "Classify the sentiment of this text as positive, negative, or neutral. Respond with just the classification and a confidence score: $inputText"
                    "analyze" -> "Analyze this text and provide insights: $inputText"
                    else -> "Analyze this text and provide insights: $inputText"
                }

                val prompt = Prompt(UserMessage(promptText))
                val response = chatModel.call(prompt)
                val result = response.result.output.text

                model.addAttribute("inputText", inputText)
                model.addAttribute("operation", operation)
                model.addAttribute("maxWords", maxWordsInt)
                model.addAttribute("result", result)
                model.addAttribute("modelUsed", chatModel.javaClass.simpleName)
                model.addAttribute("results", true)
                model.addAttribute("success", true)

            } catch (e: Exception) {
                model.addAttribute("error", "Spring AI operation failed: ${e.message}")
                model.addAttribute("inputText", inputText)
                model.addAttribute("operation", operation)
                model.addAttribute("maxWords", maxWordsInt)
            }

            model.addAttribute("defaultText", defaultText)
            model.addAttribute("defaultMaxWords", 50)
            model.addAttribute("explanation", "Compare Spring AI's native framework approach vs. custom HTTP implementations")

        }
        return "spring-ai-demo"

    }
}