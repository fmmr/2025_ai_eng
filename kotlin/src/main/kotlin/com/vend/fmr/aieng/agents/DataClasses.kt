package com.vend.fmr.aieng.agents

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * Data classes for Multi-Agent Trip Planning
 */

@Serializable
data class TripPlan(
    val destination: String,
    val summary: String,
    val timeline: List<TimelineItem>
)

@Serializable
data class ResearchInfo(
    val cityName: String,
    val country: String,
    val description: String,
    val topAttractions: List<String>,
    val culturalNotes: String,
    val bestTimeToVisit: String
)

@Serializable
data class WeatherInfo(
    val currentConditions: String,
    val forecast: String,
    val recommendedClothing: String,
    val outdoorSuitability: String
)

@Serializable
data class Activity(
    val name: String,
    val type: String, // indoor, outdoor, cultural, adventure
    val description: String,
    val duration: String,
    val weatherDependent: Boolean,
    val cost: String // free, low, medium, high
)

@Serializable
data class Restaurant(
    val name: String,
    val cuisine: String,
    val description: String,
    val priceRange: String,
    val specialty: String
)

@Serializable
data class TimelineItem(
    val time: String,
    val activity: String,
    val notes: String
)

@Serializable
data class AgentResult(
    val agentName: String,
    val executionTimeMs: Long,
    val success: Boolean,
    @Contextual val data: Any? = null,
    val error: String? = null,
    val insights: List<String> = emptyList(),
    val recommendations: List<String> = emptyList()
)
