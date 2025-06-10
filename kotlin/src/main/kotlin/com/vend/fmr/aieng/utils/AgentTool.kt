package com.vend.fmr.aieng.utils

import com.vend.fmr.aieng.apis.geolocation.Geolocation
import com.vend.fmr.aieng.apis.nasa.Nasa
import com.vend.fmr.aieng.apis.news.News
import com.vend.fmr.aieng.apis.openai.*
import com.vend.fmr.aieng.apis.polygon.Polygon
import com.vend.fmr.aieng.apis.weather.Weather
import com.vend.fmr.aieng.mcp.InputSchema
import com.vend.fmr.aieng.mcp.PropertySchema
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.springframework.stereotype.Component
import com.vend.fmr.aieng.mcp.Tool as McpTool

/**
 * AgentTool enum with service injection via static inner component
 * Each tool can use real Spring services in its executor lambda
 */
enum class AgentTool(
    val functionName: String,
    val description: String,
    val parameters: Map<String, ToolParameter>,
    val readmeDescription: String,
    val testParams: Map<String, String>,
    val executor: suspend (Map<String, String>, HttpServletRequest?) -> String
) {
    HELLO_WORLD(
        functionName = "hello_world",
        description = "Test function that greets a person by name. Use this to verify the system is working.",
        parameters = mapOf(
            "name" to ToolParameter("string", "Name to greet", false)
        ),
        readmeDescription = "Simple greeting function for testing",
        testParams = mapOf("name" to "BOSS"),
        executor = { args, _ ->
            val name = args["name"] ?: "World"
            "Hello, $name! 🎉"
        }
    ),


    CURRENT_TIME(
        functionName = "current_time",
        description = "Get the current date and time. Returns server time plus UTC time with timezone calculation examples. Use this for any time-related queries including specific locations like Tokyo, New York, etc.",
        parameters = emptyMap(),
        readmeDescription = "Current date and time with timezone examples",
        testParams = emptyMap(),
        executor = { _, _ ->
            val now = java.time.ZonedDateTime.now()
            val utc = now.withZoneSameInstant(java.time.ZoneId.of("UTC"))
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            """
            Server time: ${now.format(formatter)} ${now.zone}
            UTC time: ${utc.format(formatter)} UTC
            """.trimIndent()
        }
    ),


    RANDOM_QUOTE(
        functionName = "random_quote",
        description = "Generate a random inspirational quote using AI.",
        parameters = emptyMap(),
        readmeDescription = "AI-generated inspirational quotes",
        testParams = emptyMap(),
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

    COMPANY_INFO(
        functionName = "company_info",
        description = "Get detailed company information including name, description, and business details for any stock symbol.",
        parameters = mapOf(
            "symbol" to ToolParameter("string", "Stock symbol (e.g. AAPL, MSFT)", true)
        ),
        readmeDescription = "Company information and business details",
        testParams = mapOf("symbol" to "AAPL"),
        executor = { args, _ ->
            val symbol = args["symbol"] ?: throw IllegalArgumentException("Missing symbol parameter")
            val stockInfo = polygon.getTickerDetails(symbol, debug = false)
            val description = stockInfo.results.description ?: "No description for: ${stockInfo.results.name}"
            "🏢 $symbol: ${stockInfo.results.name}\n$description"
        }
    ),

    WEATHER_NOWCAST(
        functionName = "weather_nowcast",
        description = "Get real-time weather nowcast with 5-minute precision. Only works for Nordic countries (Norway, Sweden, Denmark, Finland).",
        parameters = mapOf(
            "latitude" to ToolParameter("number", "Latitude (Nordic region: 55°N-75°N)", true),
            "longitude" to ToolParameter("number", "Longitude (Nordic region: 0°E-35°E)", true)
        ),
        readmeDescription = "5-minute precision weather for Nordic countries",
        testParams = mapOf("latitude" to "59.9139", "longitude" to "10.7522"),
        executor = { args, _ ->
            val lat = args["latitude"]?.toDoubleOrNull() ?: throw IllegalArgumentException("Missing or invalid latitude")
            val lon = args["longitude"]?.toDoubleOrNull() ?: throw IllegalArgumentException("Missing or invalid longitude")
            "⚡ ${weather.getNowcastSummary(lat, lon, debug = false)}"
        }
    ),

    WEATHER_FORECAST(
        functionName = "weather_forecast",
        description = "Get detailed weather forecast for any location worldwide including temperature, humidity, wind, pressure, and clouds.",
        parameters = mapOf(
            "latitude" to ToolParameter("number", "Latitude (global coverage)", true),
            "longitude" to ToolParameter("number", "Longitude (global coverage)", true)
        ),
        readmeDescription = "Global weather forecasts with detailed metrics",
        testParams = mapOf("latitude" to "35.6762", "longitude" to "139.6503"),
        executor = { args, _ ->
            val lat = args["latitude"]?.toDoubleOrNull() ?: throw IllegalArgumentException("Missing or invalid latitude")
            val lon = args["longitude"]?.toDoubleOrNull() ?: throw IllegalArgumentException("Missing or invalid longitude")
            "🌍 ${weather.getForecastSummary(lat, lon, debug = false)}"
        }
    ),

    LOCATION(
        functionName = "location",
        description = "Get user's real geographic location from their IP address including city, country, and coordinates. Use this when you need the actual user location.",
        parameters = mapOf(
            "ip" to ToolParameter("string", "IP address (optional - uses client IP if not provided)", false)
        ),
        readmeDescription = "Real user location from IP address",
        testParams = emptyMap(),
        executor = { args, request ->
            val ip = args["ip"] ?: request?.let { getClientIpAddress(it) }
            val (location, note) = geolocation.getLocationByIpWithFallback(ip, debug = false)
            val summary = geolocation.formatLocationSummary(location)
            "📍 $summary$note"
        }
    ),

    STOCK_PRICE(
        functionName = "stock_price",
        description = "Get real-time stock market data including current price, open/close prices, volume, and trading information. Use this for actual market data.",
        parameters = mapOf(
            "symbol" to ToolParameter("string", "Stock symbol (e.g. AAPL, NHYDY)", true)
        ),
        readmeDescription = "Real-time stock market data",
        testParams = mapOf("symbol" to "AAPL"),
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
    ),

    NEWS_HEADLINES(
        functionName = "news_headlines",
        description = "Get real current news headlines from major news sources worldwide. Use country codes: 'us' (USA), 'gb' (UK), 'fr' (France), 'de' (Germany), 'no' (Norway), 'se' (Sweden), 'ca' (Canada), 'au' (Australia).",
        parameters = mapOf(
            "country" to ToolParameter("string", "2-letter country code: us, gb, fr, de, no, se, ca, au, etc. Defaults to 'us'", false),
            "category" to ToolParameter("string", "Category: business, entertainment, health, science, sports, technology", false)
        ),
        readmeDescription = "Current news headlines worldwide",
        testParams = emptyMap(),
        executor = { args, _ ->
            val country = args["country"] ?: "us"
            val category = args["category"]
            news.getHeadlinesSummary(country, category, debug = true)
        }
    ),

    NASA_APOD(
        functionName = "nasa_apod",
        description = "Get NASA's Astronomy Picture of the Day with stunning space images and explanations. Optionally specify a date (YYYY-MM-DD format).",
        parameters = mapOf(
            "date" to ToolParameter("string", "Date in YYYY-MM-DD format (optional, defaults to today)", false)
        ),
        readmeDescription = "NASA's daily space images and explanations",
        testParams = emptyMap(),
        executor = { args, _ ->
            val date = args["date"]
            nasa.getApodSummary(date, debug = false)
        }
    ),

    NEAR_EARTH_OBJECTS(
        functionName = "near_earth_objects",
        description = "Get information about asteroids and other Near Earth Objects approaching Earth. Shows distances, speeds, and potential hazards.",
        parameters = mapOf(
            "date" to ToolParameter("string", "Date in YYYY-MM-DD format (optional, defaults to today)", false)
        ),
        readmeDescription = "Asteroids and objects approaching Earth",
        testParams = emptyMap(),
        executor = { args, _ ->
            val date = args["date"]
            nasa.getNearEarthObjectsSummary(date, date, debug = false)
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
        private lateinit var nasa: Nasa
        private lateinit var news: News
        private lateinit var json: Json


        fun setServices(openAI: OpenAI, weather: Weather, polygon: Polygon, geolocation: Geolocation, nasa: Nasa, news: News, json: Json) {
            this.openAI = openAI
            this.weather = weather
            this.polygon = polygon
            this.geolocation = geolocation
            this.nasa = nasa
            this.news = news
            this.json = json
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
        private val geolocation: Geolocation,
        private val nasa: Nasa,
        private val news: News,
        private val json: Json
    ) {
        @PostConstruct
        fun injectServices() {
            AgentTool.setServices(openAI, weather, polygon, geolocation, nasa, news, json)
        }
    }
}

