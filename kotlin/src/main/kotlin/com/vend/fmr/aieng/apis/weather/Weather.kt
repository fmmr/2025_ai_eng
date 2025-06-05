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
        private const val NOWCAST_URL = "https://api.met.no/weatherapi/nowcast/2.0"
        private const val FORECAST_URL = "https://api.met.no/weatherapi/locationforecast/2.0"
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
        val response = client.get("$NOWCAST_URL/complete") {
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

    suspend fun getLocationForecast(latitude: Double, longitude: Double, debug: Boolean = false): LocationForecastResponse {
        val response = client.get("$FORECAST_URL/compact") {
            parameter("lat", latitude)
            parameter("lon", longitude)
            header("User-Agent", USER_AGENT)
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("Weather Forecast API Status: ${response.status}")
            println("Response: $responseText")
        }

        if (!response.status.isSuccess()) {
            throw Exception("Weather Forecast API request failed with status ${response.status}: $responseText")
        }

        return json.decodeFromString<LocationForecastResponse>(responseText)
    }

    fun getCurrentWeather(weatherResponse: WeatherResponse): WeatherTimeSeries? {
        return weatherResponse.properties.timeseries.firstOrNull()
    }

    fun getCurrentForecast(forecastResponse: LocationForecastResponse): LocationForecastTimeSeries? {
        return forecastResponse.properties.timeseries.firstOrNull()
    }

    fun formatWeatherSummary(weather: WeatherTimeSeries): String {
        val details = weather.data.instant.details
        val forecast = weather.data.next1Hours
        
        val temp = details.airTemperature?.let { "${it}°C" } ?: "N/A"
        val humidity = details.relativeHumidity?.let { "${it}%" } ?: "N/A"
        val windSpeed = details.windSpeed?.let { "${it} m/s" } ?: "N/A"
        val windDirection = details.windFromDirection?.let { "${it}°" } ?: "N/A"
        val precipitation = details.precipitationRate?.let { "${it} mm/h" } ?: "No radar data"
        val condition = forecast?.summary?.symbolCode ?: "unknown"
        
        return """
            Current Weather (Nowcast):
            Temperature: $temp
            Humidity: $humidity
            Wind: $windSpeed from $windDirection
            Precipitation: $precipitation
            Condition: $condition
            Time: ${weather.time}
        """.trimIndent()
    }

    fun formatForecastSummary(forecast: LocationForecastTimeSeries): String {
        val details = forecast.data.instant.details
        val next1h = forecast.data.next1Hours
        
        val temp = details.airTemperature?.let { "${it}°C" } ?: "N/A"
        val humidity = details.relativeHumidity?.let { "${it}%" } ?: "N/A"
        val windSpeed = details.windSpeed?.let { "${it} m/s" } ?: "N/A"
        val windDirection = details.windFromDirection?.let { "${it}°" } ?: "N/A"
        val pressure = details.airPressureAtSeaLevel?.let { "${it} hPa" } ?: "N/A"
        val cloudCover = details.cloudAreaFraction?.let { "${it}%" } ?: "N/A"
        val condition = next1h?.summary?.symbolCode ?: "unknown"
        val precipitation = next1h?.details?.precipitationAmount?.let { "${it} mm" } ?: "0 mm"
        
        return """
            Weather Forecast:
            Temperature: $temp
            Humidity: $humidity
            Wind: $windSpeed from $windDirection
            Pressure: $pressure
            Cloud Cover: $cloudCover
            Next hour precipitation: $precipitation
            Condition: $condition
            Time: ${forecast.time}
        """.trimIndent()
    }

    override fun close() {
        client.close()
    }
}