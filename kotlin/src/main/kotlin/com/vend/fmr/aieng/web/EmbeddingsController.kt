package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.apis.openai.OpenAI
import com.vend.fmr.aieng.utils.Demo
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/demo/embeddings")
class EmbeddingsController(
    private val openAI: OpenAI
) : BaseController(Demo.EMBEDDINGS) {

    @PostMapping
    suspend fun processEmbeddings(
        @RequestParam(defaultValue = "") inputText: String,
        @RequestParam(defaultValue = "false") showVector: Boolean,
        model: Model
    ): String {

        if (inputText.isNotBlank()) {
            try {
                val embedding = openAI.createEmbedding(inputText)
                
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
        
        return demo.id
    }
}

