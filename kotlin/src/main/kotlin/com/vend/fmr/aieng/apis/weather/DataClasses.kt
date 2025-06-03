package com.vend.fmr.aieng.apis.weather

import kotlinx.serialization.*

@Serializable
data class WeatherResponse(
    val type: String,
    val geometry: WeatherGeometry,
    val properties: WeatherProperties
)

@Serializable
data class WeatherGeometry(
    val type: String,
    val coordinates: List<Double>
)

@Serializable
data class WeatherProperties(
    val meta: WeatherMeta,
    val timeseries: List<WeatherTimeSeries>
)

@Serializable
data class WeatherMeta(
    @SerialName("updated_at") val updatedAt: String,
    val units: WeatherUnits,
    @SerialName("radar_coverage") val radarCoverage: String
)

@Serializable
data class WeatherUnits(
    @SerialName("air_temperature") val airTemperature: String,
    @SerialName("precipitation_amount") val precipitationAmount: String,
    @SerialName("precipitation_rate") val precipitationRate: String,
    @SerialName("relative_humidity") val relativeHumidity: String,
    @SerialName("wind_from_direction") val windFromDirection: String,
    @SerialName("wind_speed") val windSpeed: String,
    @SerialName("wind_speed_of_gust") val windSpeedOfGust: String
)

@Serializable
data class WeatherTimeSeries(
    val time: String,
    val data: WeatherData
)

@Serializable
data class WeatherData(
    val instant: WeatherInstant,
    @SerialName("next_1_hours") val next1Hours: WeatherForecast? = null
)

@Serializable
data class WeatherInstant(
    val details: WeatherDetails
)

@Serializable
data class WeatherDetails(
    @SerialName("air_temperature") val airTemperature: Double? = null,
    @SerialName("precipitation_rate") val precipitationRate: Double,
    @SerialName("relative_humidity") val relativeHumidity: Double? = null,
    @SerialName("wind_from_direction") val windFromDirection: Double? = null,
    @SerialName("wind_speed") val windSpeed: Double? = null,
    @SerialName("wind_speed_of_gust") val windSpeedOfGust: Double? = null
)

@Serializable
data class WeatherForecast(
    val summary: WeatherSummary,
    val details: WeatherForecastDetails
)

@Serializable
data class WeatherSummary(
    @SerialName("symbol_code") val symbolCode: String
)

@Serializable
data class WeatherForecastDetails(
    @SerialName("precipitation_amount") val precipitationAmount: Double
)