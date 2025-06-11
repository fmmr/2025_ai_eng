package com.vend.fmr.aieng.agents

/**
 * Base interface for all trip planning agents
 */
interface TripPlanningAgent {
    val agentName: String
    suspend fun process(destination: String): AgentResult
}

/**
 * Research Agent - Gathers destination information
 */
class ResearchAgent : TripPlanningAgent {
    override val agentName = "Research Agent"
    
    override suspend fun process(destination: String): AgentResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            // Simulate API call delay
            kotlinx.coroutines.delay(50)

            val researchInfo = getDestinationInfo(destination)
            
            val insights = generateInsights(researchInfo)
            val recommendations = generateRecommendations(researchInfo)
            
            AgentResult(
                agentName = agentName,
                executionTimeMs = System.currentTimeMillis() - startTime,
                success = true,
                data = researchInfo,
                insights = insights,
                recommendations = recommendations
            )
        } catch (e: Exception) {
            AgentResult(
                agentName = agentName,
                executionTimeMs = System.currentTimeMillis() - startTime,
                success = false,
                error = e.message,
                insights = listOf("⚠️ Unable to gather destination research"),
                recommendations = listOf("Research destination independently before traveling")
            )
        }
    }
    
    private fun getDestinationInfo(destination: String): ResearchInfo {
        // Mock data - will be replaced with real API later
        return when (destination.lowercase()) {
            "stockholm", "sweden" -> ResearchInfo(
                cityName = "Stockholm",
                country = "Sweden", 
                description = "Sweden's capital built on 14 islands, known for its Nordic design, rich history, and beautiful archipelago.",
                topAttractions = listOf("Gamla Stan (Old Town)", "Vasa Museum", "Royal Palace", "ABBA Museum", "Archipelago boat tours"),
                culturalNotes = "Swedes value punctuality, personal space, and environmental consciousness. Tipping is not mandatory.",
                bestTimeToVisit = "May-September for warm weather, December-February for winter activities"
            )
            "oslo", "norway" -> ResearchInfo(
                cityName = "Oslo",
                country = "Norway",
                description = "Norway's capital surrounded by forests and fjords, famous for its museums, parks, and modern architecture.",
                topAttractions = listOf("Vigeland Park", "Oslo Opera House", "Munch Museum", "Akershus Fortress", "Norwegian Folk Museum"),
                culturalNotes = "Norwegians appreciate nature, equality, and work-life balance. English is widely spoken.",
                bestTimeToVisit = "June-August for midnight sun, December-March for Northern Lights"
            )
            "copenhagen", "denmark" -> ResearchInfo(
                cityName = "Copenhagen",
                country = "Denmark",
                description = "Denmark's charming capital known for its bike culture, colorful harbors, and world-class cuisine.",
                topAttractions = listOf("Nyhavn", "Tivoli Gardens", "The Little Mermaid", "Rosenborg Castle", "Freetown Christiania"),
                culturalNotes = "Danes value hygge (coziness), cycling, and sustainable living. Very bike-friendly city.",
                bestTimeToVisit = "April-October for mild weather and long days"
            )
            else -> ResearchInfo(
                cityName = destination,
                country = "Unknown",
                description = "A wonderful destination waiting to be explored!",
                topAttractions = listOf("Local landmarks", "Cultural sites", "Historic areas", "Natural attractions"),
                culturalNotes = "Every destination has its unique culture and customs to discover.",
                bestTimeToVisit = "Research the best time to visit based on weather and local events"
            )
        }
    }
    
    private fun generateInsights(info: ResearchInfo): List<String> {
        return buildList {
            add("🏤 ${info.cityName} is known for: ${info.topAttractions.take(2).joinToString(" and ")}")
            add("👥 Cultural tip: ${info.culturalNotes.take(50)}...")
            add("📅 Optimal timing: ${info.bestTimeToVisit}")
            if (info.topAttractions.size > 3) {
                add("🎆 This destination has ${info.topAttractions.size} major attractions to explore")
            }
        }
    }
    
    private fun generateRecommendations(info: ResearchInfo): List<String> {
        return buildList {
            add("Plan at least 2-3 days to see the main attractions")
            add("Research local customs before visiting: ${info.culturalNotes.take(30)}...")
            if (info.bestTimeToVisit.contains("summer", ignoreCase = true) || info.bestTimeToVisit.contains("May-September", ignoreCase = true)) {
                add("Book accommodations early during peak season")
            }
            add("Consider purchasing a city pass for ${info.topAttractions.size} attractions")
        }
    }
}