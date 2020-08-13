package de.schnettler.database

import de.schnettler.database.models.EntityInfo
import de.schnettler.database.models.EntityWithStats
import de.schnettler.database.models.EntityWithStatsAndInfo
import de.schnettler.database.models.LastFmEntity
import de.schnettler.database.models.Stats
import kotlin.random.Random

object DataGenerator {
    fun generateAlbums(number: Int, artistName: String? = null) = List(number) {
        generateAlbum(it, artistName)
    }

    fun generateAlbum(current: Int = 0, artist: String?) =
        LastFmEntity.Album("album$current", "url$current", artist ?: "artist$current")

    fun generateAlbumsWithStats(count: Int, artist: String?) = List(count) {
        val album = generateAlbum(it, artist)
        EntityWithStats.AlbumWithStats(
            album,
            generateStats(album.id)
        )
    }

    fun generateAlbumWithStatsAndInfo(count: Int, artist: String?) = List(count) {
        val album = generateAlbum(it, artist)
        EntityWithStatsAndInfo.AlbumWithStatsAndInfo(
            album,
            generateStats(album.id),
            generateInfo(album.id)
        )
    }

    fun generateArtistWithStatsAndInfo(count: Int) = List(count) {
        val artist = generateArtist(it)
        EntityWithStatsAndInfo.ArtistWithStatsAndInfo(
            artist,
            generateStats(artist.id),
            generateInfo(artist.id)
        )
    }

    private fun generateStats(forId: String) = Stats(forId, Random.nextLong(10))

    private fun generateInfo(forId: String) = EntityInfo(forId, emptyList(), 10, "wiki")

    fun generateArtists(number: Int, prefix: String = "artist") = List(number) {
        generateArtist(it, prefix)
    }

    private fun generateArtist(current: Int = 0, prefix: String = "artist") =
        LastFmEntity.Artist("$prefix$current", "url$current")
}
