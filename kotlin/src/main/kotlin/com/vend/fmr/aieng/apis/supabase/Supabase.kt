package com.vend.fmr.aieng.apis.supabase

import com.vend.fmr.aieng.utils.env
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service

@Service
class Supabase(val client: HttpClient, val json: Json) : Closeable {
    private val supabaseUrl = "SUPABASE_URL".env()
    private val supabaseKey = "SUPABASE_ANON_KEY".env()

    suspend fun matchDocuments(queryEmbedding: List<Double>, matches: Int = 5): List<DocumentMatch> {
        val embeddingStr = "[${queryEmbedding.joinToString(",")}]"
        val url = "$supabaseUrl/rest/v1/rpc/match_documents"

        val requestBody = MatchDocumentsRequest(
            queryEmbedding = embeddingStr,
            matchThreshold = 0.5,
            matchCount = matches
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

    suspend fun insertDocumentsFromPair(data: List<Pair<String, List<Double>>>): List<Document> {
        return insertDocuments(data.map { it.first to it.second })
    }

    suspend fun insertDocuments(data: List<Pair<String, List<Double>>>): List<Document> {
        val documents = data.map { (content, embedding) ->
            Document(content = content, embedding = "[${embedding.joinToString(",")}]")
        }

        return insertDocumentsInternal(documents)
    }

    private suspend fun insertDocumentsInternal(documents: List<Document>): List<Document> {
        val url = "$supabaseUrl/rest/v1/documents"

        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            headers {
                append("apikey", supabaseKey)
                append("Authorization", "Bearer $supabaseKey")
                append("Prefer", "return=representation")
            }
            setBody(documents)
        }

        val responseText = response.bodyAsText()

        return if (response.status.isSuccess()) {
            json.decodeFromString<List<Document>>(responseText)
        } else {
            throw Exception("Supabase insert failed: $responseText")
        }
    }

    suspend fun clearDocuments() {
        val deleteUrl = "$supabaseUrl/rest/v1/documents?id=gte.0"
        val response = client.delete(deleteUrl) {
            headers {
                append("apikey", supabaseKey)
                append("Authorization", "Bearer $supabaseKey")
            }
        }

        if (!response.status.isSuccess()) {
            val responseText = response.bodyAsText()
            throw Exception("Supabase clear failed: $responseText")
        }
    }

    override fun close() {
        client.close()
    }
}