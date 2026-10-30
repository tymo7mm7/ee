package com.tymo.dziennik.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun HelpScreen() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CrisisCard()
        BreathingCard()
        SectionCard("🖐️ Uziemienie 5-4-3-2-1") {
            Text(
                "Gdy pojawia się silny lęk, rozejrzyj się i nazwij:\n" +
                    "• 5 rzeczy, które widzisz\n" +
                    "• 4 rzeczy, których możesz dotknąć\n" +
                    "• 3 dźwięki, które słyszysz\n" +
                    "• 2 zapachy\n" +
                    "• 1 smak\n\n" +
                    "To ćwiczenie pomaga wrócić uwagą do „tu i teraz”."
            )
        }
        SectionCard("🌱 Małe kroki na lepszy dzień") {
            Text(
                "• Krótki spacer na świeżym powietrzu\n" +
                    "• Szklanka wody i regularny posiłek\n" +
                    "• Wiadomość lub rozmowa z bliską osobą\n" +
                    "• Stała pora snu\n" +
                    "• Zapisanie jednej rzeczy, która dziś się udała"
            )
        }
        Text(
            "Mój Dziennik pomaga obserwować samopoczucie, ale nie zastępuje lekarza ani psychoterapeuty. " +
                "Jeśli objawy utrzymują się dłużej niż 2 tygodnie lub utrudniają codzienne życie, skonsultuj się ze specjalistą.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BreathingCard() {
    var running by remember { mutableStateOf(false) }
    var phase by remember { mutableIntStateOf(0) } // 0 = inhale, 1 = hold, 2 = exhale
    var seconds by remember { mutableIntStateOf(4) }

    LaunchedEffect(running) {
        phase = 0
        while (running) {
            for (p in 0..2) {
                phase = p
                for (s in 4 downTo 1) {
                    seconds = s
                    delay(1000)
                }
            }
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (!running) 0.6f else when (phase) { 0 -> 1f; 1 -> 1f; else -> 0.6f },
        animationSpec = tween(if (phase == 1) 100 else 4000),
        label = "breath",
    )

    SectionCard("🌬️ Ćwiczenie oddechowe") {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.size(160.dp).scale(scale)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape)
                )
                Text(
                    if (!running) "Gotowy?" else "${phaseLabel(phase)}\n$seconds",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
            Text(
                "Oddychaj w rytmie 4 s wdech · 4 s pauza · 4 s wydech.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            Button(onClick = { running = !running }) { Text(if (running) "Zakończ" else "Rozpocznij") }
            Box(Modifier.height(4.dp))
        }
    }
}

private fun phaseLabel(p: Int) = when (p) {
    0 -> "Wdech"
    1 -> "Zatrzymaj"
    else -> "Wydech"
}
