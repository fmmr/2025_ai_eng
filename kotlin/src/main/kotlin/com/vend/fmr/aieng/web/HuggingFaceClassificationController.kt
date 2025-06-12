package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.apis.huggingface.HuggingFace
import com.vend.fmr.aieng.utils.Demo
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/demo/huggingface-classification")
class HuggingFaceClassificationController(
    private val huggingface: HuggingFace
) : BaseController(Demo.HUGGINGFACE_CLASSIFICATION) {

    @GetMapping
    fun classificationDemo(model: Model): String {
        model.addAttribute("defaultText", "I love this new product, it works amazing!")
        model.addAttribute("defaultLabels", "positive, negative, neutral")
        model.addAttribute("explanation", "Classify text using open-source BART model")
        
        return "huggingface-classification-demo"
    }

    @PostMapping
    suspend fun processClassification(
        @RequestParam("text", defaultValue = "") text: String,
        @RequestParam("labels", defaultValue = "") labels: String,
        model: Model
    ): String {
        val inputText = if (text.isBlank()) "I love this new product, it works amazing!" else text.trim()
        val inputLabels = if (labels.isBlank()) "positive, negative, neutral" else labels.trim()
        val labelList = inputLabels.split(",").map { it.trim() }
        
        val formData = mutableMapOf<String, String>()
        formData["text"] = inputText
        formData["labels"] = inputLabels
        model.addAttribute("formData", formData)
        
        try {
            val result = huggingface.classify(
                text = inputText,
                labels = labelList,
                debug = false
            )
            
            val classifications = result.labels.zip(result.scores).map { (label, score) ->
                mapOf(
                    "label" to label,
                    "score" to score,
                    "percentage" to (score * 100).toInt()
                )
            }.sortedByDescending { it["score"] as Double }
            
            model.addAttribute("inputText", inputText)
            model.addAttribute("inputLabels", labelList)
            model.addAttribute("classifications", classifications)
            model.addAttribute("modelUsed", "facebook/bart-large-mnli")
            model.addAttribute("results", true)
            model.addAttribute("success", true)
            
        } catch (e: Exception) {
            model.addAttribute("error", "Classification failed: ${e.message}")
            model.addAttribute("inputText", inputText)
            model.addAttribute("inputLabels", labelList)
        }
        
        model.addAttribute("defaultText", "I love this new product, it works amazing!")
        model.addAttribute("defaultLabels", "positive, negative, neutral")
        model.addAttribute("explanation", "Classify text using open-source BART model")
        
        return "huggingface-classification-demo"
    }
}