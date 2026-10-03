package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Obsidian950,
    primaryContainer = Obsidian800,
    onPrimaryContainer = EmeraldLight,
    secondary = AmethystAccent,
    onSecondary = Color.White,
    secondaryContainer = Obsidian800,
    onSecondaryContainer = Porcelain200,
    tertiary = AmberAccent,
    onTertiary = Obsidian950,
    background = Obsidian950,
    onBackground = Porcelain100,
    surface = Obsidian900,
    onSurface = Porcelain100,
    surfaceVariant = Obsidian800,
    onSurfaceVariant = Slate400,
    outline = Obsidian700,
    outlineVariant = Obsidian800,
    error = CoralAccent,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = EmeraldLight,
    onPrimaryContainer = Obsidian900,
    secondary = AmethystAccent,
    onSecondary = Color.White,
    secondaryContainer = Porcelain100,
    onSecondaryContainer = Obsidian900,
    tertiary = AmberAccent,
    onTertiary = Obsidian950,
    background = Porcelain50,
    onBackground = Obsidian950,
    surface = Color.White,
    onSurface = Obsidian950,
    surfaceVariant = Porcelain100,
    onSurfaceVariant = Obsidian600,
    outline = Porcelain200,
    outlineVariant = Porcelain200,
    error = CoralAccent,
    onError = Color.White
)

@Composable
fun DataLensAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
