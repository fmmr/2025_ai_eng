package com.vend.fmr.aieng.agents

import com.vend.fmr.aieng.apis.openai.OpenAI
import com.vend.fmr.aieng.apis.weather.Weather
import com.vend.fmr.aieng.utils.Prompts.aiSummarySystem
import com.vend.fmr.aieng.utils.aiSummaryUser
import com.vend.fmr.aieng.utils.aiTimelineSystem
import com.vend.fmr.aieng.utils.aiTimelineUser
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Multi-Agent Trip Planning Coordinator
 * Orchestrates multiple agents working in parallel to create comprehensive trip plans
 */
@Service
class TripPlanningCoordinator(weatherService: Weather, private val openAI: OpenAI) {

    private val researchAgent = ResearchAgent()
    private val weatherAgent = WeatherAgent(weatherService)
    private val activityAgent = ActivityAgent()
    private val foodAgent = FoodAgent()

    companion object {
        private val logger = LoggerFactory.getLogger(TripPlanningCoordinator::class.java)
    }

    /**
     * Main trip planning function - runs all agents in parallel
     */
    suspend fun planTrip(destination: String, progressCallback: ((String) -> Unit)? = null): TripPlan {
        progressCallback?.invoke("🚀 Launching multi-agent trip planning for $destination...")

        val startTime = System.currentTimeMillis()

        return coroutineScope {
            progressCallback?.invoke("🤖 Deploying 4 specialized agents in parallel...")

            // Launch all agents in parallel with immediate progress reporting
            val researchDeferred = async {
                progressCallback?.invoke("🔍 Research Agent: Gathering destination information...")
                researchAgent.process(destination).also { progressCallback?.invoke("🔍 Research agent: $it") }
            }
            val weatherDeferred = async {
                progressCallback?.invoke("🌤️ Weather Agent: Analyzing weather conditions...")
                weatherAgent.process(destination).also { progressCallback?.invoke("🌤️ Weather Agent: $it") }
            }
            val activityDeferred = async {
                progressCallback?.invoke("🎯 Activity Agent: Finding things to do...")
                activityAgent.process(destination).also { progressCallback?.invoke("🎯 Activity Agent: $it") }
            }
            val foodDeferred = async {
                progressCallback?.invoke("🍽️ Food Agent: Discovering local cuisine...")
                foodAgent.process(destination).also { progressCallback?.invoke("🍽️ Food Agent: $it") }
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

            // AI-powered synthesis of comprehensive trip plan
            progressCallback?.invoke("🤖 AI Coordinator: Analyzing all agent data and creating intelligent trip plan...")
            @Suppress("UNCHECKED_CAST") val tripPlan = synthesizeResults(
                destination = destination,
                researchInfo = researchResult.data as? ResearchInfo,
                weatherInfo = weatherResult.data as? WeatherInfo,
                activities = activityResult.data as? List<Activity> ?: emptyList(),
                restaurants = foodResult.data as? List<Restaurant> ?: emptyList(),
                agentResults = results,
                progressCallback = progressCallback
            )

            progressCallback?.invoke("🎉 Trip plan complete! AI-synthesized comprehensive trip plan ready")

            tripPlan
        }
    }

    /**
     * AI-powered synthesis of all agent results into a comprehensive trip plan
     */
    private suspend fun synthesizeResults(
        destination: String,
        researchInfo: ResearchInfo?,
        weatherInfo: WeatherInfo?,
        activities: List<Activity>,
        restaurants: List<Restaurant>,
        agentResults: List<AgentResult>,
        progressCallback: ((String) -> Unit)? = null
    ): TripPlan {

        // Prepare comprehensive data for AI synthesis
        val agentDataSummary = buildString {
            append("DESTINATION: $destination\n\n")

            researchInfo?.let { info ->
                append("RESEARCH FINDINGS:\n")
                append("City: ${info.cityName}, ${info.country}\n")
                append("Description: ${info.description}\n")
                append("Top Attractions: ${info.topAttractions.joinToString(", ")}\n")
                append("Cultural Notes: ${info.culturalNotes}\n")
                append("Best Time to Visit: ${info.bestTimeToVisit}\n\n")
            }

            weatherInfo?.let { weather ->
                append("WEATHER ANALYSIS:\n")
                append("Current Conditions: ${weather.currentConditions}\n")
                append("Forecast: ${weather.forecast}\n")
                append("Recommended Clothing: ${weather.recommendedClothing}\n")
                append("Outdoor Suitability: ${weather.outdoorSuitability}\n\n")
            }

            if (activities.isNotEmpty()) {
                append("AVAILABLE ACTIVITIES:\n")
                activities.forEach { activity ->
                    append("- ${activity.name} (${activity.type}, ${activity.duration}, ${activity.cost} cost)\n")
                    append("  ${activity.description}\n")
                    append("  Weather dependent: ${activity.weatherDependent}\n")
                }
                append("\n")
            }

            if (restaurants.isNotEmpty()) {
                append("RESTAURANT OPTIONS:\n")
                restaurants.forEach { restaurant ->
                    append("- ${restaurant.name} (${restaurant.cuisine}, ${restaurant.priceRange})\n")
                    append("  ${restaurant.description}\n")
                    append("  Specialty: ${restaurant.specialty}\n")
                }
                append("\n")
            }

            append("AGENT PERFORMANCE:\n")
            agentResults.forEach { result ->
                append("- ${result.agentName}: ${if (result.success) "Success" else "Failed"} (${result.executionTimeMs}ms)\n")
                result.error?.let { append("  Error: $it\n") }
            }
        }

        // Launch both AI calls in parallel
        progressCallback?.invoke("🧠 AI: Generating answer from all our research")

        val (aiSummary, aiTimeline) = coroutineScope {
            val summaryDeferred = async {
                try {
                    generateAISummary(destination, agentDataSummary).also {
                        progressCallback?.invoke("🧠 AI: DONE: trip Summary... ${it.length} chars")
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to generate summary", e)
                    "AI synthesis unavailable - Trip to $destination with ${activities.size} activities and ${restaurants.size} restaurants."
                }
            }

            val timelineDeferred = async {
                try {
                    generateAITimeline(destination, agentDataSummary).also {
                        progressCallback?.invoke("🧠 AI: DONE: timeline... ${it.size} items")
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to generate summary", e)
                    listOf(
                        TimelineItem("Morning (9:00 AM)", "Start your adventure", "AI timeline generation failed")
                    )
                }
            }

            // Wait for both to complete and return as pair
            Pair(summaryDeferred.await(), timelineDeferred.await())
        }

        progressCallback?.invoke("✨ AI: Finalizing comprehensive trip plan...")

        return TripPlan(destination, aiSummary, aiTimeline)
    }

    /**
     * Generate AI-powered trip summary
     */
    private suspend fun generateAISummary(destination: String, agentData: String): String {
        val systemPrompt = aiSummarySystem(destination)
        val userPrompt = aiSummaryUser(destination, agentData)
        val response = openAI.createChatCompletion(
            systemMessage = systemPrompt, prompt = userPrompt, maxTokens = 500, temperature = 0.7
        )
        return response.choices.firstOrNull()?.message?.content?.toString() ?: "Summary generation failed"
    }

    /**
     * Generate AI-powered timeline
     */
    private suspend fun generateAITimeline(destination: String, agentData: String): List<TimelineItem> {
        val systemPrompt = aiTimelineSystem(destination)
        val userPrompt = aiTimelineUser(destination, agentData)
        val response = openAI.createChatCompletion(
            systemMessage = systemPrompt, prompt = userPrompt, maxTokens = 400, temperature = 0.5
        )
        val jsonResponse = response.choices.firstOrNull()?.message?.content?.toString() ?: "[]"

        return try {
            // Parse AI response as JSON
            val json = Json { ignoreUnknownKeys = true }
            json.decodeFromString<List<TimelineItem>>(jsonResponse)
        } catch (e: Exception) {
            logger.warn("unable to decode AI timeline response: $jsonResponse", e)
            listOf(
                TimelineItem("Morning (9:00 AM)", "Start your adventure", "AI timeline generation failed - using fallback")
            )
        }
    }
}