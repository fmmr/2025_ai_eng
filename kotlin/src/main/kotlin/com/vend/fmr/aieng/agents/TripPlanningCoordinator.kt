package com.vend.fmr.aieng.agents

import com.vend.fmr.aieng.apis.openai.OpenAI
import com.vend.fmr.aieng.apis.weather.Weather
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
class TripPlanningCoordinator(
    weatherService: Weather,
    private val openAI: OpenAI
) {

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

        return TripPlan(
            summary = aiSummary,
            timeline = aiTimeline
        )
    }

    /**
     * Generate AI-powered trip summary
     */
    private suspend fun generateAISummary(destination: String, agentData: String): String {
        val systemPrompt = """
            You are an expert travel advisor with extensive knowledge of global destinations. Create compelling trip summaries for web display.
            
            IMPORTANT: Use your comprehensive knowledge of $destination to create an authentic, detailed trip summary. The agent data is just basic information - you should ENHANCE it with your expertise.
            
            For $destination specifically:
            - Include famous landmarks, neighborhoods, and unique experiences
            - Mention specific local dishes, restaurants types, and food culture
            - Reference actual cultural practices, customs, and etiquette 
            - Suggest realistic activities that match the destination's character
            - Include practical local tips (transportation, tipping, language, etc.)
            - Reference seasonal considerations and optimal timing
            
            IGNORE generic agent data like "Local Bistro" or "City Walking Tour" - replace with REAL destination-specific recommendations.
            
            Format as clean HTML with emojis and Bootstrap 5 classes:
            - Engaging destination intro with flag emoji and specific highlights
            - Weather section (use agent data if available, otherwise seasonal guidance)
            - Top Activities: 4-5 SPECIFIC attractions/experiences for this destination
            - Restaurants: 3-4 REAL cuisine types or famous food areas
            - Cultural Tips: SPECIFIC customs, etiquette, language tips in alert box
            - Local Insights: Transportation, neighborhoods, practical tips in alert box
            
            Boostrap 5 is used for styling, so use Bootstrap classes and HTML tags to format the response.
            Use proper HTML: <h4>, <p>, <ul>, <li>, <div class="alert alert-info">, etc.
            Make it destination-specific and informative, around 300-400 words.
        """.trimIndent()

        val userPrompt = """
            Create a comprehensive trip summary for $destination. Use your extensive knowledge of this destination.
            
            Agent data (basic info only - enhance with your knowledge):
            $agentData
            
            Replace any generic content with REAL $destination recommendations. Make it specific and authentic to this destination.
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
            You are an expert trip planner with deep knowledge of $destination. Create a realistic daily itinerary.
            
            IMPORTANT: Use your knowledge of $destination to suggest REAL places and experiences, not generic activities.
            Include actual landmark names, neighborhoods, restaurant types, and local experiences specific to $destination.
            
            Consider for $destination:
            - Famous attractions and their optimal visiting times
            - Local transportation patterns and travel times
            - Traditional meal times and food culture
            - Weather patterns and seasonal considerations
            - Cultural rhythms (siesta, early dinners, etc.)
            
            IGNORE generic agent data - use your knowledge of what makes $destination unique.
            
            Return ONLY a JSON array in this exact format:
            [{"time":"Morning (9:00 AM)","activity":"Specific Activity/Place Name","notes":"Why this timing works for this destination"}]
            
            Create 4-6 timeline items covering a full day with destination-specific recommendations.
        """.trimIndent()

        val userPrompt = """
            Create a realistic daily itinerary for $destination using your knowledge of this destination.
            Include REAL places, attractions, and experiences specific to $destination.
            
            Agent data (basic reference only):
            $agentData
            
            Focus on authentic $destination experiences with proper timing and flow.
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
            logger.warn("unable to decode AI timeline response: $jsonResponse", e)
            listOf(
                TimelineItem("Morning (9:00 AM)", "Start your adventure", "AI timeline generation failed - using fallback")
            )
        }
    }


}