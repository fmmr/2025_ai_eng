package com.vend.fmr.aieng.agents

import com.vend.fmr.aieng.apis.openai.OpenAI
import com.vend.fmr.aieng.apis.weather.Weather
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service

/**
 * Multi-Agent Trip Planning Coordinator
 * Orchestrates multiple agents working in parallel to create comprehensive trip plans
 */
@Service
class TripPlanningCoordinator(
    private val weatherService: Weather,
    private val openAI: OpenAI
) {

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

            // Launch all agents in parallel with immediate progress reporting
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

            // AI-powered synthesis of comprehensive trip plan
            progressCallback?.invoke("🤖 AI Coordinator: Analyzing all agent data and creating intelligent trip plan...")
            @Suppress("UNCHECKED_CAST")
            val tripPlan = synthesizeResults(
                destination = destination,
                researchInfo = researchResult.data as? ResearchInfo,
                weatherInfo = weatherResult.data as? WeatherInfo,
                activities = activityResult.data as? List<Activity> ?: emptyList(),
                restaurants = foodResult.data as? List<Restaurant> ?: emptyList(),
                agentResults = results,
                progressCallback = progressCallback
            )

            progressCallback?.invoke("🎉 Trip plan complete! Generated ${tripPlan.activities.size} activities and ${tripPlan.restaurants.size} restaurant recommendations")

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
                    "AI synthesis unavailable - Trip to $destination with ${activities.size} activities and ${restaurants.size} restaurants."

                }
            }

            val timelineDeferred = async {
                try {
                    generateAITimeline(destination, agentDataSummary).also {
                        progressCallback?.invoke("🧠 AI: DONE: timeline... ${it.size} items")
                    }
                } catch (e: Exception) {
                    listOf(
                        TimelineItem("Morning (9:00 AM)", "Start your adventure", "AI timeline generation failed")
                    )
                }
            }

            // Wait for both to complete and return as pair
            Pair(summaryDeferred.await(), timelineDeferred.await())
        }

        progressCallback?.invoke("✨ AI: Finalizing comprehensive trip plan...")

        return TripPlan(
            destination = destination,
            researchInfo = researchInfo ?: getDefaultResearchInfo(destination),
            weatherInfo = weatherInfo ?: getDefaultWeatherInfo(),
            activities = activities,
            restaurants = restaurants,
            summary = aiSummary,
            timeline = aiTimeline
        )
    }

    /**
     * Generate AI-powered trip summary
     */
    private suspend fun generateAISummary(destination: String, agentData: String): String {
        val systemPrompt = """
            You are an expert travel advisor creating compelling trip summaries for web display. 
            Based on the comprehensive agent data provided, create a well-structured, engaging summary for a trip.
            
            Format your response as clean HTML with emojis and Bootstrap classes. Structure it as:
            - Brief destination overview in a paragraph
            - Weather section with appropriate styling
            - Top activities in a list
            - Restaurant recommendations in a list
            - Cultural tips in a highlighted box
            - Agent performance note
            
            Use proper HTML tags like <h4>, <p>, <ul>, <li>, <div class="alert alert-info">, etc.
            Keep it concise but informative, around 200-300 words.
            Make it visually appealing and easy to read.
        """.trimIndent()

        val userPrompt = """
            Please create an engaging trip summary for $destination based on this agent data:
            
            $agentData
        """.trimIndent()

        val response = openAI.createChatCompletion(
            systemMessage = systemPrompt,
            prompt = userPrompt,
            maxTokens = 500,
            temperature = 0.7
        )

        return response.choices.firstOrNull()?.message?.content?.toString() ?: "Summary generation failed"
    }

    /**
     * Generate AI-powered timeline
     */
    private suspend fun generateAITimeline(destination: String, agentData: String): List<TimelineItem> {
        val systemPrompt = """
            You are an expert trip planner creating optimal daily itineraries.
            Based on the provided data, create a realistic timeline considering:
            - Weather conditions and seasonal factors
            - Activity types (indoor/outdoor)
            - Logical flow and travel time
            - Energy levels throughout the day
            - Cost distribution
            
            Return ONLY a JSON array of timeline items in this exact format:
            [{"time":"Morning (9:00 AM)","activity":"Activity Name","notes":"Brief explanation"}]
            
            Create 3-5 timeline items covering a full day.
        """.trimIndent()

        val userPrompt = """
            Create an optimal timeline for $destination:
            
            $agentData
        """.trimIndent()

        val response = openAI.createChatCompletion(
            systemMessage = systemPrompt,
            prompt = userPrompt,
            maxTokens = 400,
            temperature = 0.5
        )

        val jsonResponse = response.choices.firstOrNull()?.message?.content?.toString() ?: "[]"

        return try {
            // Parse AI response as JSON
            val json = Json { ignoreUnknownKeys = true }
            json.decodeFromString<List<TimelineItem>>(jsonResponse)
        } catch (e: Exception) {
            // Fallback to a simple timeline if parsing fails
            listOf(
                TimelineItem("Morning (9:00 AM)", "Start your adventure", "AI timeline generation failed - using fallback")
            )
        }
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