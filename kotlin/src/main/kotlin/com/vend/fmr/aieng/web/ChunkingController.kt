package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.apis.chunker.Chunker
import com.vend.fmr.aieng.utils.Demo
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/demo/chunking")
class ChunkingController(
    private val chunker: Chunker
) : BaseController(Demo.CHUNKING) {

    @PostMapping
    fun processChunking(
        @RequestParam(defaultValue = "250") chunkSize: Int,
        @RequestParam(defaultValue = "35") chunkOverlap: Int,
        @RequestParam(defaultValue = "") textInput: String,
        model: Model
    ): String {
        if (textInput.isNotBlank()) {
            val chunks = chunker.split(textInput, chunkSize, chunkOverlap)
            
            val chunksWithOverlaps = chunks.mapIndexed { index, chunk ->
                val prevOverlap = if (index > 0) findOverlap(chunks[index - 1], chunk).trim() else ""
                val nextOverlap = if (index < chunks.size - 1) findOverlap(chunk, chunks[index + 1]).trim() else ""
                
                ChunkWithOverlap(
                    index = index + 1,
                    text = chunk,
                    length = chunk.length,
                    prevOverlap = prevOverlap,
                    nextOverlap = nextOverlap
                )
            }
            
            model.addAttribute("chunkingResult", ChunkingResult(
                totalChunks = chunks.size,
                totalCharacters = chunks.sumOf { it.length },
                chunks = chunksWithOverlaps
            ))
            model.addAttribute("formData", ChunkingFormData(chunkSize, chunkOverlap, textInput))
        }
        
        return demo.id
    }
    
    private fun findOverlap(text1: String, text2: String): String {
        var maxOverlap = ""
        val minLength = minOf(text1.length, text2.length)
        
        for (i in 1..minLength) {
            val suffix = text1.substring(text1.length - i)
            val prefix = text2.substring(0, i)
            if (suffix == prefix && suffix.length > maxOverlap.length) {
                if (suffix.trim().isNotEmpty()) {
                    maxOverlap = suffix
                }
            }
        }
        
        return maxOverlap
    }
}

