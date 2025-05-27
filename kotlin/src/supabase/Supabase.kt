package supabase

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.json.Json

class Supabase(private val supabaseUrl: String, private val supabaseKey: String) : Closeable {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }

        install(Logging) {
            level = LogLevel.NONE
        }
    }

    suspend fun matchDocuments(queryEmbedding: List<Double>): List<DocumentMatch> {
        val embeddingStr = "[${queryEmbedding.joinToString(",")}]"
        val url = "$supabaseUrl/rest/v1/rpc/match_documents"

        val requestBody = MatchDocumentsRequest(
            queryEmbedding = embeddingStr,
            matchThreshold = 0.5,
            matchCount = 5
        )

        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            headers {
                append("apikey", supabaseKey)
                append("Authorization", "Bearer $supabaseKey")
            }
            setBody(requestBody)
        }

        val responseText = response.bodyAsText()

        return if (response.status.isSuccess()) {
            json.decodeFromString<List<DocumentMatch>>(responseText)
        } else {
            throw Exception("Supabase request failed: $responseText")
        }
    }

    override fun close() {
        client.close()
    }
}