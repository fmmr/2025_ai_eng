package com.vend.fmr.aieng.agents

/**
 * Activity Agent - Suggests activities based on destination and weather
 */
class ActivityAgent : TripPlanningAgent {
    override val agentName = "Activity Agent"
    
    override suspend fun process(destination: String): AgentResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            // Simulate API call delay
            kotlinx.coroutines.delay(800)
            
            val activities = getActivitiesForDestination(destination)
            
            AgentResult(
                agentName = agentName,
                executionTimeMs = System.currentTimeMillis() - startTime,
                success = true,
                data = activities
            )
        } catch (e: Exception) {
            AgentResult(
                agentName = agentName,
                executionTimeMs = System.currentTimeMillis() - startTime,
                success = false,
                error = e.message
            )
        }
    }
    
    private fun getActivitiesForDestination(destination: String): List<Activity> {
        // Mock data - will be replaced with real API later
        return when (destination.lowercase()) {
            "stockholm", "sweden" -> listOf(
                Activity(
                    name = "Explore Gamla Stan",
                    type = "cultural",
                    description = "Walk through Stockholm's charming old town with cobblestone streets and colorful buildings",
                    duration = "2-3 hours",
                    weatherDependent = true,
                    cost = "free"
                ),
                Activity(
                    name = "Visit Vasa Museum",
                    type = "indoor",
                    description = "See the world's only preserved 17th-century ship",
                    duration = "1-2 hours", 
                    weatherDependent = false,
                    cost = "medium"
                ),
                Activity(
                    name = "Archipelago Boat Tour",
                    type = "outdoor",
                    description = "Cruise through Stockholm's beautiful archipelago",
                    duration = "3-4 hours",
                    weatherDependent = true,
                    cost = "high"
                ),
                Activity(
                    name = "ABBA Museum",
                    type = "indoor",
                    description = "Interactive museum dedicated to Sweden's most famous pop group",
                    duration = "1-2 hours",
                    weatherDependent = false,
                    cost = "medium"
                )
            )
            "oslo", "norway" -> listOf(
                Activity(
                    name = "Vigeland Sculpture Park",
                    type = "outdoor",
                    description = "World's largest sculpture park by a single artist",
                    duration = "2-3 hours",
                    weatherDependent = true,
                    cost = "free"
                ),
                Activity(
                    name = "Oslo Opera House",
                    type = "cultural",
                    description = "Walk on the roof of this architectural masterpiece",
                    duration = "1-2 hours",
                    weatherDependent = false,
                    cost = "free"
                ),
                Activity(
                    name = "Munch Museum",
                    type = "indoor",
                    description = "Home to Edvard Munch's famous 'The Scream'",
                    duration = "1-2 hours",
                    weatherDependent = false,
                    cost = "medium"
                ),
                Activity(
                    name = "Akershus Fortress",
                    type = "cultural",
                    description = "Medieval fortress with stunning fjord views",
                    duration = "1-2 hours",
                    weatherDependent = true,
                    cost = "low"
                )
            )
            "copenhagen", "denmark" -> listOf(
                Activity(
                    name = "Nyhavn Harbor",
                    type = "outdoor",
                    description = "Iconic colorful harbor with cafes and boat tours",
                    duration = "1-2 hours",
                    weatherDependent = true,
                    cost = "free"
                ),
                Activity(
                    name = "Tivoli Gardens",
                    type = "outdoor",
                    description = "Historic amusement park in the heart of the city",
                    duration = "3-4 hours",
                    weatherDependent = true,
                    cost = "high"
                ),
                Activity(
                    name = "Rosenborg Castle",
                    type = "indoor",
                    description = "Renaissance castle housing the Crown Jewels",
                    duration = "1-2 hours",
                    weatherDependent = false,
                    cost = "medium"
                ),
                Activity(
                    name = "Bike Tour",
                    type = "outdoor",
                    description = "Explore Copenhagen like a local on two wheels",
                    duration = "2-3 hours",
                    weatherDependent = true,
                    cost = "low"
                )
            )
            else -> listOf(
                Activity(
                    name = "City Walking Tour",
                    type = "outdoor",
                    description = "Explore the main sights and get oriented",
                    duration = "2-3 hours",
                    weatherDependent = true,
                    cost = "low"
                ),
                Activity(
                    name = "Local Museum Visit",
                    type = "indoor",
                    description = "Learn about local history and culture",
                    duration = "1-2 hours",
                    weatherDependent = false,
                    cost = "medium"
                ),
                Activity(
                    name = "Central Park/Gardens",
                    type = "outdoor",
                    description = "Relax in the city's main green space",
                    duration = "1-2 hours",
                    weatherDependent = true,
                    cost = "free"
                )
            )
        }
    }
}