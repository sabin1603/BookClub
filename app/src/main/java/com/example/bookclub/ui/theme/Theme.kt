package com.example.bookclub.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BookPrimary,
    onPrimary = BookOnPrimary,
    primaryContainer = BookPrimaryContainer,
    onPrimaryContainer = Color(0xFFE7BDB1),

    secondary = BookSecondary,
    onSecondary = BookOnPrimary,
    secondaryContainer = BookSecondaryContainer,
    onSecondaryContainer = Color(0xFF785A1A),

    tertiary = BookTertiary,
    onTertiary = BookOnPrimary,
    tertiaryContainer = BookTertiaryContainer,
    onTertiaryContainer = Color(0xFF80C683),

    error = BookError,
    onError = BookOnPrimary,
    errorContainer = BookErrorContainer,
    onErrorContainer = Color(0xFF93000A),

    background = BookBackground,
    onBackground = BookOnSurface,

    surface = BookSurface,
    onSurface = BookOnSurface,
    surfaceVariant = BookSurfaceContainerHighest,
    onSurfaceVariant = BookOnSurfaceVariant,

    outline = BookOutline,
    outlineVariant = BookOutlineVariant,

    inverseSurface = Color(0xFF2F312E),
    inverseOnSurface = Color(0xFFF2F1ED),
    inversePrimary = Color(0xFFE7BDB1)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE7BDB1),
    onPrimary = Color(0xFF442A22),
    primaryContainer = Color(0xFF5D4037),
    onPrimaryContainer = Color(0xFFFFDBD0),

    secondary = Color(0xFFE9C176),
    onSecondary = Color(0xFF3F2E00),
    secondaryContainer = Color(0xFF5D4201),
    onSecondaryContainer = Color(0xFFFFDEA5),

    tertiary = Color(0xFF90D792),
    onTertiary = Color(0xFF003A11),
    tertiaryContainer = Color(0xFF07521D),
    onTertiaryContainer = Color(0xFFABF4AC),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF1B1C1A),
    onBackground = Color(0xFFE4E3DF),

    surface = Color(0xFF1B1C1A),
    onSurface = Color(0xFFE4E3DF),
    surfaceVariant = Color(0xFF504441),
    onSurfaceVariant = Color(0xFFD4C3BE),

    outline = Color(0xFF9E8D88),
    outlineVariant = Color(0xFF504441)
)

@Composable
fun BookClubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}