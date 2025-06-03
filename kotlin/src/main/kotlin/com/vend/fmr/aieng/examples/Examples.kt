@file:Suppress("unused")

package com.vend.fmr.aieng.examples

import com.vend.fmr.aieng.utils.*
import com.vend.fmr.aieng.dtos.*
import com.vend.fmr.aieng.apis.chunker.Chunker
import com.vend.fmr.aieng.apis.supabase.Document
import com.vend.fmr.aieng.apis.supabase.DocumentMatch
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
    val conversationHistory = mutableListOf<com.vend.fmr.aieng.apis.openai.Message>()
    
    conversationHistory.add(
        com.vend.fmr.aieng.apis.openai.Message(
            role = "system", 
            content = com.vend.fmr.aieng.apis.openai.TextContent(Prompts.CHAT_ASSISTANT)
        )
    )
    
    val results = mutableListOf<String>()
    
    Prompts.ChatConversation.messages.forEachIndexed { index, userMessage ->
        conversationHistory.add(
            com.vend.fmr.aieng.apis.openai.Message(
                role = "user", 
                content = com.vend.fmr.aieng.apis.openai.TextContent(userMessage)
            )
        )
        
        if (debug) {
            println("\n--- Turn ${index + 1} ---")
            println("User: $userMessage")
        }
        
        val response = openAI.createChatCompletion(
            messages = conversationHistory,
            maxTokens = 200,
            temperature = 0.7
        )
        
        val assistantReply = response.text()
        
        conversationHistory.add(
            com.vend.fmr.aieng.apis.openai.Message(
                role = Prompts.Roles.ASSISTANT, 
                content = com.vend.fmr.aieng.apis.openai.TextContent(assistantReply)
            )
        )
        
        if (debug) {
            println("Assistant: $assistantReply")
        }
        
        results.add("User: $userMessage\nAssistant: $assistantReply")
        
        if (conversationHistory.size > 11) {
            conversationHistory.removeAt(1)
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

suspend fun temperatureDemo(debug: Boolean = false) {
    val prompt = Prompts.Defaults.CHAT_PARAMETERS_PROMPT
    val fixedTopP = 0.9
    
    val temperatureSettings = listOf(
        ParameterSet(
            name = "Very Conservative",
            description = "Extremely Predictable",
            temperature = 0.1,
            topP = fixedTopP,
            color = "🔵"
        ),
        ParameterSet(
            name = "Moderate", 
            description = "Balanced Responses",
            temperature = 0.5,
            topP = fixedTopP,
            color = "🟢"
        ),
        ParameterSet(
            name = "Creative",
            description = "More Adventurous", 
            temperature = 1.0,
            topP = fixedTopP,
            color = "🟡"
        ),
        ParameterSet(
            name = "Wild",
            description = "Maximum Creativity",
            temperature = 1.9,
            topP = fixedTopP,
            color = "🔴"
        )
    )
    
    println("=== 🌡️ TEMPERATURE EFFECTS DEMONSTRATION ===")
    println("Prompt: \"$prompt\"")
    println("Fixed: top_p = $fixedTopP")
    println("Variable: temperature (0.1 → 1.8)")
    println("Shows: How temperature affects creativity vs consistency\n")
    
    temperatureSettings.forEach { params ->
        println("${params.color} TEMPERATURE ${params.temperature} - ${params.name.uppercase()}")
        println("${params.description} (top_p = ${params.topP})")
        println("=" .repeat(60))
        
        try {
            val response = openAI.createChatCompletion(
                prompt = prompt,
                systemMessage = Prompts.BRIEF_ASSISTANT,
                maxTokens = 100,
                temperature = params.temperature,
                topP = params.topP
            )
            
            println("Response: ${response.text()}")
            
            if (debug) {
                println("Tokens used: ${response.usage?.totalTokens}")
            }
            
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
        
        println("\n" + "=".repeat(80) + "\n")
    }
}

suspend fun topPDemo(debug: Boolean = false) {
    val prompt = Prompts.Defaults.CHAT_PARAMETERS_PROMPT
    val fixedTemperature = 0.7
    
    val topPSettings = listOf(
        ParameterSet(
            name = "Very Restricted",
            description = "Only Most Likely Words",
            temperature = fixedTemperature,
            topP = 0.1,
            color = "🔵"
        ),
        ParameterSet(
            name = "Somewhat Restricted", 
            description = "Limited Vocabulary",
            temperature = fixedTemperature,
            topP = 0.5,
            color = "🟢"
        ),
        ParameterSet(
            name = "Balanced",
            description = "Good Word Variety", 
            temperature = fixedTemperature,
            topP = 0.9,
            color = "🟡"
        ),
        ParameterSet(
            name = "Unrestricted",
            description = "Full Vocabulary Access",
            temperature = fixedTemperature,
            topP = 1.0,
            color = "🔴"
        )
    )
    
    println("=== 🎯 TOP-P EFFECTS DEMONSTRATION ===")
    println("Prompt: \"$prompt\"")
    println("Fixed: temperature = $fixedTemperature")
    println("Variable: top_p (0.1 → 1.0)")
    println("Shows: How top_p affects vocabulary restriction vs exploration\n")
    
    topPSettings.forEach { params ->
        println("${params.color} TOP_P ${params.topP} - ${params.name.uppercase()}")
        println("${params.description} (temperature = ${params.temperature})")
        println("=" .repeat(60))
        
        try {
            val response = openAI.createChatCompletion(
                prompt = prompt,
                systemMessage = Prompts.BRIEF_ASSISTANT,
                maxTokens = 100,
                temperature = params.temperature,
                topP = params.topP
            )
            
            println("Response: ${response.text()}")
            
            if (debug) {
                println("Tokens used: ${response.usage?.totalTokens}")
            }
            
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
        
        println("\n" + "=".repeat(80) + "\n")
    }
}

suspend fun chatParametersDemo(debug: Boolean = false) {
    temperatureDemo(debug)
    println("\n" + "🔄".repeat(40) + "\n")
    topPDemo(debug)
    
    println("🎯 KEY LEARNINGS:")
    println("🌡️ TEMPERATURE:")
    println("  • Low (0.1) = Very predictable, safe, repetitive responses")
    println("  • High (1.8) = Creative, unpredictable, sometimes chaotic responses")
    println("🎯 TOP-P:")
    println("  • Low (0.1) = Uses only the most probable words, conservative vocabulary")
    println("  • High (1.0) = Considers full vocabulary range, more word variety")
}