package com.tymo.oceny.ui

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
import com.tymo.oceny.Kind

val PlusGreen = Color(0xFF16A34A)
val MinusRed = Color(0xFFDC2626)
val IdeaAmber = Color(0xFFD97706)
val StarGold = Color(0xFFF59E0B)

fun kindColor(k: Kind) = when (k) {
    Kind.PLUS -> PlusGreen
    Kind.MINUS -> MinusRed
    Kind.IDEA -> IdeaAmber
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> darkColorScheme(primary = Color(0xFF93C5FD))
        else -> lightColorScheme(primary = Color(0xFF334155))
    }
    MaterialTheme(colorScheme = colors, content = content)
}
