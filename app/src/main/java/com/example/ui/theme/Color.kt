package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// --- THREE-COLOR PALETTE (Black #000000, White #FFFFFF, Primary Container #243CE6) ---

val PrimaryContainerColor = Color(0xFF243CE6) // '#243ce6'
val PureBlack = Color(0xFF000000)
val PureWhite = Color(0xFFFFFFFF)

// --- OBSIDIAN DARK PALETTE ---
val ObsidianDarkBg = Color(0xFF000000)
val ObsidianDarkSurface = Color(0xFF000000)
val ObsidianDarkSurfaceVariant = Color(0xFF000000)
val ObsidianDarkSurfaceElevated = Color(0xFF000000)

val ObsidianDarkPrimary = Color(0xFF243CE6)
val ObsidianDarkOnPrimary = Color(0xFFFFFFFF)
val ObsidianDarkPrimaryContainer = Color(0xFF243CE6)
val ObsidianDarkOnPrimaryContainer = Color(0xFFFFFFFF)

val ObsidianDarkSecondary = Color(0xFF243CE6)
val ObsidianDarkOnSecondary = Color(0xFFFFFFFF)
val ObsidianDarkSecondaryContainer = Color(0xFF000000)
val ObsidianDarkOnSecondaryContainer = Color(0xFFFFFFFF)

val ObsidianDarkTertiary = Color(0xFF243CE6)
val ObsidianDarkOnTertiary = Color(0xFFFFFFFF)
val ObsidianDarkTertiaryContainer = Color(0xFF000000)
val ObsidianDarkOnTertiaryContainer = Color(0xFFFFFFFF)

val ObsidianDarkOnSurface = Color(0xFFFFFFFF)
val ObsidianDarkOnSurfaceVariant = Color(0xFFFFFFFF)
val ObsidianDarkOutline = Color(0xFF243CE6)
val ObsidianDarkOutlineVariant = Color(0xFFFFFFFF)

// --- OBSIDIAN LIGHT PALETTE ---
val ObsidianLightBg = Color(0xFFFFFFFF)
val ObsidianLightSurface = Color(0xFFFFFFFF)
val ObsidianLightSurfaceVariant = Color(0xFFFFFFFF)
val ObsidianLightSurfaceElevated = Color(0xFFFFFFFF)

val ObsidianLightPrimary = Color(0xFF243CE6)
val ObsidianLightOnPrimary = Color(0xFFFFFFFF)
val ObsidianLightPrimaryContainer = Color(0xFF243CE6)
val ObsidianLightOnPrimaryContainer = Color(0xFFFFFFFF)

val ObsidianLightSecondary = Color(0xFF243CE6)
val ObsidianLightOnSecondary = Color(0xFFFFFFFF)
val ObsidianLightSecondaryContainer = Color(0xFFFFFFFF)
val ObsidianLightOnSecondaryContainer = Color(0xFF000000)

val ObsidianLightTertiary = Color(0xFF243CE6)
val ObsidianLightOnTertiary = Color(0xFFFFFFFF)
val ObsidianLightTertiaryContainer = Color(0xFFFFFFFF)
val ObsidianLightOnTertiaryContainer = Color(0xFF000000)

val ObsidianLightOnSurface = Color(0xFF000000)
val ObsidianLightOnSurfaceVariant = Color(0xFF000000)
val ObsidianLightOutline = Color(0xFF243CE6)
val ObsidianLightOutlineVariant = Color(0xFF000000)

// --- DYNAMIC RESOLUTION FOR SCREEN COMPOSABLES ---
val DarkBackground: Color @Composable get() = MaterialTheme.colorScheme.background
val DarkSurface: Color @Composable get() = MaterialTheme.colorScheme.surface
val DarkSurfaceVariant: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant
val DarkSurfaceElevated: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh

val TextPrimary: Color @Composable get() = MaterialTheme.colorScheme.onSurface
val TextSecondary: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val TextMuted: Color @Composable get() = MaterialTheme.colorScheme.outline

val NeonCyan: Color @Composable get() = MaterialTheme.colorScheme.primary
val NeonCyanLight: Color @Composable get() = MaterialTheme.colorScheme.tertiary
val ElectricViolet: Color @Composable get() = MaterialTheme.colorScheme.secondary

// Brand & Status Accent Constants (Aligned with Primary Container #243CE6 and Black/White)
val GoldAmber = Color(0xFF243CE6)
val FlameOrange = Color(0xFF243CE6)
val EmeraldGreen = Color(0xFF243CE6)
val CrimsonRed = Color(0xFF243CE6)

val AnnouncementBg: Color @Composable get() = MaterialTheme.colorScheme.primaryContainer
val AnnouncementBorder: Color @Composable get() = MaterialTheme.colorScheme.primary
val AnnouncementText: Color @Composable get() = MaterialTheme.colorScheme.onPrimaryContainer


