@file:Suppress("unused", "RedundantSuspendModifier")

package com.vend.fmr.aieng.examples

import com.vend.fmr.aieng.apis.spotify.Spotify

/**
 * Spotify API Examples - demonstrate music search capabilities
 */
object SpotifyExamples {
    
    suspend fun searchTracksExample(spotify: Spotify): String {
        println("🎵 Spotify Track Search Example")
        println("=" .repeat(40))
        
        val query = "Bohemian Rhapsody"
        println("Searching for: $query")
        
        val result = spotify.searchTracksSummary(query, debug = true)
        println("\nResult:")
        println(result)
        
        return result
    }
    
    suspend fun searchArtistsExample(spotify: Spotify): String {
        println("🎤 Spotify Artist Search Example") 
        println("=" .repeat(40))
        
        val query = "Taylor Swift"
        println("Searching for: $query")
        
        val result = spotify.searchArtistsSummary(query, debug = true)
        println("\nResult:")
        println(result)
        
        return result
    }
}

suspend fun main() {
//    val json = createJson()
//    val spotify = Spotify(createHttpClient(json), json)
//    searchArtistsExample(spotify)
//    searchTracksExample(spotify)
//    // This would work if we had proper dependency injection setup
//    // For now, it's just an example of how clean the API is
    println("Spotify Examples - check AgentTool.SEARCH_TRACKS and AgentTool.SEARCH_ARTISTS!")
}