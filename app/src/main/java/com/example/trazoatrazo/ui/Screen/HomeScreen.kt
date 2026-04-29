import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trazoatrazo.navigation.Routes
import com.example.trazoatrazo.ui.theme.AppColors
import kotlinx.coroutines.delay


// ── Modelo de categoría ───────────────────────────────────────────────────────
data class DrawingCategory(
    val id:          String,
    val emoji:       String,
    val title:       String,
    val subtitle:    String,
    val bgColor:     Color,
    val accentColor: Color
)

val allCategories = listOf(
    DrawingCategory(
        id          = Routes.Category.FLOWERS,
        emoji       = "🌸",
        title       = "Flores",
        subtitle    = "Girasoles, ramos y más",
        bgColor     = AppColors.FlowersBg,
        accentColor = AppColors.FlowersAccent
    ),
    DrawingCategory(
        id          = Routes.Category.CARTOONS,
        emoji       = "🎭",
        title       = "Cartoons",
        subtitle    = "Próximamente",
        bgColor     = AppColors.CartoonsBg,
        accentColor = AppColors.CartoonsAccent
    ),
    DrawingCategory(
        id          = Routes.Category.ANIMALS,
        emoji       = "🐾",
        title       = "Animales",
        subtitle    = "Próximamente",
        bgColor     = AppColors.AnimalsBg,
        accentColor = AppColors.AnimalsAccent
    ),
)

// ── HomeScreen ────────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(onCategoryClick: (String) -> Unit) {

    val titleAnim  = remember { Animatable(0f) }
    val cardsAnim  = remember { List(allCategories.size) { Animatable(0f) } }

    LaunchedEffect(Unit) {
        titleAnim.animateTo(1f, tween(650, easing = EaseOutBack))
        cardsAnim.forEach { anim ->
            delay(90)
            anim.animateTo(1f, tween(480, easing = EaseOutBack))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Vacio)
    ) {
        // ── Fondo decorativo ─────────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Halo morado arriba derecha
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AppColors.Maldicion.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.85f, size.height * 0.08f),
                    radius = size.width * 0.55f
                ),
                radius = size.width * 0.55f,
                center = Offset(size.width * 0.85f, size.height * 0.08f)
            )
            // Halo índigo abajo izquierda
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AppColors.Expansion.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.1f, size.height * 0.88f),
                    radius = size.width * 0.5f
                ),
                radius = size.width * 0.5f,
                center = Offset(size.width * 0.1f, size.height * 0.88f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Header ───────────────────────────────────────────────────
            Text(
                text     = "✏️",
                fontSize = 52.sp,
                modifier = Modifier.scale(titleAnim.value)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text       = "Trazo a Trazo",
                fontSize   = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = AppColors.Reversa,
                modifier   = Modifier.scale(titleAnim.value)
            )
            Text(
                text     = "Elige una categoría",
                fontSize = 15.sp,
                color    = AppColors.ReversaSuave,
                modifier = Modifier
                    .scale(titleAnim.value)
                    .padding(top = 6.dp)
            )

            Spacer(Modifier.height(44.dp))

            // ── Tarjetas de categorías ────────────────────────────────────
            allCategories.forEachIndexed { index, category ->
                CategoryCard(
                    scale       = cardsAnim[index].value,
                    category    = category,
                    onClick     = { onCategoryClick(category.id) }
                )
                if (index < allCategories.lastIndex) Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ── CategoryCard ──────────────────────────────────────────────────────────────
@Composable
private fun CategoryCard(
    scale:    Float,
    category: DrawingCategory,
    onClick:  () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue   = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "press"
    )

    Box(
        modifier = Modifier
            .scale(scale * pressScale)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(category.bgColor)
            // Borde sutil con el color acento
            .then(
                Modifier.drawWithContent {
                    drawContent()
                    drawRoundRect(
                        color        = category.accentColor.copy(alpha = 0.35f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx()),
                        style        = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2f)
                    )
                }
            )
            .clickable {
                pressed = true
                onClick()
            }
            .padding(vertical = 20.dp, horizontal = 22.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Icono
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(category.accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(category.emoji, fontSize = 30.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = category.title,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = AppColors.Reversa
                )
                Text(
                    text     = category.subtitle,
                    fontSize = 12.sp,
                    color    = AppColors.Eco
                )
            }

            // Flecha con color acento
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(category.accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text("▶", fontSize = 13.sp, color = category.accentColor)
            }
        }
    }
}