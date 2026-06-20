package com.example.trazoatrazo.presentation.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trazoatrazo.data.DrawingItem
import com.example.trazoatrazo.data.drawingCatalog
import com.example.trazoatrazo.navigation.Routes
import com.example.trazoatrazo.ui.components.DrawingCard
import com.example.trazoatrazo.presentation.gallery.GalleryScreen
import com.example.trazoatrazo.presentation.home.HomeViewModel
import com.example.trazoatrazo.presentation.home.messageColors
import com.example.trazoatrazo.ui.theme.AppColors
import com.example.trazoatrazo.utils.adaptiveColorFor
import com.example.trazoatrazo.utils.categoryLabelFor
import kotlinx.coroutines.launch
import com.example.trazoatrazo.ui.background.DynamicBackground
import com.example.trazoatrazo.ui.background.LocalBackgroundConfig
import com.example.trazoatrazo.presentation.settings.SettingsViewModel
import com.example.trazoatrazo.ui.theme.LocalAppColors
import com.example.trazoatrazo.ui.theme.LocalAppFont
import com.example.trazoatrazo.ui.theme.fontFamilyFor

@Immutable
private data class TabInfo(
    val categoryId:  String,
    val emoji:       String,
    val label:       String,
    val accentColor: Color
)


// ── HomeScreen ────────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onDrawingClick: (categoryId: String, drawingId: String) -> Unit,
    onLetterClick:  () -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val homeTabs = remember(
        AppColors.FlowersAccent,
        AppColors.CartoonsAccent,
        AppColors.AnimalsAccent,
        AppColors.SpringAccent,
        AppColors.WinterAccent
    ) {
        listOf(
            TabInfo(Routes.Category.FLOWERS,  "🌸", "Flores",    AppColors.FlowersAccent),
            TabInfo(Routes.Category.CARTOONS, "🎭", "Cartoons",  AppColors.CartoonsAccent),
            TabInfo(Routes.Category.ANIMALS,  "🐾", "Animales",  AppColors.AnimalsAccent),
            TabInfo(Routes.Category.SPRING,   "🌱", "Primavera", AppColors.SpringAccent),
            TabInfo(Routes.Category.WINTER,   "❄️", "Invierno",  AppColors.WinterAccent),
            TabInfo(Routes.Category.SPECIAL,  "⭐", "Especial",  Color(0xFF9333EA)),
            TabInfo(Routes.Category.GALLERY,  "📷", "Galería",   Color(0xFFF06292)),
        )
    }

    // ── Animaciones de entrada ────────────────────────────────────────────────
    val headerAnim  = remember { Animatable(0f) }
    val welcomeAnim = remember { Animatable(0f) }
    val tabsAnim    = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        headerAnim.animateTo( 1f, tween(700, easing = EaseOutBack))
        welcomeAnim.animateTo(1f, tween(550, easing = EaseOutCubic))
        tabsAnim.animateTo(   1f, tween(450, easing = EaseOutCubic))
    }

    val pagerState     = rememberPagerState(pageCount = { homeTabs.size })
    val coroutineScope = rememberCoroutineScope()

    // ── Obtener tema y config del CompositionLocal ────────────────────────────
//    val settingsViewModel: SettingsViewModel = viewModel()
//    val selectedTheme by settingsViewModel.selectedTheme.collectAsStateWithLifecycle()

    DynamicBackground(
        theme   = LocalAppColors.current.appTheme,
        config  = LocalBackgroundConfig.current,
        bgColor = AppColors.Vacio
    ) {
        // ── Fondo decorativo ──────────────────────────────────────────────────


        Column(modifier = Modifier.fillMaxSize()) {

            // ── HEADER ────────────────────────────────────────────────────────
            HomeHeader(
                animValue       = headerAnim.value,
                onSettingsClick = onSettingsClick
            )

            // ── DIVIDER ───────────────────────────────────────────────────────
            HorizontalDivider(
                thickness = 1.dp,
                color     = AppColors.Maldicion.copy(alpha = 0.35f)
            )

            // ── WELCOME + ENVELOPE ────────────────────────────────────────────
            val currentMessageColor = messageColors[uiState.colorIndex]
                .adaptiveColorFor(AppColors.Sombra)

            WelcomeSection(
                message      = uiState.welcomeMessage,
                messageColor = currentMessageColor,
                animValue    = welcomeAnim.value,
                onMessageTap = { viewModel.onMessageTap(AppColors.Sombra) },
                onEnvelopeTap = onLetterClick
            )

            // ── TAB ROW ───────────────────────────────────────────────────────
            CategoryTabRow(
                tabs          = homeTabs,
                selectedIndex = pagerState.currentPage,
                animValue     = tabsAnim.value,
                onTabClick    = { index ->
                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                }
            )

            // ── PAGER ─────────────────────────────────────────────────────────
            HorizontalPager(
                state    = pagerState,
                modifier = Modifier.weight(1f),
                beyondViewportPageCount = 0,
                key                  = { page -> page }
            ) { page ->
                val tab = homeTabs[page]
                when (tab.categoryId) {
                    Routes.Category.GALLERY ->
                        GalleryScreen()
                    else ->
                        DrawingsPage(
                            categoryId     = tab.categoryId,
                            onDrawingClick = onDrawingClick
                        )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ── HEADER ────────────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HomeHeader(
    animValue:       Float,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(animValue)
            .padding(top = 52.dp, bottom = 16.dp, start = 22.dp, end = 22.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier              = Modifier.fillMaxWidth()
        ) {
            // Logo box
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                AppColors.Maldicion.copy(alpha = 0.5f),
                                AppColors.Expansion.copy(alpha = 0.5f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("✏️", fontSize = 22.sp)
            }

            // Título
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = "Trazo a Trazo",
                    fontSize   = 23.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = AppColors.Reversa
                )
                Text(
                    text     = "¡HOLAAAAA! ✨, " +
                                "V 3.0",
                    fontSize = 11.sp,
                    color    = AppColors.Eco
                )
            }

            // Botón ajustes
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(AppColors.Sombra)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onSettingsClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("⚙️", fontSize = 19.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ── WELCOME + ENVELOPE ────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun WelcomeSection(
    message:      String,
    messageColor: Color,
    animValue:    Float,
    onMessageTap: () -> Unit,
    onEnvelopeTap: () -> Unit
) {
    // Flotación del sobre
    val infiniteTransition = rememberInfiniteTransition(label = "envelope_float")
    val envelopeOffsetY by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = -9f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "envelopeY"
    )
    val envelopeRotation by infiniteTransition.animateFloat(
        initialValue  = -1.5f,
        targetValue   = 1.5f,
        animationSpec = infiniteRepeatable(
            animation  = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "envelopeRot"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(animValue)
            .offset(y = ((1f - animValue) * 20).dp)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // ── Tarjeta de mensaje ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(AppColors.Sombra)
                .then(
                    Modifier.drawWithContent {
                        drawContent()
                        drawRoundRect(
                            color        = AppColors.Maldicion.copy(alpha = 0.3f),
                            cornerRadius = CornerRadius(18.dp.toPx()),
                            style        = Stroke(1.2f)
                        )
                    }
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onMessageTap
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(AppColors.Tecnica)
                    )
                    Text(
                        text          = "MENSAJE DEL DÍA :O",
                        fontSize      = 9.sp,
                        color         = AppColors.Eco,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(Modifier.height(7.dp))

                Text(
                    text       = message,
                    fontFamily = fontFamilyFor(LocalAppFont.current),
                    fontSize   = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    color      = messageColor,
                    lineHeight = 19.sp
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text     = "Toca para cambiar →",
                    fontSize = 9.sp,
                    color    = AppColors.Eco.copy(alpha = 0.55f)
                )
            }
        }

        // ── Sobre flotante ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(72.dp)
                .offset(y = envelopeOffsetY.dp)
                .rotate(envelopeRotation)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onEnvelopeTap
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w   = size.width
                val h   = size.height
                val pad = 8f

                // Glow detrás del sobre
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(AppColors.Maldicion.copy(alpha = 0.25f), Color.Transparent),
                        center = Offset(w / 2f, h / 2f),
                        radius = w * 0.65f
                    ),
                    radius = w * 0.65f,
                    center = Offset(w / 2f, h / 2f)
                )

                val bodyTop  = h * 0.25f
                val bodyLeft = pad
                val bodyW    = w - pad * 2
                val bodyH    = h * 0.62f

                // Sombra del cuerpo
                drawRoundRect(
                    color        = Color.Black.copy(alpha = 0.35f),
                    topLeft      = Offset(bodyLeft + 2f, bodyTop + 3f),
                    size         = Size(bodyW, bodyH),
                    cornerRadius = CornerRadius(10f)
                )

                // Cuerpo del sobre
                drawRoundRect(
                    brush        = Brush.linearGradient(
                        colors = listOf(AppColors.Sombra, AppColors.Dominio),
                        start  = Offset(0f, bodyTop),
                        end    = Offset(0f, bodyTop + bodyH)
                    ),
                    topLeft      = Offset(bodyLeft, bodyTop),
                    size         = Size(bodyW, bodyH),
                    cornerRadius = CornerRadius(10f)
                )

                // Borde del cuerpo
                drawRoundRect(
                    color        = AppColors.Maldicion.copy(alpha = 0.55f),
                    topLeft      = Offset(bodyLeft, bodyTop),
                    size         = Size(bodyW, bodyH),
                    cornerRadius = CornerRadius(10f),
                    style        = Stroke(width = 1.5f)
                )

                // Solapa V (abre hacia abajo)
                val flapPath = Path().apply {
                    moveTo(bodyLeft + 2f, bodyTop + 2f)
                    lineTo(w / 2f, bodyTop + bodyH * 0.44f)
                    lineTo(bodyLeft + bodyW - 2f, bodyTop + 2f)
                }
                drawPath(flapPath, color = AppColors.Expansion.copy(alpha = 0.85f), style = Stroke(2.2f))

                // Líneas laterales de doblez
                drawLine(
                    color       = AppColors.Expansion.copy(alpha = 0.65f),
                    start       = Offset(bodyLeft + 2f, bodyTop + bodyH - 2f),
                    end         = Offset(w / 2f, bodyTop + bodyH * 0.44f),
                    strokeWidth = 1.8f
                )
                drawLine(
                    color       = AppColors.Expansion.copy(alpha = 0.65f),
                    start       = Offset(bodyLeft + bodyW - 2f, bodyTop + bodyH - 2f),
                    end         = Offset(w / 2f, bodyTop + bodyH * 0.44f),
                    strokeWidth = 1.8f
                )

                // Corazón sello al centro
                val hx  = w / 2f
                val hy  = bodyTop + bodyH * 0.68f
                val hr  = 5.5f
                val heartGlow = Path().apply {
                    moveTo(hx, hy + hr * 0.5f)
                    cubicTo(hx - hr * 2f, hy - hr * 0.8f, hx - hr * 2f, hy + hr * 1.2f, hx, hy + hr * 2.2f)
                    cubicTo(hx + hr * 2f, hy + hr * 1.2f, hx + hr * 2f, hy - hr * 0.8f, hx, hy + hr * 0.5f)
                }
                drawPath(heartGlow, color = AppColors.KiEspiritual.copy(alpha = 0.18f))

                drawCircle(AppColors.KiEspiritual, hr, Offset(hx - hr * 0.55f, hy - hr * 0.1f))
                drawCircle(AppColors.KiEspiritual, hr, Offset(hx + hr * 0.55f, hy - hr * 0.1f))
                val heartFill = Path().apply {
                    moveTo(hx - hr * 1.15f, hy + hr * 0.2f)
                    lineTo(hx, hy + hr * 1.5f)
                    lineTo(hx + hr * 1.15f, hy + hr * 0.2f)
                    close()
                }
                drawPath(heartFill, color = AppColors.KiEspiritual)

                // Estrellitas decorativas
                val stars = listOf(
                    Offset(bodyLeft + 8f, bodyTop + 6f),
                    Offset(bodyLeft + bodyW - 10f, bodyTop + 5f),
                    Offset(bodyLeft + bodyW * 0.25f, bodyTop + 8f),
                )
                stars.forEach { s ->
                    drawCircle(Color.White.copy(alpha = 0.35f), 1.8f, s)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ── TAB ROW ───────────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CategoryTabRow(
    tabs:          List<TabInfo>,
    selectedIndex: Int,
    animValue:     Float,
    onTabClick:    (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(animValue)
            .background(AppColors.Sombra)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = index == selectedIndex

            val bgColor by animateColorAsState(
                targetValue   = if (isSelected) tab.accentColor.copy(alpha = 0.22f) else Color.Transparent,
                animationSpec = tween(220),
                label         = "tabBg_$index"
            )
            val labelColor by animateColorAsState(
                targetValue   = if (isSelected) tab.accentColor else AppColors.Eco,
                animationSpec = tween(220),
                label         = "tabLabel_$index"
            )
            val tabScale by animateFloatAsState(
                targetValue   = if (isSelected) 1.08f else 1f,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label         = "tabScale_$index"
            )

            Column(
                modifier = Modifier
                    .scale(tabScale)
                    .clip(RoundedCornerShape(13.dp))
                    .background(bgColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null
                    ) { onTabClick(index) }
                    .padding(horizontal = 9.dp, vertical = 7.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(tab.emoji, fontSize = 19.sp)
                Text(
                    text       = tab.label,
                    fontSize   = 9.sp,
                    color      = labelColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    maxLines   = 1
                )
                // Indicador activo
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .width(if (isSelected) 18.dp else 0.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(tab.accentColor)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ── DRAWINGS PAGE ─────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
// ── DRAWINGS PAGE ─────────────────────────────────────────────────────────────
@Composable
private fun DrawingsPage(
    categoryId:     String,
    onDrawingClick: (categoryId: String, drawingId: String) -> Unit
    // accentColor eliminado — ya no se usa aquí
) {
    val drawings = drawingCatalog[categoryId] ?: emptyList()

    if (drawings.isEmpty()) {
        Box(
            modifier         = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.padding(32.dp)
            ) {
                Text("🎨", fontSize = 52.sp)
                Spacer(Modifier.height(14.dp))
                Text(
                    text       = "Próximamente",
                    fontSize   = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color      = AppColors.Reversa
                )
                Text(
                    text      = "Se están preparando dibujos\npara esta sección",
                    fontSize  = 14.sp,
                    color     = AppColors.ReversaSuave,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.padding(top = 8.dp)
                )
            }
        }
        return
    }

    LazyColumn(
        contentPadding      = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(
            items = drawings,
            key   = { _, drawing -> drawing.id }   // ← estabilidad de lista
        ) { _, drawing ->
            DrawingCard(
                emoji         = drawing.emoji,
                title         = drawing.title,
                description   = drawing.description,
                categoryId    = categoryId,
                accentColor   = drawing.accentColor,
                categoryLabel = categoryLabelFor(categoryId),
                onClick       = { onDrawingClick(categoryId, drawing.id) }
            )
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}