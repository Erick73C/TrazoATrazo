package com.example.trazoatrazo.drawings.special

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.trazoatrazo.ui.components.BackMenuButton
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

// ── Paleta de la carta ────────────────────────────────────────────────────────
private val PaperBg      = Color(0xFFFFFBF2)
private val PaperLine    = Color(0xFFEDE0C8)
private val TextoColor   = Color(0xFF2C1E0F)
private val TextoSuave   = Color(0xFF6B5744)
private val GoldAccent   = Color(0xFFD4A017)
private val GoldSuave    = Color(0xFFF5E6C8)
private val ScreenBg     = Color(0xFF0D0A07)

// ══════════════════════════════════════════════════════════════════════════════
// ✏️  MODELOS DE DATOS
// ══════════════════════════════════════════════════════════════════════════════
data class RecuerdoData(
    val titulo:     String,
    val texto:      String,
    val imagenDesc: String = ""
)

sealed class Segmento {
    data class Normal    (val texto: String)                             : Segmento()
    data class Remarcado (val texto: String, val recuerdo: RecuerdoData) : Segmento()
}

sealed class CartaBloque {
    data class Parrafo    (val segmentos: List<Segmento>) : CartaBloque()
    data class FotoBloque (val emoji: String, val desc: String) : CartaBloque()
    object     Separador                                  : CartaBloque()
    data class CierreFinal(val frase: String)             : CartaBloque()
}

// ══════════════════════════════════════════════════════════════════════════════
// CONTENIDO DE LA CARTA — Edita aquí tus textos (TEXTO EL QUE TU QUIERAS)
// ══════════════════════════════════════════════════════════════════════════════
private val contenidoCarta: List<CartaBloque> = listOf(
    //Lo siguiente lo puedes modificar para poner el texto que tu quieras
    CartaBloque.Parrafo(listOf(
        Segmento.Normal("Hola, ¿cómo estás? Espero que este mensaje te encuentre bien. Hay muchas cosas que quiero decirte, cosas que a veces es difícil expresar en persona.")
    )),

    CartaBloque.Separador,

    CartaBloque.Parrafo(listOf(
        Segmento.Normal("Recuerdo mucho "),
        Segmento.Remarcado(
            texto = "aquella primera vez",
            recuerdo = RecuerdoData(
                titulo = "El primer encuentro 💛",
                texto  = "Aquí escribe el recuerdo especial que quieres compartir sobre ese momento."
            )
        ),
        Segmento.Normal(" en que nos reímos tanto que nos dolía el estómago. Esos momentos son los que guardo con más cariño.")
    )),

    CartaBloque.FotoBloque(
        emoji = "🖼️",
        desc  = "Aquí va tu primera foto — agrega la imagen en res/drawable y reemplaza FotoBloque por una imagen real"
    ),

    CartaBloque.Separador,

    CartaBloque.Parrafo(listOf(
        Segmento.Normal("Gracias por estar siempre presente. Por "),
        Segmento.Remarcado(
            texto = "todos esos momentos",
            recuerdo = RecuerdoData(
                titulo = "Momentos que importan 🌟",
                texto  = "Describe aquí los momentos que más valoras y que quieres mencionar en la carta."
            )
        ),
        Segmento.Normal(" en los que simplemente estuviste ahí, sin necesitar decir nada.")
    )),

    CartaBloque.Parrafo(listOf(
        Segmento.Normal("No siempre es fácil encontrar las palabras correctas, pero espero que "),
        Segmento.Remarcado(
            texto = "esta carta",
            recuerdo = RecuerdoData(
                titulo = "Por qué escribí esto ✉️",
                texto  = "Explica aquí la razón especial que te motivó a escribir esta carta."
            )
        ),
        Segmento.Normal(" te diga todo lo que mi corazón quiere expresar.")
    )),

    CartaBloque.FotoBloque(
        emoji = "📸",
        desc  = "Aquí va tu segunda foto"
    ),

    CartaBloque.Separador,

    CartaBloque.Parrafo(listOf(
        Segmento.Normal("Ojalá siempre sepas lo importante que eres. No solo para mí, sino para todos los que te conocen. Eres una persona "),
        Segmento.Remarcado(
            texto = "única y especial",
            recuerdo = RecuerdoData(
                titulo = "Lo que más admiro de ti ✨",
                texto  = "Escribe aquí las cualidades que más admiras de la persona que recibirá esta carta."
            )
        ),
        Segmento.Normal(", y me alegra mucho tenerte en mi vida.")
    )),

    CartaBloque.CierreFinal("Con mucho cariño ❤️")
)
// ══════════════════════════════════════════════════════════════════════════════


// ── Pantalla principal ────────────────────────────────────────────────────────
@Composable
fun LetterContentScreen(onBack: () -> Unit) {
    val listState      = rememberLazyListState()
    var recuerdoActivo by remember { mutableStateOf<RecuerdoData?>(null) }
    var mostrarFinal   by remember { mutableStateOf(false) }

    // Detecta cuando el usuario llega al final
    val reachedEnd by remember {
        derivedStateOf {
            val info    = listState.layoutInfo
            val visible = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            info.totalItemsCount > 0 && visible >= info.totalItemsCount - 1
        }
    }
    LaunchedEffect(reachedEnd) {
        if (reachedEnd) {
            delay(300L)
            mostrarFinal = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        LazyColumn(
            state           = listState,
            contentPadding  = PaddingValues(
                top    = 72.dp,
                bottom = 40.dp,
                start  = 18.dp,
                end    = 18.dp
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Encabezado de la carta
            item {
                CartaEncabezado()
                Spacer(Modifier.height(20.dp))
            }

            // Bloques de contenido
            items(contenidoCarta.size) { index ->
                when (val bloque = contenidoCarta[index]) {
                    is CartaBloque.Parrafo ->
                        BloqueParrafo(
                            segmentos        = bloque.segmentos,
                            onRecuerdoClick  = { recuerdoActivo = it }
                        )
                    is CartaBloque.FotoBloque ->
                        BloqueFoto(bloque)
                    is CartaBloque.Separador ->
                        BloqueSeparador()
                    is CartaBloque.CierreFinal ->
                        BloqueCierreFinal(
                            frase          = bloque.frase,
                            mostrarFinal   = mostrarFinal
                        )
                }
            }

            item { Spacer(Modifier.height(60.dp)) }
        }

        // Botón volver
        Box(modifier = Modifier.align(Alignment.TopStart)) {
            BackMenuButton(onBack = onBack, tintColor = GoldAccent.copy(0.9f))
        }

        // Diálogo "Recuerdo"
        recuerdoActivo?.let { recuerdo ->
            RecuerdoDialog(
                recuerdo  = recuerdo,
                onDismiss = { recuerdoActivo = null }
            )
        }
    }
}

// ── Encabezado de la carta ────────────────────────────────────────────────────
@Composable
private fun CartaEncabezado() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            .background(GoldSuave)
            .padding(vertical = 22.dp, horizontal = 20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("✉️", fontSize = 32.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                text       = "Una carta para ti",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color      = Color(0xFF5C3A1E),
                textAlign  = TextAlign.Center
            )
            Text(
                text      = "Con mucho cariño 🌸",
                fontSize  = 13.sp,
                color     = TextoSuave,
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// ── Párrafo con palabras remarcadas ───────────────────────────────────────────
@Composable
private fun BloqueParrafo(
    segmentos:       List<Segmento>,
    onRecuerdoClick: (RecuerdoData) -> Unit
) {
    // Mapa para recuperar el RecuerdoData desde el tag
    val recuerdoMap = remember(segmentos) {
        mutableMapOf<String, RecuerdoData>().also { map ->
            segmentos.filterIsInstance<Segmento.Remarcado>()
                .forEachIndexed { i, seg -> map["R_$i"] = seg.recuerdo }
        }
    }

    @Suppress("DEPRECATION")
    val annotated = remember(segmentos) {
        buildAnnotatedString {
            var remarcadoIndex = 0
            segmentos.forEach { seg ->
                when (seg) {
                    is Segmento.Normal -> append(seg.texto)
                    is Segmento.Remarcado -> {
                        val key = "R_${remarcadoIndex++}"
                        pushStringAnnotation(tag = "RECUERDO", annotation = key)
                        withStyle(SpanStyle(
                            color           = GoldAccent,
                            fontWeight      = FontWeight.SemiBold,
                            background      = GoldAccent.copy(alpha = 0.14f)
                        )) { append(seg.texto) }
                        pop()
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PaperBg)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        @Suppress("DEPRECATION")
        ClickableText(
            text  = annotated,
            style = TextStyle(
                color      = TextoColor,
                fontSize   = 15.5.sp,
                lineHeight = 26.sp,
                fontFamily = FontFamily.Serif,
                textAlign  = TextAlign.Justify
            ),
            onClick = { offset ->
                annotated.getStringAnnotations("RECUERDO", offset, offset)
                    .firstOrNull()
                    ?.let { ann -> recuerdoMap[ann.item]?.let(onRecuerdoClick) }
            }
        )
    }
}

// ── Bloque de foto (placeholder) ──────────────────────────────────────────────
@Composable
private fun BloqueFoto(bloque: CartaBloque.FotoBloque) {
    // Cuando tengas las imágenes, reemplaza este Box con:
    // Image(painter = painterResource(R.drawable.tu_foto), contentDescription = bloque.desc,
    //       modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(12.dp)),
    //       contentScale = ContentScale.Crop)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PaperBg)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GoldSuave),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(bloque.emoji, fontSize = 36.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    text      = bloque.desc,
                    fontSize  = 12.sp,
                    color     = TextoSuave,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

// ── Separador decorativo ──────────────────────────────────────────────────────
@Composable
private fun BloqueSeparador() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PaperBg)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(24.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            // Línea izquierda
            drawLine(GoldAccent.copy(alpha = 0.4f),
                Offset(0f, cy), Offset(cx - 28f, cy), 1.2f)
            // Línea derecha
            drawLine(GoldAccent.copy(alpha = 0.4f),
                Offset(cx + 28f, cy), Offset(size.width, cy), 1.2f)
            // Pequeño corazón central
            drawCircle(GoldAccent.copy(alpha = 0.6f), 4f, Offset(cx - 7f, cy - 1f))
            drawCircle(GoldAccent.copy(alpha = 0.6f), 4f, Offset(cx + 7f, cy - 1f))
            val heartPath = Path().apply {
                moveTo(cx, cy + 7f)
                lineTo(cx - 11f, cy - 2f)
                lineTo(cx, cy - 6f)
                lineTo(cx + 11f, cy - 2f)
                close()
            }
            drawPath(heartPath, GoldAccent.copy(alpha = 0.6f))
        }
    }
}

// ── Cierre final con animación ────────────────────────────────────────────────
@Composable
private fun BloqueCierreFinal(frase: String, mostrarFinal: Boolean) {
    var charCount  by remember { mutableIntStateOf(0) }
    val heartAnim  = remember { Animatable(0f) }

    LaunchedEffect(mostrarFinal) {
        if (!mostrarFinal) return@LaunchedEffect
        delay(200L)
        heartAnim.animateTo(1f, tween(1000, easing = EaseOutBack))
        delay(300L)
        while (charCount < frase.length) {
            delay(45L)
            charCount++
        }
    }

    AnimatedVisibility(
        visible = mostrarFinal,
        enter   = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 2 }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PaperBg)
                .padding(vertical = 32.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Corazón animado con Canvas
                Canvas(modifier = Modifier.size(90.dp)) {
                    val cx  = size.width / 2f
                    val cy  = size.height / 2f + 4f
                    val p   = heartAnim.value
                    val sc  = size.width / 45f * p

                    if (sc <= 0f) return@Canvas

                    // Glow
                    for (i in 3 downTo 1) {
                        drawHeartFill(cx, cy, sc + i * sc * 0.06f,
                            Color(0xFFFF6B8A).copy(alpha = 0.06f * (4 - i)))
                    }
                    drawHeartFill(cx, cy, sc, Color(0xFFE8002D))
                    drawHeartFill(cx, cy, sc, Color(0xFF8B0000).copy(alpha = 0.28f))
                    // Brillo
                    drawCircle(
                        brush  = Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.55f), Color.Transparent),
                            center = Offset(cx - sc * 3.5f, cy - sc * 3.5f),
                            radius = sc * 4f
                        ),
                        radius = sc * 4f,
                        center = Offset(cx - sc * 3.5f, cy - sc * 3.5f)
                    )
                    // Borde
                    drawHeartFill(cx, cy, sc, Color(0xFF8B0000), style = Stroke(sc * 0.18f))
                }

                // Texto letra por letra
                Text(
                    text       = frase.take(charCount),
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color      = Color(0xFFB91C1C),
                    textAlign  = TextAlign.Center
                )

                if (charCount >= frase.length) {
                    Text(
                        text      = "— Con todo mi cariño 🌸",
                        fontSize  = 14.sp,
                        fontStyle = FontStyle.Italic,
                        color     = TextoSuave,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ── Diálogo "Recuerdo" ────────────────────────────────────────────────────────
@Composable
private fun RecuerdoDialog(recuerdo: RecuerdoData, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(PaperBg)
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Título
                Text(
                    text       = recuerdo.titulo,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color      = GoldAccent
                )

                // Separador
                Canvas(modifier = Modifier.fillMaxWidth().height(12.dp)) {
                    drawLine(GoldAccent.copy(alpha = 0.4f),
                        Offset(0f, 6f), Offset(size.width, 6f), 1f)
                }

                // Texto del recuerdo
                Text(
                    text       = recuerdo.texto,
                    fontSize   = 14.sp,
                    lineHeight = 22.sp,
                    color      = TextoColor,
                    fontFamily = FontFamily.Serif
                )

                if (recuerdo.imagenDesc.isNotEmpty()) {
                    // Placeholder imagen del recuerdo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GoldSuave),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text      = recuerdo.imagenDesc,
                            fontSize  = 11.sp,
                            color     = TextoSuave,
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Botón cerrar
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GoldAccent)
                        .clickable { onDismiss() }
                        .padding(horizontal = 20.dp, vertical = 9.dp)
                ) {
                    Text("Cerrar", color = Color.White, fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── Helper: rellena o traza un corazón paramétrico ────────────────────────────
private fun DrawScope.drawHeartFill(
    cx: Float, cy: Float, sc: Float,
    color: Color, style: DrawStyle = Fill
) {
    val pts = (0..200).map { i ->
        val t  = (i.toDouble() / 200) * 2 * PI
        val x  = 16 * sin(t).pow(3)
        val y  = -(13 * cos(t) - 5 * cos(2*t) - 2 * cos(3*t) - cos(4*t))
        Offset(cx + (x * sc).toFloat(), cy + (y * sc).toFloat())
    }
    val path = Path().apply {
        moveTo(pts[0].x, pts[0].y)
        pts.forEach { lineTo(it.x, it.y) }
        close()
    }
    drawPath(path, color, style = style)
}