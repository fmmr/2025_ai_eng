@file:Suppress("unused")

package com.vend.fmr.aieng.utils

import com.vend.fmr.aieng.openAI
import com.vend.fmr.aieng.polygon
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * ReAct Agent Examples - Reasoning + Acting pattern implementation
 * 
 * This implements the ReAct pattern where the AI:
 * 1. Thinks about what to do
 * 2. Takes an action (calls a function)  
 * 3. Observes the result
 * 4. Repeats until it can provide a final answer
 */
object ReActAgentExamples {

    // Data classes for function results
    data class Location(val city: String, val country: String, val latitude: Double, val longitude: Double)
    data class Weather(val location: String, val temperature: Int, val condition: String, val humidity: Int)
    data class StockInfo(val ticker: String, val price: Double, val change: Double, val changePercent: Double)
    
    /**
     * System prompt that teaches the AI how to use the ReAct pattern
     */
    const val REACT_SYSTEM_PROMPT = """You are a helpful assistant that can take actions to help users. You have access to several functions that you can call to gather information.

Available functions:
- getLocation(): Get the current location (returns city, country, coordinates)
- getWeather(location): Get weather information for a specific location
- getStockPrice(ticker): Get current stock price and change for a ticker symbol (e.g., AAPL, MSFT)
- getCurrentTime(): Get the current date and time
- calculateDistance(from, to): Calculate distance between two locations
- getNewsHeadlines(): Get recent news headlines

When responding, you MUST use this exact format:

Thought: [your reasoning about what to do next]
Action: [function_name(parameters)]

After I execute the action, I'll provide the result as:
Observation: [result of the action]

Then continue with more Thought/Action pairs until you have enough information to answer the user's question.

When you're ready to give the final answer, use:
Final Answer: [your complete response to the user]

Example:
User: "What's the weather like where I am?"
Thought: I need to first get the user's location, then get the weather for that location.
Action: getLocation()

Important rules:
1. Always start with a Thought
2. Only call one function per Action
3. Wait for the Observation before continuing
4. Use the exact function names and parameter format shown above
5. End with "Final Answer:" when you have enough information"""

    // Mock functions (would later be replaced with real APIs)
    
    fun getLocation(): Location {
        // Mock location - could later integrate with real geolocation
        return Location(
            city = "Oslo", 
            country = "Norway", 
            latitude = 59.9139, 
            longitude = 10.7522
        )
    }
    
    fun getWeather(location: String): Weather {
        // Mock weather data - could later integrate with real weather API
        val mockWeathers = mapOf(
            "oslo" to Weather("Oslo, Norway", 5, "Cloudy", 78),
            "london" to Weather("London, UK", 12, "Rainy", 85),
            "new york" to Weather("New York, USA", 18, "Sunny", 65),
            "tokyo" to Weather("Tokyo, Japan", 22, "Partly Cloudy", 70)
        )
        
        return mockWeathers[location.lowercase()] ?: Weather(
            location = location,
            temperature = (0..25).random(),
            condition = listOf("Sunny", "Cloudy", "Rainy", "Partly Cloudy").random(),
            humidity = (40..90).random()
        )
    }
    
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
    
    private fun getMockStockPrice(ticker: String): StockInfo {
        // More realistic mock stock prices based on well-known tickers
        val mockPrices = mapOf(
            "AAPL" to 175.50,
            "MSFT" to 378.25, 
            "GOOGL" to 142.30,
            "AMZN" to 145.80,
            "TSLA" to 248.75,
            "META" to 485.60,
            "NVDA" to 875.25,
            "NFLX" to 485.30
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
    
    fun getCurrentTime(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }
    
    fun calculateDistance(from: String, to: String): String {
        // Mock distance calculation - could later use real geocoding + distance APIs
        val mockDistances = mapOf(
            "oslo-london" to 1154,
            "oslo-new york" to 5585,
            "london-new york" to 5585,
            "oslo-tokyo" to 8587
        )
        
        val key1 = "${from.lowercase()}-${to.lowercase()}"
        val key2 = "${to.lowercase()}-${from.lowercase()}"
        
        val distance = mockDistances[key1] ?: mockDistances[key2] ?: (100..10000).random()
        return "$distance km"
    }
    
    fun getNewsHeadlines(): List<String> {
        // Mock news headlines - could later integrate with real news API
        return listOf(
            "Tech stocks rally amid AI innovation surge",
            "Global climate summit reaches new agreements", 
            "Breakthrough in quantum computing research announced",
            "International trade talks progress positively",
            "Renewable energy adoption accelerates worldwide"
        )
    }
    
    /**
     * Parse AI response to extract function calls
     */
    private fun parseAction(response: String): Pair<String, List<String>>? {
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
     * Execute a function call based on parsed action
     */
    private suspend fun executeFunction(functionName: String, params: List<String>): String {
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
    
    /**
     * Main ReAct agent function - implements the Thought/Action/Observation loop
     * Uses proper message-based conversation with OpenAI
     */
    suspend fun reactAgent(userQuery: String, maxIterations: Int = 10, debug: Boolean = false): String {
        val messages = mutableListOf<com.vend.fmr.aieng.impl.openai.Message>()
        
        // Add system message
        messages.add(com.vend.fmr.aieng.impl.openai.Message("system", REACT_SYSTEM_PROMPT))
        
        // Add initial user query
        messages.add(com.vend.fmr.aieng.impl.openai.Message("user", userQuery))
        
        if (debug) {
            println("🤖 Starting ReAct Agent for query: $userQuery")
            println("=" * 50)
        }
        
        for (iteration in 0 until maxIterations) {
            if (debug) {
                println("\n--- Iteration ${iteration + 1} ---")
                println("💬 Messages in conversation: ${messages.size}")
            }
            
            // Get AI response using message history
            val aiResponse = openAI.createChatCompletionWithMessages(
                messages = messages,
                temperature = 0.1,
                maxTokens = 400
            )
            
            val currentResponse = aiResponse.text()
            
            if (debug) {
                println("AI: $currentResponse")
            }
            
            // Add AI response to messages
            messages.add(com.vend.fmr.aieng.impl.openai.Message("assistant", currentResponse))
            
            // Check if AI provided final answer
            if (currentResponse.contains("Final Answer:", ignoreCase = true)) {
                val finalAnswerRegex = Regex("Final Answer:\\s*(.*)", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
                val match = finalAnswerRegex.find(currentResponse)
                if (match != null) {
                    val finalAnswer = match.groupValues[1].trim()
                    if (debug) {
                        println("\n✅ Final Answer reached!")
                    }
                    return finalAnswer
                }
            }
            
            // Parse and execute action
            val action = parseAction(currentResponse)
            if (action != null) {
                val (functionName, params) = action
                if (debug) {
                    println("🔧 Executing: $functionName(${params.joinToString(", ")})")
                }
                
                val result = executeFunction(functionName, params)
                val observation = "Observation: $result"
                
                if (debug) {
                    println("📋 $observation")
                }
                
                // Add observation as a user message (simulating function result)
                messages.add(com.vend.fmr.aieng.impl.openai.Message("user", observation))
            } else {
                // No action found, AI might be done or confused
                if (debug) {
                    println("⚠️ No action found in response, ending loop")
                }
                break
            }
        }
        
        return "I wasn't able to complete this task within the iteration limit."
    }
}

