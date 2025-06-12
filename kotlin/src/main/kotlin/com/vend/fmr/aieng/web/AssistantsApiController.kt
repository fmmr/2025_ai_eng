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
import org.springframework.web.bind.annotation.*

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
                    "list" -> processList(request.sessionId)
                    "cleanup" -> processCleanup(request.sessionId, session)
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
            sendMessage(sessionId, "✅ File uploaded: ${file.id} (${file.bytes} bytes) - created: ${file.createdAtFormatted}", "success")

        } catch (e: Exception) {
            logger.error("File upload failed for session $sessionId", e)
            sendMessage(sessionId, "❌ Upload failed: ${e.message}", "error")
        }
    }

    private suspend fun processList(sessionId: String) {
        sendMessage(sessionId, "📋 Listing files...", "progress")

        try {
            val files = openAIAssistant.listFiles(debug = false)
            val relevantFiles = files.data.filter { 
                it.purpose == "assistants"
            }
            
            sendMessage(sessionId, "📁 Found ${relevantFiles.size} relevant files (${files.data.size} total):", "info")

            relevantFiles.forEach { file ->
                sendMessage(sessionId, "  • ${file.filename} (${file.id}) - ${file.bytes} bytes - created: ${file.createdAtFormatted} - status: ${file.status}", "info")
            }

        } catch (e: Exception) {
            logger.error("File listing failed for session $sessionId", e)
            sendMessage(sessionId, "❌ List failed: ${e.message}", "error")
        }
    }

    private suspend fun processCleanup(sessionId: String, session: HttpSession) {
        sendMessage(sessionId, "🗑️ Starting cleanup...", "progress")

        try {
            val files = openAIAssistant.listFiles(debug = false)
            var deletedCount = 0

            // Only delete files that are relevant to assistants demo
            val filesToDelete = files.data.filter { 
                it.purpose == "assistants"
            }
            
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
            sendMessage(sessionId, "🧹 Cleanup complete! Deleted $deletedCount files.", "success")

        } catch (e: Exception) {
            logger.error("File cleanup failed for session $sessionId", e)
            sendMessage(sessionId, "❌ Cleanup failed: ${e.message}", "error")
        }
    }
}