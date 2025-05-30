package com.vend.fmr.aieng.controller

import com.vend.fmr.aieng.OPEN_AI_KEY
import com.vend.fmr.aieng.POLYGON_API_KEY
import com.vend.fmr.aieng.openai.OpenAI
import com.vend.fmr.aieng.polygon.AggregatesResponse
import com.vend.fmr.aieng.polygon.Polygon
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/demo/stock")
class StockController {

    @GetMapping
    fun stockDemo(model: Model): String {
        model.addAttribute("pageTitle", "Stock Data Analysis")
        model.addAttribute("activeTab", "stock")
        return "stock-demo"
    }

    @PostMapping
    suspend fun processStockQuery(
        @RequestParam(defaultValue = "AAPL,MSFT,GOOGL") tickers: String,
        @RequestParam(defaultValue = "3") daysBack: Int,
        model: Model
    ): String {
        model.addAttribute("pageTitle", "Stock Data Analysis")
        model.addAttribute("activeTab", "stock")
        if (tickers.isNotBlank()) {
            try {
                val openAI = OpenAI(OPEN_AI_KEY)
                val polygon = Polygon(POLYGON_API_KEY)
                
                val tickerList = tickers.split(",").map { it.trim().uppercase() }.toTypedArray()
                
                // Step 1: Fetch stock data from Polygon API
                val aggregates = polygon.getAggregates(
                    tickers = tickerList,
                    multiplier = 1,
                    timespan = "day",
                    from = java.time.LocalDate.now().minusDays(daysBack.toLong()).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    to = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                )
                
                // Step 2: Convert to JSON for AI processing
                val jsonData = polygon.aggregatesToJson(aggregates)
                
                // Step 3: Generate AI analysis
                val systemMessage = "You handle financial information. " +
                    "The input will be data in JSON fetched from the polygon.io api - " +
                    "more specifically https://api.polygon.io/v2/aggs/. " +
                    "the format will be in json " +
                    "It may contain data for multiple companies/tickers. " +
                    "Your task is to create a short report based on these data to be shown to the end user. " +
                    "The inputs are known to the user, so no need to repeat this. " +
                    "If multiple companies are in the input focus on the main differences and what sets them apart. " +
                    "Try to use fewer than 100 words."
                
                val response = openAI.createChatCompletion(
                    prompt = jsonData,
                    systemMessage = systemMessage
                )
                
                openAI.close()
                polygon.close()
                
                model.addAttribute("stockResult", StockResult(
                    requestedTickers = tickerList.toList(),
                    daysBack = daysBack,
                    rawData = aggregates,
                    jsonData = jsonData,
                    dataPoints = aggregates.sumOf { it.results?.size ?: 0 },
                    analysisReport = response.text(),
                    usage = response.usage()
                ))
                model.addAttribute("formData", StockFormData(tickers, daysBack))
                
            } catch (e: Exception) {
                model.addAttribute("error", "Error: ${e.message}")
                model.addAttribute("formData", StockFormData(tickers, daysBack))
            }
        }
        
        return "stock-demo"
    }
}

data class StockResult(
    val requestedTickers: List<String>,
    val daysBack: Int,
    val rawData: List<AggregatesResponse>,
    val jsonData: String,
    val dataPoints: Int,
    val analysisReport: String,
    val usage: String
)

data class StockFormData(
    val tickers: String,
    val daysBack: Int
)