package de.schnettler.scrobbler.ui.common.compose.widget

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.schnettler.database.models.Toplist
import de.schnettler.scrobbler.ui.common.compose.R
import de.schnettler.scrobbler.ui.common.compose.navigation.UIAction
import de.schnettler.scrobbler.ui.common.compose.rememberDominantColorCache

@Composable
fun <T> Carousel(
    items: List<T>?,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    @StringRes titleRes: Int? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    itemSpacing: Dp = 8.dp,
    verticalGravity: Alignment.Vertical = Alignment.Top,
    action: @Composable () -> Unit = { },
    itemContent: @Composable LazyItemScope.(T) -> Unit
) {
    titleRes?.let {
        Header(
            title = stringResource(id = titleRes),
            loading = loading,
            action = action,
            modifier = Modifier.fillMaxWidth()
        )
    }

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        contentPadding = contentPadding,
        verticalAlignment = verticalGravity
    ) {
        items(items = items ?: emptyList()) { item ->
            itemContent(item)
        }
    }
}

@Composable
fun <T : Toplist> TopListCarousel(
    topList: List<T>?,
    @StringRes titleRes: Int? = null,
    spacing: Dp = 8.dp,
    itemSize: Dp = 160.dp,
    actionHandler: (UIAction) -> Unit,
) {
    val colorCache = rememberDominantColorCache()

    Carousel(
        items = topList,
        itemSpacing = spacing,
        titleRes = titleRes,
        action = {
            TextButton(
                onClick = { },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colors.secondary),
            ) {
                Text(text = stringResource(id = R.string.header_more))
            }
        }
    ) { toplist ->
        MediaCard(
            name = toplist.value.name,
            modifier = Modifier.size(itemSize),
            imageUrl = toplist.value.imageUrl,
            plays = toplist.listing.count,
            colorCache = colorCache
        ) { actionHandler(UIAction.ListingSelected(toplist.value)) }
    }
}