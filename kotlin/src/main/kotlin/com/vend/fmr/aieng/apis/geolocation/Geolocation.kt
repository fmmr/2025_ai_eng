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

        return json.decodeFromString<GeolocationResponse>(responseText)
    }

    fun formatLocationSummary(location: GeolocationResponse): String {
        return """
            Location Information:
            IP: ${location.ip}
            City: ${location.city}, ${location.region}
            Country: ${location.countryName} (${location.countryCode})
            Coordinates: ${location.latitude}, ${location.longitude}
            Timezone: ${location.timezone} (${location.utcOffset})
            ISP: ${location.org}
            Currency: ${location.currencyName} (${location.currency})
        """.trimIndent()
    }

    override fun close() {
        client.close()
    }
}