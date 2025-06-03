package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.OPEN_AI_KEY
import com.vend.fmr.aieng.apis.openai.OpenAI
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/demo/embeddings")
class EmbeddingsController {

    @GetMapping
    fun embeddingsDemo(model: Model): String {
        model.addAttribute("pageTitle", "OpenAI Embeddings")
        model.addAttribute("activeTab", "embeddings")
        return "embeddings-demo"
    }

    @PostMapping
    suspend fun processEmbeddings(
        @RequestParam(defaultValue = "") inputText: String,
        @RequestParam(defaultValue = "false") showVector: Boolean,
        model: Model
    ): String {
        model.addAttribute("pageTitle", "OpenAI Embeddings")
        model.addAttribute("activeTab", "embeddings")
        
        if (inputText.isNotBlank()) {
            try {
                val apiKey = OPEN_AI_KEY

                val openAI = OpenAI(apiKey)
                val embedding = openAI.createEmbedding(inputText)
                openAI.close()
                
                model.addAttribute("embeddingResult", EmbeddingResult(
                    text = inputText,
                    embedding = embedding,
                    dimensions = embedding.size,
                    showVector = showVector,
                    first10Values = embedding.take(10),
                    minValue = embedding.minOrNull() ?: 0.0,
                    maxValue = embedding.maxOrNull() ?: 0.0,
                    avgValue = embedding.average()
                ))
                model.addAttribute("formData", EmbeddingFormData(inputText, showVector))
                
            } catch (e: Exception) {
                model.addAttribute("error", "Error: ${e.message}")
                model.addAttribute("formData", EmbeddingFormData(inputText, showVector))
            }
        }
        
        return "embeddings-demo"
    }
}

