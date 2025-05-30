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

suspend fun queryVectorDbForMovies(query: String, matches: Int = 5, debug: Boolean = false): List<DocumentMatch> {
    val response = supabase.matchDocuments(openAI.createEmbedding(query), matches)
    if (debug) {
        response.forEach { match ->
            println("ID: ${match.id}, Content: ${match.content}, Similarity: ${match.similarity}")
        }
    }
    return response
}
