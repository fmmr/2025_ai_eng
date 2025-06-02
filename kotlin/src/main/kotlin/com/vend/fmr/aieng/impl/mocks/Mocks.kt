package com.vend.fmr.aieng.impl.mocks

import com.vend.fmr.aieng.impl.openai.*
import com.vend.fmr.aieng.polygon
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Mock functions and utilities for AI agent demos
 * Provides realistic test data and centralized parsing/execution logic
 * Used by both ReAct agents and OpenAI function calling
 */
object Mocks {

    // Data classes for function results
    data class Location(val city: String, val country: String, val latitude: Double, val longitude: Double)
    data class Weather(val location: String, val temperature: Int, val condition: String, val humidity: Int)
    data class StockInfo(val ticker: String, val price: Double, val change: Double, val changePercent: Double)
    
    fun getLocation(): Location {
        return Location(
            city = "Oslo", 
            country = "Norway", 
            latitude = 59.9139, 
            longitude = 10.7522
        )
    }
    
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
    
    suspend fun getStockPrice(ticker: String): StockInfo {
        return try {
            val aggregatesResponse = polygon.getAggregates(ticker)
            val response = aggregatesResponse.firstOrNull()
            val aggregate = response?.results?.lastOrNull()
            
            if (aggregate != null && aggregate.c > 0) {
                val change = aggregate.c - aggregate.o
                val changePercent = (change / aggregate.o) * 100
                StockInfo(
                    ticker = ticker.uppercase(),
                    price = aggregate.c,
                    change = change,
                    changePercent = changePercent
                )
            } else {
                getMockStockPrice(ticker)
            }
        } catch (_: Exception) {
            getMockStockPrice(ticker)
        }
    }
    
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
        val changePercent = (-50..50).random() / 10.0
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
        
        val params = if (paramsString.isEmpty()) {
            emptyList()
        } else {
            paramsString.split(",").map { it.trim().removeSurrounding("\"", "'") }
        }
        
        return functionName to params
    }
    
    /**
     * Execute a function call based on parsed action - centralized function executor
     * This is the single point where all function calls are handled
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
    
    // OpenAI Function Calling Support
    
    fun getAvailableTools(): List<Tool> {
        return listOf(
            Tool(
                function = FunctionDefinition(
                    name = "getLocation",
                    description = "Get the current location (GPS coordinates) of the user",
                    parameters = FunctionParameters(
                        properties = emptyMap(),
                        required = emptyList()
                    )
                )
            ),
            Tool(
                function = FunctionDefinition(
                    name = "getWeather",
                    description = "Get current weather information for a specific location",
                    parameters = FunctionParameters(
                        properties = mapOf(
                            "location" to PropertyDefinition(
                                type = "string",
                                description = "The city or location to get weather for (e.g., 'Oslo', 'New York')"
                            )
                        ),
                        required = listOf("location")
                    )
                )
            ),
            Tool(
                function = FunctionDefinition(
                    name = "getStockPrice",
                    description = "Get current stock price and information for a ticker symbol",
                    parameters = FunctionParameters(
                        properties = mapOf(
                            "ticker" to PropertyDefinition(
                                type = "string",
                                description = "Stock ticker symbol (e.g., 'AAPL', 'MSFT', 'GOOGL')"
                            )
                        ),
                        required = listOf("ticker")
                    )
                )
            ),
            Tool(
                function = FunctionDefinition(
                    name = "getCurrentTime",
                    description = "Get the current date and time",
                    parameters = FunctionParameters(
                        properties = emptyMap(),
                        required = emptyList()
                    )
                )
            ),
            Tool(
                function = FunctionDefinition(
                    name = "calculateDistance",
                    description = "Calculate the distance between two locations",
                    parameters = FunctionParameters(
                        properties = mapOf(
                            "from" to PropertyDefinition(
                                type = "string",
                                description = "Starting location (e.g., 'Oslo', 'London')"
                            ),
                            "to" to PropertyDefinition(
                                type = "string",
                                description = "Destination location (e.g., 'Paris', 'New York')"
                            )
                        ),
                        required = listOf("from", "to")
                    )
                )
            ),
            Tool(
                function = FunctionDefinition(
                    name = "getNewsHeadlines",
                    description = "Get current news headlines",
                    parameters = FunctionParameters(
                        properties = emptyMap(),
                        required = emptyList()
                    )
                )
            )
        )
    }
    
    /**
     * Execute a function call from OpenAI function calling format
     * Parses the JSON arguments and delegates to executeFunction
     */
    suspend fun executeFunctionCall(functionCall: FunctionCall): String {
        val json = Json { ignoreUnknownKeys = true }
        
        return try {
            val params = if (functionCall.arguments.isBlank() || functionCall.arguments == "{}") {
                emptyList()
            } else {
                val args = json.parseToJsonElement(functionCall.arguments).jsonObject
                when (functionCall.name.lowercase()) {
                    "getlocation", "getcurrenttime", "getnewsheadlines" -> {
                        emptyList()
                    }
                    "getweather" -> {
                        listOf(args["location"]?.jsonPrimitive?.content ?: "oslo")
                    }
                    "getstockprice" -> {
                        listOf(args["ticker"]?.jsonPrimitive?.content ?: "AAPL")
                    }
                    "calculatedistance" -> {
                        listOf(
                            args["from"]?.jsonPrimitive?.content ?: "Oslo",
                            args["to"]?.jsonPrimitive?.content ?: "London"
                        )
                    }
                    else -> emptyList()
                }
            }
            
            executeFunction(functionCall.name, params)
            
        } catch (e: Exception) {
            "Error executing ${functionCall.name}: ${e.message}"
        }
    }
}