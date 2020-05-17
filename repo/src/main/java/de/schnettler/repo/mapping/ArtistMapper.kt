package de.schnettler.repo.mapping

import de.schnettler.database.models.*
import de.schnettler.lastfm.models.*


object ArtistMapper : Mapper<ArtistDto, Artist> {
    override suspend fun map(from: ArtistDto): Artist = Artist(
            from.name,
            from.playcount,
            from.listeners ?: 0,
            from.mbid,
            from.url,
            from.streamable
    )
}

object AlbumMapper : Mapper<AlbumDto, Album> {
    override suspend fun map(from: AlbumDto): Album = Album(
        name = from.name,
        artist = from.artist.name,
        mbid = from.mbid,
        playcount = from.playcount,
        url = from.url,
        imageUrl = from.images[3].url
    )
}

object SessionMapper: Mapper<SessionDto, Session> {
    override suspend fun map(from: SessionDto): Session = Session(
        from.name,
        from.key,
        System.currentTimeMillis()
    )
}

object UserMapper: Mapper<UserDto, User> {
    override suspend fun map(from: UserDto): User {
        val user = User(
            name = from.name,
            playcount = from.playcount,
            url = from.url,
            countryCode = from.country,
            age = from.age,
            realname = from.realname,
            registerDate = from.registerDate.unixtime,
            imageUrl = from.image[3].url
        )
        return user
    }
}

object TrackMapper: Mapper<TrackDto, Track> {
    override suspend fun map(from: TrackDto) = Track(
        name = from.name,
        id = from.mbid,
        artist = from.artist.name,
        playcount = from.playcount
    )
}

object TrackWithAlbumMapper: Mapper<TrackWithAlbumDto, Track> {
    override suspend fun map(from: TrackWithAlbumDto) = Track(
        name = from.name,
        id = from.mbid,
        album = from.album.name,
        artist = from.artist.name
    )
}

object ArtistInfoMapper: Mapper<ArtistInfoDto, ArtistInfo> {
    override suspend fun map(from: ArtistInfoDto) = ArtistInfo(
        name = from.name,
        bio = from.bio.content,
        similar = from.similar.artist.map { artist ->
            ArtistMin(name = artist.name, url = artist.url)
        },
        tags = from.tags.tag.map { tag -> tag.name },
        playcount = from.stats.playcount,
        listeners = from.stats.listeners
    )
}