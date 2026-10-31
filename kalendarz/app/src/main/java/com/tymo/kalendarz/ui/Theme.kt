package com.tymo.kalendarz.ui

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
import com.tymo.kalendarz.calColors

val TodayBlue = Color(0xFF1A73E8)
val HolidayRed = Color(0xFFD93025)
fun eventColor(i: Int) = Color(calColors[i.coerceIn(0, calColors.lastIndex)].argb)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> darkColorScheme(primary = Color(0xFF8AB4F8))
        else -> lightColorScheme(primary = TodayBlue)
    }
    MaterialTheme(colorScheme = colors, content = content)
}
