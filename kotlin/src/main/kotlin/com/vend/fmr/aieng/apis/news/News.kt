package com.vend.fmr.aieng.apis.news

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service

@Service
class News(
    private val httpClient: HttpClient,
    private val json: Json
) {

    companion object {
        private const val BASE_URL = "https://newsapi.org/v2"
        private val API_KEY = System.getenv("NEWS_API_KEY") ?: "demo"
    }

    /**
     * Get top headlines from news sources
     */
    suspend fun getTopHeadlines(country: String = "us", category: String? = null, debug: Boolean = false): NewsResponse {
        val url = "$BASE_URL/top-headlines"
        
        if (debug) {
            println("📰 News API Request: $url")
            println("🌍 Country: $country, Category: ${category ?: "all"}")
        }
        
        val response = httpClient.get(url) {
            parameter("apiKey", API_KEY)
            parameter("country", country)
            parameter("pageSize", 10)
            if (category != null) {
                parameter("category", category)
            }
        }
        
        val responseText = response.body<String>()
        if (debug) {
            println("📡 News API Response: $responseText")
        }
        
        return json.decodeFromString<NewsResponse>(responseText)
    }

    /**
     * Get formatted news headlines summary
     */
    suspend fun getHeadlinesSummary(country: String = "us", category: String? = null, debug: Boolean = false): String {
        return try {
            val newsResponse = getTopHeadlines(country, category, debug)
            
            if (newsResponse.status != "ok") {
                return "Sorry, couldn't retrieve news headlines at this time."
            }
            
            val categoryText = category?.let { " ($it)" } ?: ""
            val headlines = newsResponse.articles.take(5).mapIndexed { index, article ->
                "${index + 1}. ${article.title} - ${article.source.name}"
            }
            
            """
            📰 Top Headlines${categoryText} (${country.uppercase()})
            
            ${headlines.joinToString("\n")}
            
            Total articles: ${newsResponse.totalResults}
            """.trimIndent()
            
        } catch (e: Exception) {
            if (debug) {
                println("❌ News API Error: ${e.message}")
            }
            "Sorry, I couldn't retrieve news headlines. Please try again later."
        }
    }
}