package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.OPEN_AI_KEY
import com.vend.fmr.aieng.SUPABASE_KEY
import com.vend.fmr.aieng.SUPABASE_URL
import com.vend.fmr.aieng.impl.openai.OpenAI
import com.vend.fmr.aieng.impl.supabase.DocumentMatch
import com.vend.fmr.aieng.impl.supabase.Supabase
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/demo/supabase")
class SupabaseController {

    @GetMapping
    fun supabaseDemo(model: Model): String {
        model.addAttribute("pageTitle", "Vector Database Search")
        model.addAttribute("activeTab", "supabase")
        return "supabase-demo"
    }

    @PostMapping
    suspend fun processVectorSearch(
        @RequestParam(defaultValue = "") query: String,
        @RequestParam(defaultValue = "5") maxMatches: Int,
        model: Model
    ): String {
        model.addAttribute("pageTitle", "Vector Database Search")
        model.addAttribute("activeTab", "supabase")
        
        if (query.isNotBlank()) {
            try {
                val openAI = OpenAI(OPEN_AI_KEY)
                val supabase = Supabase(SUPABASE_URL, SUPABASE_KEY)
                
                // Step 1: Convert query to embedding vector
                val queryEmbedding = openAI.createEmbedding(query)
                
                // Step 2: Search for similar documents in PostgreSQL with pgvector
                val matches = supabase.matchDocuments(queryEmbedding, maxMatches)
                
                openAI.close()
                supabase.close()
                
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