package de.schnettler.scrobbler.screens.details

import androidx.compose.Composable
import androidx.ui.core.Modifier
import androidx.ui.foundation.*
import androidx.ui.foundation.shape.corner.CircleShape
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.layout.fillMaxWidth
import androidx.ui.layout.padding
import androidx.ui.layout.preferredHeight
import androidx.ui.layout.preferredWidth
import androidx.ui.material.Card
import androidx.ui.material.ListItem
import androidx.ui.material.Surface
import androidx.ui.material.ripple.ripple
import androidx.ui.res.colorResource
import androidx.ui.unit.dp
import androidx.ui.unit.sp
import de.schnettler.database.models.Artist
import de.schnettler.scrobbler.BackStack
import de.schnettler.scrobbler.R
import de.schnettler.scrobbler.Screen
import de.schnettler.scrobbler.components.ExpandingSummary
import de.schnettler.scrobbler.components.TitleComponent
import de.schnettler.scrobbler.screens.HorizontalScrollableComponent
import de.schnettler.scrobbler.screens.formatter
import de.schnettler.scrobbler.util.cardCornerRadius
import de.schnettler.scrobbler.util.defaultSpacerSize
import timber.log.Timber

@Composable
fun ArtistDetailScreen(artist: Artist, loading: Boolean) {
    VerticalScroller() {
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
        artist.topTracks.forEachIndexed { index, track ->
            Clickable(onClick =  {
                backstack.push(Screen.Detail(track))
            }, modifier = Modifier.ripple()) {
                ListItem(
                    text = {
                        Text(track.name)
                    },
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
                    }
                )
            }
        }

        TitleComponent(title = "Top Albums")
        HorizontalScrollableComponent(
            content = artist.topAlbums.sortedByDescending { it.plays },
            onEntrySelected = {
                Timber.d("Selected")
            },
            width = 136.dp,
            height = 136.dp,
            hintTextSize = 32.sp,
            subtitleSuffix = "Wiedergaben"
        )

        TitleComponent(title = "Ähnliche Künstler")
        HorizontalScrollableComponent(
            content = artist.similarArtists,
            onEntrySelected = {
                backstack.push(Screen.Detail(it))
            },
            width = 104.dp,
            height = 104.dp,
            hintTextSize = 32.sp
        )
    }
}