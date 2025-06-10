package com.vend.fmr.aieng.apis.spotify

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpotifyTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Int
)

@Serializable
data class SpotifySearchResponse(
    val tracks: SpotifyTracks? = null,
    val artists: SpotifyArtists? = null,
    val albums: SpotifyAlbums? = null
)

@Serializable
data class SpotifyTracks(
    val items: List<SpotifyTrack>
)

@Serializable
data class SpotifyArtists(
    val items: List<SpotifyArtist>
)

@Serializable
data class SpotifyAlbums(
    val items: List<SpotifyAlbum>
)

@Serializable
data class SpotifyTrack(
    val id: String,
    val name: String,
    val artists: List<SpotifyArtist>,
    val album: SpotifyAlbum,
    @SerialName("duration_ms") val durationMs: Int,
    val popularity: Int,
    @SerialName("external_urls") val externalUrls: SpotifyExternalUrls,
    @SerialName("preview_url") val previewUrl: String? = null
)

@Serializable
data class SpotifyArtist(
    val id: String,
    val name: String,
    val genres: List<String> = emptyList(),
    val popularity: Int = 0,
    val followers: SpotifyFollowers? = null,
    @SerialName("external_urls") val externalUrls: SpotifyExternalUrls? = null,
    val images: List<SpotifyImage> = emptyList()
)

@Serializable
data class SpotifyAlbum(
    val id: String,
    val name: String,
    @SerialName("album_type") val albumType: String,
    @SerialName("release_date") val releaseDate: String,
    @SerialName("total_tracks") val totalTracks: Int,
    val artists: List<SpotifyArtist>,
    @SerialName("external_urls") val externalUrls: SpotifyExternalUrls,
    val images: List<SpotifyImage> = emptyList()
)

@Serializable
data class SpotifyFollowers(
    val total: Int
)

@Serializable
data class SpotifyExternalUrls(
    val spotify: String
)

@Serializable
data class SpotifyImage(
    val url: String,
    val height: Int? = null,
    val width: Int? = null
)