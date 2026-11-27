package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkStudioColorScheme = darkColorScheme(
    primary = OrangePrimary,
    onPrimary = PureWhite,
    primaryContainer = OrangeDark,
    onPrimaryContainer = PureWhite,
    secondary = OrangeLight,
    onSecondary = CharcoalBg,
    secondaryContainer = CharcoalSurfaceElevated,
    onSecondaryContainer = PureWhite,
    tertiary = InfoBlue,
    onTertiary = PureWhite,
    background = CharcoalBg,
    onBackground = PureWhite,
    surface = CharcoalSurface,
    onSurface = PureWhite,
    surfaceVariant = CharcoalSurfaceElevated,
    onSurfaceVariant = MutedGray,
    outline = CharcoalBorder,
    error = ErrorRed,
    onError = PureWhite
)

private val LightStudioColorScheme = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = PureWhite,
    primaryContainer = OrangeLight,
    onPrimaryContainer = CharcoalBg,
    secondary = OrangeDark,
    onSecondary = PureWhite,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightTextPrimary,
    tertiary = InfoBlue,
    onTertiary = PureWhite,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = ErrorRed,
    onError = PureWhite
)

val StudioShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ArfinIllustratorTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkStudioColorScheme else LightStudioColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = StudioShapes,
        content = content
    )
}
