package com.vend.fmr.aieng.apis.geolocation

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

@Suppress("unused")
class Geolocation : Closeable {
    
    companion object {
        private const val BASE_URL = "https://ipapi.co"
        private const val USER_AGENT = "AI-Engineering-Course/1.0 (contact@rodland.no)"
        
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.NONE
        }
    }

    suspend fun getLocationByIp(ipAddress: String, debug: Boolean = false): GeolocationResponse {
        val response = client.get("$BASE_URL/$ipAddress/json/") {
            header("User-Agent", USER_AGENT)
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("Geolocation API Status: ${response.status}")
            println("Response: $responseText")
        }

        if (!response.status.isSuccess()) {
            throw Exception("Geolocation API request failed with status ${response.status}: $responseText")
        }

        // Check if response is an error response
        try {
            val errorResponse = json.decodeFromString<GeolocationError>(responseText)
            if (errorResponse.error) {
                throw Exception("Geolocation API error: ${errorResponse.reason} - ${errorResponse.message}")
            }
        } catch (e: Exception) {
            // If it's not an error response, continue with normal parsing
            if (debug) {
                println("Not an error response, continuing with normal parsing")
            }
        }

        return json.decodeFromString<GeolocationResponse>(responseText)
    }

    fun formatLocationSummary(location: GeolocationResponse): String {
        return """
            Location Information:
            IP: ${location.ip}
            City: ${location.city ?: "Unknown"}, ${location.region ?: "Unknown"}
            Country: ${location.countryName ?: "Unknown"} (${location.countryCode ?: "Unknown"})
            Coordinates: ${location.latitude ?: "Unknown"}, ${location.longitude ?: "Unknown"}
            Timezone: ${location.timezone ?: "Unknown"} (${location.utcOffset ?: "Unknown"})
            ISP: ${location.org ?: "Unknown"}
            Currency: ${location.currencyName ?: "Unknown"} (${location.currency ?: "Unknown"})
        """.trimIndent()
    }

    override fun close() {
        client.close()
    }
}