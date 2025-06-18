package com.vend.fmr.aieng.apis.openai

import com.vend.fmr.aieng.utils.Models
import com.vend.fmr.aieng.utils.env
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service

@Service
class OpenAIAssistant(val client: HttpClient, val json: Json) : Closeable {
    private val openaiApiKey = "OPENAI_API_KEY".env()

    suspend fun uploadFile(
        fileBytes: ByteArray,
        filename: String,
        purpose: String = "assistants",
        debug: Boolean = false
    ): FileUploadResponse {
        val response = client.submitFormWithBinaryData(
            url = "https://api.openai.com/v1/files",
            formData = formData {
                append("purpose", purpose)
                append("file", fileBytes, Headers.build {
                    append(HttpHeaders.ContentType, "text/plain")
                    append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                })
            }
        ) {
            headers {
                append("Authorization", "Bearer $openaiApiKey")
            }
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("File Upload Status: ${response.status}")
            println("Response: $responseText")
        }

        if (!response.status.isSuccess()) {
            throw Exception("File upload failed with status ${response.status}: $responseText")
        }

        return json.decodeFromString<FileUploadResponse>(responseText)
    }

    suspend fun createVectorStore(
        name: String,
        fileIds: List<String>,
        debug: Boolean = false
    ): VectorStoreResponse {
        val request = VectorStoreRequest(
            name = name,
            fileIds = fileIds
        )

        val response = client.post("https://api.openai.com/v1/vector_stores") {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $openaiApiKey")
                append("OpenAI-Beta", "assistants=v2")
            }
            setBody(request)
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("Vector Store Creation Status: ${response.status}")
            println("Response: $responseText")
        }

        if (!response.status.isSuccess()) {
            throw Exception("Vector store creation failed with status ${response.status}: $responseText")
        }

        return json.decodeFromString<VectorStoreResponse>(responseText)
    }

    suspend fun createAssistant(
        name: String,
        instructions: String,
        model: String = Models.Defaults.CHAT_COMPLETION,
        tools: List<AssistantTool> = listOf(AssistantTool("file_search")),
        vectorStoreIds: List<String>? = null,
        debug: Boolean = false
    ): AssistantResponse {
        val toolResources = vectorStoreIds?.let {
            ToolResources(fileSearch = FileSearchResource(vectorStoreIds = it))
        }
        
        val request = AssistantRequest(
            model = model,
            name = name,
            instructions = instructions,
            tools = tools,
            toolResources = toolResources
        )

        val response = client.post("https://api.openai.com/v1/assistants") {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $openaiApiKey")
                append("OpenAI-Beta", "assistants=v2")
            }
            setBody(request)
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("Assistant Creation Status: ${response.status}")
            println("Response: $responseText")
        }

        if (!response.status.isSuccess()) {
            throw Exception("Assistant creation failed with status ${response.status}: $responseText")
        }

        return json.decodeFromString<AssistantResponse>(responseText)
    }

    suspend fun createThread(
        messages: List<ThreadMessage>? = null,
        debug: Boolean = false
    ): ThreadResponse {
        val request = ThreadRequest(messages = messages)

        val response = client.post("https://api.openai.com/v1/threads") {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $openaiApiKey")
                append("OpenAI-Beta", "assistants=v2")
            }
            setBody(request)
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("Thread Creation Status: ${response.status}")
            println("Response: $responseText")
        }

        if (!response.status.isSuccess()) {
            throw Exception("Thread creation failed with status ${response.status}: $responseText")
        }

        return json.decodeFromString<ThreadResponse>(responseText)
    }

    suspend fun addMessageToThread(
        threadId: String,
        content: String,
        role: String = "user",
        debug: Boolean = false
    ): AssistantMessage {
        val message = ThreadMessage(role = role, content = content)

        val response = client.post("https://api.openai.com/v1/threads/$threadId/messages") {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $openaiApiKey")
                append("OpenAI-Beta", "assistants=v2")
            }
            setBody(message)
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("Add Message Status: ${response.status}")
            println("Response: $responseText")
        }

        if (!response.status.isSuccess()) {
            throw Exception("Add message failed with status ${response.status}: $responseText")
        }

        return json.decodeFromString<AssistantMessage>(responseText)
    }

    suspend fun runAssistant(
        threadId: String,
        assistantId: String,
        debug: Boolean = false
    ): RunResponse {
        val request = RunRequest(assistantId = assistantId)

        val response = client.post("https://api.openai.com/v1/threads/$threadId/runs") {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $openaiApiKey")
                append("OpenAI-Beta", "assistants=v2")
            }
            setBody(request)
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("Run Assistant Status: ${response.status}")
            println("Response: $responseText")
        }

        if (!response.status.isSuccess()) {
            throw Exception("Run assistant failed with status ${response.status}: $responseText")
        }

        return json.decodeFromString<RunResponse>(responseText)
    }

    suspend fun getRunStatus(
        threadId: String,
        runId: String,
        debug: Boolean = false
    ): RunResponse {
        val response = client.get("https://api.openai.com/v1/threads/$threadId/runs/$runId") {
            headers {
                append("Authorization", "Bearer $openaiApiKey")
                append("OpenAI-Beta", "assistants=v2")
            }
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("Run Status: ${response.status}")
            println("Response: $responseText")
        }

        if (!response.status.isSuccess()) {
            throw Exception("Get run status failed with status ${response.status}: $responseText")
        }

        return json.decodeFromString<RunResponse>(responseText)
    }

    suspend fun getMessages(
        threadId: String,
        debug: Boolean = false
    ): MessagesResponse {
        val response = client.get("https://api.openai.com/v1/threads/$threadId/messages") {
            headers {
                append("Authorization", "Bearer $openaiApiKey")
                append("OpenAI-Beta", "assistants=v2")
            }
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("Get Messages Status: ${response.status}")
            println("Response: $responseText")
        }

        if (!response.status.isSuccess()) {
            throw Exception("Get messages failed with status ${response.status}: $responseText")
        }

        return json.decodeFromString<MessagesResponse>(responseText)
    }

    suspend fun deleteFile(
        fileId: String,
        debug: Boolean = false
    ): Boolean {
        val response = client.delete("https://api.openai.com/v1/files/$fileId") {
            headers {
                append("Authorization", "Bearer $openaiApiKey")
            }
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("Delete File Status: ${response.status}")
            println("Response: $responseText")
        }

        return response.status.isSuccess()
    }

    suspend fun deleteAssistant(
        assistantId: String,
        debug: Boolean = false
    ): Boolean {
        val response = client.delete("https://api.openai.com/v1/assistants/$assistantId") {
            headers {
                append("Authorization", "Bearer $openaiApiKey")
                append("OpenAI-Beta", "assistants=v2")
            }
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("Delete Assistant Status: ${response.status}")
            println("Response: $responseText")
        }

        return response.status.isSuccess()
    }

    suspend fun deleteVectorStore(
        vectorStoreId: String,
        debug: Boolean = false
    ): Boolean {
        val response = client.delete("https://api.openai.com/v1/vector_stores/$vectorStoreId") {
            headers {
                append("Authorization", "Bearer $openaiApiKey")
                append("OpenAI-Beta", "assistants=v2")
            }
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("Delete Vector Store Status: ${response.status}")
            println("Response: $responseText")
        }

        return response.status.isSuccess()
    }

    suspend fun listFiles(debug: Boolean = false): FileListResponse {
        val response = client.get("https://api.openai.com/v1/files") {
            headers {
                append("Authorization", "Bearer $openaiApiKey")
            }
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("List Files Status: ${response.status}")
            println("Response: $responseText")
        }

        if (!response.status.isSuccess()) {
            throw Exception("List files failed with status ${response.status}: $responseText")
        }

        val list = json.decodeFromString<FileListResponse>(responseText)
        return list.copy(data = list.data.filter { it.isFmr })
    }

    suspend fun listAssistants(debug: Boolean = false): AssistantListResponse {
        val response = client.get("https://api.openai.com/v1/assistants") {
            headers {
                append("Authorization", "Bearer $openaiApiKey")
                append("OpenAI-Beta", "assistants=v2")
            }
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("List Assistants Status: ${response.status}")
            println("Response: $responseText")
        }

        if (!response.status.isSuccess()) {
            throw Exception("List assistants failed with status ${response.status}: $responseText")
        }

        val list = json.decodeFromString<AssistantListResponse>(responseText)
        return list.copy(data = list.data.filter { it.isFmr })
    }

    suspend fun listVectorStores(debug: Boolean = false): VectorStoreListResponse {
        val response = client.get("https://api.openai.com/v1/vector_stores") {
            headers {
                append("Authorization", "Bearer $openaiApiKey")
                append("OpenAI-Beta", "assistants=v2")
            }
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("List Vector Stores Status: ${response.status}")
            println("Response: $responseText")
        }

        if (!response.status.isSuccess()) {
            throw Exception("List vector stores failed with status ${response.status}: $responseText")
        }

        val list = json.decodeFromString<VectorStoreListResponse>(responseText)
        return list.copy(data = list.data.filter { it.isFmr })
    }

    override fun close() {
        client.close()
    }
}