package com.vend.fmr.aieng.utils

import com.vend.fmr.aieng.apis.geolocation.Geolocation
import com.vend.fmr.aieng.apis.mocks.Mocks
import com.vend.fmr.aieng.apis.openai.*
import com.vend.fmr.aieng.apis.polygon.Polygon
import com.vend.fmr.aieng.apis.weather.Weather
import com.vend.fmr.aieng.mcp.InputSchema
import com.vend.fmr.aieng.mcp.PropertySchema
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.springframework.stereotype.Component
import com.vend.fmr.aieng.mcp.Tool as McpTool

/**
 * Tools enum with service injection via static inner component
 * Each tool can use real Spring services in its executor lambda
 */
enum class Tools(
    val functionName: String,
    val description: String,
    val parameters: Map<String, ToolParameter>,
    val mock: Boolean,  // has mock implementation
    val api: Boolean,   // has API implementation
    val executor: suspend (Map<String, String>, HttpServletRequest?) -> String
) {
    HELLO_WORLD(
        functionName = "hello_world",
        description = "Test function that greets a person by name. Use this to verify the system is working.",
        parameters = mapOf(
            "name" to ToolParameter("string", "Name to greet", false)
        ),
        mock = true,
        api = false,
        executor = { args, _ ->
            val name = args["name"] ?: "World"
            "Hello, $name! 🎉"
        }
    ),
    
    GET_LOCATION(
        functionName = "get_location",
        description = "Get a mock geographic location (Oslo) for testing. Use get_location_from_ip for real user location.",
        parameters = emptyMap(),
        mock = true,
        api = false,
        executor = { _, _ ->
            val location = Mocks.getLocation()
            "Location: ${location.city}, ${location.country} (${location.latitude}, ${location.longitude})"
        }
    ),
    
    GET_WEATHER(
        functionName = "get_weather",
        description = "Get current weather conditions for any city or location including temperature, conditions, and humidity.",
        parameters = mapOf(
            "location" to ToolParameter("string", "The city or location to get weather for (e.g., 'Oslo', 'New York')", true)
        ),
        mock = true,
        api = false,
        executor = { args, _ ->
            val location = args["location"] ?: throw IllegalArgumentException("Missing location parameter")
            val weather = Mocks.getWeather(location)
            "Weather in ${weather.location}: ${weather.temperature}°C, ${weather.condition}, Humidity: ${weather.humidity}%"
        }
    ),
    
    GET_STOCK_PRICE(
        functionName = "get_stock_price_mock",
        description = "Get mock stock price data for testing and demos. Always available, works with any symbol.", 
        parameters = mapOf(
            "symbol" to ToolParameter("string", "Stock symbol (e.g., 'AAPL', 'MSFT', 'GOOGL')", true)
        ),
        mock = true,
        api = false,
        executor = { args, _ ->
            val symbol = args["symbol"] ?: throw IllegalArgumentException("Missing symbol parameter")
            val stockInfo = Mocks.getStockPrice(symbol)
            "${stockInfo.ticker}: $${stockInfo.price} (${if (stockInfo.change >= 0) "+" else ""}${stockInfo.change} / ${String.format("%.2f", stockInfo.changePercent)}%)"
        }
    ),
    
    GET_CURRENT_TIME(
        functionName = "get_current_time",
        description = "Get the current date and time. Returns server time plus UTC time with timezone calculation examples. Use this for any time-related queries including specific locations like Tokyo, New York, etc.",
        parameters = emptyMap(),
        mock = true,
        api = true,
        executor = { _, _ ->
            Mocks.getCurrentTime()
        }
    ),
    
    GET_NEWS_HEADLINES(
        functionName = "get_news_headlines",
        description = "Get recent news headlines from various sources.",
        parameters = emptyMap(),
        mock = true,
        api = false,
        executor = { _, _ ->
            val headlines = Mocks.getNewsHeadlines()
            "Recent headlines:\n" + headlines.joinToString("\n") { "- $it" }
        }
    ),
    
    GET_RANDOM_QUOTE(
        functionName = "get_random_quote",
        description = "Generate a random inspirational quote using AI.",
        parameters = emptyMap(),
        mock = false,
        api = true,
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
    ),
    
    GET_COMPANY_INFO(
        functionName = "get_company_info",
        description = "Get detailed company information including name, description, and business details for any stock symbol.",
        parameters = mapOf(
            "symbol" to ToolParameter("string", "Stock symbol (e.g. AAPL, MSFT)", true)
        ),
        mock = false,
        api = true,
        executor = { args, _ ->
            val symbol = args["symbol"] ?: throw IllegalArgumentException("Missing symbol parameter")
            val stockInfo = polygon.getTickerDetails(symbol, debug = false)
            val description = stockInfo.results.description ?: "No description for: ${stockInfo.results.name}"
            "🏢 $symbol: ${stockInfo.results.name}\n$description"
        }
    ),
    
    GET_WEATHER_NOWCAST(
        functionName = "get_weather_nowcast", 
        description = "Get real-time weather nowcast with 5-minute precision. Only works for Nordic countries (Norway, Sweden, Denmark, Finland).",
        parameters = mapOf(
            "latitude" to ToolParameter("number", "Latitude (Nordic region: 55°N-75°N)", true),
            "longitude" to ToolParameter("number", "Longitude (Nordic region: 0°E-35°E)", true)
        ),
        mock = false,
        api = true,
        executor = { args, _ ->
            val lat = args["latitude"]?.toDoubleOrNull() ?: throw IllegalArgumentException("Missing or invalid latitude")
            val lon = args["longitude"]?.toDoubleOrNull() ?: throw IllegalArgumentException("Missing or invalid longitude")
            "⚡ ${weather.getNowcastSummary(lat, lon, debug = false)}"
        }
    ),
    
    GET_WEATHER_FORECAST(
        functionName = "get_weather_forecast",
        description = "Get detailed weather forecast for any location worldwide including temperature, humidity, wind, pressure, and clouds.", 
        parameters = mapOf(
            "latitude" to ToolParameter("number", "Latitude (global coverage)", true),
            "longitude" to ToolParameter("number", "Longitude (global coverage)", true)
        ),
        mock = false,
        api = true,
        executor = { args, _ ->
            val lat = args["latitude"]?.toDoubleOrNull() ?: throw IllegalArgumentException("Missing or invalid latitude")
            val lon = args["longitude"]?.toDoubleOrNull() ?: throw IllegalArgumentException("Missing or invalid longitude")
            "🌍 ${weather.getForecastSummary(lat, lon, debug = false)}"
        }
    ),
    
    GET_LOCATION_FROM_IP(
        functionName = "get_location_from_ip",
        description = "Get user's real geographic location from their IP address including city, country, and coordinates. Use this when you need the actual user location.",
        parameters = mapOf(
            "ip" to ToolParameter("string", "IP address (optional - uses client IP if not provided)", false)
        ),
        mock = false,
        api = true,
        executor = { args, request ->
            val ip = args["ip"] ?: request?.let { getClientIpAddress(it) }
            val (location, note) = geolocation.getLocationByIpWithFallback(ip, debug = false)
            val summary = geolocation.formatLocationSummary(location)
            "📍 $summary$note"
        }
    ),
    
    GET_STOCK_PRICE_API(
        functionName = "get_stock_price",
        description = "Get real-time stock market data including current price, open/close prices, volume, and trading information. Use this for actual market data.",
        parameters = mapOf(
            "symbol" to ToolParameter("string", "Stock symbol (e.g. AAPL, NHYDY)", true)
        ),
        mock = false,
        api = true,
        executor = { args, _ ->
            val symbol = args["symbol"] ?: throw IllegalArgumentException("Missing symbol parameter")
            val aggregates = polygon.getAggregates(symbol, debug = false)
            val latest = aggregates.firstOrNull()?.results?.lastOrNull()
            if (latest != null) {
                "📈 $symbol ${latest.formattedDate()}: Open $${latest.openPrice}, Close $${latest.closePrice}, Volume ${latest.volume}"
            } else {
                "📈 $symbol: No recent price data available"
            }
        }
    );
    
    
    /**
     * Convert to OpenAI Function Calling format
     */
    fun toOpenAITool(): Tool {
        return Tool(
            function = FunctionDefinition(
                name = functionName,
                description = description,
                parameters = FunctionParameters(
                    properties = parameters.mapValues { (_, param) ->
                        PropertyDefinition(
                            type = param.type,
                            description = param.description
                        )
                    },
                    required = parameters.filter { it.value.required }.keys.toList()
                )
            )
        )
    }
    
    /**
     * Convert to MCP Protocol format
     */
    fun toMcpTool(): McpTool {
        return McpTool(
            name = functionName,
            description = description,
            inputSchema = InputSchema(
                properties = parameters.mapValues { (_, param) ->
                    PropertySchema(
                        type = param.type,
                        description = param.description
                    )
                }
            )
        )
    }

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
        
        /**
         * Execute a tool by function name with JSON arguments
         */
        suspend fun execute(functionName: String, arguments: String, request: HttpServletRequest? = null): String {
            val tool = entries.find { it.functionName == functionName }
                ?: return "Error: Unknown function '$functionName'"
            
            val params = if (arguments.isBlank() || arguments == "{}") {
                emptyMap()
            } else {
                try {
                    val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                    val args = json.parseToJsonElement(arguments).jsonObject
                    args.mapValues { it.value.jsonPrimitive.content }
                } catch (e: Exception) {
                    return "Error parsing arguments: ${e.message}"
                }
            }
            
            return try {
                tool.executor(params, request)
            } catch (e: Exception) {
                "Error executing $functionName: ${e.message}"
            }
        }
        
        /**
         * Parse ReAct action format: "Action: functionName(param1, param2)" and convert to JSON
         */
        fun parseAction(response: String): Pair<String, String>? {
            val actionRegex = Regex("Action:\\s*(\\w+)\\((.*?)\\)", RegexOption.IGNORE_CASE)
            val match = actionRegex.find(response) ?: return null
            
            val functionName = match.groupValues[1]
            val paramsString = match.groupValues[2].trim()
            
            if (paramsString.isEmpty()) {
                return functionName to "{}"
            }
            
            // Parse parameters and convert to JSON
            val params = paramsString.split(",").map { it.trim().removeSurrounding("\"", "'") }
            val tool = entries.find { it.functionName.equals(functionName, ignoreCase = true) }
            
            val json = if (tool != null && tool.parameters.isNotEmpty()) {
                // Map positional parameters to named parameters
                val paramKeys = tool.parameters.keys.toList()
                val paramMap = paramKeys.zip(params).toMap()
                "{" + paramMap.entries.joinToString(",") { "\"${it.key}\":\"${it.value}\"" } + "}"
            } else {
                "{}"
            }
            
            return functionName to json
        }
    }

    /**
     * Spring Component that injects services into enum instances,
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