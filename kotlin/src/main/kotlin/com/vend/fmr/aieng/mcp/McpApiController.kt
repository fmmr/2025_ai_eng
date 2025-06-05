package com.vend.fmr.aieng.mcp

import com.vend.fmr.aieng.geolocation
import com.vend.fmr.aieng.openAI
import com.vend.fmr.aieng.polygon
import com.vend.fmr.aieng.utils.getClientIpAddress
import com.vend.fmr.aieng.weather
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/mcp")
@CrossOrigin(origins = ["*"])
class McpApiController {

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
            encodeDefaults = true
        }
    }


    /**
     * Main MCP endpoint - handles JSON-RPC 2.0 requests
     * MCP (Model Context Protocol) uses JSON-RPC 2.0 over HTTP
     */
    @PostMapping("/", 
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun handleMcpRequest(@RequestBody request: String, httpRequest: HttpServletRequest): String {
        
        return try {
            val mcpRequest = json.decodeFromString<McpRequest>(request)
            
            when (mcpRequest.method) {
                "initialize" -> handleInitialize(mcpRequest.id)
                "tools/list" -> handleToolsList(mcpRequest.id)
                "tools/call" -> handleToolsCall(mcpRequest, mcpRequest.id, httpRequest)
                else -> createErrorResponse(mcpRequest.id, -32601, "Method not found: ${mcpRequest.method}")
            }
        } catch (e: Exception) {
            println("❌ MCP Error: ${e.message}")
            createErrorResponse(null, -32700, "Parse error: ${e.message}")
        }
    }

    private fun handleInitialize(id: Int?): String {
        val response = McpResponse(
            id = id,
            result = McpResult(
                protocolVersion = "2024-11-05",
                serverInfo = ServerInfo(
                    name = "Kotlin AI Engineering MCP Server",
                    version = "1.0.0"
                ),
                capabilities = Capabilities()
            )
        )
        return json.encodeToString(McpResponse.serializer(), response)
    }

    private fun handleToolsList(id: Int?): String {
        val tools = listOf(
            Tool(
                name = "hello_world",
                description = "Says hello to test MCP connection",
                inputSchema = InputSchema(
                    properties = mapOf(
                        "name" to PropertySchema(type = "string", description = "Name to greet")
                    )
                )
            ),
            Tool(
                name = "get_company_info",
                description = "Get company details and description for a stock symbol",
                inputSchema = InputSchema(
                    properties = mapOf(
                        "symbol" to PropertySchema(type = "string", description = "Stock symbol (e.g. AAPL, MSFT)")
                    )
                )
            ),
            Tool(
                name = "get_weather_nowcast",
                description = "Get high-precision nowcast weather for Nordic countries (Norway, Sweden, Denmark, Finland) - 5-minute resolution, 2-hour forecast",
                inputSchema = InputSchema(
                    properties = mapOf(
                        "latitude" to PropertySchema(type = "number", description = "Latitude (Nordic region: 55°N-75°N)"),
                        "longitude" to PropertySchema(type = "number", description = "Longitude (Nordic region: 0°E-35°E)")
                    )
                )
            ),
            Tool(
                name = "get_weather_forecast",
                description = "Get weather forecast for any global location - hourly resolution, multi-day forecast with pressure and cloud data",
                inputSchema = InputSchema(
                    properties = mapOf(
                        "latitude" to PropertySchema(type = "number", description = "Latitude (global coverage)"),
                        "longitude" to PropertySchema(type = "number", description = "Longitude (global coverage)")
                    )
                )
            ),
            Tool(
                name = "get_location_from_ip",
                description = "Get geographic location from IP address (defaults to client IP if not specified)",
                inputSchema = InputSchema(
                    properties = mapOf(
                        "ip" to PropertySchema(type = "string", description = "IP address (optional - uses client IP if not provided)")
                    )
                )
            ),
            Tool(
                name = "get_stock_price",
                description = "Get current stock price data (open, close, volume) for a symbol",
                inputSchema = InputSchema(
                    properties = mapOf(
                        "symbol" to PropertySchema(type = "string", description = "Stock symbol (e.g. AAPL, NHYDY)")
                    )
                )
            ),
            Tool(
                name = "get_random_quote",
                description = "Get a random inspirational quote",
                inputSchema = InputSchema(
                    properties = emptyMap()
                )
            ),
        )
        
        val response = McpResponse(
            id = id,
            result = McpResult(tools = tools)
        )
        return json.encodeToString(McpResponse.serializer(), response)
    }

    private fun handleToolsCall(mcpRequest: McpRequest, id: Int?, httpRequest: HttpServletRequest): String {
        val toolName = mcpRequest.params?.name
        val arguments = mcpRequest.params?.arguments
        
        return try {
            when (toolName) {
                "hello_world" -> {
                    val name = arguments?.get("name") ?: "World"
                    createSuccessResponse(id, "Hello, $name! 🎉 MCP is working from Kotlin Spring Boot!")
                }
                "get_company_info" -> {
                    val symbol = arguments?.get("symbol") ?: return createErrorResponse(id, -32602, "Missing symbol parameter")
                    runBlocking {
                        val stockInfo = polygon.getTickerDetails(symbol, debug = false)
                        val description = stockInfo.results.description ?: "No description for: ${stockInfo.results.name}"
                        createSuccessResponse(id, "🏢 $symbol: ${stockInfo.results.name}\n$description")
                    }
                }
                "get_weather_nowcast" -> {
                    val lat = arguments?.get("latitude")?.toDoubleOrNull() ?: return createErrorResponse(id, -32602, "Missing or invalid latitude")
                    val lon = arguments["longitude"]?.toDoubleOrNull() ?: return createErrorResponse(id, -32602, "Missing or invalid longitude")
                    runBlocking {
                        val weatherData = weather.getNowcast(lat, lon, debug = false)
                        val current = weather.getCurrentWeather(weatherData)
                        val summary = current?.let { weather.formatWeatherSummary(it) } ?: "Nowcast data not available"
                        createSuccessResponse(id, "⚡ $summary")
                    }
                }
                "get_weather_forecast" -> {
                    val lat = arguments?.get("latitude")?.toDoubleOrNull() ?: return createErrorResponse(id, -32602, "Missing or invalid latitude")
                    val lon = arguments["longitude"]?.toDoubleOrNull() ?: return createErrorResponse(id, -32602, "Missing or invalid longitude")
                    runBlocking {
                        val forecastData = weather.getLocationForecast(lat, lon, debug = false)
                        val current = weather.getCurrentForecast(forecastData)
                        val summary = current?.let { weather.formatForecastSummary(it) } ?: "Forecast data not available"
                        createSuccessResponse(id, "🌍 $summary")
                    }
                }
                "get_location_from_ip" -> {
                    val rawIp = arguments?.get("ip") ?: getClientIpAddress(httpRequest)
                    // Use fallback IP for local testing (IPv6 localhost won't work with geolocation API)
                    val ip = if (rawIp == "0:0:0:0:0:0:0:1" || rawIp == "127.0.0.1" || rawIp == "::1") {
                        "8.8.8.8" // Google DNS as fallback for demo purposes
                    } else {
                        rawIp
                    }
                    runBlocking {
                        val location = geolocation.getLocationByIp(ip, debug = false)
                        val summary = geolocation.formatLocationSummary(location)
                        val note = if (ip == "8.8.8.8") " (Demo: Using Google DNS location for local testing)" else ""
                        createSuccessResponse(id, "📍 $summary$note")
                    }
                }
                "get_stock_price" -> {
                    val symbol = arguments?.get("symbol") ?: return createErrorResponse(id, -32602, "Missing symbol parameter")
                    runBlocking {
                        val aggregates = polygon.getAggregates(symbol, debug = false)
                        val latest = aggregates.firstOrNull()?.results?.lastOrNull()
                        if (latest != null) {
                            createSuccessResponse(id, "📈 $symbol ${latest.formattedDate()}: Open $${latest.openPrice}, Close $${latest.closePrice}, Volume ${latest.volume}")
                        } else {
                            createSuccessResponse(id, "📈 $symbol: No recent price data available")
                        }
                    }
                }
                "get_random_quote" -> {
                    runBlocking {
                        val response = openAI.createChatCompletion(
                            prompt = "Generate a random inspirational quote. Return only the quote with attribution, nothing else.",
                            maxTokens = 100,
                            temperature = 0.9,
                            debug = false
                        )
                        val quote = response.choices.firstOrNull()?.message?.content ?: "Inspiration comes from within."
                        createSuccessResponse(id, "✨ $quote")
                    }
                }
                else -> createErrorResponse(id, -32602, "Unknown tool: $toolName")
            }
        } catch (e: Exception) {
            createErrorResponse(id, -32603, "Tool execution failed: ${e.message}")
        }
    }

    private fun createSuccessResponse(id: Int?, text: String): String {
        val response = McpResponse(
            id = id,
            result = McpResult(
                content = listOf(Content(text = text))
            )
        )
        return json.encodeToString(McpResponse.serializer(), response)
    }

    private fun createErrorResponse(id: Int?, code: Int, message: String): String {
        val response = McpResponse(
            id = id,
            error = McpError(code = code, message = message)
        )
        return json.encodeToString(McpResponse.serializer(), response)
    }
}