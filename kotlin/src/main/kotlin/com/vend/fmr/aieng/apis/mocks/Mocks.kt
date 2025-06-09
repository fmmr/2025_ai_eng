package com.vend.fmr.aieng.apis.mocks

import java.time.format.DateTimeFormatter

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
    
    fun getStockPrice(ticker: String): StockInfo {
        return getMockStockPrice(ticker)
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
        val now = java.time.ZonedDateTime.now()
        val utc = now.withZoneSameInstant(java.time.ZoneId.of("UTC"))
        return """
            Server time: ${now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))} ${now.zone}
            UTC time: ${utc.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))} UTC
        """.trimIndent()
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
    
    
}