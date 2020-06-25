package de.schnettler.scrobbler.screens.details

import androidx.compose.Composable
import androidx.ui.core.ContentScale
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.foundation.*
import androidx.ui.foundation.shape.corner.CircleShape
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.layout.*
import androidx.ui.material.Card
import androidx.ui.material.ListItem
import androidx.ui.material.Surface
import androidx.ui.res.colorResource
import androidx.ui.unit.dp
import de.schnettler.database.models.Artist
import de.schnettler.scrobbler.AppRoute
import de.schnettler.scrobbler.BackStack
import de.schnettler.scrobbler.R
import de.schnettler.scrobbler.components.ExpandingSummary
import de.schnettler.scrobbler.components.ListingScroller
import de.schnettler.scrobbler.components.PlaysStyle
import de.schnettler.scrobbler.components.TitleComponent
import de.schnettler.scrobbler.screens.formatter
import de.schnettler.scrobbler.util.cardCornerRadius
import de.schnettler.scrobbler.util.defaultSpacerSize
import dev.chrisbanes.accompanist.coil.CoilImageWithCrossfade
import timber.log.Timber

@Composable
fun ArtistDetailScreen(artist: Artist) {
    VerticalScroller() {
        Backdrop(imageUrl = artist.imageUrl, modifier = Modifier.aspectRatio(16 / 10f))

        Card(border = Border(1.dp, colorResource(id = R.color.colorStroke)), modifier = Modifier.padding(
            defaultSpacerSize
        ) + Modifier.fillMaxWidth(), shape = RoundedCornerShape(
            cardCornerRadius
        )
        ) {
            ExpandingSummary(artist.bio, modifier = Modifier.padding(defaultSpacerSize))
        }

        StatsRow(artist)

        TagCategory(tags = artist.tags)

        TitleComponent(title = "Top Tracks")
        val backstack = BackStack.current
        val context = ContextAmbient.current
        artist.topTracks.forEachIndexed { index, track ->
            ListItem(
                text = { Text(track.name) },
                secondaryText = {
                    Text(formatter.format(track.listeners).toString() + " Hörer")
                },
                icon = {
                    Surface(
                        color = colorResource(id = R.color.colorBackgroundElevated),
                        shape = CircleShape,
                        modifier = Modifier.preferredHeight(40.dp) + Modifier.preferredWidth(40.dp)) {
                        Box(gravity = ContentGravity.Center) {
                            Text(text = "${index +1}")
                        }
                    }
                }, onClick = {
                    backstack.push(AppRoute.DetailRoute(track, context))
                }
            )
        }
        ListingScroller(
            title = "Top Albums",
            content = artist.topAlbums.sortedByDescending { it.plays },
            width = 136.dp,
            height = 136.dp,
            playsStyle = PlaysStyle.PUBLIC_PLAYS
        ) { Timber.d("Selected $it") }

        ListingScroller(
            title = "Ähnliche Künstler",
            content = artist.similarArtists,
            width = 104.dp,
            height = 104.dp,
            playsStyle = PlaysStyle.NO_PLAYS
        ) { Timber.d("Selected $it") }
    }
}

@Composable
private fun Backdrop(
    imageUrl: String?,
    modifier: Modifier
) {
    Surface(modifier = modifier) {
        Stack(Modifier.fillMaxSize()) {
            imageUrl?.let {
                CoilImageWithCrossfade(
                    data = it,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}