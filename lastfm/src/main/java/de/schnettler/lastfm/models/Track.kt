package de.schnettler.lastfm.models

import com.squareup.moshi.Json

data class TrackDto(
    override val name: String,
    override val mbid: String?,
    override val url: String,
    val playcount: Long?,
    val listeners: Long?,
    val artist: ListingDto
): ListingDto(name, mbid, url)

data class TrackWithAlbumDto(
    override val name: String,
    override val mbid: String,
    override val url: String,
    val artist: TrackRelationDto,
    val album: TrackRelationDto
): ListingDto(name, mbid, url)

data class TrackRelationDto(
    @Json(name = "#text") val name: String,
    val mbid: String
)

data class ImageDto(
    val size: String,
    @Json(name = "#text") val url: String
)

data class LovedTracksResponse(
    val track: List<TrackDto>,
    @Json(name = "@attr") val info: ResponseInfo
)