package com.growthpet.student.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val GrowthPetColorScheme = lightColorScheme(
    primary = ColorTokens.TechBluePrimary,
    onPrimary = ColorTokens.SurfaceCard,
    primaryContainer = ColorTokens.SkyHaloLight,
    onPrimaryContainer = ColorTokens.InkPrimary,
    secondary = ColorTokens.SkyCyanGlow,
    onSecondary = ColorTokens.SurfaceCard,
    background = ColorTokens.CanvasIce,
    onBackground = ColorTokens.InkPrimary,
    surface = ColorTokens.SurfaceCard,
    onSurface = ColorTokens.InkPrimary,
    surfaceVariant = ColorTokens.SurfaceMuted,
    onSurfaceVariant = ColorTokens.InkSecondary,
    outline = ColorTokens.BorderHairline,
    error = ColorTokens.DangerCoral,
    onError = ColorTokens.SurfaceCard
)

@Composable
fun GrowthPetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GrowthPetColorScheme,
        typography = AppTypography,
        content = content
    )
}
