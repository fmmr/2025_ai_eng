package com.vend.fmr.aieng.utils

import com.vend.fmr.aieng.apis.geolocation.Geolocation
import com.vend.fmr.aieng.apis.mocks.Mocks
import com.vend.fmr.aieng.apis.openai.*
import com.vend.fmr.aieng.apis.polygon.Polygon
import com.vend.fmr.aieng.apis.weather.Weather
import com.vend.fmr.aieng.mcp.Tool as McpTool
import com.vend.fmr.aieng.mcp.InputSchema
import com.vend.fmr.aieng.mcp.PropertySchema
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import jakarta.annotation.PostConstruct

/**
 * Tools enum with service injection via static inner component
 * Each tool can use real Spring services in its executor lambda
 */
enum class Tools(
    val functionName: String,
    val description: String,
    val parameters: Map<String, ToolParameter>,
    val isMock: Boolean, // true = local execution, false = calls external service
    val executor: suspend (Map<String, String>, HttpServletRequest?) -> String
) {
    HELLO_WORLD(
        functionName = "hello_world",
        description = "Says hello to test connections",
        parameters = mapOf(
            "name" to ToolParameter("string", "Name to greet", false)
        ),
        isMock = true,
        executor = { args, _ ->
            val name = args["name"] ?: "World"
            "Hello, $name! 🎉"
        }
    ),
    
    GET_LOCATION(
        functionName = "getLocation",
        description = "Get the current location (GPS coordinates) of the user",
        parameters = emptyMap(),
        isMock = true,
        executor = { _, _ ->
            val location = Mocks.getLocation()
            "Location: ${location.city}, ${location.country} (${location.latitude}, ${location.longitude})"
        }
    ),
    
    GET_WEATHER(
        functionName = "getWeather", 
        description = "Get current weather information for a specific location",
        parameters = mapOf(
            "location" to ToolParameter("string", "The city or location to get weather for (e.g., 'Oslo', 'New York')", true)
        ),
        isMock = true,
        executor = { args, _ ->
            val location = args["location"] ?: throw IllegalArgumentException("Missing location parameter")
            val weather = Mocks.getWeather(location)
            "Weather in ${weather.location}: ${weather.temperature}°C, ${weather.condition}, Humidity: ${weather.humidity}%"
        }
    ),
    
    GET_STOCK_PRICE(
        functionName = "getStockPrice",
        description = "Get current stock price and information for a ticker symbol", 
        parameters = mapOf(
            "ticker" to ToolParameter("string", "Stock ticker symbol (e.g., 'AAPL', 'MSFT', 'GOOGL')", true)
        ),
        isMock = true,
        executor = { args, _ ->
            val ticker = args["ticker"] ?: throw IllegalArgumentException("Missing ticker parameter")
            val stockInfo = Mocks.getStockPrice(ticker)
            "${stockInfo.ticker}: $${stockInfo.price} (${if (stockInfo.change >= 0) "+" else ""}${stockInfo.change} / ${String.format("%.2f", stockInfo.changePercent)}%)"
        }
    ),
    
    GET_CURRENT_TIME(
        functionName = "getCurrentTime",
        description = "Get the current date and time",
        parameters = emptyMap(),
        isMock = true,
        executor = { _, _ ->
            Mocks.getCurrentTime()
        }
    ),
    
    GET_RANDOM_QUOTE(
        functionName = "get_random_quote",
        description = "Get a random inspirational quote",
        parameters = emptyMap(),
        isMock = false,
        executor = { _, _ ->
            val response = openAI.createChatCompletion(
                prompt = "Generate a random inspirational quote. Return only the quote with attribution, nothing else.",
                maxTokens = 100,
                temperature = 0.9,
                debug = false
            )
            val content = response.choices.firstOrNull()?.message?.content
            when (content) {
                is TextContent -> content.text
                else -> "Inspiration comes from within."
            }
        }
    );
    
    // Late-initialized services - will be injected by ServiceInjector
    companion object {
        private lateinit var openAI: OpenAI
        private lateinit var weather: Weather
        private lateinit var polygon: Polygon
        private lateinit var geolocation: Geolocation
        
        fun setServices(openAI: OpenAI, weather: Weather, polygon: Polygon, geolocation: Geolocation) {
            Companion.openAI = openAI
            Companion.weather = weather
            Companion.polygon = polygon
            Companion.geolocation = geolocation
        }
    }
    
    
    /**
     * Spring Component that injects services into enum instances
     * This is the magic that makes service injection work!
     */
    @Component
    class ServiceInjector(
        private val openAI: OpenAI,
        private val weather: Weather,
        private val polygon: Polygon,
        private val geolocation: Geolocation
    ) {
        @PostConstruct
        fun injectServices() {
            Tools.setServices(openAI, weather, polygon, geolocation)
        }
    }
}

/**
 * Tool parameter definition
 */
data class ToolParameter(
    val type: String,
    val description: String, 
    val required: Boolean
)