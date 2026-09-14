package com.palmz.ui.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.palmz.R
import com.palmz.ui.theme.LocalClayColors

/** The four top-level destinations — everything else (Scanner, Horoscope, Tarot, Language…)
 *  is reached by drilling in from one of these, and hides the bar while open. */
val TOP_LEVEL_TABS: List<Screen> = listOf(Screen.Today, Screen.Readings, Screen.Love, Screen.Settings)

@Composable
fun PalmlensBottomBar(current: Screen, onSelect: (Screen) -> Unit) {
    val ink = LocalClayColors.current.ink
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        // A full rectangular border would put hard 90° corners right where the bar meets
        // the screen's own rounded bottom corners — they read as clipped there. A top-only
        // hairline avoids the curve entirely and still separates the bar from the content.
        modifier = Modifier.drawBehind {
            drawLine(
                color = ink,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 1.5.dp.toPx(),
            )
        },
    ) {
        TabItem(Screen.Today, current, Icons.Outlined.WbSunny, stringResource(R.string.nav_today), onSelect)
        TabItem(Screen.Readings, current, Icons.Outlined.AutoAwesome, stringResource(R.string.nav_readings), onSelect)
        TabItem(Screen.Love, current, Icons.Outlined.FavoriteBorder, stringResource(R.string.nav_compatibility), onSelect)
        TabItem(Screen.Settings, current, Icons.Outlined.Settings, stringResource(R.string.settings), onSelect)
    }
}

@Composable
private fun RowScope.TabItem(screen: Screen, current: Screen, icon: ImageVector, label: String, onSelect: (Screen) -> Unit) {
    NavigationBarItem(
        selected = current == screen,
        onClick = { onSelect(screen) },
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}
