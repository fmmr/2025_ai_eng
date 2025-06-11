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
            
            val insights = generateActivityInsights(activities, destination)
            val recommendations = generateActivityRecommendations(activities)
            
            AgentResult(
                agentName = agentName,
                executionTimeMs = System.currentTimeMillis() - startTime,
                success = true,
                data = activities,
                insights = insights,
                recommendations = recommendations
            )
        } catch (e: Exception) {
            AgentResult(
                agentName = agentName,
                executionTimeMs = System.currentTimeMillis() - startTime,
                success = false,
                error = e.message,
                insights = listOf("⚠️ Unable to load activity recommendations"),
                recommendations = listOf("Research local activities independently")
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
    
    private fun generateActivityInsights(activities: List<Activity>, destination: String): List<String> {
        return buildList {
            val outdoorCount = activities.count { it.type == "outdoor" }
            val indoorCount = activities.count { it.type == "indoor" }
            val freeCount = activities.count { it.cost == "free" }
            
            add("🎯 ${activities.size} activities available in $destination")
            add("🌳 $outdoorCount outdoor and $indoorCount indoor options")
            if (freeCount > 0) {
                add("🆓 $freeCount free activities available")
            }
            
            val weatherDependent = activities.count { it.weatherDependent }
            if (weatherDependent > activities.size / 2) {
                add("🌦️ Many activities are weather-dependent - have backup plans")
            }
        }
    }
    
    private fun generateActivityRecommendations(activities: List<Activity>): List<String> {
        return buildList {
            val freeActivities = activities.filter { it.cost == "free" }
            if (freeActivities.isNotEmpty()) {
                add("Start with free activities: ${freeActivities.take(2).joinToString(", ") { it.name }}")
            }
            
            val indoorActivities = activities.filter { it.type == "indoor" }
            if (indoorActivities.isNotEmpty()) {
                add("Rainy day options: ${indoorActivities.take(2).joinToString(", ") { it.name }}")
            }
            
            add("Book weather-dependent activities when forecast is good")
            add("Allow 2-4 hours for major activities and attractions")
        }
    }
}