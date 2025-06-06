package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.huggingface
import com.vend.fmr.aieng.utils.Demo
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class HuggingFaceSummarizationController : BaseController(Demo.HUGGINGFACE_SUMMARIZATION) {

    @GetMapping("/demo/huggingface-summarization")
    fun summarizationDemo(model: Model): String {
        model.addAttribute("pageTitle", "HuggingFace Summarization")
        model.addAttribute("activeTab", "huggingface-summarization")
        
        model.addAttribute("defaultText", """
            The emergence of artificial intelligence has transformed numerous industries and continues to shape our daily lives. 
            From healthcare to transportation, AI technologies are being integrated into systems that improve efficiency and decision-making. 
            Machine learning algorithms can analyze vast amounts of data to identify patterns and make predictions. 
            Natural language processing enables computers to understand and generate human language. 
            Computer vision allows machines to interpret and analyze visual information. 
            These advancements are creating new opportunities while also raising important questions about ethics, privacy, and the future of work.
        """.trimIndent())
        model.addAttribute("defaultMaxLength", 100)
        model.addAttribute("defaultMinLength", 30)
        model.addAttribute("explanation", "Summarize text using open-source BART model")
        
        return "huggingface-summarization-demo"
    }

    @PostMapping("/demo/huggingface-summarization")
    suspend fun processSummarization(
        @RequestParam("text", defaultValue = "") text: String,
        @RequestParam("maxLength", defaultValue = "100") maxLength: String,
        @RequestParam("minLength", defaultValue = "30") minLength: String,
        model: Model
    ): String {
        model.addAttribute("pageTitle", "HuggingFace Summarization")
        model.addAttribute("activeTab", "huggingface-summarization")
        
        val defaultText = """
            The emergence of artificial intelligence has transformed numerous industries and continues to shape our daily lives. 
            From healthcare to transportation, AI technologies are being integrated into systems that improve efficiency and decision-making. 
            Machine learning algorithms can analyze vast amounts of data to identify patterns and make predictions. 
            Natural language processing enables computers to understand and generate human language. 
            Computer vision allows machines to interpret and analyze visual information. 
            These advancements are creating new opportunities while also raising important questions about ethics, privacy, and the future of work.
        """.trimIndent()
        
        val inputText = if (text.isBlank()) defaultText else text.trim()
        val maxLen = maxLength.toIntOrNull() ?: 100
        val minLen = minLength.toIntOrNull() ?: 30
        
        val formData = mutableMapOf<String, String>()
        formData["text"] = inputText
        formData["maxLength"] = maxLen.toString()
        formData["minLength"] = minLen.toString()
        model.addAttribute("formData", formData)
        
        try {
            val result = huggingface.summarize(
                text = inputText,
                maxLength = maxLen,
                minLength = minLen,
                debug = false
            )
            
            val originalLength = inputText.length
            val summaryLength = result.summaryText.length
            val compressionRatio = ((originalLength - summaryLength).toDouble() / originalLength * 100).toInt()
            
            model.addAttribute("inputText", inputText)
            model.addAttribute("originalLength", originalLength)
            model.addAttribute("summary", result.summaryText)
            model.addAttribute("summaryLength", summaryLength)
            model.addAttribute("compressionRatio", compressionRatio)
            model.addAttribute("maxLength", maxLen)
            model.addAttribute("minLength", minLen)
            model.addAttribute("modelUsed", "facebook/bart-large-cnn")
            model.addAttribute("results", true)
            model.addAttribute("success", true)
            
        } catch (e: Exception) {
            model.addAttribute("error", "Summarization failed: ${e.message}")
            model.addAttribute("inputText", inputText)
            model.addAttribute("maxLength", maxLen)
            model.addAttribute("minLength", minLen)
        }
        
        model.addAttribute("defaultText", defaultText)
        model.addAttribute("defaultMaxLength", 100)
        model.addAttribute("defaultMinLength", 30)
        model.addAttribute("explanation", "Summarize text using open-source BART model")
        
        return "huggingface-summarization-demo"
    }
}