package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.apis.openai.AssistantTool
import com.vend.fmr.aieng.apis.openai.OpenAIAssistant
import com.vend.fmr.aieng.utils.Demo
import com.vend.fmr.aieng.utils.Prompts.MOVIE_ASSISTANT_PROMPT
import com.vend.fmr.aieng.utils.read
import jakarta.servlet.http.HttpSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

data class AssistantRequest(
    val operation: String,
    val sessionId: String,
    val message: String? = null
)

@Controller
@RequestMapping("/demo/assistants-api")
class AssistantsApiController(private val openAIAssistant: OpenAIAssistant) : BaseController(Demo.ASSISTANTS_API) {

    private val logger = LoggerFactory.getLogger(AssistantsApiController::class.java)
    
    companion object {
        private const val FMR_FILE_PREFIX = "fmr-movies.txt"
        private const val FMR_VECTOR_STORE_NAME = "fmr-Movie Database"
        private const val FMR_ASSISTANT_NAME = "fmr-Movie Recommendation Expert"
    }

    private fun sendMessage(sessionId: String, message: String, type: String = "info") {
        sendSseEvent(sessionId, type, message)
    }


    @PostMapping("/process")
    @ResponseBody
    fun processOperation(@RequestBody request: AssistantRequest, session: HttpSession): Map<String, String> {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (request.operation) {
                    "upload" -> processUpload(request.sessionId, session)
                    "create-vector-store" -> processCreateVectorStore(request.sessionId, session)
                    "create-assistant" -> processCreateAssistant(request.sessionId, session)
                    "query" -> processQuery(request.sessionId, request.message ?: "", session)
                    "list" -> processList(request.sessionId)
                    "cleanup-files" -> processCleanupFiles(request.sessionId, session)
                    "cleanup-vector-stores" -> processCleanupVectorStores(request.sessionId, session)
                    "cleanup-assistants" -> processCleanupAssistants(request.sessionId, session)
                    "cleanup-all" -> processCleanupAll(request.sessionId, session)
                    else -> sendMessage(request.sessionId, "Unknown operation: ${request.operation}", "error")
                }
            } catch (e: Exception) {
                logger.error("Operation ${request.operation} failed for session ${request.sessionId}", e)
                sendMessage(request.sessionId, "Operation failed: ${e.message}", "error")
            }
        }

        return mapOf("status" to "started")
    }

    private suspend fun processUpload(sessionId: String, session: HttpSession) {
        sendMessage(sessionId, "📁 Starting file upload...", "progress")

        try {
            val movieFile = "/movies.txt".read()
            sendMessage(sessionId, "📄 File loaded, uploading to OpenAI...", "progress")

            val file = openAIAssistant.uploadFile(
                fileBytes = movieFile.toByteArray(),
                filename = FMR_FILE_PREFIX,
                purpose = "assistants",
                debug = false
            )

            session.setAttribute("fileId", file.id)
            sendMessage(sessionId, "✅ File uploaded successfully!", "success")
            sendMessage(sessionId, file.toSSEHtml(), "final_result")

        } catch (e: Exception) {
            logger.error("File upload failed for session $sessionId", e)
            sendMessage(sessionId, "❌ Upload failed: ${e.message}", "error")
        }
    }

    private suspend fun processCreateVectorStore(sessionId: String, session: HttpSession) {
        sendMessage(sessionId, "📚 Starting vector store creation...", "progress")

        try {
            var fileId = session.getAttribute("fileId") as String?

            if (fileId == null) {
                sendMessage(sessionId, "🔍 No file in session, checking for existing FMR files...", "progress")
                val files = openAIAssistant.listFiles(debug = false)
                val fmrFiles = files.data.filter { it.purpose == "assistants" && it.isFmr }

                if (fmrFiles.isEmpty()) {
                    sendMessage(sessionId, "❌ No FMR files available. Please upload a file first.", "error")
                    return
                }

                fileId = fmrFiles.first().id
                sendMessage(sessionId, "✅ Found existing FMR file: ${fmrFiles.first().filename} (${fileId})", "info")
            } else {
                sendMessage(sessionId, "✅ Using file from session: $fileId", "info")
            }

            sendMessage(sessionId, "🔧 Creating vector store with file...", "progress")

            val vectorStore = openAIAssistant.createVectorStore(
                name = FMR_VECTOR_STORE_NAME,
                fileIds = listOf(fileId),
                debug = false
            )

            session.setAttribute("vectorStoreId", vectorStore.id)
            sendMessage(sessionId, "✅ Vector store created successfully!", "success")
            sendMessage(sessionId, vectorStore.toSSEHtml(), "final_result")

        } catch (e: Exception) {
            logger.error("Vector store creation failed for session $sessionId", e)
            sendMessage(sessionId, "❌ Vector store creation failed: ${e.message}", "error")
        }
    }

    private suspend fun processCreateAssistant(sessionId: String, session: HttpSession) {
        sendMessage(sessionId, "🤖 Starting assistant creation...", "progress")

        try {
            // Get vector store ID from session or find existing one
            var vectorStoreId = session.getAttribute("vectorStoreId") as String?
            
            if (vectorStoreId == null) {
                sendMessage(sessionId, "🔍 No vector store in session, checking for existing FMR ones...", "progress")
                val vectorStores = openAIAssistant.listVectorStores(debug = false)
                val fmrVectorStores = vectorStores.data.filter { it.isFmr }
                
                if (fmrVectorStores.isEmpty()) {
                    sendMessage(sessionId, "❌ No FMR vector stores available. Please create a vector store first.", "error")
                    return
                }
                
                vectorStoreId = fmrVectorStores.first().id
                sendMessage(sessionId, "✅ Found existing FMR vector store: ${fmrVectorStores.first().name} (${vectorStoreId})", "info")
            } else {
                sendMessage(sessionId, "✅ Using vector store from session: $vectorStoreId", "info")
            }

            sendMessage(sessionId, "🔧 Creating movie recommendation assistant...", "progress")
            
            val assistant = openAIAssistant.createAssistant(
                name = FMR_ASSISTANT_NAME,
                instructions = MOVIE_ASSISTANT_PROMPT,
                tools = listOf(AssistantTool("file_search")),
                vectorStoreIds = listOf(vectorStoreId),
                debug = false
            )

            session.setAttribute("assistantId", assistant.id)
            sendMessage(sessionId, "✅ Assistant created successfully!", "success")
            sendMessage(sessionId, assistant.toSSEHtml(), "final_result")

        } catch (e: Exception) {
            logger.error("Assistant creation failed for session $sessionId", e)
            sendMessage(sessionId, "❌ Assistant creation failed: ${e.message}", "error")
        }
    }

    private suspend fun processQuery(sessionId: String, userMessage: String, session: HttpSession) {
        if (userMessage.isBlank()) {
            sendMessage(sessionId, "❌ Please provide a message to query the assistant", "error")
            return
        }

        // Display user question first
        sendMessage(sessionId, userMessage, "user_question")
        sendMessage(sessionId, "💬 Starting chat with assistant...", "progress")

        try {
            // Get assistant ID from session or find existing one
            var assistantId = session.getAttribute("assistantId") as String?
            
            if (assistantId == null) {
                sendMessage(sessionId, "🔍 No assistant in session, checking for existing FMR ones...", "progress")
                val assistants = openAIAssistant.listAssistants(debug = false)
                val fmrAssistants = assistants.data.filter { it.isFmr }
                
                if (fmrAssistants.isEmpty()) {
                    sendMessage(sessionId, "❌ No FMR assistants available. Please create an assistant first.", "error")
                    return
                }
                
                assistantId = fmrAssistants.first().id
                sendMessage(sessionId, "✅ Found existing FMR assistant: ${fmrAssistants.first().name} (${assistantId})", "info")
            } else {
                sendMessage(sessionId, "✅ Using assistant from session: $assistantId", "info")
            }

            // Get or create thread
            var threadId = session.getAttribute("threadId") as String?
            
            if (threadId == null) {
                sendMessage(sessionId, "🧵 Creating new conversation thread...", "progress")
                val thread = openAIAssistant.createThread(debug = false)
                threadId = thread.id
                session.setAttribute("threadId", threadId)
                sendMessage(sessionId, "✅ New conversation started!", "info")
            } else {
                sendMessage(sessionId, "🧵 Continuing conversation in existing thread...", "progress")
            }

            // Add message to thread
            sendMessage(sessionId, "📝 Adding your message to thread...", "progress")
            openAIAssistant.addMessageToThread(threadId, userMessage, debug = false)

            // Run assistant
            sendMessage(sessionId, "⚡ Running assistant...", "progress")
            val run = openAIAssistant.runAssistant(threadId, assistantId, debug = false)

            // Poll for completion
            var runStatus = run
            while (runStatus.status == "queued" || runStatus.status == "in_progress") {
                sendMessage(sessionId, "⏳ Assistant is thinking... (${runStatus.status})", "progress")
                kotlinx.coroutines.delay(2000)
                runStatus = openAIAssistant.getRunStatus(threadId, run.id, debug = false)
            }

            if (runStatus.status == "completed") {
                sendMessage(sessionId, "✅ Assistant completed! Getting response...", "progress")
                val messages = openAIAssistant.getMessages(threadId, debug = false)
                val assistantReply = messages.data
                    .filter { it.role == "assistant" }
                    .maxByOrNull { it.createdAt }
                    ?.content?.firstOrNull()?.text?.value
                    ?: "No response found"

                sendMessage(sessionId, assistantReply, "final_result")
            } else {
                sendMessage(sessionId, "❌ Assistant run failed with status: ${runStatus.status}", "error")
            }

        } catch (e: Exception) {
            logger.error("Assistant query failed for session $sessionId", e)
            sendMessage(sessionId, "❌ Query failed: ${e.message}", "error")
        }
    }

    private suspend fun processList(sessionId: String) {
        sendMessage(sessionId, "📋 Listing all resources...", "progress")

        try {
            // List All Files
            val files = openAIAssistant.listFiles(debug = false)
            val assistantFiles = files.data.filter { it.purpose == "assistants" }
            val fmrFileCount = assistantFiles.count { it.isFmr }

            sendMessage(sessionId, "📁 Files (${assistantFiles.size} total, $fmrFileCount FMR):", "info")
            if (assistantFiles.isEmpty()) {
                sendMessage(sessionId, "No files found", "info")
            } else {
                assistantFiles.forEach { file -> 
                    sendMessage(sessionId, file.toSSEHtml(), "final_result") 
                }
            }

            // List All Vector Stores
            val vectorStores = openAIAssistant.listVectorStores(debug = false)
            val fmrVectorStoreCount = vectorStores.data.count { it.isFmr }

            sendMessage(sessionId, "📚 Vector Stores (${vectorStores.data.size} total, $fmrVectorStoreCount FMR):", "info")
            if (vectorStores.data.isEmpty()) {
                sendMessage(sessionId, "No vector stores found", "info")
            } else {
                vectorStores.data.forEach { vs -> 
                    sendMessage(sessionId, vs.toSSEHtml(), "final_result") 
                }
            }

            // List All Assistants
            val assistants = openAIAssistant.listAssistants(debug = false)
            val fmrAssistantCount = assistants.data.count { it.isFmr }

            sendMessage(sessionId, "🤖 Assistants (${assistants.data.size} total, $fmrAssistantCount FMR):", "info")
            if (assistants.data.isEmpty()) {
                sendMessage(sessionId, "No assistants found", "info")
            } else {
                assistants.data.forEach { assistant -> 
                    sendMessage(sessionId, assistant.toSSEHtml(), "final_result") 
                }
            }

        } catch (e: Exception) {
            logger.error("Resource listing failed for session $sessionId", e)
            sendMessage(sessionId, "❌ List failed: ${e.message}", "error")
        }
    }

    private suspend fun processCleanupFiles(sessionId: String, session: HttpSession) {
        sendMessage(sessionId, "🗑️ Starting file cleanup...", "progress")

        try {
            val files = openAIAssistant.listFiles(debug = false)
            var deletedCount = 0

            val filesToDelete = files.data.filter { it.purpose == "assistants" && it.isFmr }

            sendMessage(sessionId, "🔍 Found ${filesToDelete.size} FMR files to delete...", "progress")

            filesToDelete.forEach { file ->
                if (openAIAssistant.deleteFile(file.id, debug = false)) {
                    deletedCount++
                    sendMessage(sessionId, "  ✅ Deleted: ${file.filename}, ${file.id}", "progress")
                } else {
                    sendMessage(sessionId, "  ❌ Failed to delete: ${file.filename}", "progress")
                }
            }

            session.removeAttribute("fileId")
            session.removeAttribute("threadId") // Clear thread when files are deleted
            sendMessage(sessionId, "🧹 FMR file cleanup complete! Deleted $deletedCount files.", "success")

        } catch (e: Exception) {
            logger.error("File cleanup failed for session $sessionId", e)
            sendMessage(sessionId, "❌ File cleanup failed: ${e.message}", "error")
        }
    }

    private suspend fun processCleanupVectorStores(sessionId: String, session: HttpSession) {
        sendMessage(sessionId, "🗑️ Starting vector store cleanup...", "progress")

        try {
            val vectorStores = openAIAssistant.listVectorStores(debug = false)
            var deletedCount = 0
            val fmrVectorStores = vectorStores.data.filter { it.isFmr }

            sendMessage(sessionId, "🔍 Found ${fmrVectorStores.size} FMR vector stores to delete...", "progress")

            fmrVectorStores.forEach { vs ->
                if (openAIAssistant.deleteVectorStore(vs.id, debug = false)) {
                    deletedCount++
                    sendMessage(sessionId, "  ✅ Deleted: ${vs.name ?: "Unnamed"}, ${vs.id}", "progress")
                } else {
                    sendMessage(sessionId, "  ❌ Failed to delete: ${vs.name ?: "Unnamed"}", "progress")
                }
            }

            session.removeAttribute("vectorStoreId")
            sendMessage(sessionId, "🧹 FMR vector store cleanup complete! Deleted $deletedCount vector stores.", "success")

        } catch (e: Exception) {
            logger.error("Vector store cleanup failed for session $sessionId", e)
            sendMessage(sessionId, "❌ Vector store cleanup failed: ${e.message}", "error")
        }
    }

    private suspend fun processCleanupAssistants(sessionId: String, session: HttpSession) {
        sendMessage(sessionId, "🗑️ Starting assistant cleanup...", "progress")

        try {
            val assistants = openAIAssistant.listAssistants(debug = false)
            var deletedCount = 0
            val fmrAssistants = assistants.data.filter { it.isFmr }
            
            sendMessage(sessionId, "🔍 Found ${fmrAssistants.size} FMR assistants to delete...", "progress")

            fmrAssistants.forEach { assistant ->
                if (openAIAssistant.deleteAssistant(assistant.id, debug = false)) {
                    deletedCount++
                    sendMessage(sessionId, "  ✅ Deleted: ${assistant.name ?: "Unnamed"}, ${assistant.id}", "progress")
                } else {
                    sendMessage(sessionId, "  ❌ Failed to delete: ${assistant.name ?: "Unnamed"}", "progress")
                }
            }

            session.removeAttribute("assistantId")
            session.removeAttribute("threadId") // Clear thread when assistants are deleted
            sendMessage(sessionId, "🧹 FMR assistant cleanup complete! Deleted $deletedCount assistants.", "success")

        } catch (e: Exception) {
            logger.error("Assistant cleanup failed for session $sessionId", e)
            sendMessage(sessionId, "❌ Assistant cleanup failed: ${e.message}", "error")
        }
    }

    private suspend fun processCleanupAll(sessionId: String, session: HttpSession) {
        sendMessage(sessionId, "🗑️ Starting complete FMR cleanup (assistants + vector stores + files)...", "progress")

        try {
            // Delete assistants first (they depend on vector stores)
            processCleanupAssistants(sessionId, session)
            
            // Then delete vector stores (they depend on files)
            processCleanupVectorStores(sessionId, session)
            
            // Finally delete files
            processCleanupFiles(sessionId, session)
            
            sendMessage(sessionId, "🧹 Complete FMR cleanup finished!", "success")

        } catch (e: Exception) {
            logger.error("Complete cleanup failed for session $sessionId", e)
            sendMessage(sessionId, "❌ Complete cleanup failed: ${e.message}", "error")
        }
    }
}