package com.aima.koraki.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Koraki is always dark — no light theme variant, no dynamic colour (Material You).
 * Colors map exactly to the Unified Red Design System tokens.
 */
private val KorakiColorScheme = darkColorScheme(
    // Background layers
    background = AppBackground,
    onBackground = TextPrimary,
    
    // Surface layers
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = BackgroundElevated,

    // Primary 
    primary = PrimaryCrimson,
    onPrimary = TextOnRed,
    primaryContainer = Red700,
    onPrimaryContainer = TextPrimary,

    // Secondary (Active states / Hover)
    secondary = BrightCrimson,
    onSecondary = TextOnRed,
    secondaryContainer = DeepWine,
    onSecondaryContainer = TextSecondary,
    
    // Tertiary (Glow & Highlights)
    tertiary = CrimsonGlow,
    onTertiary = TextOnRed,

    // Borders / outlines
    outline = BorderDefault,
    outlineVariant = BorderStrong,

    // Error
    error = SemanticError,
    onError = TextOnRed,
    
    // Scrim & Immersion
    scrim = BlackRose,
    inverseSurface = BlackRose,
)

@Composable
fun KorakiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KorakiColorScheme,
        typography = Typography,
        content = content,
    )
}

private val VaultColorScheme = darkColorScheme(
    background = VaultBackground,
    onBackground = VaultText,
    surface = VaultSurface,
    onSurface = VaultText,
    surfaceVariant = VaultElevated,
    onSurfaceVariant = VaultMuted,
    primary = VaultActive,        // #D12642 — lock icon / security indicator
    onPrimary = TextOnRed,
    primaryContainer = VaultAccent,
    onPrimaryContainer = VaultText,
    secondary = VaultAccent,      // #94152A — FAB / pinned bookmark
    onSecondary = TextOnRed,
    outline = VaultBorder,
    outlineVariant = BorderStrong,
    error = SemanticError,
    onError = TextOnRed,
)

@Composable
fun VaultTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VaultColorScheme,
        typography = Typography,
        content = content,
    )
}