package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.apis.openai.OpenAI
import com.vend.fmr.aieng.apis.supabase.DocumentMatch
import com.vend.fmr.aieng.apis.supabase.Supabase
import com.vend.fmr.aieng.utils.Demo
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/demo/supabase")
class SupabaseController(
    private val openAI: OpenAI,
    private val supabase: Supabase
) : BaseController(Demo.VECTOR_DATABASE) {

    @GetMapping
    fun supabaseDemo(): String {
        return "supabase-demo"
    }

    @PostMapping
    suspend fun processVectorSearch(
        @RequestParam(defaultValue = "") query: String,
        @RequestParam(defaultValue = "5") maxMatches: Int,
        model: Model
    ): String {
        if (query.isNotBlank()) {
            try {
                val queryEmbedding = openAI.createEmbedding(query)
                val matches = supabase.matchDocuments(queryEmbedding, maxMatches)
                
                model.addAttribute("vectorResult", VectorResult(
                    originalQuery = query,
                    embeddingDimensions = queryEmbedding.size,
                    queryEmbeddingPreview = queryEmbedding.take(10).joinToString(", ") { "%.4f".format(it) },
                    maxMatches = maxMatches,
                    actualMatches = matches.size,
                    matches = matches
                ))
                model.addAttribute("formData", VectorFormData(query, maxMatches))
                
            } catch (e: Exception) {
                model.addAttribute("error", "Error: ${e.message}")
                model.addAttribute("formData", VectorFormData(query, maxMatches))
            }
        }
        
        return "supabase-demo"
    }
}

data class VectorResult(
    val originalQuery: String,
    val embeddingDimensions: Int,
    val queryEmbeddingPreview: String,
    val maxMatches: Int,
    val actualMatches: Int,
    val matches: List<DocumentMatch>
)

data class VectorFormData(
    val query: String,
    val maxMatches: Int
)