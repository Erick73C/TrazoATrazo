package com.example.trazoatrazo.presentation.home

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.example.trazoatrazo.utils.luminance
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ── Constantes de archivo (inmutables, fuera del ViewModel) ───────────────────
// List<Color> normal sería inestable para Compose dentro del ViewModel.
// Al ser top-level val, el compilador la trata como referencia fija y estable.
val messageColors = listOf(
    Color(0xFFEAEAEA), Color(0xFFC8A8F0), Color(0xFF9333EA),
    Color(0xFFFFD700), Color(0xFFFF6B8A), Color(0xFF4FC3F7),
    Color(0xFFFFB74D), Color(0xFFE0E0E0),
    Color(0xFFF48FB1), Color(0xFFCE93D8), Color(0xFF90CAF9),
    Color(0xFFA5D6A7), Color(0xFFFFF59D),
    Color(0xFFFFCC80), Color(0xFFFF8A80), Color(0xFFB0BEC5),
    Color(0xFF7986CB), Color(0xFFFF8A65), Color(0xFFDCE775),
    Color(0xFFC5E1A5), Color(0xFFF06292), Color(0xFFFFB300),
    Color(0xFFB39DDB), Color(0xFF81D4FA), Color(0xFFB2DFDB),
    Color(0xFFF0E68C), Color(0xFFE1BEE7),
    Color(0xFFA1887F), Color(0xFF9575CD), Color(0xFF4DB6AC),
    Color(0xFFD4E157), Color(0xFFFFD54F),
    Color(0xFF42A5F5), Color(0xFF80DEEA), Color(0xFFF48FB1),
)

// ── Estado de la UI ───────────────────────────────────────────────────────────
@Immutable
data class HomeUiState(
    val welcomeMessage: String,
    val colorIndex:     Int = 0
)

// ── ViewModel ─────────────────────────────────────────────────────────────────
@Stable
class HomeViewModel : ViewModel() {
    private val welcomeMessages = listOf(
        "✨ ¡Que hoy sea un día increíble!",
        "💜 Que te vaya muy bien hoy",
        "🎯 ¡Vas a lograr todo lo que te propones!",
        "🌈 Hoy es un gran día para ti",
        "💫 Eres más fuerte de lo que crees",
        "🌻 Que todo te salga bien el día de hoy",
        "⭐ Brilla con todo lo que tienes",
        "🦋 Cada pequeño avance cuenta mucho",
        "🌙 Que descanses y recargues energía",
        "🎵 La vida es mejor con un poco de arte",
        "💎 Eres única e irremplazable",
        "🎨 El arte es la forma más bonita de expresarte",
        "🤍 Te quiero mucho ",
        "🌻 Me alegra mucho haberte conocido",
        "🤍 Siempre voy a guardar un bonito recuerdo tuyo",
        "💜 Espero que la vida te regale días muy felices",
        "🌈 Aunque tomemos caminos distintos, deseo que te vaya increíble",
        "✨ Gracias por todos los momentos bonitos",
        "🦋 Ojalá nunca olvides lo increíble que eres",
        "🌸 Espero que siempre encuentres motivos para sonreír",
        "💫 Mereces todo lo bonito que te pase",
        "🌷 Qué bonito fue coincidir contigo",
        "⭐ Nunca dejes de brillar así",
        "🤍 Siempre voy a desearte lo mejor",
        "🌺 Espero que cumplas todos tus sueños",
        "☁️ Aun en los días difíciles, recuerda lo fuerte que eres",
        "🌙 Descansa también, has hecho mucho",
        "🫶 Gracias por formar parte de mi vida",
        "💎 Hay personas que dejan huellas bonitas, tú eres una de ellas",
        "🌻 Espero que siempre te vaya bonito",
        "✏️ Trazo a trazo es un app para ti",
        "😶‍🌫️ Hay muchos errores de ortografía por la app jajaja",
        "🎉 Es pero que si te guste la app",
        "✨ El mundo se siente más bonito con personas como tú",
        "🎀 Nunca dudes de lo especial que eres",
        "💜 Ojalá la vida te sorprenda con cosas hermosas",
        "🌈 Te esperan cosas muy bonitas, lo sé",
        "🤍 Siempre tendrás un lugar especial en mis recuerdos",
        "🌸 Espero que sonrías mucho hoy",
        "🦋 Sigue siendo esa persona tan linda que eres",
        "✨ Gracias por ser tu",
        "🍣 Hay que ir todos por makis jajaja",
        "👋 Holaaaaaaaaa",
        "🤎 Servicios T",
        "🍗 Abajo el América ",
        "🦅 Penal para el america",
        "💟 Viva el Real Madrid ",
        "📉 Enséñame a evadir impuestos porfa jajaja",
        "👀 ¿En verdad lees esto?",
        "🖥️ Los voy a volver a meter al itsur, pero hora a sistemas para que sean mas felices",
        "🔘 Esta app no la pueden usar los que le van al real Madrid ",
        "👨‍🦲 App creada por erickzen :O",
        "🎮 Hay que jugar jaja",
        "👻 Te extraño ",
        "🍔 Te invito una hamburguesa",
        "🤔 Como te va? ",
        "💰 Me prestas 100000$ ? jaja",
        "🐸 Rebeck"
    )

    private val _uiState = MutableStateFlow(
        HomeUiState(welcomeMessage = welcomeMessages.random())
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onMessageTap(background: Color) {
        _uiState.update { current ->
            val isDarkBg = background.luminance() <= 0.45f
            var nextIdx  = (current.colorIndex + 1) % messageColors.size

            // Buscar un color que contraste bien con el fondo actual
            for (i in 0 until messageColors.size) {
                val candidate = messageColors[nextIdx]
                val candLum   = candidate.luminance()
                
                // Si fondo oscuro, queremos luminancia alta (colores claros)
                // Si fondo claro, queremos luminancia baja (colores oscuros)
                val isGoodContrast = if (isDarkBg) candLum > 0.42f else candLum < 0.58f
                
                if (isGoodContrast) break
                nextIdx = (nextIdx + 1) % messageColors.size
            }

            val nextMsg = welcomeMessages
                .filter { it != current.welcomeMessage }
                .random()
            current.copy(welcomeMessage = nextMsg, colorIndex = nextIdx)
        }
    }
}