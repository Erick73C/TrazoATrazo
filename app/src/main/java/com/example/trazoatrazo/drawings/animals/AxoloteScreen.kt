package com.example.trazoatrazo.drawings.animals

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
fun AxoloteScreen(onBack: () -> Unit) {
    var progress by remember { mutableFloatStateOf(0f) }
    var repetir by remember { mutableIntStateOf(0) }

    LaunchedEffect(repetir) {
        progress = 0f
        animate(0f, 1f, animationSpec = tween(2000)) { value, _ -> progress = value }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCE4EC)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🌸", // Axolote emoji is hard, using pink flower as symbol for now
            fontSize = 120.sp,
            modifier = Modifier.scale(progress)
        )

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            DrawingButtons(
                visible = progress >= 1f,
                message = "💖 Axolote Tierno 💖",
                subMessage = "El tesoro de México",
                accentColor = Color(0xFFF06292),
                backgroundColor = Color(0xFFFCE4EC),
                onRepeat = { repetir++ },
                onBack = onBack
            )
        }
        BackMenuButton(onBack = onBack)
    }
}
