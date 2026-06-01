package com.bitdesal.tabala.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryMain,
    onPrimary = SurfacePaper,
    secondary = SecondaryMain,
    onSecondary = TertiaryDark,
    tertiary = TertiaryMain,
    background = BackgroundDefault,
    surface = SurfacePaper
)

@Composable
fun TabalaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
