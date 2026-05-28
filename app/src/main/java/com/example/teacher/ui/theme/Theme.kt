package com.example.teacher.ui.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = WarmDarkPrimary,
    onPrimary = WarmDarkOnPrimary,
    primaryContainer = WarmDarkPrimaryContainer,
    onPrimaryContainer = WarmDarkOnPrimaryContainer,
    secondary = WarmDarkSecondary,
    onSecondary = WarmDarkOnSecondary,
    secondaryContainer = WarmDarkSecondaryContainer,
    onSecondaryContainer = WarmDarkOnSecondaryContainer,
    tertiary = WarmDarkTertiary,
    onTertiary = WarmDarkOnTertiary,
    tertiaryContainer = WarmDarkTertiaryContainer,
    onTertiaryContainer = WarmDarkOnTertiaryContainer,
    background = WarmDarkBackground,
    onBackground = WarmDarkOnBackground,
    surface = WarmDarkSurface,
    onSurface = WarmDarkOnSurface,
    surfaceVariant = WarmDarkSurfaceVariant,
    onSurfaceVariant = WarmDarkOnSurfaceVariant,
    outline = WarmDarkOutline,
    outlineVariant = WarmDarkOutlineVariant,
    error = WarmError,
    onError = WarmOnError,
    errorContainer = WarmErrorContainer,
    onErrorContainer = WarmOnErrorContainer,
    inverseSurface = WarmDarkInverseSurface,
    inverseOnSurface = WarmDarkInverseOnSurface,
    inversePrimary = WarmDarkInversePrimary,
)

private val LightColorScheme = lightColorScheme(
    primary = WarmYellowPrimary,
    onPrimary = WarmYellowOnPrimary,
    primaryContainer = WarmYellowPrimaryContainer,
    onPrimaryContainer = WarmYellowOnPrimaryContainer,
    secondary = WarmOrangeSecondary,
    onSecondary = WarmOrangeOnSecondary,
    secondaryContainer = WarmOrangeSecondaryContainer,
    onSecondaryContainer = WarmOrangeOnSecondaryContainer,
    tertiary = SoftTealTertiary,
    onTertiary = SoftTealOnTertiary,
    tertiaryContainer = SoftTealTertiaryContainer,
    onTertiaryContainer = SoftTealOnTertiaryContainer,
    background = WarmBackground,
    onBackground = WarmOnBackground,
    surface = WarmSurface,
    onSurface = WarmOnSurface,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = WarmOnSurfaceVariant,
    outline = WarmOutline,
    outlineVariant = WarmOutlineVariant,
    error = WarmError,
    onError = WarmOnError,
    errorContainer = WarmErrorContainer,
    onErrorContainer = WarmOnErrorContainer,
    inverseSurface = WarmInverseSurface,
    inverseOnSurface = WarmInverseOnSurface,
    inversePrimary = WarmInversePrimary,
)

@Composable
fun JiaonilaileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val shapes = Shapes(
        extraSmall = RoundedCornerShape(10.dp),
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(22.dp),
        large = RoundedCornerShape(24.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = shapes,
        content = content
    )
}
