package com.vend.fmr.aieng.apis.polygon

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StockTicker(
    val ticker: String,
    val name: String,
    val market: String,
    val locale: String,
    val type: String,
    @SerialName("primary_exchange") val primaryExchange: String? = null,
    val active: Boolean,
    @SerialName("currency_name") val currencyName: String? = null,
    val cik: String? = null,
    @SerialName("composite_figi") val compositeFigi: String? = null,
    @SerialName("share_class_figi") val shareClassFigi: String? = null
)

@Serializable
data class TickersResponse(
    val results: List<StockTicker>? = null,
    val status: String,
    @SerialName("request_id") val requestId: String,
    val count: Int? = null,
    @SerialName("next_url") val nextUrl: String? = null
)

@Serializable
data class StockQuote(
    val ticker: String,
    @SerialName("last_quote") val lastQuote: QuoteData? = null,
    val status: String
)

@Serializable
data class QuoteData(
    @SerialName("last_updated") val lastUpdated: Long,
    val timeframe: String,
    val bid: Double? = null,
    @SerialName("bid_size") val bidSize: Long? = null,
    val ask: Double? = null,
    @SerialName("ask_size") val askSize: Long? = null,
    @SerialName("exchange") val exchange: Int? = null
)

@Serializable
data class AggregateBar(
    @SerialName("o") val openPrice: Double,
    @SerialName("h") val highPrice: Double,
    @SerialName("l") val lowPrice: Double,
    @SerialName("c") val closePrice: Double,
    @SerialName("v") val volume: Double,
    @SerialName("t") val timestamp: Long,
    @SerialName("n") val numberOfTransactions: Int? = null,
    @SerialName("vw") val volumeWeightedAveragePrice: Double? = null
) {
    fun formattedDate(): String =
        java.time.Instant.ofEpochMilli(timestamp)
            .atZone(java.time.ZoneId.systemDefault())
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}

@Serializable
data class AggregatesResponse(
    val ticker: String,
    @SerialName("queryCount") val queryCount: Int,
    @SerialName("resultsCount") val resultsCount: Int,
    val adjusted: Boolean,
    val results: List<AggregateBar>? = null,
    val status: String,
    @SerialName("request_id") val requestId: String,
    val count: Int? = null
)

@Serializable
data class StockDetails(
    val ticker: String,
    val name: String,
    val description: String? = null,
    @SerialName("market_cap") val marketCap: Double? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    val address: Address? = null,
    val homepage: String? = null,
    @SerialName("total_employees") val totalEmployees: Int? = null,
    @SerialName("list_date") val listDate: String? = null,
    val branding: Branding? = null,
    @SerialName("share_class_shares_outstanding") val shareClassSharesOutstanding: Long? = null,
    @SerialName("weighted_shares_outstanding") val weightedSharesOutstanding: Long? = null
)

@Serializable
data class Address(
    val address1: String? = null,
    val city: String? = null,
    val state: String? = null,
    @SerialName("postal_code") val postalCode: String? = null
)

@Serializable
data class Branding(
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("icon_url") val iconUrl: String? = null
)

@Serializable
data class TickerDetailsResponse(
    val results: StockDetails,
    val status: String,
    @SerialName("request_id") val requestId: String
)