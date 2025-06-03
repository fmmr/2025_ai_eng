package com.vend.fmr.aieng.apis.polygon

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Suppress("unused")
class Polygon(private val apiKey: String) : Closeable {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.NONE
        }
    }

    suspend fun getTickers(
        ticker: String? = null,
        type: String? = null,
        market: String? = null,
        exchange: String? = null,
        cusip: String? = null,
        cik: String? = null,
        date: String? = null,
        search: String? = null,
        active: Boolean? = null,
        limit: Int = 100,
        sort: String? = null,
        order: String? = null,
        debug: Boolean = false
    ): TickersResponse {
        val url = "https://api.polygon.io/v3/reference/tickers"

        val response = client.get(url) {
            parameter("apikey", apiKey)
            ticker?.let { parameter("ticker", it) }
            type?.let { parameter("type", it) }
            market?.let { parameter("market", it) }
            exchange?.let { parameter("exchange", it) }
            cusip?.let { parameter("cusip", it) }
            cik?.let { parameter("cik", it) }
            date?.let { parameter("date", it) }
            search?.let { parameter("search", it) }
            active?.let { parameter("active", it) }
            parameter("limit", limit)
            sort?.let { parameter("sort", it) }
            order?.let { parameter("order", it) }
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("Status: ${response.status}")
            println("Raw API response: $responseText")
        }

        if (!response.status.isSuccess()) {
            throw Exception("API request failed with status ${response.status}: $responseText")
        }

        return json.decodeFromString<TickersResponse>(responseText)
    }

    suspend fun getTickerDetails(
        ticker: String,
        date: String? = null,
        debug: Boolean = false
    ): TickerDetailsResponse {
        val url = "https://api.polygon.io/v3/reference/tickers/$ticker"

        val response = client.get(url) {
            parameter("apikey", apiKey)
            date?.let { parameter("date", it) }
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("Status: ${response.status}")
            println("Raw API response: $responseText")
        }

        if (!response.status.isSuccess()) {
            throw Exception("API request failed with status ${response.status}: $responseText")
        }

        return json.decodeFromString<TickerDetailsResponse>(responseText)
    }

    suspend fun getLastQuote(
        ticker: String,
        debug: Boolean = false
    ): StockQuote {
        val url = "https://api.polygon.io/v2/last/nbbo/$ticker"

        val response = client.get(url) {
            parameter("apikey", apiKey)
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("Status: ${response.status}")
            println("Raw API response: $responseText")
        }

        if (!response.status.isSuccess()) {
            throw Exception("API request failed with status ${response.status}: $responseText")
        }

        return json.decodeFromString<StockQuote>(responseText)
    }

    suspend fun getAggregates(
        vararg tickers: String = arrayOf("MSFT", "NHYDY", "TSLA"),
        multiplier: Int = 1,
        timespan: String = "day",
        from: String = LocalDate.now().minusDays(3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
        to: String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
        adjusted: Boolean = true,
        sort: String = "asc",
        limit: Int = 120,
        debug: Boolean = false
    ): List<AggregatesResponse> {
        return tickers.map { ticker ->
            val url = "https://api.polygon.io/v2/aggs/ticker/$ticker/range/$multiplier/$timespan/$from/$to"

            val response = client.get(url) {
                parameter("apikey", apiKey)
                parameter("adjusted", adjusted)
                parameter("sort", sort)
                parameter("limit", limit)
            }

            val responseText = response.bodyAsText()
            if (debug) {
                println("Ticker: $ticker")
                println("Status: ${response.status}")
                println("Raw API response: $responseText")
                println("---")
            }

            if (!response.status.isSuccess()) {
                throw Exception("API request failed for ticker $ticker with status ${response.status}: $responseText")
            }

            json.decodeFromString<AggregatesResponse>(responseText)
        }
    }

    suspend fun getDailyPrices(
        vararg tickers: String,
        daysBack: Int = 30,
        debug: Boolean = false
    ): List<AggregatesResponse> {
        val to = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val from = LocalDate.now().minusDays(daysBack.toLong()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        return getAggregates(
            tickers = tickers,
            multiplier = 1,
            timespan = "day",
            from = from,
            to = to,
            debug = debug
        )
    }

    fun aggregatesToJson(aggregates: List<AggregatesResponse>): String {
        return json.encodeToString(aggregates)
    }

    override fun close() {
        client.close()
    }
}