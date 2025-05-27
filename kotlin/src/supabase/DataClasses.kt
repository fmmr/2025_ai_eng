package supabase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DocumentMatch(
    val id: Long,
    val content: String,
    val similarity: Double
)

@Serializable
data class MatchDocumentsRequest(
    @SerialName("query_embedding") val queryEmbedding: String,
    @SerialName("match_threshold") val matchThreshold: Double,
    @SerialName("match_count") val matchCount: Int
)