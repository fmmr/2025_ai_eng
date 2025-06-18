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
                    else -> {
                        this@AssistantsApiController.sendSseEvent(request.sessionId, "app_error", "Unknown operation: ${request.operation}")
                    }
                }
            } catch (e: Exception) {
                logger.error("Operation ${request.operation} failed for session ${request.sessionId}", e)
                this@AssistantsApiController.sendSseEvent(request.sessionId, "app_error", "Operation failed: ${e.message}")
            }
        }

        return mapOf("status" to "started")
    }

    private suspend fun processUpload(sessionId: String, session: HttpSession) {
        sendSseEvent(sessionId, "progress", "📁 Starting file upload...")

        try {
            val movieFile = "/movies.txt".read()
            sendSseEvent(sessionId, "progress", "📄 File loaded, uploading to OpenAI...")

            val file = openAIAssistant.uploadFile(
                fileBytes = movieFile.toByteArray(),
                filename = FMR_FILE_PREFIX,
                purpose = "assistants",
                debug = false
            )

            session.setAttribute("fileId", file.id)
            sendSseEvent(sessionId, "success", "✅ File uploaded successfully!")
            sendSseEvent(sessionId, "final_result", file.toSSEHtml())
        } catch (e: Exception) {
            logger.error("File upload failed for session $sessionId", e)
            sendSseEvent(sessionId, "app_error", "❌ Upload failed: ${e.message}")
        }
    }

    private suspend fun processCreateVectorStore(sessionId: String, session: HttpSession) {
        sendSseEvent(sessionId, "progress", "📚 Starting vector store creation...")

        try {
            var fileId = session.getAttribute("fileId") as String?

            if (fileId == null) {
                sendSseEvent(sessionId, "progress", "🔍 No file in session, checking for existing FMR files...")
                val files = openAIAssistant.listFiles(debug = false)
                val fmrFiles = files.data.filter { it.purpose == "assistants" && it.isFmr }

                if (fmrFiles.isEmpty()) {
                    sendSseEvent(sessionId, "app_error", "❌ No FMR files available. Please upload a file first.")
                    return
                }

                fileId = fmrFiles.first().id
                sendSseEvent(sessionId, "info", "✅ Found existing FMR file: ${fmrFiles.first().filename} (${fileId})")
            } else {
                sendSseEvent(sessionId, "info", "✅ Using file from session: $fileId")
            }

            sendSseEvent(sessionId, "progress", "🔧 Creating vector store with file...")

            val vectorStore = openAIAssistant.createVectorStore(
                name = FMR_VECTOR_STORE_NAME,
                fileIds = listOf(fileId),
                debug = false
            )

            session.setAttribute("vectorStoreId", vectorStore.id)
            sendSseEvent(sessionId, "success", "✅ Vector store created successfully!")
            sendSseEvent(sessionId, "final_result", vectorStore.toSSEHtml())

        } catch (e: Exception) {
            logger.error("Vector store creation failed for session $sessionId", e)
            sendSseEvent(sessionId, "app_error", "❌ Vector store creation failed: ${e.message}")
        }
    }

    private suspend fun processCreateAssistant(sessionId: String, session: HttpSession) {
        sendSseEvent(sessionId, "progress", "🤖 Starting assistant creation...")

        try {
            // Get vector store ID from session or find existing one
            var vectorStoreId = session.getAttribute("vectorStoreId") as String?

            if (vectorStoreId == null) {
                sendSseEvent(sessionId, "progress", "🔍 No vector store in session, checking for existing FMR ones...")
                val vectorStores = openAIAssistant.listVectorStores(debug = false)
                val fmrVectorStores = vectorStores.data.filter { it.isFmr }

                if (fmrVectorStores.isEmpty()) {
                    sendSseEvent(sessionId, "app_error", "❌ No FMR vector stores available. Please create a vector store first.")
                    return
                }

                vectorStoreId = fmrVectorStores.first().id
                sendSseEvent(sessionId, "info", "✅ Found existing FMR vector store: ${fmrVectorStores.first().name} (${vectorStoreId})")
            } else {
                sendSseEvent(sessionId, "info", "✅ Using vector store from session: $vectorStoreId")
            }

            sendSseEvent(sessionId, "progress", "🔧 Creating movie recommendation assistant...")

            val assistant = openAIAssistant.createAssistant(
                name = FMR_ASSISTANT_NAME,
                instructions = MOVIE_ASSISTANT_PROMPT,
                tools = listOf(AssistantTool("file_search")),
                vectorStoreIds = listOf(vectorStoreId),
                debug = false
            )

            session.setAttribute("assistantId", assistant.id)
            sendSseEvent(sessionId, "success", "✅ Assistant created successfully!")
            sendSseEvent(sessionId, "final_result", assistant.toSSEHtml())

        } catch (e: Exception) {
            logger.error("Assistant creation failed for session $sessionId", e)
            sendSseEvent(sessionId, "app_error", "❌ Assistant creation failed: ${e.message}")
        }
    }

    private suspend fun processQuery(sessionId: String, userMessage: String, session: HttpSession) {
        if (userMessage.isBlank()) {
            sendSseEvent(sessionId, "app_error", "❌ Please provide a message to query the assistant")
            return
        }

        // Display user question first
        sendSseEvent(sessionId, "user_question", userMessage)
        sendSseEvent(sessionId, "progress", "💬 Starting chat with assistant...")

        try {
            // Get assistant ID from session or find existing one
            var assistantId = session.getAttribute("assistantId") as String?

            if (assistantId == null) {
                sendSseEvent(sessionId, "progress", "🔍 No assistant in session, checking for existing FMR ones...")
                val assistants = openAIAssistant.listAssistants(debug = false)
                val fmrAssistants = assistants.data.filter { it.isFmr }

                if (fmrAssistants.isEmpty()) {
                    sendSseEvent(sessionId, "app_error", "❌ No FMR assistants available. Please create an assistant first.")
                    return
                }

                assistantId = fmrAssistants.first().id
                session.setAttribute("assistantId", assistantId)
                sendSseEvent(sessionId, "info", "✅ Found existing FMR assistant: ${fmrAssistants.first().name} (${assistantId})")
            } else {
                sendSseEvent(sessionId, "info", "✅ Using assistant from session: $assistantId")
            }

            // Get or create thread
            var threadId = session.getAttribute("threadId") as String?

            if (threadId == null) {
                sendSseEvent(sessionId, "progress", "🧵 Creating new conversation thread...")
                val thread = openAIAssistant.createThread(debug = false)
                threadId = thread.id
                session.setAttribute("threadId", threadId)
                openAIAssistant.addMessageToThread(threadId, MOVIE_ASSISTANT_PROMPT, role = "system", debug = false)
                sendSseEvent(sessionId, "info", "✅ New conversation started!")
            } else {
                sendSseEvent(sessionId, "progress", "🧵 Continuing conversation in existing thread...")
            }

            // Add a message to thread
            sendSseEvent(sessionId, "progress", "📝 Adding your message to thread...")
            openAIAssistant.addMessageToThread(threadId, userMessage, debug = false)

            // Run assistant
            sendSseEvent(sessionId, "progress", "⚡ Running assistant...")
            val run = openAIAssistant.runAssistant(threadId, assistantId, debug = false)

            // Poll for completion
            var runStatus = run
            while (runStatus.status == "queued" || runStatus.status == "in_progress") {
                sendSseEvent(sessionId, "progress", "⏳ Assistant is thinking... (${runStatus.status})")
                kotlinx.coroutines.delay(2000)
                runStatus = openAIAssistant.getRunStatus(threadId, run.id, debug = false)
            }

            if (runStatus.status == "completed") {
                sendSseEvent(sessionId, "progress", "✅ Assistant completed! Getting response...")
                val messages = openAIAssistant.getMessages(threadId, debug = false)
                val assistantReply = messages.data
                    .filter { it.role == "assistant" }
                    .maxByOrNull { it.createdAt }
                    ?.content?.firstOrNull()?.text?.value
                    ?: "No response found"

                sendSseEvent(sessionId, "final_result", assistantReply)
            } else {
                sendSseEvent(sessionId, "app_error", "❌ Assistant run failed with status: ${runStatus.status}")
            }

        } catch (e: Exception) {
            logger.error("Assistant query failed for session $sessionId", e)
            sendSseEvent(sessionId, "app_error", "❌ Query failed: ${e.message}")
        }
    }

    private suspend fun processList(sessionId: String) {
        sendSseEvent(sessionId, "progress", "📋 Listing all resources...")

        try {
            // List All Files
            val files = openAIAssistant.listFiles(debug = false)
            val assistantFiles = files.data.filter { it.purpose == "assistants" }
            val fmrFileCount = assistantFiles.count { it.isFmr }

            sendSseEvent(sessionId, "info", "📁 Files (${assistantFiles.size} total, $fmrFileCount FMR):")
            if (assistantFiles.isEmpty()) {
                sendSseEvent(sessionId, "info", "No files found")
            } else {
                assistantFiles.forEach { file ->
                    sendSseEvent(sessionId, "final_result", file.toSSEHtml())
                }
            }

            // List All Vector Stores
            val vectorStores = openAIAssistant.listVectorStores(debug = false)
            val fmrVectorStoreCount = vectorStores.data.count { it.isFmr }

            sendSseEvent(sessionId, "info", "📚 Vector Stores (${vectorStores.data.size} total, $fmrVectorStoreCount FMR):")
            if (vectorStores.data.isEmpty()) {
                sendSseEvent(sessionId, "info", "No vector stores found")
            } else {
                vectorStores.data.forEach { vs ->
                    sendSseEvent(sessionId, "final_result", vs.toSSEHtml())
                }
            }

            // List All Assistants
            val assistants = openAIAssistant.listAssistants(debug = false)
            val fmrAssistantCount = assistants.data.count { it.isFmr }

            sendSseEvent(sessionId, "info", "🤖 Assistants (${assistants.data.size} total, $fmrAssistantCount FMR):")
            if (assistants.data.isEmpty()) {
                sendSseEvent(sessionId, "info", "No assistants found")
            } else {
                assistants.data.forEach { assistant ->
                    sendSseEvent(sessionId, "final_result", assistant.toSSEHtml())
                }
            }

        } catch (e: Exception) {
            logger.error("Resource listing failed for session $sessionId", e)
            sendSseEvent(sessionId, "app_error", "❌ List failed: ${e.message}")
        }
    }

    private suspend fun processCleanupFiles(sessionId: String, session: HttpSession) {
        sendSseEvent(sessionId, "progress", "🗑️ Starting file cleanup...")

        try {
            val files = openAIAssistant.listFiles(debug = false)
            var deletedCount = 0

            val filesToDelete = files.data.filter { it.purpose == "assistants" && it.isFmr }

            sendSseEvent(sessionId, "progress", "🔍 Found ${filesToDelete.size} FMR files to delete...")

            filesToDelete.forEach { file ->
                if (openAIAssistant.deleteFile(file.id, debug = false)) {
                    deletedCount++
                    sendSseEvent(sessionId, "progress", "  ✅ Deleted: ${file.filename}, ${file.id}")
                } else {
                    sendSseEvent(sessionId, "progress", "  ❌ Failed to delete: ${file.filename}")
                }
            }

            session.removeAttribute("fileId")
            session.removeAttribute("threadId") // Clear thread when files are deleted
            sendSseEvent(sessionId, "success", "🧹 FMR file cleanup complete! Deleted $deletedCount files.")

        } catch (e: Exception) {
            logger.error("File cleanup failed for session $sessionId", e)
            sendSseEvent(sessionId, "app_error", "❌ File cleanup failed: ${e.message}")
        }
    }

    private suspend fun processCleanupVectorStores(sessionId: String, session: HttpSession) {
        sendSseEvent(sessionId, "progress", "🗑️ Starting vector store cleanup...")

        try {
            val vectorStores = openAIAssistant.listVectorStores(debug = false)
            var deletedCount = 0
            val fmrVectorStores = vectorStores.data.filter { it.isFmr }

            sendSseEvent(sessionId, "progress", "🔍 Found ${fmrVectorStores.size} FMR vector stores to delete...")

            fmrVectorStores.forEach { vs ->
                if (openAIAssistant.deleteVectorStore(vs.id, debug = false)) {
                    deletedCount++
                    sendSseEvent(sessionId, "progress", "  ✅ Deleted: ${vs.name ?: "Unnamed"}, ${vs.id}")
                } else {
                    sendSseEvent(sessionId, "progress", "  ❌ Failed to delete: ${vs.name ?: "Unnamed"}")
                }
            }

            session.removeAttribute("vectorStoreId")
            sendSseEvent(sessionId, "success", "🧹 FMR vector store cleanup complete! Deleted $deletedCount vector stores.")

        } catch (e: Exception) {
            logger.error("Vector store cleanup failed for session $sessionId", e)
            sendSseEvent(sessionId, "app_error", "❌ Vector store cleanup failed: ${e.message}")
        }
    }

    private suspend fun processCleanupAssistants(sessionId: String, session: HttpSession) {
        sendSseEvent(sessionId, "progress", "🗑️ Starting assistant cleanup...")

        try {
            val assistants = openAIAssistant.listAssistants(debug = false)
            var deletedCount = 0
            val fmrAssistants = assistants.data.filter { it.isFmr }

            sendSseEvent(sessionId, "progress", "🔍 Found ${fmrAssistants.size} FMR assistants to delete...")

            fmrAssistants.forEach { assistant ->
                if (openAIAssistant.deleteAssistant(assistant.id, debug = false)) {
                    deletedCount++
                    sendSseEvent(sessionId, "progress", "  ✅ Deleted: ${assistant.name ?: "Unnamed"}, ${assistant.id}")
                } else {
                    sendSseEvent(sessionId, "progress", "  ❌ Failed to delete: ${assistant.name ?: "Unnamed"}")
                }
            }

            session.removeAttribute("assistantId")
            session.removeAttribute("threadId") // Clear thread when assistants are deleted
            sendSseEvent(sessionId, "success", "🧹 FMR assistant cleanup complete! Deleted $deletedCount assistants.")

        } catch (e: Exception) {
            logger.error("Assistant cleanup failed for session $sessionId", e)
            sendSseEvent(sessionId, "app_error", "❌ Assistant cleanup failed: ${e.message}")
        }
    }

    private suspend fun processCleanupAll(sessionId: String, session: HttpSession) {
        sendSseEvent(sessionId, "progress", "🗑️ Starting complete FMR cleanup (assistants + vector stores + files)...")

        try {
            // Delete assistants first (they depend on vector stores)
            processCleanupAssistants(sessionId, session)

            // Then delete vector stores (they depend on files)
            processCleanupVectorStores(sessionId, session)

            // Finally delete files
            processCleanupFiles(sessionId, session)

            sendSseEvent(sessionId, "success", "🧹 Complete FMR cleanup finished!")

        } catch (e: Exception) {
            logger.error("Complete cleanup failed for session $sessionId", e)
            sendSseEvent(sessionId, "app_error", "❌ Complete cleanup failed: ${e.message}")
        }
    }
}