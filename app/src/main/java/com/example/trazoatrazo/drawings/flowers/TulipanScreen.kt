package com.example.trazoatrazo.drawings.flowers

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.example.trazoatrazo.ui.components.BackMenuButton
import com.example.trazoatrazo.ui.components.DrawingButtons

@Composable
fun TulipanScreen(onBack: () -> Unit) {
    var progress by remember { mutableFloatStateOf(0f) }
    var repetir by remember { mutableIntStateOf(0) }

    LaunchedEffect(repetir) {
        progress = 0f
        animate(0f, 1f, animationSpec = tween(1500)) { value, _ -> progress = value }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3E5F5)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🌷",
            fontSize = (120 * progress).sp,
            modifier = Modifier.offset(y = ((-50) * (1-progress)).dp)
        )

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            DrawingButtons(
                visible = progress >= 1f,
                message = "🌷 Lindo Tulipán 🌷",
                accentColor = Color(0xFF9C27B0),
                backgroundColor = Color(0xFFF3E5F5),
                onRepeat = { repetir++ },
                onBack = onBack
            )
        }
        BackMenuButton(onBack = onBack)
    }
}
