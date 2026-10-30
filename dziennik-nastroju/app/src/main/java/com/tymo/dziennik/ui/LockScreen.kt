package com.tymo.dziennik.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 4-digit PIN pad. [onPin] returns false when the PIN is wrong. */
@Composable
fun PinPad(title: String, onPin: (String) -> Boolean, onCancel: (() -> Unit)? = null) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    fun press(d: String) {
        if (pin.length >= 4) return
        pin += d
        error = false
        if (pin.length == 4) {
            if (!onPin(pin)) error = true
            pin = ""
        }
    }

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().systemBarsPadding().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("🔒", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { i ->
                    Box(
                        Modifier.size(16.dp).background(
                            if (i < pin.length) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                            CircleShape,
                        )
                    )
                }
            }
            Text(
                if (error) "Nieprawidłowy PIN" else " ",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp),
            )
            Spacer(Modifier.height(16.dp))
            listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("", "0", "⌫")).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                    row.forEach { key ->
                        if (key.isEmpty()) {
                            Spacer(Modifier.size(72.dp))
                        } else {
                            FilledTonalButton(
                                onClick = { if (key == "⌫") pin = pin.dropLast(1) else press(key) },
                                modifier = Modifier.size(72.dp),
                                shape = CircleShape,
                            ) { Text(key, fontSize = 24.sp) }
                        }
                    }
                }
            }
            if (onCancel != null) {
                TextButton(onClick = onCancel, modifier = Modifier.padding(top = 8.dp)) { Text("Anuluj") }
            }
        }
    }
}
