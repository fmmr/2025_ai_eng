package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.OPEN_AI_KEY
import com.vend.fmr.aieng.SUPABASE_KEY
import com.vend.fmr.aieng.SUPABASE_URL
import com.vend.fmr.aieng.apis.openai.OpenAI
import com.vend.fmr.aieng.apis.supabase.DocumentMatch
import com.vend.fmr.aieng.apis.supabase.Supabase
import com.vend.fmr.aieng.utils.Demo
import com.vend.fmr.aieng.utils.Prompts
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/demo/rag")
class RagController : BaseController(Demo.RAG) {

    @GetMapping
    fun ragDemo(model: Model): String {
        model.addAttribute("pageTitle", "RAG Implementation")
        model.addAttribute("activeTab", "rag")
        return "rag-demo"
    }

    @PostMapping
    suspend fun processRagQuery(
        @RequestParam(defaultValue = "") query: String,
        @RequestParam(defaultValue = "5") maxMatches: Int,
        model: Model
    ): String {
        model.addAttribute("pageTitle", "RAG Implementation")
        model.addAttribute("activeTab", "rag")
        
        if (query.isNotBlank()) {
            try {
                val openAI = OpenAI(OPEN_AI_KEY)
                val supabase = Supabase(SUPABASE_URL, SUPABASE_KEY)
                
                val queryEmbedding = openAI.createEmbedding(query)
                
                val matches = supabase.matchDocuments(queryEmbedding, maxMatches)
                
                val context = matches.joinToString("\n") { it.content }
                
                val response = openAI.createChatCompletion(
                    prompt = Prompts.formatRagQuery(context, query),
                    systemMessage = Prompts.MOVIE_EXPERT_RAG
                )
                
                openAI.close()
                supabase.close()
                
                model.addAttribute("ragResult", RagResult(
                    originalQuery = query,
                    embeddingDimensions = queryEmbedding.size,
                    queryEmbeddingPreview = queryEmbedding.take(10).joinToString(", ") { "%.4f".format(it) },
                    matchCount = matches.size,
                    matches = matches,
                    context = context,
                    systemMessage = Prompts.MOVIE_EXPERT_RAG,
                    finalAnswer = response.text(),
                    usage = response.usage()
                ))
                model.addAttribute("formData", RagFormData(query, maxMatches))
                
            } catch (e: Exception) {
                model.addAttribute("error", "Error: ${e.message}")
                model.addAttribute("formData", RagFormData(query, maxMatches))
            }
        }
        
        return "rag-demo"
    }
}

data class RagResult(
    val originalQuery: String,
    val embeddingDimensions: Int,
    val queryEmbeddingPreview: String,
    val matchCount: Int,
    val matches: List<DocumentMatch>,
    val context: String,
    val systemMessage: String,
    val finalAnswer: String,
    val usage: String
)

data class RagFormData(
    val query: String,
    val maxMatches: Int
)