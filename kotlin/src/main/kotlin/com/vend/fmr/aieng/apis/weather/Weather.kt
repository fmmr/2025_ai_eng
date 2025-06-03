package com.vend.fmr.aieng.apis.weather

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
class Weather : Closeable {
    
    companion object {
        private const val BASE_URL = "https://api.met.no/weatherapi/nowcast/2.0"
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

    suspend fun getNowcast(latitude: Double, longitude: Double, debug: Boolean = false): WeatherResponse {
        val response = client.get("$BASE_URL/complete") {
            parameter("lat", latitude)
            parameter("lon", longitude)
            header("User-Agent", USER_AGENT)
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("Weather API Status: ${response.status}")
            println("Response: $responseText")
        }

        if (!response.status.isSuccess()) {
            throw Exception("Weather API request failed with status ${response.status}: $responseText")
        }

        return json.decodeFromString<WeatherResponse>(responseText)
    }

    fun getCurrentWeather(weatherResponse: WeatherResponse): WeatherTimeSeries? {
        return weatherResponse.properties.timeseries.firstOrNull()
    }

    fun formatWeatherSummary(weather: WeatherTimeSeries): String {
        val details = weather.data.instant.details
        val forecast = weather.data.next1Hours
        
        val temp = details.airTemperature?.let { "${it}°C" } ?: "N/A"
        val humidity = details.relativeHumidity?.let { "${it}%" } ?: "N/A"
        val windSpeed = details.windSpeed?.let { "${it} m/s" } ?: "N/A"
        val windDirection = details.windFromDirection?.let { "${it}°" } ?: "N/A"
        val precipitation = details.precipitationRate.let { "${it} mm/h" }
        val condition = forecast?.summary?.symbolCode ?: "unknown"
        
        return """
            Current Weather:
            Temperature: $temp
            Humidity: $humidity
            Wind: $windSpeed from $windDirection
            Precipitation: $precipitation
            Condition: $condition
            Time: ${weather.time}
        """.trimIndent()
    }

    override fun close() {
        client.close()
    }
}