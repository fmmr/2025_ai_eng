package com.vend.fmr.aieng.apis.nasa

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class Nasa(private val httpClient: HttpClient, private val json: Json) {

    companion object {
        private const val BASE_URL = "https://api.nasa.gov"
        private val API_KEY = System.getenv("NASA_API_KEY") ?: "DEMO_KEY"
    }

    /**
     * Get Astronomy Picture of the Day
     */
    suspend fun getApod(date: String? = null, debug: Boolean = false): ApodResponse {
        val url = "$BASE_URL/planetary/apod"

        if (debug) {
            println("🚀 NASA APOD Request: $url")
            println("📅 Date: ${date ?: "today"}")
        }

        val response = httpClient.get(url) {
            parameter("api_key", API_KEY)
            if (date != null) {
                parameter("date", date)
            }
        }

        val responseText = response.body<String>()
        if (debug) {
            println("📡 NASA APOD Response: $responseText")
        }

        return json.decodeFromString<ApodResponse>(responseText)
    }

    /**
     * Get a summary of today's Astronomy Picture of the Day
     */
    suspend fun getApodSummary(date: String? = null, debug: Boolean = false): String {
        return try {
            val apod = getApod(date, debug)

            val dateStr = date ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val mediaInfo = if (apod.mediaType == "image") {
                "📸 Image: ${apod.url}"
            } else {
                "🎥 Video: ${apod.url}"
            }

            """
            🌌 NASA Astronomy Picture of the Day ($dateStr)
            
            📡 ${apod.title}
            
            ${apod.explanation.take(300)}${if (apod.explanation.length > 300) "..." else ""}
            
            $mediaInfo
            ${apod.copyright?.let { "© $it" } ?: ""}
            """.trimIndent()

        } catch (e: Exception) {
            if (debug) {
                println("❌ NASA APOD Error: ${e.message}")
            }
            "Sorry, I couldn't retrieve the NASA Astronomy Picture of the Day. Please try again later."
        }
    }

    /**
     * Get Near Earth Objects for today
     */
    suspend fun getNearEarthObjects(startDate: String? = null, endDate: String? = null, debug: Boolean = false): NearEarthObjectsResponse {
        val url = "$BASE_URL/neo/rest/v1/feed"
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        if (debug) {
            println("🚀 NASA NEO Request: $url")
            println("📅 Date range: ${startDate ?: today} to ${endDate ?: today}")
        }

        val response = httpClient.get(url) {
            parameter("api_key", API_KEY)
            parameter("start_date", startDate ?: today)
            parameter("end_date", endDate ?: today)
        }

        val responseText = response.body<String>()
        if (debug) {
            println("📡 NASA NEO Response: $responseText")
        }

        return json.decodeFromString<NearEarthObjectsResponse>(responseText)
    }

    /**
     * Get a summary of Near Earth Objects
     */
    suspend fun getNearEarthObjectsSummary(startDate: String? = null, endDate: String? = null, debug: Boolean = false): String {
        return try {
            val neoResponse = getNearEarthObjects(startDate, endDate, debug)
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val dateStr = startDate ?: today

            val allNeos = neoResponse.nearEarthObjects.values.flatten()
            val hazardousCount = allNeos.count { it.isPotentiallyHazardousAsteroid }

            val topNeos = allNeos.take(3)
            val neoDetails = topNeos.joinToString("\n\n") { neo ->
                val approach = neo.closeApproachData.firstOrNull()
                val distance = approach?.missDistance?.kilometers?.toDoubleOrNull()?.let {
                    String.format("%.0f", it / 1000) + "k km"
                } ?: "unknown distance"
                val velocity = approach?.relativeVelocity?.kilometersPerHour?.toDoubleOrNull()?.let {
                    String.format("%.0f", it / 1000) + "k km/h"
                } ?: "unknown speed"

                "🪨 ${neo.name}\n   Distance: $distance, Speed: $velocity${if (neo.isPotentiallyHazardousAsteroid) " ⚠️ HAZARDOUS" else ""}"
            }

            """
            🌍 Near Earth Objects ($dateStr)
            
            📊 Found ${allNeos.size} objects${if (hazardousCount > 0) ", $hazardousCount potentially hazardous" else ""}
            
            ${if (topNeos.isNotEmpty()) "Top objects:\n$neoDetails" else "No objects found for this date."}
            """.trimIndent()

        } catch (e: Exception) {
            if (debug) {
                println("❌ NASA NEO Error: ${e.message}")
            }
            "Sorry, I couldn't retrieve Near Earth Objects data. Please try again later."
        }
    }
}