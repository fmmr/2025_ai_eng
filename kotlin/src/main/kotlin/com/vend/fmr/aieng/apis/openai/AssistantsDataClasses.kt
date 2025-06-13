package com.vend.fmr.aieng.apis.openai

import com.vend.fmr.aieng.utils.Models
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object FmrConstants {
    const val PREFIX = "fmr"
}

typealias FileUploadResponse = FileData

@Serializable
data class AssistantRequest(
    val model: String = Models.Defaults.CHAT_COMPLETION,
    val name: String? = null,
    val description: String? = null,
    val instructions: String? = null,
    val tools: List<AssistantTool>? = null,
    @SerialName("tool_resources") val toolResources: ToolResources? = null,
    val metadata: Map<String, String>? = null
)

@Serializable
data class ToolResources(
    @SerialName("file_search") val fileSearch: FileSearchResource? = null
)

@Serializable
data class FileSearchResource(
    @SerialName("vector_store_ids") val vectorStoreIds: List<String>? = null
)

@Serializable
data class VectorStoreRequest(
    val name: String? = null,
    @SerialName("file_ids") val fileIds: List<String>? = null,
    val metadata: Map<String, String>? = null
)

@Serializable
data class ExpiresAfter(
    val anchor: String,
    val days: Int
)

@Serializable
data class VectorStoreResponse(
    val id: String,
    val `object`: String,
    @SerialName("created_at") val createdAt: Long,
    val name: String? = null,
    @SerialName("usage_bytes") val usageBytes: Long,
    @SerialName("file_counts") val fileCounts: FileCounts,
    val status: String,
    @SerialName("expires_after") val expiresAfter: ExpiresAfter? = null,
    @SerialName("expires_at") val expiresAt: Long? = null,
    @SerialName("last_active_at") val lastActiveAt: Long? = null,
    val metadata: Map<String, String>? = null
) {
    val createdDate = Instant.ofEpochSecond(createdAt)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))

    val expiresInfo = expiresAfter?.let { "${it.anchor}+${it.days}d" } ?: "Never"
    val statusColor = if (status == "completed") "success" else "warning"
    val isFmr = name?.startsWith(FmrConstants.PREFIX) == true


    fun toSSEHtml(): String {
        val ownershipBadge = if (isFmr) {
            "<span class=\"badge bg-success ms-1\">FMR</span>"
        } else {
            "<span class=\"badge bg-secondary ms-1\">Other</span>"
        }
        
        return """
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <strong>📚 ${name ?: "Unnamed"}</strong>$ownershipBadge
                        <div class="small text-muted">${id}</div>
                    </div>
                    <div class="text-end">
                        <span class="badge bg-${statusColor} me-1">${status}</span>
                        <span class="badge bg-info me-1">${fileCounts.total} files</span>
                        <div class="small">${createdDate} | Exp: ${expiresInfo}</div>
                    </div>
                </div>
            """.trimIndent()
    }
}

@Serializable
data class FileCounts(
    @SerialName("in_progress") val inProgress: Int,
    val completed: Int,
    val failed: Int,
    val cancelled: Int,
    val total: Int
)

@Serializable
data class AssistantTool(
    val type: String
)

@Serializable
data class AssistantResponse(
    val id: String,
    val `object`: String,
    @SerialName("created_at") val createdAt: Long,
    val name: String? = null,
    val description: String? = null,
    val model: String,
    val instructions: String? = null,
    val tools: List<AssistantTool>? = null,
    @SerialName("file_ids") val fileIds: List<String>? = null,
    val metadata: Map<String, String>? = null
) {
    val createdDate = Instant.ofEpochSecond(createdAt)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
    val isFmr = name?.startsWith(FmrConstants.PREFIX) == true

    fun toSSEHtml(): String {
        val toolsInfo = tools?.joinToString(", ") { it.type } ?: "No tools"
        val ownershipBadge = if (isFmr) {
            "<span class=\"badge bg-success ms-1\">FMR</span>"
        } else {
            "<span class=\"badge bg-secondary ms-1\">Other</span>"
        }
        
        return """
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <strong>🤖 ${name ?: "Unnamed"}</strong>$ownershipBadge
                        <div class="small text-muted">${id}</div>
                    </div>
                    <div class="text-end">
                        <span class="badge bg-primary me-1">${model}</span>
                        <span class="badge bg-secondary me-1">${toolsInfo}</span>
                        <div class="small">${createdDate}</div>
                    </div>
                </div>
            """.trimIndent()
    }
}

@Serializable
data class ThreadRequest(
    val messages: List<ThreadMessage>? = null,
    val metadata: Map<String, String>? = null
)

@Serializable
data class ThreadMessage(
    val role: String = "user",
    val content: String,
    @SerialName("file_ids") val fileIds: List<String>? = null,
    val metadata: Map<String, String>? = null
)

@Serializable
data class ThreadResponse(
    val id: String,
    val `object`: String,
    @SerialName("created_at") val createdAt: Long,
    val metadata: Map<String, String>? = null
)

@Serializable
data class RunRequest(
    @SerialName("assistant_id") val assistantId: String,
    val model: String? = null,
    val instructions: String? = null,
    val tools: List<AssistantTool>? = null,
    val metadata: Map<String, String>? = null
)

@Serializable
data class RunResponse(
    val id: String,
    val `object`: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("thread_id") val threadId: String,
    @SerialName("assistant_id") val assistantId: String,
    val status: String,
    @SerialName("started_at") val startedAt: Long? = null,
    @SerialName("completed_at") val completedAt: Long? = null,
    @SerialName("cancelled_at") val cancelledAt: Long? = null,
    @SerialName("expires_at") val expiresAt: Long? = null,
    @SerialName("failed_at") val failedAt: Long? = null,
    val model: String? = null,
    val instructions: String? = null,
    val tools: List<AssistantTool>? = null,
    @SerialName("file_ids") val fileIds: List<String>? = null,
    val metadata: Map<String, String>? = null,
    @SerialName("last_error") val lastError: String? = null
)

@Serializable
data class MessagesResponse(
    val `object`: String,
    val data: List<AssistantMessage>,
    @SerialName("first_id") val firstId: String?,
    @SerialName("last_id") val lastId: String?,
    @SerialName("has_more") val hasMore: Boolean
)

@Serializable
data class AssistantMessage(
    val id: String,
    val `object`: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("thread_id") val threadId: String,
    val role: String,
    val content: List<MessageContentBlock>,
    @SerialName("assistant_id") val assistantId: String? = null,
    @SerialName("run_id") val runId: String? = null,
    @SerialName("file_ids") val fileIds: List<String>? = null,
    val metadata: Map<String, String>? = null
)

@Serializable
data class MessageContentBlock(
    val type: String,
    val text: TextBlock? = null
)

@Serializable
data class TextBlock(
    val value: String,
    val annotations: List<Annotation>? = null
)

@Serializable
data class Annotation(
    val type: String,
    @SerialName("file_citation") val fileCitation: FileCitation? = null,
    @SerialName("file_path") val filePath: FilePath? = null,
    @SerialName("start_index") val startIndex: Int? = null,
    @SerialName("end_index") val endIndex: Int? = null,
    val text: String? = null
)

@Serializable
data class FileCitation(
    @SerialName("file_id") val fileId: String,
    val quote: String? = null
)

@Serializable
data class FilePath(
    @SerialName("file_id") val fileId: String
)

@Serializable
data class FileListResponse(
    val `object`: String,
    val data: List<FileData>,
    @SerialName("has_more") val hasMore: Boolean
)

@Serializable
data class FileData(
    val id: String,
    val `object`: String,
    val bytes: Int,
    @SerialName("created_at") val createdAt: Long,
    val filename: String,
    val purpose: String,
    val status: String,
    @SerialName("status_details") val statusDetails: String? = null
) {
    val createdAtFormatted: String = Instant.ofEpochSecond(createdAt)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("YYYYMMDD_HHmmss"))

    val sizeKB = bytes / 1024
    val isFmr = filename.startsWith(FmrConstants.PREFIX)

    fun toSSEHtml(): String {
        val ownershipBadge = if (isFmr) {
            "<span class=\"badge bg-success ms-1\">FMR</span>"
        } else {
            "<span class=\"badge bg-secondary ms-1\">Other</span>"
        }
        
        return """
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <strong>📁 ${filename}</strong>$ownershipBadge
                                <div class="small text-muted">${id}</div>
                            </div>
                            <div class="text-end">
                                <span class="badge bg-secondary me-1">${sizeKB}KB</span>
                                <div class="small">${createdAtFormatted}</div>
                            </div>
                        </div>
                    """.trimIndent()

    }
}

@Serializable
data class AssistantListResponse(
    val `object`: String,
    val data: List<AssistantResponse>,
    @SerialName("first_id") val firstId: String?,
    @SerialName("last_id") val lastId: String?,
    @SerialName("has_more") val hasMore: Boolean
)

@Serializable
data class VectorStoreListResponse(
    val `object`: String,
    val data: List<VectorStoreResponse>,
    @SerialName("first_id") val firstId: String?,
    @SerialName("last_id") val lastId: String?,
    @SerialName("has_more") val hasMore: Boolean
)

