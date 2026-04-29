import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*

// ── Paleta ───────────────────────────────────────────────────────────────────
private val BgBlue      = Color(0xFF87CEEB)
private val KhakiLight  = Color(0xFFF0E68C)
private val KhakiDark   = Color(0xFFBDB76B)
private val GreenDark   = Color(0xFF006400)
private val GreenLime   = Color(0xFF32CD32)
private val GreenLawn   = Color(0xFF7CFC00)
private val PinkLight   = Color(0xFFFFC0CB)
private val PinkDark    = Color(0xFFE75480)
private val YellowPetal = Color(0xFFFFE000)
private val RedCenter   = Color(0xFF8B1A1A)
private val BlackLine   = Color(0xFF111111)

// ── Cono ─────────────────────────────────────────────────────────────────────
// cx,cy = centro del borde SUPERIOR del cono
private fun DrawScope.drawCono(cx: Float, cy: Float, s: Float) {
    val topW = 95f  * s   // mitad ancho superior
    val botW = 18f  * s   // mitad ancho inferior
    val h    = 120f * s   // altura

    val path = Path().apply {
        moveTo(cx - topW, cy)
        quadraticBezierTo(cx, cy - 10 * s, cx + topW, cy)            // borde superior suave
        quadraticBezierTo(cx + topW * 0.55f, cy + h * 0.75f, cx + botW, cy + h)  // lado derecho
        quadraticBezierTo(cx, cy + h + 10 * s, cx - botW, cy + h)    // base redondeada
        quadraticBezierTo(cx - topW * 0.55f, cy + h * 0.75f, cx - topW, cy)      // lado izquierdo
        close()
    }
    drawPath(path, color = KhakiLight)
    drawPath(path, color = KhakiDark, style = Stroke(width = 3f * s))
    // Línea central
    drawLine(KhakiDark, Offset(cx, cy + 5 * s), Offset(cx, cy + h - 8 * s), 1.5f * s)
}

// ── Hoja ─────────────────────────────────────────────────────────────────────
private fun DrawScope.drawHoja(
    rootX: Float, rootY: Float,
    angleDeg: Float, len: Float, wid: Float,
    s: Float, fill: Color
) {
    val rad  = Math.toRadians(angleDeg.toDouble()).toFloat()
    val tipX = rootX + len * s * cos(rad)
    val tipY = rootY - len * s * sin(rad)
    val px   = -sin(rad)
    val py   =  cos(rad)
    val hw   = wid * s

    val path = Path().apply {
        moveTo(rootX, rootY)
        quadraticBezierTo(
            rootX + len * s * 0.45f * cos(rad) + px * hw,
            rootY - len * s * 0.45f * sin(rad) - py * hw,
            tipX, tipY
        )
        quadraticBezierTo(
            rootX + len * s * 0.45f * cos(rad) - px * hw,
            rootY - len * s * 0.45f * sin(rad) + py * hw,
            rootX, rootY
        )
        close()
    }
    drawPath(path, color = fill)
    drawPath(path, color = GreenDark, style = Stroke(width = 2.5f * s))
}

// ── Flor ──────────────────────────────────────────────────────────────────────
private fun DrawScope.drawFlor(cx: Float, cy: Float, s: Float) {
    val nPetals = 8
    val pLen    = 36f * s   // pétalos más largos
    val pWid    = 16f * s   // pétalos más anchos
    val centerR = 20f * s   // centro más grande

    for (i in 0 until nPetals) {
        val rad   = Math.toRadians(360.0 / nPetals * i).toFloat()
        val px    = cx + pLen * cos(rad)
        val py    = cy + pLen * sin(rad)
        val perpX = -sin(rad)
        val perpY =  cos(rad)

        val petal = Path().apply {
            moveTo(cx, cy)
            quadraticBezierTo(
                cx + pLen * 0.5f * cos(rad) + perpX * pWid,
                cy + pLen * 0.5f * sin(rad) + perpY * pWid,
                px, py
            )
            quadraticBezierTo(
                cx + pLen * 0.5f * cos(rad) - perpX * pWid,
                cy + pLen * 0.5f * sin(rad) - perpY * pWid,
                cx, cy
            )
            close()
        }
        drawPath(petal, color = YellowPetal)
        drawPath(petal, color = BlackLine, style = Stroke(width = 2f * s))
    }
    drawCircle(RedCenter, centerR, Offset(cx, cy))
    drawCircle(BlackLine, centerR, Offset(cx, cy), style = Stroke(2f * s))
}

// ── Moño ──────────────────────────────────────────────────────────────────────
private fun DrawScope.drawMonyo(cx: Float, cy: Float, s: Float) {
    val wingW = 52f * s
    val wingH = 24f * s

    val left = Path().apply {
        moveTo(cx, cy)
        quadraticBezierTo(cx - wingW, cy - wingH, cx - wingW * 1.1f, cy + 4 * s)
        quadraticBezierTo(cx - wingW * 0.65f, cy + wingH, cx, cy)
        close()
    }
    val right = Path().apply {
        moveTo(cx, cy)
        quadraticBezierTo(cx + wingW, cy - wingH, cx + wingW * 1.1f, cy + 4 * s)
        quadraticBezierTo(cx + wingW * 0.65f, cy + wingH, cx, cy)
        close()
    }
    drawPath(left,  color = PinkLight)
    drawPath(left,  color = PinkDark, style = Stroke(2.5f * s))
    drawPath(right, color = PinkLight)
    drawPath(right, color = PinkDark, style = Stroke(2.5f * s))
    drawCircle(PinkLight, 10f * s, Offset(cx, cy))
    drawCircle(PinkDark,  10f * s, Offset(cx, cy), style = Stroke(2f * s))
}

// ── Pantalla ──────────────────────────────────────────────────────────────────
@Composable
fun FlowerScreen(onBack: () -> Unit) {
    var etapa by remember { mutableIntStateOf(0) }

    LaunchedEffect(etapa) {
        if (etapa <= 4) {
            delay(650L)
            etapa++
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlue)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val s = w / 420f

            // Ancla: centro X del ramo, Y = 40% de la pantalla para el borde superior del cono
            val conoCx  = w * 0.52f
            val conoTop = h * 0.42f
            val conoH   = 120f * s
            val conoBot = conoTop + conoH

            // ── 1. Cono (se dibuja primero para que quede detrás de hojas y flores) ──
            if (etapa >= 1) drawCono(conoCx, conoTop, s)

            // ── 2. Hojas — detrás a la izquierda, raíz en el borde izq del cono ──────
            if (etapa >= 2) {
                // La raíz de las hojas sale del borde izquierdo superior del cono
                val hRootX = conoCx - 75f * s
                val hRootY = conoTop + 15f * s

                // Hoja grande trasera (LimeGreen, apuntando arriba-izquierda)
                drawHoja(hRootX - 5f*s,  hRootY - 5f*s,  150f, 95f, 38f, s, GreenLime)
                // Hoja mediana trasera
                drawHoja(hRootX - 15f*s, hRootY + 15f*s, 125f, 85f, 33f, s, GreenLime)
                // Hoja grande delantera (LawnGreen)
                drawHoja(hRootX + 10f*s, hRootY + 5f*s,  140f, 90f, 35f, s, GreenLawn)
                // Hoja pequeña delantera
                drawHoja(hRootX,         hRootY + 28f*s,  110f, 72f, 28f, s, GreenLawn)
            }

            // ── 3. Flores — agrupadas sobre el borde superior del cono ────────────────
            if (etapa >= 3) {
                // El centro del grupo de flores está justo encima del cono
                val fCx = conoCx + 10f * s   // ligeramente a la derecha (como en el original)
                val fCy = conoTop - 10f * s  // justo encima del borde superior

                // Distribución: 2 arriba, 3 abajo (como en el original)
                // Fila superior
                drawFlor(fCx - 38f * s, fCy - 42f * s, s)
                drawFlor(fCx + 38f * s, fCy - 42f * s, s)
                // Fila inferior
                drawFlor(fCx - 68f * s, fCy + 5f  * s, s)
                drawFlor(fCx,           fCy - 5f  * s, s)
                drawFlor(fCx + 68f * s, fCy + 5f  * s, s)
            }

            // ── 4. Moño — en la unión del cono, cerca de la base ──────────────────────
            if (etapa >= 4) drawMonyo(conoCx, conoBot - 30f * s, s)
        }

        // ── 5. Texto + botón ──────────────────────────────────────────────────────
        if (etapa >= 5) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Ten un bonito dia :D",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { etapa = 0 },
                    colors = ButtonDefaults.buttonColors(containerColor = PinkDark)
                ) {
                    Text("Repetir 🌸", color = Color.White)
                }
            }
        }
    }
}