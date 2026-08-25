package com.aima.koraki.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.aima.koraki.navigation.BottomNavItem
import com.aima.koraki.ui.theme.BrightCrimson
import com.aima.koraki.ui.theme.DeepBackground
import com.aima.koraki.ui.theme.GlowGeneral
import com.aima.koraki.ui.theme.MutedText
import com.aima.koraki.ui.theme.SecondaryText

/**
 * Koraki's bottom navigation bar.
 * Renders [items] and highlights [currentRoute] with a small crimson pill indicator.
 *
 * Visual spec:
 *   - Height: ~68 dp (≈15 % reduction from M3 default 80 dp)
 *   - Active icon + label: BrightCrimson (#D72D48)
 *   - Inactive icon: MutedText (#77686C)
 *   - Inactive label: SecondaryText (#B9A6AA)
 *   - Indicator pill: GlowGeneral (subdued crimson tint, not solid fill)
 *
 * @param items The list of nav items (typically [bottomNavItems]).
 * @param currentRoute The active route string from the NavController.
 * @param onItemClick Called when a tab is tapped.
 */
@Composable
fun KorakiBottomBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit,
) {
    NavigationBar(
        containerColor = DeepBackground,
        tonalElevation = 0.dp,
        // Let the outer Scaffold manage system-bar insets; suppress internal padding here
        windowInsets = WindowInsets(0, 0, 0, 0),
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(item) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrightCrimson,
                    selectedTextColor = BrightCrimson,
                    unselectedIconColor = MutedText,
                    unselectedTextColor = SecondaryText,
                    // Small subdued crimson pill — not a large solid fill
                    indicatorColor = GlowGeneral,
                ),
            )
        }
    }
}
