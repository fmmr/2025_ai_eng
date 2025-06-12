package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.apis.openai.OpenAIAssistant
import com.vend.fmr.aieng.utils.Demo
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
    val sessionId: String
)

@Controller
@RequestMapping("/demo/assistants-api")
class AssistantsApiController(private val openAIAssistant: OpenAIAssistant) : BaseController(Demo.ASSISTANTS_API) {

    private val logger = LoggerFactory.getLogger(AssistantsApiController::class.java)

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
                    "list" -> processList(request.sessionId)
                    "cleanup-files" -> processCleanupFiles(request.sessionId, session)
                    "cleanup-vector-stores" -> processCleanupVectorStores(request.sessionId, session)
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
                filename = "movies.txt",
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
                sendMessage(sessionId, "🔍 No file in session, checking for existing files...", "progress")
                val files = openAIAssistant.listFiles(debug = false)
                val assistantFiles = files.data.filter { it.purpose == "assistants" }

                if (assistantFiles.isEmpty()) {
                    sendMessage(sessionId, "❌ No files available. Please upload a file first.", "error")
                    return
                }

                fileId = assistantFiles.first().id
                sendMessage(sessionId, "✅ Found existing file: ${assistantFiles.first().filename} (${fileId})", "info")
            } else {
                sendMessage(sessionId, "✅ Using file from session: $fileId", "info")
            }

            sendMessage(sessionId, "🔧 Creating vector store with file...", "progress")

            val vectorStore = openAIAssistant.createVectorStore(
                name = "Movie Database",
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

    private suspend fun processList(sessionId: String) {
        sendMessage(sessionId, "📋 Listing all resources...", "progress")

        try {
            // List Files
            val files = openAIAssistant.listFiles(debug = false)
            val assistantFiles = files.data.filter { it.purpose == "assistants" }

            sendMessage(sessionId, "📁 Files (${assistantFiles.size}):", "info")
            if (assistantFiles.isEmpty()) {
                sendMessage(sessionId, "No files found", "info")
            } else {
                assistantFiles.forEach { file -> sendMessage(sessionId, file.toSSEHtml(), "final_result") }
            }

            // List Vector Stores
            val vectorStores = openAIAssistant.listVectorStores(debug = false)

            sendMessage(sessionId, "📚 Vector Stores (${vectorStores.data.size}):", "info")
            if (vectorStores.data.isEmpty()) {
                sendMessage(sessionId, "No vector stores found", "info")
            } else {
                vectorStores.data.forEach { vs -> sendMessage(sessionId, vs.toSSEHtml(), "final_result") }
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

            val filesToDelete = files.data.filter { it.purpose == "assistants" }

            sendMessage(sessionId, "🔍 Found ${filesToDelete.size} files to delete...", "progress")

            filesToDelete.forEach { file ->
                if (openAIAssistant.deleteFile(file.id, debug = false)) {
                    deletedCount++
                    sendMessage(sessionId, "  ✅ Deleted: ${file.filename}, ${file.id}", "progress")
                } else {
                    sendMessage(sessionId, "  ❌ Failed to delete: ${file.filename}", "progress")
                }
            }

            session.removeAttribute("fileId")
            sendMessage(sessionId, "🧹 File cleanup complete! Deleted $deletedCount files.", "success")

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

            sendMessage(sessionId, "🔍 Found ${vectorStores.data.size} vector stores to delete...", "progress")

            vectorStores.data.forEach { vs ->
                if (openAIAssistant.deleteVectorStore(vs.id, debug = false)) {
                    deletedCount++
                    sendMessage(sessionId, "  ✅ Deleted: ${vs.name ?: "Unnamed"}, ${vs.id}", "progress")
                } else {
                    sendMessage(sessionId, "  ❌ Failed to delete: ${vs.name ?: "Unnamed"}", "progress")
                }
            }

            session.removeAttribute("vectorStoreId")
            sendMessage(sessionId, "🧹 Vector store cleanup complete! Deleted $deletedCount vector stores.", "success")

        } catch (e: Exception) {
            logger.error("Vector store cleanup failed for session $sessionId", e)
            sendMessage(sessionId, "❌ Vector store cleanup failed: ${e.message}", "error")
        }
    }

    private suspend fun processCleanupAll(sessionId: String, session: HttpSession) {
        sendMessage(sessionId, "🗑️ Starting complete cleanup (files + vector stores)...", "progress")

        try {
            // Delete vector stores first
            val vectorStores = openAIAssistant.listVectorStores(debug = false)
            var deletedVS = 0

            sendMessage(sessionId, "📚 Deleting ${vectorStores.data.size} vector stores...", "progress")
            vectorStores.data.forEach { vs ->
                if (openAIAssistant.deleteVectorStore(vs.id, debug = false)) {
                    deletedVS++
                    sendMessage(sessionId, "  ✅ Deleted vector store: ${vs.name ?: "Unnamed"}", "progress")
                }
            }

            // Then delete files
            val files = openAIAssistant.listFiles(debug = false)
            var deletedFiles = 0
            val filesToDelete = files.data.filter { it.purpose == "assistants" }

            sendMessage(sessionId, "📁 Deleting ${filesToDelete.size} files...", "progress")
            filesToDelete.forEach { file ->
                if (openAIAssistant.deleteFile(file.id, debug = false)) {
                    deletedFiles++
                    sendMessage(sessionId, "  ✅ Deleted file: ${file.filename}", "progress")
                }
            }

            session.removeAttribute("fileId")
            session.removeAttribute("vectorStoreId")
            sendMessage(sessionId, "🧹 Complete cleanup done! Deleted $deletedVS vector stores and $deletedFiles files.", "success")

        } catch (e: Exception) {
            logger.error("Complete cleanup failed for session $sessionId", e)
            sendMessage(sessionId, "❌ Complete cleanup failed: ${e.message}", "error")
        }
    }
}