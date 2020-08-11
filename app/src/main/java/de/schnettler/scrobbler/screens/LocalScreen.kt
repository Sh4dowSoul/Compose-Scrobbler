package de.schnettler.scrobbler.screens

import android.content.Intent
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.contentColor
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Stack
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.EmphasisAmbient
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.state
import androidx.compose.runtime.stateFor
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import androidx.ui.tooling.preview.PreviewParameter
import de.schnettler.database.models.LastFmEntity
import de.schnettler.database.models.Scrobble
import de.schnettler.scrobble.MediaListenerService
import de.schnettler.scrobbler.components.CustomDivider
import de.schnettler.scrobbler.components.ErrorSnackbar
import de.schnettler.scrobbler.components.LiveDataLoadingComponent
import de.schnettler.scrobbler.components.NameListIcon
import de.schnettler.scrobbler.components.PlainListIconBackground
import de.schnettler.scrobbler.components.QuickActionsRow
import de.schnettler.scrobbler.components.SwipeRefreshPrograssIndicator
import de.schnettler.scrobbler.components.SwipeToRefreshLayout
import de.schnettler.scrobbler.screens.preview.FakeHistoryTrackProvider
import de.schnettler.scrobbler.util.RefreshableUiState
import de.schnettler.scrobbler.util.ScrobbleAction
import de.schnettler.scrobbler.util.ScrobbleAction.DELETE
import de.schnettler.scrobbler.util.ScrobbleAction.EDIT
import de.schnettler.scrobbler.util.ScrobbleAction.OPEN
import de.schnettler.scrobbler.util.ScrobbleAction.SUBMIT
import de.schnettler.scrobbler.util.copyByState
import de.schnettler.scrobbler.util.milliSecondsToDate
import de.schnettler.scrobbler.util.milliSecondsToMinSeconds
import de.schnettler.scrobbler.util.packageNameToAppName
import de.schnettler.scrobbler.viewmodels.LocalViewModel
import timber.log.Timber
import kotlin.math.roundToInt

@Composable
fun LocalScreen(localViewModel: LocalViewModel, onListingSelected: (LastFmEntity) -> Unit) {
    val context = ContextAmbient.current
    when (MediaListenerService.isEnabled(context)) {
        true -> Content(localViewModel = localViewModel, onListingSelected = onListingSelected)
        false -> Button(onClick = {
            context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }) {
            Text(text = "Enable")
        }
    }
    Timber.i("Started Service")
}

@Suppress("LongMethod")
@Composable
fun Content(localViewModel: LocalViewModel, onListingSelected: (LastFmEntity) -> Unit) {
    val recentTracksState by localViewModel.recentTracksState.collectAsState()
    val cachedNumber by localViewModel.cachedScrobblesCOunt.collectAsState(initial = 0)
    var showEditDialog by state { false }
    var showConfirmDialog by state { false }
    val selectedTrack: MutableState<Scrobble?> = state { null }
    val (showSnackbarError, updateShowSnackbarError) = stateFor(recentTracksState) {
        recentTracksState is RefreshableUiState.Error
    }

    Stack(modifier = Modifier.padding(bottom = 56.dp).fillMaxSize()) {
        if (recentTracksState.isLoading) {
            LiveDataLoadingComponent()
        } else {
            SwipeToRefreshLayout(
                refreshingState = recentTracksState.isRefreshing,
                onRefresh = { localViewModel.refresh() },
                refreshIndicator = { SwipeRefreshPrograssIndicator() }
            ) {
                recentTracksState.currentData?.let { list ->
                    HistoryTrackList(
                        tracks = list,
                        onActionClicked = { track, actionType ->
                            selectedTrack.value = track
                            when (actionType) {
                                EDIT -> showEditDialog = true
                                DELETE -> showConfirmDialog = true
                                OPEN -> onListingSelected(track.asLastFmTrack())
                                SUBMIT -> localViewModel.submitScrobble(track)
                            }
                        },
                        onNowPlayingSelected = { }
                    )
                }
            }
        }
        ErrorSnackbar(
            showError = showSnackbarError,
            onErrorAction = { localViewModel.refresh() },
            onDismiss = { updateShowSnackbarError(false) },
            state = recentTracksState,
            fallBackMessage = "Unable to refresh history",
            modifier = Modifier.gravity(Alignment.BottomCenter)
        )
        if (cachedNumber > 0) {
            ExtendedFloatingActionButton(
                text = {
                    Text(text = "$cachedNumber Scrobbles")
                }, onClick = {
                    localViewModel.scheduleScrobbleSubmission()
                },
                icon = {
                    Icon(asset = Icons.Outlined.CloudUpload)
                },
                contentColor = Color.White,
                modifier = Modifier.gravity(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp)
            )
        }
    }

    if (showEditDialog) {
        TrackEditDialog(onSelect = { track ->
            showEditDialog = false
            track?.let { updatedTrack ->
                Timber.d("[LocalEdit - Old]${selectedTrack.value}")
                Timber.d("[LocalEdit - New]$updatedTrack")
            }
        }, onDismiss = {
            showEditDialog = false
        }, track = selectedTrack.value)
    }
    if (showConfirmDialog) {
        ConfirmDialog(
            title = "Delete Scrobble",
            description = "Are you sure you want to delete the selected scrobble?") { confirmed ->
            if (confirmed && selectedTrack.value != null) {
                selectedTrack.value?.let { localViewModel.deleteScrobble(it) }
            }
            showConfirmDialog = false
        }
    }
}

@Composable
private fun TrackEditDialog(
    track: Scrobble?,
    onSelect: (selected: Scrobble?) -> Unit,
    onDismiss: () -> Unit
) {
    if (track != null) {
        val trackState = state { TextFieldValue(track.name) }
        val artistState = state { TextFieldValue(track.artist) }
        val albumState = state { TextFieldValue(track.album) }

        AlertDialog(
            onCloseRequest = {
                if (track.copyByState(trackState, artistState, albumState) == track) {
                    onDismiss()
                }
            },
            title = { Text(text = "Scrobble bearbeiten") },
            text = {
                Column {
                    TextField(
                        value = trackState.value,
                        onValueChange = { trackState.value = it },
                        label = { Text("Song") })
                    Spacer(modifier = Modifier.preferredHeight(16.dp))
                    TextField(
                        value = artistState.value,
                        onValueChange = { artistState.value = it },
                        label = { Text("Künstler") })
                    Spacer(modifier = Modifier.preferredHeight(16.dp))
                    TextField(
                        value = albumState.value,
                        onValueChange = { albumState.value = it },
                        label = { Text("Album") })
                }
            },
            confirmButton = {
                val updated = track.copyByState(trackState, artistState, albumState)
                TextButton(
                    onClick = { onSelect(if (updated != track) updated else null) },
                    contentColor = MaterialTheme.colors.secondary
                ) {
                    Text(text = "Select")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    contentColor = EmphasisAmbient.current.medium.applyEmphasis(
                        contentColor()
                    )
                ) {
                    Text(text = "Discard")
                }
            }
        )
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    description: String = "Are you sure?",
    onDismiss: (Boolean) -> Unit
) {
    AlertDialog(
        onCloseRequest = { onDismiss(false) },
        title = { Text(text = title) },
        text = { Text(text = description) },
        confirmButton = {
            TextButton(
                onClick = { onDismiss(true) },
                contentColor = MaterialTheme.colors.secondary
            ) {
                Text(text = "Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss(false) },
                contentColor = EmphasisAmbient.current.medium.applyEmphasis(contentColor())
            ) {
                Text(text = "Cancel")
            }
        }
    )
}

@Composable
fun NowPlayingTrack(name: String, artist: String, onClick: () -> Unit) {
    Card(modifier = Modifier.padding(16.dp)) {
        ListItem(
            text = { Text(name) },
            secondaryText = { Text(artist) },
            icon = {
                PlainListIconBackground(MaterialTheme.colors.secondary) {
                    Icon(
                        asset = Icons.Rounded.MusicNote,
                        tint = Color.White
                    )
                }
            },
            onClick = { onClick() }
        )
    }
}

@Composable
fun ScrobbledTrack(track: Scrobble, onActionClicked: (ScrobbleAction) -> Unit) {
    var expanded by state { false }
    ListItem(
        text = { Text(text = track.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        secondaryText = {
            Column {
                Text(
                    text = "${track.artist} ⦁ ${track.album}",
                    maxLines = if (expanded) 3 else 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (expanded) {
                    if (track.isLocal()) ComposeAdditionalInformation(
                        playedBy = track.playedBy,
                        amountPlayed = track.amountPlayed,
                        duration = track.duration,
                        timestamp = track.timestamp
                    )
                    else Spacer(modifier = Modifier.preferredHeight(16.dp))
                    CustomDivider()
                    ComposeQuickActions(track.isCached(), onActionClicked)
                }
            }
        },
        icon = { NameListIcon(title = track.name) },
        onClick = { expanded = !expanded },
        trailing = {
            track.timestampToRelativeTime()?.let {
                Column {
                    Text(text = it)
                    Row {
                        if (track.isCached()) {
                            Icon(
                                asset = Icons.Rounded.CloudOff.copy(
                                    defaultWidth = 16.dp,
                                    defaultHeight = 16.dp
                                )
                            )
                            Spacer(modifier = Modifier.preferredWidth(8.dp))
                        }
                        if (track.isLocal()) Text(text = "${track.playPercent()} %")
                    }
                }
            }
        }
    )
    CustomDivider()
}

@Composable
fun ComposeAdditionalInformation(
    playedBy: String,
    amountPlayed: Long,
    duration: Long,
    timestamp: Long
) {
    Spacer(modifier = Modifier.preferredHeight(8.dp))
    Text(text = "Source: ${packageNameToAppName(playedBy)}")
    Text(
        text = "Runtime: ${milliSecondsToMinSeconds(amountPlayed)}/${
            milliSecondsToMinSeconds(duration)
        } (${(amountPlayed.toFloat() / duration * 100).roundToInt()}%)"
    )
    Text(text = "Timestamp: ${(timestamp * 1000).milliSecondsToDate()}")
    Spacer(modifier = Modifier.preferredHeight(8.dp))
}

@Composable
fun ComposeQuickActions(isCached: Boolean, onActionClicked: (ScrobbleAction) -> Unit) {
    val actions = mutableListOf<ScrobbleAction>()
    if (isCached) { actions.addAll(listOf(EDIT, DELETE, SUBMIT)) }
    actions.add(OPEN)
    QuickActionsRow(items = actions, onSelect = onActionClicked)
}

@Preview
@Composable
fun HistoryTrack(
    @PreviewParameter(FakeHistoryTrackProvider::class) track: Scrobble,
    onActionClicked: (ScrobbleAction) -> Unit = { },
    onNowPlayingSelected: () -> Unit = { }
) {
    if (track.isPlaying()) {
        NowPlayingTrack(name = track.name, artist = track.artist, onClick = onNowPlayingSelected)
    } else {
        ScrobbledTrack(track = track, onActionClicked = onActionClicked)
    }
}

@Composable
fun HistoryTrackList(
    tracks: List<Scrobble>,
    onActionClicked: (Scrobble, ScrobbleAction) -> Unit,
    onNowPlayingSelected: (Scrobble) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumnFor(items = tracks, modifier = modifier) { track ->
        HistoryTrack(
            track = track,
            onActionClicked = { onActionClicked(track, it) },
            onNowPlayingSelected = { onNowPlayingSelected(track) }
        )
    }
}