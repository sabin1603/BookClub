package com.example.bookclub.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = BookGold,
    secondary = BookGreen,
    tertiary = BookBrown,
    background = BookBrownDark,
    surface = BookBrown
)

private val LightColorScheme = lightColorScheme(
    primary = BookBrown,
    secondary = BookGreen,
    tertiary = BookGold,
    background = BookCream,
    surface = BookCream
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