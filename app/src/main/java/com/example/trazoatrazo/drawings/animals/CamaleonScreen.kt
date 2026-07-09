package com.example.trazoatrazo.drawings.animals

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.example.trazoatrazo.ui.components.BackMenuButton
import com.example.trazoatrazo.ui.components.DrawingButtons

@Composable
fun CamaleonScreen(onBack: () -> Unit) {
    var progress by remember { mutableFloatStateOf(0f) }
    var repetir by remember { mutableIntStateOf(0) }

    LaunchedEffect(repetir) {
        progress = 0f
        animate(0f, 1f, animationSpec = tween(2500)) { value, _ -> progress = value }
    }

    val color by animateColorAsState(
        targetValue = if (progress < 0.3f) Color.Green else if (progress < 0.6f) Color.Yellow else Color.Red,
        animationSpec = tween(1000)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🦎",
            fontSize = 120.sp,
            modifier = Modifier.scale(progress)
        )

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            DrawingButtons(
                visible = progress >= 1f,
                message = "🦎 Camaleón Mágico 🦎",
                accentColor = Color(0xFF689F38),
                backgroundColor = color.copy(alpha = 0.1f),
                onRepeat = { repetir++ },
                onBack = onBack
            )
        }
        BackMenuButton(onBack = onBack)
    }
}
