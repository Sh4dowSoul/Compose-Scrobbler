package de.schnettler.scrobbler.ui.history.widet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.schnettler.scrobbler.ui.history.model.HistoryError

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ErrorItem(item: HistoryError, onSelect: () -> Unit) {
    ListItem(
        text = { Text(text = stringResource(id = item.titleRes)) },
        secondaryText = { Text(text = stringResource(id = item.subtitleRes)) },
        singleLineSecondaryText = false,
        icon = {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
                Icon(imageVector = item.icon, null)
            }
        },
        modifier = Modifier.clickable(onClick = onSelect)
    )
}