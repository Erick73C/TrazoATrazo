package com.example.trazoatrazo.presentation.home

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ── Estado de la UI ───────────────────────────────────────────────────────────
data class HomeUiState(
    val welcomeMessage: String,
    val colorIndex:     Int = 0
)

// ── ViewModel ─────────────────────────────────────────────────────────────────
class HomeViewModel : ViewModel() {

    // Lista pública para que HomeScreen pueda leerla por índice sin Boxing
    val messageColors = listOf(
        Color(0xFFEAEAEA),   // Reversa  (blanco suave — default)
        Color(0xFFC8A8F0),   // Ki espiritual (lila suave)
        Color(0xFF9333EA),   // Técnica (morado)
        Color(0xFFFFD700),   // Dorado
        Color(0xFFFF6B8A),   // Rosa
        Color(0xFF4FC3F7),   // Azul cielo
        Color(0xFF81C784),   // Verde suave
        Color(0xFFFFB74D),   // Ámbar
        Color(0xFFE0E0E0),   // Plata
        Color(0xFFF48FB1),   // Rosa pastel
        Color(0xFFCE93D8),   // Lavanda
        Color(0xFF90CAF9),   // Azul pastel
        Color(0xFF80CBC4),   // Turquesa suave
        Color(0xFFA5D6A7),   // Verde menta
        Color(0xFFFFF59D),   // Amarillo canario
        Color(0xFFFFCC80),   // Melocotón
        Color(0xFFFF8A80),   // Coral
        Color(0xFFB0BEC5),   // Gris azulado
        Color(0xFF7986CB),   // Índigo suave
        Color(0xFFFF8A65),   // Naranja profundo
        Color(0xFFDCE775),   // Lima
        Color(0xFF4DD0E1),   // Cian
        Color(0xFFC5E1A5),   // Verde té (Ojos gato)
        Color(0xFFF8BBD0),   // Rosa pálido
        Color(0xFFF06292),   // Rosa fuerte
        Color(0xFFFFF176),   // Amarillo suave
        Color(0xFFFFB300),   // Ambar brillante
        Color(0xFFB39DDB),   // Violeta suave
        Color(0xFF81D4FA),   // Azul cielo claro
        Color(0xFFB2DFDB),   // Menta claro
        Color(0xFFF0E68C),   // Arena / Khaki
        Color(0xFFE1BEE7),   // Lila muy claro
        Color(0xFFFFA07A),   // Salmón
        Color(0xFFA1887F),   // Café claro / Tierra
        Color(0xFF9575CD),   // Morado medio
        Color(0xFF4DB6AC),   // Verde azulado
        Color(0xFFD4E157),   // Lima brillante
        Color(0xFFFFD54F),   // Ámbar claro
        Color(0xFFBA68C8),   // Amatista
        Color(0xFF42A5F5),   // Azul brillante
    )

    private val welcomeMessages = listOf(
        "✨ ¡Que hoy sea un día increíble!",
        "🌟 Tú puedes con todo lo que venga",
        "💜 Que te vaya muy bien hoy",
        "🌸 Cada día es una nueva oportunidad",
        "🎯 ¡Vas a lograr todo lo que te propones!",
        "🌈 Hoy es un gran día para ti",
        "💫 Eres más fuerte de lo que crees",
        "🌻 Que todo te salga bien el día de hoy",
        "⭐ Brilla con todo lo que tienes",
        "🦋 Cada pequeño avance cuenta mucho",
        "🌙 Que descanses y recargues energía",
        "🎵 La vida es mejor con un poco de arte",
        "💎 Eres única e irremplazable",
        "🌊 Fluye, todo llegará a su tiempo",
        "🏆 Hoy será tu mejor día, lo sé",
        "🎨 El arte es la forma más bonita de expresarte",
        "🌺 Que florezca algo bonito en tu día",
        "🦅 Vuela alto, sin miedo a caer",
        "💪 Eres capaz de todo lo que te propongas",
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
        "✨ El mundo se siente más bonito con personas como tú",
        "🎀 Nunca dudes de lo especial que eres",
        "💜 Ojalá la vida te sorprenda con cosas hermosas",
        "🌈 Te esperan cosas muy bonitas, lo sé",
        "🤍 Siempre tendrás un lugar especial en mis recuerdos",
        "🌸 Espero que sonrías mucho hoy",
        "🦋 Sigue siendo esa persona tan linda que eres",
        "✨ Gracias por existir",
        "🤎 Servicios T",
        "🎮 Hay que jugar jaja"
    )

    private val _uiState = MutableStateFlow(
        HomeUiState(welcomeMessage = welcomeMessages.random())
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // ── Acción: tap al mensaje → cambia mensaje Y color ───────────────────────
    fun onMessageTap() {
        _uiState.update { current ->
            val nextColor = (current.colorIndex + 1) % messageColors.size
            val nextMsg   = welcomeMessages
                .filter { it != current.welcomeMessage }
                .random()
            current.copy(welcomeMessage = nextMsg, colorIndex = nextColor)
        }
    }
}