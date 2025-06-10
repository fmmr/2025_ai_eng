package com.vend.fmr.aieng.apis.spotify

import com.vend.fmr.aieng.utils.env
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service
import java.util.*

@Service
class Spotify(private val client: HttpClient, private val json: Json) : Closeable {
    
    private val clientId = "SPOTIFY_CLIENT_ID".env()
    private val clientSecret = "SPOTIFY_CLIENT_SECRET".env()
    private var accessToken: String? = null
    private var tokenExpiresAt: Long = 0
    
    /**
     * Get access token using client credentials flow
     */
    private suspend fun getAccessToken(): String {
        if (accessToken != null && System.currentTimeMillis() < tokenExpiresAt) {
            return accessToken!!
        }
        
        val credentials = Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())
        
        val response = client.post("https://accounts.spotify.com/api/token") {
            header("Authorization", "Basic $credentials")
            setBody(FormDataContent(Parameters.build {
                append("grant_type", "client_credentials")
            }))
        }
        
        val responseText = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw Exception("Spotify token request failed: ${response.status} - $responseText")
        }
        
        val tokenResponse = json.decodeFromString<SpotifyTokenResponse>(responseText)
        accessToken = tokenResponse.accessToken
        tokenExpiresAt = System.currentTimeMillis() + (tokenResponse.expiresIn * 1000) - 60000 // 1 min buffer
        
        return accessToken!!
    }
    
    /**
     * Search for tracks, artists, or albums
     */
    suspend fun search(query: String, type: String = "track", limit: Int = 5, debug: Boolean = false): SpotifySearchResponse {
        val token = getAccessToken()
        
        val response = client.get("https://api.spotify.com/v1/search") {
            header("Authorization", "Bearer $token")
            parameter("q", query)
            parameter("type", type) // track, artist, album
            parameter("limit", limit)
        }
        
        val responseText = response.bodyAsText()
        if (debug) {
            println("Spotify Search Response: $responseText")
        }
        
        if (!response.status.isSuccess()) {
            throw Exception("Spotify search failed: ${response.status} - $responseText")
        }
        
        return json.decodeFromString<SpotifySearchResponse>(responseText)
    }
    
    /**
     * Search for tracks and format as user-friendly summary
     */
    fun searchTracksSummary(query: String, debug: Boolean = false): String {
        return try {
            kotlinx.coroutines.runBlocking {
                val searchResults = search(query, "track", 3, debug)
                val tracks = searchResults.tracks?.items ?: emptyList()
                
                if (tracks.isEmpty()) {
                    "🎵 No tracks found for '$query'"
                } else {
                    val trackList = tracks.joinToString("\n") { track ->
                        val artists = track.artists.joinToString(", ") { it.name }
                        val duration = formatDuration(track.durationMs)
                        "🎵 \"${track.name}\" by $artists ($duration) - ${track.album.name}"
                    }
                    "🎵 Found ${tracks.size} tracks for '$query':\n$trackList"
                }
            }
        } catch (e: Exception) {
            "🎵 Search failed: ${e.message}"
        }
    }
    
    /**
     * Get popular/trending tracks by searching recent music
     */
    fun getPopularTracksSummary(genre: String? = null, debug: Boolean = false): String {
        return try {
            kotlinx.coroutines.runBlocking {
                val query = buildString {
                    append("year:2024")
                    if (!genre.isNullOrBlank()) {
                        append(" genre:$genre")
                    }
                }
                
                val searchResults = search(query, "track", 5, debug)
                val tracks = searchResults.tracks?.items ?: emptyList()
                
                if (tracks.isEmpty()) {
                    "🎵 No popular tracks found" + if (genre != null) " for genre '$genre'" else ""
                } else {
                    val genreText = if (genre != null) " $genre" else ""
                    val trackList = tracks.joinToString("\n") { track ->
                        val artists = track.artists.joinToString(", ") { it.name }
                        val duration = formatDuration(track.durationMs)
                        val popularity = "★".repeat((track.popularity / 20).coerceAtMost(5))
                        "🎵 \"${track.name}\" by $artists ($duration) $popularity"
                    }
                    "🔥 Popular${genreText} tracks (2024):\n$trackList"
                }
            }
        } catch (e: Exception) {
            "🎵 Popular tracks search failed: ${e.message}"
        }
    }
    
    /**
     * Search for artists and format as user-friendly summary
     */
    fun searchArtistsSummary(query: String, debug: Boolean = false): String {
        return try {
            kotlinx.coroutines.runBlocking {
                val searchResults = search(query, "artist", 3, debug)
                val artists = searchResults.artists?.items ?: emptyList()
                
                if (artists.isEmpty()) {
                    "🎤 No artists found for '$query'"
                } else {
                    val artistList = artists.joinToString("\n") { artist ->
                        val genres = if (artist.genres.isNotEmpty()) {
                            " (${artist.genres.take(2).joinToString(", ")})"
                        } else ""
                        val followers = artist.followers?.total?.let { " - ${formatNumber(it)} followers" } ?: ""
                        "🎤 ${artist.name}$genres$followers"
                    }
                    "🎤 Found ${artists.size} artists for '$query':\n$artistList"
                }
            }
        } catch (e: Exception) {
            "🎤 Artist search failed: ${e.message}"
        }
    }
    
    private fun formatDuration(durationMs: Int): String {
        val minutes = durationMs / 60000
        val seconds = (durationMs % 60000) / 1000
        return "%d:%02d".format(minutes, seconds)
    }
    
    private fun formatNumber(number: Int): String {
        return when {
            number >= 1_000_000 -> "%.1fM".format(number / 1_000_000.0)
            number >= 1_000 -> "%.1fK".format(number / 1_000.0)
            else -> number.toString()
        }
    }
    
    override fun close() {
        // HTTP client is managed by Spring, no need to close
    }
}