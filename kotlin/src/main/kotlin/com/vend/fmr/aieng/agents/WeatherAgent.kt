package com.vend.fmr.aieng.agents

import com.vend.fmr.aieng.apis.weather.Weather

/**
 * Weather Agent - Analyzes weather conditions for trip planning
 */
class WeatherAgent(private val weatherService: Weather) : TripPlanningAgent {
    override val agentName = "Weather Agent"
    
    override suspend fun process(destination: String): AgentResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            // Get coordinates for major cities (would use geocoding API in real implementation)
            val coordinates = getCoordinatesForDestination(destination)
            
            // Get actual weather forecast
            val forecast = weatherService.getForecastSummary(coordinates.first, coordinates.second, debug = false)
            
            val weatherInfo = analyzeWeatherForTrip(forecast)
            
            val insights = generateWeatherInsights(weatherInfo)
            val recommendations = generateWeatherRecommendations(weatherInfo)
            
            AgentResult(
                agentName = agentName,
                executionTimeMs = System.currentTimeMillis() - startTime,
                success = true,
                data = weatherInfo,
                insights = insights,
                recommendations = recommendations
            )
        } catch (_: Exception) {
            // Fallback to mock data if weather service fails
            val mockWeatherInfo = getMockWeatherInfo()
            
            val insights = generateWeatherInsights(mockWeatherInfo)
            val recommendations = generateWeatherRecommendations(mockWeatherInfo)
            
            AgentResult(
                agentName = agentName,
                executionTimeMs = System.currentTimeMillis() - startTime,
                success = true,
                data = mockWeatherInfo,
                insights = insights,
                recommendations = recommendations
            )
        }
    }
    
    private fun getCoordinatesForDestination(destination: String): Pair<Double, Double> {
        // Mock coordinates for major cities - would use geocoding API in real implementation
        return when (destination.lowercase()) {
            "stockholm", "sweden" -> Pair(59.3293, 18.0686)
            "oslo", "norway" -> Pair(59.9139, 10.7522)
            "copenhagen", "denmark" -> Pair(55.6761, 12.5683)
            "paris", "france" -> Pair(48.8566, 2.3522)
            "london", "england", "uk" -> Pair(51.5074, -0.1278)
            "tokyo", "japan" -> Pair(35.6762, 139.6503)
            else -> Pair(59.9139, 10.7522) // Default to Oslo
        }
    }
    
    private fun analyzeWeatherForTrip(forecast: String): WeatherInfo {
        // Extract key info from weather forecast and provide travel recommendations
        val isRainy = forecast.contains("rain", ignoreCase = true) || forecast.contains("shower", ignoreCase = true)
        val isSnowy = forecast.contains("snow", ignoreCase = true)
        val isCold = forecast.contains("cold", ignoreCase = true) || forecast.matches(Regex(".*-?\\d+°.*")) 
        val isSunny = forecast.contains("sun", ignoreCase = true) || forecast.contains("clear", ignoreCase = true)
        
        val recommendedClothing = when {
            isSnowy -> "Winter jacket, warm layers, waterproof boots, hat and gloves"
            isRainy -> "Waterproof jacket, umbrella, comfortable walking shoes"
            isCold -> "Warm jacket, layers, comfortable shoes"
            isSunny -> "Light layers, sunglasses, sunscreen"
            else -> "Comfortable clothes suitable for changing conditions"
        }
        
        val outdoorSuitability = when {
            isSnowy -> "Limited outdoor activities recommended, great for winter sports"
            isRainy -> "Indoor activities preferred, brief outdoor visits with rain gear"
            isSunny -> "Perfect for outdoor activities and sightseeing"
            else -> "Suitable for most activities with proper preparation"
        }
        
        return WeatherInfo(
            currentConditions = forecast.take(100) + "...",
            forecast = forecast,
            recommendedClothing = recommendedClothing,
            outdoorSuitability = outdoorSuitability
        )
    }
    
    private fun getMockWeatherInfo(): WeatherInfo {
        return WeatherInfo(
            currentConditions = "Partly cloudy, 15°C",
            forecast = "Mixed conditions expected - pack layers for changing weather",
            recommendedClothing = "Layers, light jacket, comfortable walking shoes",
            outdoorSuitability = "Good for both indoor and outdoor activities"
        )
    }
    
    private fun generateWeatherInsights(weatherInfo: WeatherInfo): List<String> {
        return buildList {
            add("🌡️ Current conditions: ${weatherInfo.currentConditions}")
            
            val isGoodWeather = weatherInfo.outdoorSuitability.contains("perfect", ignoreCase = true) ||
                weatherInfo.outdoorSuitability.contains("good", ignoreCase = true)
            
            if (isGoodWeather) {
                add("☀️ Excellent weather for outdoor activities!")
            } else {
                add("🌧️ Weather may limit outdoor activities")
            }
            
            if (weatherInfo.recommendedClothing.contains("winter", ignoreCase = true)) {
                add("🧥 Cold weather - dress warmly")
            } else if (weatherInfo.recommendedClothing.contains("light", ignoreCase = true)) {
                add("👕 Mild weather - comfortable clothing recommended")
            }
        }
    }
    
    private fun generateWeatherRecommendations(weatherInfo: WeatherInfo): List<String> {
        return buildList {
            add("Pack: ${weatherInfo.recommendedClothing}")
            
            if (weatherInfo.outdoorSuitability.contains("perfect", ignoreCase = true)) {
                add("Perfect day for outdoor sightseeing and walking tours")
                add("Consider booking outdoor activities or boat tours")
            } else if (weatherInfo.outdoorSuitability.contains("limited", ignoreCase = true)) {
                add("Focus on indoor attractions and museums")
                add("Have backup indoor plans ready")
            }
            
            if (weatherInfo.forecast.contains("rain", ignoreCase = true)) {
                add("Bring umbrella and waterproof clothing")
            }
            
            if (weatherInfo.forecast.contains("sun", ignoreCase = true)) {
                add("Don't forget sunscreen and sunglasses")
            }
        }
    }
}