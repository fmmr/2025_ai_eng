@file:Suppress("unused")

package com.vend.fmr.aieng.examples

import com.vend.fmr.aieng.apis.openai.OpenAIAssistant
import com.vend.fmr.aieng.utils.*
import com.vend.fmr.aieng.utils.Prompts.MOVIE_ASSISTANT_PROMPT

suspend fun uploadMovieFile(assistant: OpenAIAssistant, debug: Boolean = false): String {
    val movieFile = "/movies.txt".read()
    
    println("📁 Uploading movies.txt file...")
    
    val fileUpload = assistant.uploadFile(
        fileBytes = movieFile.toByteArray(),
        filename = "movies.txt",
        purpose = "assistants",
        debug = debug
    )
    
    println("✅ File uploaded successfully!")
    println("📋 FILE ID: ${fileUpload.id}")
    println("📊 Size: ${fileUpload.bytes} bytes")
    println("🎯 Copy this ID for createMovieVectorStore()")
    
    return fileUpload.id
}

suspend fun createMovieVectorStore(assistant: OpenAIAssistant, fileId: String, debug: Boolean = false): String {
    println("📚 Creating vector store with movie file...")
    
    val vectorStore = assistant.createVectorStore(
        name = "Movie Database",
        fileIds = listOf(fileId),
        debug = debug
    )
    
    println("✅ Vector store created successfully!")
    println("📋 VECTOR STORE ID: ${vectorStore.id}")
    println("📊 Status: ${vectorStore.status}")
    println("📁 Files: ${vectorStore.fileCounts.total}")
    println("🎯 Copy this ID for createMovieAssistant()")
    
    return vectorStore.id
}

suspend fun createMovieAssistant(assistant: OpenAIAssistant, vectorStoreId: String, debug: Boolean = false): String {
    println("🤖 Creating Movie Recommendation Assistant...")

    val assistantResponse = assistant.createAssistant(
        name = "Movie Recommendation Expert",
        instructions = MOVIE_ASSISTANT_PROMPT,
        model = Models.Chat.GPT_4O,
        tools = listOf(com.vend.fmr.aieng.apis.openai.AssistantTool("file_search")),
        vectorStoreIds = listOf(vectorStoreId),
        debug = debug
    )
    
    println("✅ Assistant created successfully!")
    println("📋 ASSISTANT ID: ${assistantResponse.id}")
    println("🎬 Name: ${assistantResponse.name}")
    println("🎯 Copy this ID for movieRecommendationChat()")
    
    return assistantResponse.id
}

suspend fun movieRecommendationChat(
    assistant: OpenAIAssistant,
    assistantId: String, 
    userMessage: String,
    debug: Boolean = false
): String {
    println("💬 Starting movie recommendation chat...")
    
    val thread = assistant.createThread(debug = debug)
    println("🧵 Created thread: ${thread.id}")
    
    assistant.addMessageToThread(thread.id, userMessage, debug = debug)
    println("📝 User: \"$userMessage\"")
    
    val run = assistant.runAssistant(thread.id, assistantId, debug = debug)
    println("⚡ Started run: ${run.id}")
    
    var runStatus = run
    while (runStatus.status == "queued" || runStatus.status == "in_progress") {
        kotlinx.coroutines.delay(1000)
        runStatus = assistant.getRunStatus(thread.id, run.id, debug = debug)
        println("⏳ Status: ${runStatus.status}")
    }
    
    if (runStatus.status == "completed") {
        val messages = assistant.getMessages(thread.id, debug = debug)
        val assistantReply = messages.data
            .filter { it.role == "assistant" }
            .maxByOrNull { it.createdAt }
            ?.content?.firstOrNull()?.text?.value
            ?: "No response found"
        
        println("🎯 Assistant response:")
        println(assistantReply)
        return assistantReply
    } else {
        val error = "Run failed with status: ${runStatus.status}"
        println("❌ $error")
        return error
    }
}

suspend fun deleteMovieFile(assistant: OpenAIAssistant, fileId: String, debug: Boolean = false): Boolean {
    println("🗑️ Deleting file: $fileId")
    val success = assistant.deleteFile(fileId, debug)
    
    if (success) {
        println("✅ File deleted successfully!")
    } else {
        println("❌ Failed to delete file")
    }
    
    return success
}

suspend fun deleteMovieVectorStore(assistant: OpenAIAssistant, vectorStoreId: String, debug: Boolean = false): Boolean {
    println("🗑️ Deleting vector store: $vectorStoreId")
    val success = assistant.deleteVectorStore(vectorStoreId, debug)
    
    if (success) {
        println("✅ Vector store deleted successfully!")
    } else {
        println("❌ Failed to delete vector store")
    }
    
    return success
}

suspend fun deleteMovieAssistant(assistant: OpenAIAssistant, assistantId: String, debug: Boolean = false): Boolean {
    println("🗑️ Deleting assistant: $assistantId")
    val success = assistant.deleteAssistant(assistantId, debug)
    
    if (success) {
        println("✅ Assistant deleted successfully!")
    } else {
        println("❌ Failed to delete assistant")
    }
    
    return success
}

suspend fun listAllAssistantResources(assistant: OpenAIAssistant, debug: Boolean = false) {
    println("📋 Listing all Assistant API resources...")
    
    val files = assistant.listFiles(debug = false)
    println("\n📁 Files (${files.data.size}):")
    files.data.forEach { file ->
        println("  ${file.id} | ${file.filename} | ${file.bytes} bytes | ${file.purpose} | 🚫 No download (OpenAI policy)")
    }
    
    val vectorStores = assistant.listVectorStores(debug = false)
    println("\n📚 Vector Stores (${vectorStores.data.size}):")
    vectorStores.data.forEach { vs ->
        println("  ${vs.id} | ${vs.name ?: "Unnamed"} | ${vs.status} | ${vs.fileCounts.total} files")
    }
    
    val assistants = assistant.listAssistants(debug = false)
    println("\n🤖 Assistants (${assistants.data.size}):")
    assistants.data.forEach { asst ->
        println("  ${asst.id} | ${asst.name ?: "Unnamed"} | ${asst.model}")
    }
}

suspend fun deleteAllAssistantResources(assistant: OpenAIAssistant, debug: Boolean = false) {
    println("🗑️ Deleting ALL Assistant API resources...")
    
    val assistants = assistant.listAssistants(debug = false)
    println("🤖 Deleting ${assistants.data.size} assistants...")
    assistants.data.forEach { asst ->
        val success = assistant.deleteAssistant(asst.id, debug = false)
        println("  ${if (success) "✅" else "❌"} ${asst.id} | ${asst.name}")
    }
    
    val vectorStores = assistant.listVectorStores(debug = false)
    println("📚 Deleting ${vectorStores.data.size} vector stores...")
    vectorStores.data.forEach { vs ->
        val success = assistant.deleteVectorStore(vs.id, debug = false)
        println("  ${if (success) "✅" else "❌"} ${vs.id} | ${vs.name}")
    }
    
    val files = assistant.listFiles(debug = false)
    val assistantFiles = files.data.filter { it.purpose == "assistants" }
    println("📁 Deleting ${assistantFiles.size} assistant files...")
    assistantFiles.forEach { file ->
        val success = assistant.deleteFile(file.id, debug = false)
        println("  ${if (success) "✅" else "❌"} ${file.id} | ${file.filename}")
    }
    
    println("🧹 Cleanup complete!")
}