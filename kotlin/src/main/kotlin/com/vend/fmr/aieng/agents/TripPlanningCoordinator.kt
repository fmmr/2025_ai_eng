package com.vend.fmr.aieng.agents

import com.vend.fmr.aieng.apis.weather.Weather
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

/**
 * Multi-Agent Trip Planning Coordinator
 * Orchestrates multiple agents working in parallel to create comprehensive trip plans
 */
@Service
class TripPlanningCoordinator(weatherService: Weather) {

    private val researchAgent = ResearchAgent()
    private val weatherAgent = WeatherAgent(weatherService)
    private val activityAgent = ActivityAgent()
    private val foodAgent = FoodAgent()

    /**
     * Main trip planning function - runs all agents in parallel
     */
    suspend fun planTrip(destination: String, progressCallback: ((String) -> Unit)? = null): TripPlan {
        progressCallback?.invoke("🚀 Launching multi-agent trip planning for $destination...")

        val startTime = System.currentTimeMillis()

        return coroutineScope {
            progressCallback?.invoke("🤖 Deploying 4 specialized agents in parallel...")

            // Launch all agents in parallel
            val researchDeferred = async {
                progressCallback?.invoke("🔍 Research Agent: Gathering destination information...")
                researchAgent.process(destination)
            }
            val weatherDeferred = async {
                progressCallback?.invoke("🌤️ Weather Agent: Analyzing weather conditions...")
                weatherAgent.process(destination)
            }
            val activityDeferred = async {
                progressCallback?.invoke("🎯 Activity Agent: Finding things to do...")
                activityAgent.process(destination)
            }
            val foodDeferred = async {
                progressCallback?.invoke("🍽️ Food Agent: Discovering local cuisine...")
                foodAgent.process(destination)
            }

            // Wait for all agents to complete
            progressCallback?.invoke("⏳ Waiting for all agents to complete...")
            val results = awaitAll(researchDeferred, weatherDeferred, activityDeferred, foodDeferred)

            val totalTime = System.currentTimeMillis() - startTime
            progressCallback?.invoke("✅ All agents completed in ${totalTime}ms. Synthesizing results...")

            // Extract results
            val researchResult = results[0]
            val weatherResult = results[1]
            val activityResult = results[2]
            val foodResult = results[3]

            // Create comprehensive trip plan
            @Suppress("UNCHECKED_CAST")
            val tripPlan = synthesizeResults(
                destination = destination,
                researchInfo = researchResult.data as? ResearchInfo,
                weatherInfo = weatherResult.data as? WeatherInfo,
                activities = activityResult.data as? List<Activity> ?: emptyList(),
                restaurants = foodResult.data as? List<Restaurant> ?: emptyList(),
                agentResults = results
            )

            progressCallback?.invoke("🎉 Trip plan complete! Generated ${tripPlan.activities.size} activities and ${tripPlan.restaurants.size} restaurant recommendations")

            tripPlan
        }
    }

    /**
     * Synthesize all agent results into a comprehensive trip plan
     */
    private fun synthesizeResults(
        destination: String,
        researchInfo: ResearchInfo?,
        weatherInfo: WeatherInfo?,
        activities: List<Activity>,
        restaurants: List<Restaurant>,
        agentResults: List<AgentResult>
    ): TripPlan {

        // Create intelligent summary based on all agent findings
        val summary = buildString {
            append("🗺️ **Trip to ${researchInfo?.cityName ?: destination}**\n\n")

            researchInfo?.let { info ->
                append("📍 **About ${info.cityName}**: ${info.description}\n\n")
            }

            weatherInfo?.let { weather ->
                append("🌤️ **Weather**: ${weather.currentConditions}\n")
                append("👕 **What to wear**: ${weather.recommendedClothing}\n")
                append("🌳 **Outdoor activities**: ${weather.outdoorSuitability}\n\n")
            }

            if (activities.isNotEmpty()) {
                append("🎯 **Top Activities**: ")
                append(activities.take(3).joinToString(", ") { it.name })
                append("\n\n")
            }

            if (restaurants.isNotEmpty()) {
                append("🍽️ **Must-try Food**: ")
                append(restaurants.take(2).joinToString(", ") { "${it.name} (${it.cuisine})" })
                append("\n\n")
            }

            val successfulAgents = agentResults.count { it.success }
            append("🤖 **Agent Performance**: $successfulAgents/4 agents completed successfully")
        }

        // Create smart timeline based on weather and activity types
        val timeline = createTimeline(activities, weatherInfo)

        return TripPlan(
            destination = destination,
            researchInfo = researchInfo ?: getDefaultResearchInfo(destination),
            weatherInfo = weatherInfo ?: getDefaultWeatherInfo(),
            activities = activities,
            restaurants = restaurants,
            summary = summary,
            timeline = timeline
        )
    }

    /**
     * Create an intelligent timeline based on activities and weather
     */
    private fun createTimeline(activities: List<Activity>, weatherInfo: WeatherInfo?): List<TimelineItem> {
        val timeline = mutableListOf<TimelineItem>()

        // Smart scheduling based on weather
        val isGoodOutdoorWeather = weatherInfo?.outdoorSuitability?.contains("perfect", ignoreCase = true) == true ||
            weatherInfo?.outdoorSuitability?.contains("good", ignoreCase = true) == true

        if (isGoodOutdoorWeather) {
            // Prioritize outdoor activities when weather is good
            activities.filter { it.type == "outdoor" }.take(2).forEachIndexed { index, activity ->
                timeline.add(
                    TimelineItem(
                        time = if (index == 0) "Morning (10:00 AM)" else "Afternoon (2:00 PM)",
                        activity = activity.name,
                        notes = "Great weather for outdoor exploration!"
                    )
                )
            }

            // Add indoor backup
            activities.firstOrNull { it.type == "indoor" }?.let { activity ->
                timeline.add(
                    TimelineItem(
                        time = "Evening (6:00 PM)",
                        activity = activity.name,
                        notes = "Wind down with an indoor activity"
                    )
                )
            }
        } else {
            // Prioritize indoor activities when weather is poor
            activities.filter { it.type == "indoor" }.take(2).forEachIndexed { index, activity ->
                timeline.add(
                    TimelineItem(
                        time = if (index == 0) "Morning (10:00 AM)" else "Afternoon (2:00 PM)",
                        activity = activity.name,
                        notes = "Perfect indoor activity for the weather"
                    )
                )
            }
        }

        return timeline
    }

    private fun getDefaultResearchInfo(destination: String) = ResearchInfo(
        cityName = destination,
        country = "Unknown",
        description = "An exciting destination to explore",
        topAttractions = emptyList(),
        culturalNotes = "Local customs and culture to discover",
        bestTimeToVisit = "Year-round"
    )

    private fun getDefaultWeatherInfo() = WeatherInfo(
        currentConditions = "Variable conditions",
        forecast = "Check local weather before your trip",
        recommendedClothing = "Comfortable layers",
        outdoorSuitability = "Prepare for changing conditions"
    )
}