@file:Suppress("unused")

package com.vend.fmr.aieng.utils

import com.vend.fmr.aieng.impl.chunker.Chunker
import com.vend.fmr.aieng.impl.supabase.Document
import com.vend.fmr.aieng.impl.supabase.DocumentMatch
import com.vend.fmr.aieng.openAI
import com.vend.fmr.aieng.polygon
import com.vend.fmr.aieng.supabase


suspend fun tickers(vararg tickers: String, debug: Boolean = false): String {
    val aggregates = polygon.getAggregates(*tickers)
    if (debug) {
        println(aggregates)
    }

    val json = polygon.aggregatesToJson(aggregates)
    val response = openAI.createChatCompletion(json, Prompts.FINANCIAL_ANALYSIS)
    if (debug) {
        println(response.text())
    }
    return response.text()
}

suspend fun enrichedMovieChat(query: String, debug: Boolean = false): String {
    val fromDb = queryVectorDbForMovies(query, 5).joinToString("\n") { it.content }
    val chatGPT = openAI.createChatCompletion(Prompts.formatRagQuery(fromDb, query), Prompts.MOVIE_EXPERT_RAG)
    val response = chatGPT.text()
    if (debug) {
        println("Found $fromDb")
        println(response)
    }
    return response
}

suspend fun clearDbAndInsertDocs(file: String, debug: Boolean = false): List<Document> {
    val embeddingData = embeddings(file)
    supabase.clearDocuments()
    val docs = supabase.insertDocumentsFromPair(embeddingData)
    if (debug) {
        println("Inserted ${docs.size} documents into Supabase.")
        docs.forEach { println(it) }
    }
    return docs
}


suspend fun embeddings(file: String, debug: Boolean = false): List<Pair<String, List<Double>>> {
    val texts = splitText(file)
    val embeddings = openAI.createEmbeddings(texts).mapIndexed { i, it ->
        texts[i] to it
    }
    if (debug) {
        embeddings.forEachIndexed { index, pair ->
            println("Text $index: ${pair.first} (${pair.second.take(10)}... ${pair.second.size} dimensions)")
        }
    }
    return embeddings
}

fun splitText(file: String, debug: Boolean = false): List<String> {
    val text = file.read()
    val splits = Chunker().split(text)
    if (debug) {
        splits.forEachIndexed { index, split ->
            println("Chunk $index: $split (${split.length} characters)")
        }
    }
    return splits
}


suspend fun chatGPT() {
    val chatGPT = openAI.createChatCompletion(
        "What is the capital of France?",
        Prompts.RHYMING_ASSISTANT
    )
    println(chatGPT.text())
    println(chatGPT.usage())
}

suspend fun multiMessageChat(debug: Boolean = false): String {
    val conversationHistory = mutableListOf<com.vend.fmr.aieng.impl.openai.Message>()
    
    // Add system message first
    conversationHistory.add(
        com.vend.fmr.aieng.impl.openai.Message(
            role = "system", 
            content = Prompts.CHAT_ASSISTANT
        )
    )
    
    val results = mutableListOf<String>()
    
    Prompts.ChatConversation.messages.forEachIndexed { index, userMessage ->
        // Add user message to conversation history
        conversationHistory.add(
            com.vend.fmr.aieng.impl.openai.Message(
                role = "user", 
                content = userMessage
            )
        )
        
        if (debug) {
            println("\n--- Turn ${index + 1} ---")
            println("User: $userMessage")
        }
        
        // Get AI response with full conversation context
        val response = openAI.createChatCompletionWithMessages(
            messages = conversationHistory,
            maxTokens = 200,
            temperature = 0.7
        )
        
        val assistantReply = response.text()
        
        // Add assistant response to conversation history
        conversationHistory.add(
            com.vend.fmr.aieng.impl.openai.Message(
                role = Prompts.Roles.ASSISTANT, 
                content = assistantReply
            )
        )
        
        if (debug) {
            println("Assistant: $assistantReply")
        }
        
        results.add("User: $userMessage\nAssistant: $assistantReply")
        
        // Keep conversation history manageable (last 10 messages + system)
        if (conversationHistory.size > 11) {
            // Keep system message and last 10 messages
            conversationHistory.removeAt(1) // Remove oldest non-system message
        }
    }
    
    return results.joinToString("\n\n")
}

suspend fun queryVectorDbForMovies(query: String, matches: Int = 5, debug: Boolean = false): List<DocumentMatch> {
    val response = supabase.matchDocuments(openAI.createEmbedding(query), matches)
    if (debug) {
        response.forEach { match ->
            println("ID: ${match.id}, Content: ${match.content}, Similarity: ${match.similarity}")
        }
    }
    return response
}

data class PromptComparison(
    val scenario: String,
    val vague: String,
    val better: String,
    val excellent: String,
    val vagueResponse: String = "",
    val betterResponse: String = "",
    val excellentResponse: String = ""
)

suspend fun promptEngineeringDemo(debug: Boolean = false) {
    val scenarios = listOf(
        PromptComparison(
            scenario = "Recipe Generation",
            vague = "Make pasta",
            better = "Give me an Italian pasta recipe",
            excellent = "Create an authentic Italian carbonara recipe for 4 people, including: exact ingredient quantities, prep time, cooking steps, and tips for perfect texture"
        ),
        PromptComparison(
            scenario = "Travel Planning", 
            vague = "Plan a trip",
            better = "Plan a 3-day trip to Paris",
            excellent = "Create a detailed 3-day budget-friendly Paris itinerary for a couple in spring, focusing on art museums and local cuisine, with daily schedules, transportation, and cost estimates"
        )
    )
    
    println("=== PROMPT ENGINEERING DEMONSTRATION ===")
    println("Showing how prompt specificity dramatically improves AI responses\n")
    
    scenarios.forEach { scenario ->
        println("🎯 SCENARIO: ${scenario.scenario}")
        println("=" .repeat(50))
        
        println("\n❌ VAGUE PROMPT: \"${scenario.vague}\"")
        val vagueResponse = openAI.createChatCompletion(scenario.vague, Prompts.CONCISE_ASSISTANT)
        println("Response: ${vagueResponse.text()}")
        
        println("\n⚠️ BETTER PROMPT: \"${scenario.better}\"")
        val betterResponse = openAI.createChatCompletion(scenario.better, Prompts.CONCISE_ASSISTANT)
        println("Response: ${betterResponse.text()}")
        
        println("\n✅ EXCELLENT PROMPT: \"${scenario.excellent}\"")
        val excellentResponse = openAI.createChatCompletion(scenario.excellent, Prompts.CONCISE_ASSISTANT)
        println("Response: ${excellentResponse.text()}")
        
        if (debug) {
            println("\nTokens used - Vague: ${vagueResponse.usage?.totalTokens}, Better: ${betterResponse.usage?.totalTokens}, Excellent: ${excellentResponse.usage?.totalTokens}")
        }
        
        println("\n" + "=".repeat(80) + "\n")
    }
}
