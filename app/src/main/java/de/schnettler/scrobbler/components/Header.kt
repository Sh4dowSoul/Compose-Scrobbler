package de.schnettler.scrobbler.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.EmphasisAmbient
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideEmphasis
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.WithConstraints
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import de.schnettler.scrobbler.util.Orientation

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Header(
    title: String,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    action: (@Composable () -> Unit)? = null
) {
    Row(modifier) {
        Spacer(size = 16.dp, orientation = Orientation.Horizontal)

        ProvideEmphasis(EmphasisAmbient.current.high) {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(vertical = 8.dp)
                    .weight(1f, true)
            )
        }

        AnimatedVisibility(visible = loading) {
            AutoSizedCircularProgressIndicator(
                color = MaterialTheme.colors.secondary,
                modifier = Modifier.padding(8.dp).preferredSize(16.dp)
            )
        }

        if (action != null) action()

        Spacer(size = 16.dp, orientation = Orientation.Horizontal)
    }
}

// Default stroke size
private val DefaultStrokeWidth = 4.dp
// Preferred diameter for CircularProgressIndicator
private val DefaultDiameter = 40.dp
// Internal padding added by CircularProgressIndicator
private val InternalPadding = 4.dp

private val StrokeDiameterFraction = DefaultStrokeWidth / DefaultDiameter

@Composable
fun AutoSizedCircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.primary
) {
    WithConstraints(modifier) {
        val diameter = with(DensityAmbient.current) {
            // We need to minus the padding added within CircularProgressIndicator
            min(constraints.maxWidth.toDp(), constraints.maxHeight.toDp()) - InternalPadding
        }

        CircularProgressIndicator(
            strokeWidth = (diameter * StrokeDiameterFraction).coerceAtLeast(1.dp),
            color = color
        )
    }
}
