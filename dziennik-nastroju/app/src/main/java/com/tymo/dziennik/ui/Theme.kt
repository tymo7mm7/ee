package com.tymo.dziennik.ui

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

fun moodColor(m: Int): Color = when (m) {
    1 -> Color(0xFFEF4444)
    2 -> Color(0xFFF97316)
    3 -> Color(0xFFEAB308)
    4 -> Color(0xFF84CC16)
    else -> Color(0xFF22C55E)
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> darkColorScheme(primary = Color(0xFFC4B5FD))
        else -> lightColorScheme(primary = Color(0xFF7C3AED))
    }
    MaterialTheme(colorScheme = colors, content = content)
}
