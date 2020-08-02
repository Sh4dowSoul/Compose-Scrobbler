package de.schnettler.repo.mapping

import de.schnettler.database.models.EntityInfo
import de.schnettler.database.models.EntityWithStatsAndInfo.TrackWithStatsAndInfo
import de.schnettler.database.models.LastFmEntity.Track
import de.schnettler.database.models.LocalTrack
import de.schnettler.database.models.ScrobbleStatus
import de.schnettler.database.models.Stats
import de.schnettler.lastfm.models.RecentTracksDto
import de.schnettler.lastfm.models.TrackInfoDto
import javax.inject.Inject

class TrackMapper @Inject constructor() : Mapper<TrackInfoDto, TrackWithStatsAndInfo> {
    override suspend fun map(from: TrackInfoDto): TrackWithStatsAndInfo {
        val track = Track(
            name = from.name,
            url = from.url,
            artist = from.artist.name,
            album = from.album?.title
        )
        val stats = Stats(
            id = track.id,
            plays = from.playcount,
            listeners = from.listeners,
            userPlays = from.userplaycount ?: 0
        )
        val info = EntityInfo(
            id = track.id,
            tags = from.toptags.tag.map { tag -> tag.name },
            wiki = from.wiki?.content ?: from.wiki?.summary
        )
        return TrackWithStatsAndInfo(track, stats, info)
    }
}

fun RecentTracksDto.mapToLocal() = LocalTrack(
    name = name,
    artist = artist.name,
    album = album.name,
    duration = 1,
    timestamp = date?.uts ?: -1,
    playedBy = "external",
    status = if (date != null) ScrobbleStatus.EXTERNAL else ScrobbleStatus.PLAYING
)