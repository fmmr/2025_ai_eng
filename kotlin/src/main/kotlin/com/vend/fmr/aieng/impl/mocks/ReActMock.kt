package com.vend.fmr.aieng.impl.mocks

import com.vend.fmr.aieng.polygon
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * ReAct Mock functions and utilities for ReAct Agent demos
 * Provides realistic test data and centralized ReAct parsing/execution logic
 */
object ReActMock {

    // Data classes for function results
    data class Location(val city: String, val country: String, val latitude: Double, val longitude: Double)
    data class Weather(val location: String, val temperature: Int, val condition: String, val humidity: Int)
    data class StockInfo(val ticker: String, val price: Double, val change: Double, val changePercent: Double)
    
    /**
     * Get mock current location (GPS coordinates)
     * In a real implementation, this would use device GPS or IP geolocation
     */
    fun getLocation(): Location {
        return Location(
            city = "Oslo", 
            country = "Norway", 
            latitude = 59.9139, 
            longitude = 10.7522
        )
    }
    
    /**
     * Get mock weather data for a location
     * In a real implementation, this would call a weather API like OpenWeatherMap
     */
    fun getWeather(location: String): Weather {
        val mockWeathers = mapOf(
            "oslo" to Weather("Oslo, Norway", 5, "Cloudy", 78),
            "london" to Weather("London, UK", 12, "Rainy", 85),
            "new york" to Weather("New York, USA", 18, "Sunny", 65),
            "tokyo" to Weather("Tokyo, Japan", 22, "Partly Cloudy", 70),
            "paris" to Weather("Paris, France", 15, "Overcast", 72),
            "berlin" to Weather("Berlin, Germany", 8, "Foggy", 88),
            "stockholm" to Weather("Stockholm, Sweden", 3, "Snow", 82)
        )
        
        return mockWeathers[location.lowercase()] ?: Weather(
            location = location,
            temperature = (0..25).random(),
            condition = listOf("Sunny", "Cloudy", "Rainy", "Partly Cloudy", "Overcast").random(),
            humidity = (40..90).random()
        )
    }
    
    /**
     * Get stock price data - tries real Polygon API, falls back to realistic mock data
     */
    suspend fun getStockPrice(ticker: String): StockInfo {
        return try {
            // Try real Polygon API first
            val aggregatesResponse = polygon.getAggregates(ticker)
            val response = aggregatesResponse.firstOrNull()
            val aggregate = response?.results?.lastOrNull() // Get most recent data
            
            if (aggregate != null && aggregate.c > 0) {
                // Real data found
                val change = aggregate.c - aggregate.o
                val changePercent = (change / aggregate.o) * 100
                StockInfo(
                    ticker = ticker.uppercase(),
                    price = aggregate.c,
                    change = change,
                    changePercent = changePercent
                )
            } else {
                // No valid data from API, use realistic mock data
                getMockStockPrice(ticker)
            }
        } catch (e: Exception) {
            // API error, fallback to realistic mock data
            getMockStockPrice(ticker)
        }
    }
    
    /**
     * Generate realistic mock stock prices for well-known tickers
     */
    private fun getMockStockPrice(ticker: String): StockInfo {
        val mockPrices = mapOf(
            "AAPL" to 175.50,
            "MSFT" to 378.25, 
            "GOOGL" to 142.30,
            "AMZN" to 145.80,
            "TSLA" to 248.75,
            "META" to 485.60,
            "NVDA" to 875.25,
            "NFLX" to 485.30,
            "ADBE" to 520.40,
            "CRM" to 210.85
        )
        
        val basePrice = mockPrices[ticker.uppercase()] ?: (50..400).random().toDouble()
        val changePercent = (-50..50).random() / 10.0 // -5.0 to 5.0
        val change = basePrice * (changePercent / 100)
        
        return StockInfo(
            ticker = ticker.uppercase(),
            price = basePrice + change,
            change = change,
            changePercent = changePercent
        )
    }
    
    /**
     * Get current date and time
     */
    fun getCurrentTime(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }
    
    /**
     * Calculate mock distance between two locations
     * In a real implementation, this would use geocoding + distance calculation APIs
     */
    fun calculateDistance(from: String, to: String): String {
        val mockDistances = mapOf(
            "oslo-london" to 1154,
            "oslo-new york" to 5585,
            "london-new york" to 5585,
            "oslo-tokyo" to 8587,
            "oslo-paris" to 1359,
            "london-paris" to 344,
            "new york-tokyo" to 10838,
            "berlin-stockholm" to 504
        )
        
        val key1 = "${from.lowercase()}-${to.lowercase()}"
        val key2 = "${to.lowercase()}-${from.lowercase()}"
        
        val distance = mockDistances[key1] ?: mockDistances[key2] ?: (100..10000).random()
        return "$distance km"
    }
    
    /**
     * Get mock news headlines
     * In a real implementation, this would call a news API like NewsAPI
     */
    fun getNewsHeadlines(): List<String> {
        return listOf(
            "Tech stocks rally amid AI innovation surge",
            "Global climate summit reaches new agreements", 
            "Breakthrough in quantum computing research announced",
            "International trade talks progress positively",
            "Renewable energy adoption accelerates worldwide",
            "Major cryptocurrency exchange launches new features",
            "Space exploration mission achieves new milestone",
            "Healthcare AI shows promising clinical trial results"
        )
    }
    
    /**
     * Parse AI response to extract function calls - centralized action parser
     * Returns function name and parameters, or null if no action found
     */
    fun parseAction(response: String): Pair<String, List<String>>? {
        val actionRegex = Regex("Action:\\s*(\\w+)\\((.*?)\\)", RegexOption.IGNORE_CASE)
        val match = actionRegex.find(response) ?: return null
        
        val functionName = match.groupValues[1]
        val paramsString = match.groupValues[2].trim()
        
        // Simple parameter parsing - split by comma and trim quotes
        val params = if (paramsString.isEmpty()) {
            emptyList()
        } else {
            paramsString.split(",").map { it.trim().removeSurrounding("\"", "'") }
        }
        
        return functionName to params
    }
    
    /**
     * Execute a function call based on parsed action - centralized function executor
     * This is the single point where all ReAct function calls are handled
     */
    suspend fun executeFunction(functionName: String, params: List<String>): String {
        return try {
            when (functionName.lowercase()) {
                "getlocation" -> {
                    val location = getLocation()
                    "Location: ${location.city}, ${location.country} (${location.latitude}, ${location.longitude})"
                }
                "getweather" -> {
                    val location = params.firstOrNull() ?: "oslo"
                    val weather = getWeather(location)
                    "Weather in ${weather.location}: ${weather.temperature}°C, ${weather.condition}, Humidity: ${weather.humidity}%"
                }
                "getstockprice" -> {
                    val ticker = params.firstOrNull() ?: "AAPL"
                    val stock = getStockPrice(ticker)
                    "Stock ${stock.ticker}: $${String.format("%.2f", stock.price)} (${if (stock.change >= 0) "+" else ""}${String.format("%.2f", stock.change)}, ${String.format("%.2f", stock.changePercent)}%)"
                }
                "getcurrenttime" -> {
                    "Current time: ${getCurrentTime()}"
                }
                "calculatedistance" -> {
                    val from = params.getOrNull(0) ?: "Oslo"
                    val to = params.getOrNull(1) ?: "London"
                    "Distance from $from to $to: ${calculateDistance(from, to)}"
                }
                "getnewsheadlines" -> {
                    val headlines = getNewsHeadlines()
                    "Recent headlines:\n" + headlines.joinToString("\n") { "- $it" }
                }
                else -> "Error: Unknown function '$functionName'"
            }
        } catch (e: Exception) {
            "Error executing $functionName: ${e.message}"
        }
    }
}