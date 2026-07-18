package com.example.trazoatrazo.presentation.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trazoatrazo.R
import com.example.trazoatrazo.data.DrawingItem
import com.example.trazoatrazo.data.drawingCatalog
import com.example.trazoatrazo.domain.model.TimeCapsule
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
import com.example.trazoatrazo.ui.components.LockedCapsuleOverlay
import com.example.trazoatrazo.ui.theme.LocalAppColors
import com.example.trazoatrazo.ui.theme.LocalAppFont
import com.example.trazoatrazo.ui.theme.LocalMessageStyle
import com.example.trazoatrazo.ui.theme.MessageStyle
import com.example.trazoatrazo.ui.theme.fontFamilyFor
import com.example.trazoatrazo.utils.CapsuleUtils
import com.example.trazoatrazo.domain.model.SpecialEvent
import com.example.trazoatrazo.presentation.usage.AppUsageViewModel
import com.example.trazoatrazo.ui.components.EventBanner
import com.example.trazoatrazo.ui.components.EventInfoBox
import com.example.trazoatrazo.ui.components.UnlockBanner
import com.example.trazoatrazo.utils.EventDetector
import com.example.trazoatrazo.utils.NotificationHelper
import com.example.trazoatrazo.utils.UnlockUtils

@Immutable
private data class TabInfo(
    val categoryId:  String,
    val emoji:       String,
    val label:       String,
    val accentColor: Color
)


// HomeScreen
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    appUsageViewModel: AppUsageViewModel,
    onDrawingClick: (categoryId: String, drawingId: String) -> Unit,
    onLetterClick:  () -> Unit,
    onSettingsClick: () -> Unit,
    onMyCreationsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Evento activo hoy — se calcula una sola vez por composición del Home
    val activeEvent = remember { EventDetector.activeEventToday() }

    LaunchedEffect(activeEvent) {
        viewModel.refreshWelcomeMessageForEvent(activeEvent)
    }

    // Racha de uso: registro de apertura + detección de desbloqueos ────
    val daysOpenedCount by appUsageViewModel.daysOpenedCount.collectAsStateWithLifecycle()
    val notifiedIds     by appUsageViewModel.notifiedIds.collectAsStateWithLifecycle()
    val lastShownEventId by appUsageViewModel.lastShownEventId.collectAsStateWithLifecycle()

    var showEventInfo by remember { mutableStateOf(false) }

    // Auto-mostrar información del evento una sola vez por cada nuevo evento
    LaunchedEffect(activeEvent?.id, lastShownEventId) {
        if (activeEvent != null && activeEvent.id != lastShownEventId) {
            showEventInfo = true
        }
    }

    // Se registra la apertura de hoy una sola vez por sesión.
    LaunchedEffect(Unit) {
        appUsageViewModel.registerAppOpenToday()
    }

    // Mensaje del banner de desbloqueo pendiente de mostrar (null = ninguno)
    var unlockBannerMessage by remember { mutableStateOf<String?>(null) }

    // Cada vez que cambia la racha, revisa si algo cruzó su umbral y aún
    // no fue notificado. Solo muestra UN banner a la vez (el primero que
    // encuentre) — si hubiera varios pendientes, los siguientes aparecerán
    // en la próxima recomposición de daysOpenedCount.
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(daysOpenedCount, notifiedIds) {
        val unlockedNow = UnlockUtils.currentlyUnlockedDrawingIds(daysOpenedCount)
        val pendingId = unlockedNow.firstOrNull { it !in notifiedIds }
        if (pendingId != null) {
            val requirement = UnlockUtils.findRequirementFor(pendingId)
            val drawingTitle = drawingCatalog.values.flatten().find { it.id == pendingId }?.title ?: "Nuevo dibujo"
            
            unlockBannerMessage = requirement?.unlockedMessage
                ?: "🌻 Nuevo recuerdo desbloqueado"
            
            // Notificación Push
            NotificationHelper.showUnlockNotification(context, drawingTitle)
            
            appUsageViewModel.markAsNotified(pendingId)
        }
    }

    // Detección de desbloqueo por Cápsula del Tiempo
    LaunchedEffect(Unit, notifiedIds) {
        drawingCatalog.values.flatten().forEach { drawing ->
            if (drawing.capsuleId != null && !CapsuleUtils.isCapsuleLocked(drawing.id)) {
                if (drawing.id !in notifiedIds) {
                    NotificationHelper.showUnlockNotification(context, drawing.title)
                    appUsageViewModel.markAsNotified(drawing.id)
                }
            }
        }
    }

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

    val headerAnimProvider  = remember { { headerAnim.value } }
    val welcomeAnimProvider = remember { { welcomeAnim.value } }
    val tabsAnimProvider    = remember { { tabsAnim.value } }

    DynamicBackground(
        theme   = LocalAppColors.current.appTheme,
        config  = LocalBackgroundConfig.current,
        bgColor = AppColors.Vacio,
        eventOverride = activeEvent
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── HEADER ────────────────────────────────────────────────────────
            HomeHeader(
                animValueProvider = headerAnimProvider,
                activeEvent       = activeEvent,
                streakCount       = daysOpenedCount,
                onSettingsClick   = onSettingsClick,
                onMyCreationsClick = onMyCreationsClick,
                onEventClick       = { showEventInfo = true }
            )

            // ── Banner de desbloqueo (independiente del de evento) ───────
            unlockBannerMessage?.let { message ->
                UnlockBanner(
                    message     = message,
                    onDismissed = { unlockBannerMessage = null }
                )
            }

            // ── DIVIDER ───────────────────────────────────────────────────────
            HorizontalDivider(
                thickness = 1.dp,
                color     = AppColors.Maldicion.copy(alpha = 0.35f)
            )

            // ── WELCOME + ENVELOPE ────────────────────────────────────────────
            val currentMessageColor = remember(uiState.colorIndex) {
                messageColors[uiState.colorIndex]
                    .adaptiveColorFor(AppColors.Sombra)
            }

            WelcomeSection(
                message           = uiState.welcomeMessage,
                messageColor      = currentMessageColor,
                animValueProvider = welcomeAnimProvider,
                onMessageTap      = { viewModel.onMessageTap(AppColors.Sombra, activeEvent) },
                onEnvelopeTap     = onLetterClick
            )

            // ── TAB ROW ───────────────────────────────────────────────────────
            CategoryTabRow(
                tabs              = homeTabs,
                selectedIndex     = pagerState.currentPage,
                animValueProvider = tabsAnimProvider,
                onTabClick        = { index ->
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
                            activeEvent    = activeEvent,
                            daysOpenedCount = daysOpenedCount,
                            onDrawingClick = onDrawingClick
                        )
                }
            }
        }

        if (showEventInfo && activeEvent != null) {
            EventInfoBox(
                event     = activeEvent,
                onDismiss = {
                    showEventInfo = false
                    appUsageViewModel.markEventAsShown(activeEvent.id)
                }
            )
        }
    }
}

//HEADER
@Composable
private fun HomeHeader(
    animValueProvider: () -> Float,
    activeEvent:       SpecialEvent?,
    streakCount:       Int,
    onSettingsClick:   () -> Unit,
    onMyCreationsClick: () -> Unit,
    onEventClick:      () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                val scale = animValueProvider()
                scaleX = scale
                scaleY = scale
            }
            .padding(top = 52.dp, bottom = 12.dp, start = 20.dp, end = 20.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier              = Modifier.fillMaxWidth()
        ) {
            // Logo box
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
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
                Image(
                    painter = painterResource(id = R.drawable.ic_recuerdos_logo_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp)
                )
            }

            // Título y Racha
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = "Trazo a Trazo",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = AppColors.Reversa
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Píldora de racha
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppColors.KiEspiritual.copy(alpha = 0.15f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "🔥 $streakCount días",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.KiEspiritual
                        )
                    }

                    Text(
                        text     = "V 4.0",
                        fontSize = 10.sp,
                        color    = AppColors.Eco.copy(alpha = 0.7f)
                    )
                }
            }

            // Botones de acción agrupados
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Botón de evento
                if (activeEvent != null) {
                    HeaderActionButton(
                        content = { Text(activeEvent.bannerEmoji, fontSize = 16.sp) },
                        onClick = onEventClick
                    )
                }

                HeaderActionButton(
                    content = { Text("🖼️", fontSize = 16.sp) },
                    onClick = onMyCreationsClick
                )

                HeaderActionButton(
                    content = { Text("⚙️", fontSize = 17.sp) },
                    onClick = onSettingsClick
                )
            }
        }
    }
}

@Composable
private fun HeaderActionButton(
    content: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(AppColors.Sombra)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            ),
        contentAlignment = Alignment.Center,
        content = { content() }
    )
}

// WELCOME + ENVELOPE
@Composable
private fun WelcomeSection(
    message:           String,
    messageColor:      Color,
    animValueProvider: () -> Float,
    onMessageTap:      () -> Unit,
    onEnvelopeTap:     () -> Unit
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
            .graphicsLayer { alpha = animValueProvider() }
            .offset {
                val v = animValueProvider()
                IntOffset(0, ((1f - v) * 15).dp.roundToPx())
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Tarjeta de mensaje
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

                val messageStyle = LocalMessageStyle.current
                Text(
                    text       = message,
                    fontFamily = fontFamilyFor(LocalAppFont.current),
                    fontSize   = 13.5.sp,
                    fontWeight = if (messageStyle == MessageStyle.BOLD) FontWeight.Bold else FontWeight.Medium,
                    fontStyle  = if (messageStyle == MessageStyle.ITALIC) FontStyle.Italic else FontStyle.Normal,
                    textDecoration = if (messageStyle == MessageStyle.UNDERLINE) TextDecoration.Underline else TextDecoration.None,
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

        // Sobre flotante
        Box(
            modifier = Modifier
                .size(72.dp)
                .offset { IntOffset(0, envelopeOffsetY.dp.roundToPx()) }
                .graphicsLayer { rotationZ = envelopeRotation }
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

// TAB ROW
@Composable
private fun CategoryTabRow(
    tabs:              List<TabInfo>,
    selectedIndex:     Int,
    animValueProvider: () -> Float,
    onTabClick:        (Int) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = animValueProvider() }
            .background(AppColors.Sombra)
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
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


@Composable
private fun DrawingsPage(
    categoryId:     String,
    activeEvent:    SpecialEvent?,
    daysOpenedCount: Int,
    onDrawingClick: (categoryId: String, drawingId: String) -> Unit
) {
    val drawings = drawingCatalog[categoryId] ?: emptyList()

    var lockedCapsuleToShow by remember { mutableStateOf<TimeCapsule?>(null) }

    // Si el evento activo trae un dibujo temporal para ESTA categoría,
    // se busca su DrawingItem real en el catálogo para reusar su emoji/
    // accentColor tal cual, solo se destaca con un badge distinto.
    val eventDrawing = remember(activeEvent?.id, categoryId) {
        if (activeEvent?.temporaryDrawingCategoryId == categoryId) {
            drawingCatalog[categoryId]?.firstOrNull {
                it.id == activeEvent.temporaryDrawingId
            }
        } else null
    }

    if (drawings.isEmpty()) {
        // ... (sin cambios, se queda igual)
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

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding      = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Tarjeta destacada del evento (solo aparece durante la temporada) ──
            eventDrawing?.let { drawing ->
                item(key = "event_${drawing.id}") {
                    DrawingCard(
                        emoji         = drawing.emoji,
                        title         = drawing.title,
                        description   = activeEvent?.bannerMessage ?: drawing.description,
                        categoryId    = categoryId,
                        accentColor   = activeEvent?.accentColor ?: drawing.accentColor,
                        categoryLabel = "Destacado",
                        isNew         = true,
                        onClick       = { onDrawingClick(categoryId, drawing.id) }
                    )
                }
            }

            itemsIndexed(
                items = drawings,
                key   = { _, drawing -> drawing.id }
            ) { _, drawing ->
                val capsule = remember(drawing.capsuleId) {
                    drawing.capsuleId?.let { CapsuleUtils.findCapsuleById(it) }
                }
                val isCapsuleLockedNow = capsule != null &&
                        CapsuleUtils.isCapsuleLocked(drawing.id)
                val capsuleDaysRemaining = if (isCapsuleLockedNow)
                    CapsuleUtils.daysRemainingFor(drawing.id) else 0L

                // Estado de bloqueo por racha de uso — una sola búsqueda del
                // requisito, reutilizada para ambos valores derivados
                val requirement = remember(drawing.id) {
                    UnlockUtils.findRequirementFor(drawing.id)
                }
                val isRaceLockedNow = remember(requirement, daysOpenedCount) {
                    requirement != null && daysOpenedCount < requirement.requiredDaysOpened
                }
                val raceDaysRemaining = remember(requirement, daysOpenedCount) {
                    (requirement?.let { it.requiredDaysOpened - daysOpenedCount } ?: 0)
                        .coerceAtLeast(0)
                }

                DrawingCard(
                    emoji                = drawing.emoji,
                    title                = drawing.title,
                    description          = drawing.description,
                    categoryId           = categoryId,
                    accentColor          = drawing.accentColor,
                    categoryLabel        = categoryLabelFor(categoryId),
                    isCapsuleLocked      = isCapsuleLockedNow,
                    capsuleDaysRemaining = capsuleDaysRemaining,
                    isRaceLocked         = isRaceLockedNow,
                    raceDaysRemaining    = raceDaysRemaining,
                    onClick              = { onDrawingClick(categoryId, drawing.id) },
                    onLockedClick        = {
                        if (isCapsuleLockedNow) lockedCapsuleToShow = capsule
                    }
                )
            }
            item { Spacer(Modifier.height(20.dp)) }
        }

        lockedCapsuleToShow?.let { capsule ->
            LockedCapsuleOverlay(
                capsule = capsule,
                onBack  = { lockedCapsuleToShow = null }
            )
        }
    }
}