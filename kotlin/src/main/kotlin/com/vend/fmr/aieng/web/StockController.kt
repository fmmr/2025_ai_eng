package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.apis.openai.OpenAI
import com.vend.fmr.aieng.apis.polygon.AggregatesResponse
import com.vend.fmr.aieng.apis.polygon.Polygon
import com.vend.fmr.aieng.utils.Demo
import com.vend.fmr.aieng.utils.Prompts
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/demo/stock")
class StockController(
    private val openAI: OpenAI,
    private val polygon: Polygon
) : BaseController(Demo.STOCK_DATA) {

    @GetMapping
    fun stockDemo(): String {
        return "stock-demo"
    }

    @PostMapping
    suspend fun processStockQuery(
        @RequestParam(defaultValue = "AAPL,MSFT,GOOGL") tickers: String,
        @RequestParam(defaultValue = "3") daysBack: Int,
        model: Model
    ): String {
        if (tickers.isNotBlank()) {
            try {
                
                val tickerList = tickers.split(",").map { it.trim().uppercase() }.toTypedArray()
                
                val aggregates = polygon.getAggregates(
                    tickers = tickerList,
                    multiplier = 1,
                    timespan = "day",
                    from = java.time.LocalDate.now().minusDays(daysBack.toLong()).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    to = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                )
                
                val jsonData = polygon.aggregatesToJson(aggregates)
                
                val response = openAI.createChatCompletion(
                    prompt = jsonData,
                    systemMessage = Prompts.FINANCIAL_ANALYSIS
                )
                
                
                model.addAttribute("stockResult", StockResult(
                    requestedTickers = tickerList.toList(),
                    daysBack = daysBack,
                    rawData = aggregates,
                    jsonData = jsonData,
                    dataPoints = aggregates.sumOf { it.results?.size ?: 0 },
                    systemMessage = Prompts.FINANCIAL_ANALYSIS,
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
    val systemMessage: String,
    val analysisReport: String,
    val usage: String
)

data class StockFormData(
    val tickers: String,
    val daysBack: Int
)