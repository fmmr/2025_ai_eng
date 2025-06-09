package com.vend.fmr.aieng.apis.geolocation

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service

@Suppress("unused")
@Service
class Geolocation(val client: HttpClient, val json: Json) : Closeable {
    
    companion object {
        private const val BASE_URL = "https://ipapi.co"
    }

    /**
     * Get location by IP with automatic fallback for local/localhost IPs
     */
    suspend fun getLocationByIpWithFallback(ipAddress: String?, debug: Boolean = false): Pair<GeolocationResponse, String> {
        val ip = when {
            ipAddress.isNullOrBlank() -> "8.8.8.8"
            ipAddress == "0:0:0:0:0:0:0:1" || ipAddress == "127.0.0.1" || ipAddress == "::1" -> "8.8.8.8"
            else -> ipAddress
        }
        
        val location = getLocationByIp(ip, debug)
        val note = if (ip == "8.8.8.8") " (Demo: Using Google DNS location for local testing)" else ""
        
        return location to note
    }

    suspend fun getLocationByIp(ipAddress: String, debug: Boolean = false): GeolocationResponse {
        val response = client.get("$BASE_URL/$ipAddress/json/")

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