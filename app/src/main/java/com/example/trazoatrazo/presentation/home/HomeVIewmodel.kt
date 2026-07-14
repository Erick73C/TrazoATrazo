package com.example.trazoatrazo.presentation.home

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.example.trazoatrazo.utils.luminance
import androidx.lifecycle.ViewModel
import com.example.trazoatrazo.domain.model.SpecialEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ── Constantes de archivo (inmutables, fuera del ViewModel) ───────────────────
// List<Color> normal sería inestable para Compose dentro del ViewModel.
// Al ser top-level val, el compilador la trata como referencia fija y estable.
val messageColors = listOf(
    // Claros y Pasteles (Originales)
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
    // Tonos Oscuros (Basados en temas para mejor visibilidad en fondos claros)
    Color(0xFF6B21A8), // JJK Dark Maldición
    Color(0xFF2D1B69), // JJK Dark Expansión
    Color(0xFF1D4ED8), // Midnight Blue Maldición
    Color(0xFF1E3A6E), // Midnight Blue Expansión
    Color(0xFF166534), // Forest Maldición
    Color(0xFF14532D), // Forest Expansión
    Color(0xFF92400E), // Amber Maldición
    Color(0xFF451A03), // Amber Expansión
    Color(0xFF991B1B), // Crimson Maldición
    Color(0xFF450A0A), // Crimson Expansión
    Color(0xFF881144), // Sakura Night Maldición
    Color(0xFF4A0E2A), // Sakura Night Expansión
    Color(0xFF7C2D12), // Autumn Leaves Maldición
    Color(0xFF431407), // Autumn Leaves Expansión
    Color(0xFF1E40AF), // Winter Snow Maldición
    Color(0xFFD4A017), // Golden Night Maldición
    Color(0xFF1A1A1A), // Golden Night Expansión (Casi negro)
    Color(0xFFAD1457), // Valentine Eco
    Color(0xFF00838F), // Ocean Tide Eco
    Color(0xFF9F1239), // Sunset Party Eco
    Color(0xFFB91C1C), // Ruby Red Maldición
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
    private companion object {
        val MESSAGES = listOf(
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
            "🐸 Rebeck",
            "😺 Me encanta mucho hablar contigo",
            "🤪 Barcelona mejor que el madrid :O , nunca nadie dijo jajajaja",
            "🤍 Tu amistad hizo esta etapa mucho más bonita",
            "🌻 Gracias por todos los momentos compartidos",
            "✨ Eres una de las mejores personas que he conocido",
            "🦋 Me alegra mucho haber coincidido contigo",
            "❤️‍🩹 Gracias por escucharme cuando más lo necesité",
            "🌸 Nunca dudes de lo increíble que eres",
            "🌻 Algunas amistades dejan huellas para siempre",
            "✨ Gracias por formar parte de mis recuerdos favoritos",
            "🤍 Siempre recordaré nuestras pláticas",
            "🌷 Conocerte fue una de las mejores cosas de estos años",
            "🎓 Estoy seguro de que lograrás grandes cosas",
            "🌟 Este es solo el comienzo de algo increíble",
            "🌸 Confía en ti tanto como yo confío en ti",
            "🦋 Sigue persiguiendo tus sueños",
            "💛 Nunca cambies esa forma tan bonita de ser",
            "✨ Gracias por todos los consejos que me diste",
            "👩🏼 Qué suerte haber encontrado una amiga como tú",
            "🌸 Espero que cuando veas esto recuerdes lo mucho que te aprecio",
            "🦋 No importa a dónde te lleve la vida, te deseo lo mejor",
            "🌻 Los girasoles siempre buscarán la luz, igual que tú",
            "🤍 Gracias por existir",
            "✨ Me alegra mucho haberte conocido",
            "🌷 Que la vida te regrese toda la felicidad que das",
            "🔴 Y recuerda: siempre estaré agradecido por haberte conocido",
            "⌨️ Todo comenzó con un girasol programado",
            "💻 Cada programa tenía un poquito de cariño escondido",
            "✨ Programar para ti siempre fue divertido",
            "🌸 Detrás de cada dibujo había una razón para sonreír",
            "😀 El girasol fue el primero, pero no el último",
            "💛 A veces el código también puede decir te aprecio",
            "✨ Algunas líneas de código fueron escritas pensando en ti",
            "🌠 Espero que estos pequeños detalles te acompañen siempre",
            "😂 Todavía recuerdo cuando te compré pan por primera vez, estaba muy bueno jaja",
            "🎨 Cada dibujo en esta app fue hecho pensando en ti",
            "💻 Programar esto fue mi forma favorita de decirte que te aprecio",
            "✨ Espero que cada trazo de esta app te saque una sonrisa",
            "🦋 Trazo a trazo",
            "🎁 Esta app es un pequeño rinconcito digital solo para ti",
            "🌟 ¿Ya viste el dibujo de Harley? Me costó que quedara perfecto jaja",
            "🖍️ 'Trazo a Trazo' no es solo código, es un detalle para ti",
            "⚙️ Si algo falla, recuerda: no es un bug, es un detalle artístico",
            "💝 De un girasol a una app entera, así de mucho te aprecio",
            "📜 Hay mensajes aquí que solo tú entenderías, búscalos",
            "💖 Espero que 'Trazo a Trazo' sea tu app favorita hoy y siempre",
            "🌝 Todos los mensajes de la app son para ti",
            "🪷 Ojala averte conocido antes",
            "⭐ Tuve la suerte de conocer a alguien tan especial como tú",
            "🐵 Te deseo que te valla muy bien en esta nueva estapa de tu vida",
            "😸 Eres increible",
            "🙈 Gestion es mejor que industrial",
            "🤍 Oyeeeeeeee, Ten un muy bonito dia ",
            "🤔 ¿Sabías que, para hacer un dibujo toma entre 2 a 4 horas? :O ",
            "😱 Algunos de los dibujos son referencia a algo que te regale o hice para ti"
            ).let { base ->
            base + "😯 ¿Sabías que, hay actualmente en la app ${base.size + 1} mensajes de inicio de sesión? "
        }
    }


    private val _uiState = MutableStateFlow(
        HomeUiState(welcomeMessage = MESSAGES.random())
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /**
     * Cambia el mensaje de bienvenida y avanza el color al tocar la tarjeta.
     *
     * @param background   Color de fondo actual, usado para elegir un color
     *                      de mensaje con buen contraste.
     * @param activeEvent   Evento especial activo hoy, si lo hay (ver
     *                      [com.example.trazoatrazo.utils.EventDetector]).
     *                      Cuando no es null, el nuevo mensaje se sortea desde
     *                      [messagesForToday] en vez de [MESSAGES] a secas,
     *                      dándole probabilidad extra a los mensajes propios
     *                      de la fecha sin garantizar que siempre salga uno.
     */
    fun onMessageTap(background: Color, activeEvent: SpecialEvent? = null) {
        _uiState.update { current ->
            val isDarkBg = background.luminance() <= 0.45f

            // 1. Optimización de contraste: buscar siguiente índice válido
            var nextIdx = (current.colorIndex + 1) % messageColors.size
            for (i in messageColors.indices) {
                val candidate = messageColors[nextIdx]
                val candLum = candidate.luminance()
                val isGoodContrast = if (isDarkBg) candLum > 0.42f else candLum < 0.58f

                if (isGoodContrast) break
                nextIdx = (nextIdx + 1) % messageColors.size
            }

            // Ahora se sortea desde messagesForToday() en vez de MESSAGES directo
            val candidatePool = messagesForToday(activeEvent)
            var nextMsg: String
            do {
                nextMsg = candidatePool.random()
            } while (nextMsg == current.welcomeMessage)

            current.copy(welcomeMessage = nextMsg, colorIndex = nextIdx)
        }
    }

    /**
     * Mezcla los mensajes normales de bienvenida con los mensajes propios
     * de [activeEvent] cuando hay uno activo (ver
     * [com.example.trazoatrazo.utils.EventDetector]). No reemplaza
     * [MESSAGES] — solo los suma, así que durante un evento hay más
     * probabilidad (no exclusividad) de que salga un mensaje alusivo a la
     * fecha, manteniendo la sorpresa del resto del catálogo normal.
     */
    fun messagesForToday(activeEvent: SpecialEvent?): List<String> =
        if (activeEvent != null) MESSAGES + activeEvent.extraWelcomeMessages
        else MESSAGES


    /**
     * Refresca el mensaje de bienvenida inicial para que también pueda
     * salir uno de [activeEvent] desde el primer momento, no solo al tocar
     * la tarjeta. Pensada para llamarse una única vez desde `HomeScreen`
     * en un `LaunchedEffect(activeEvent)` apenas se resuelve el evento del
     * día — nunca en cada recomposición.
     */
    fun refreshWelcomeMessageForEvent(activeEvent: SpecialEvent?) {
        if (activeEvent == null) return
        _uiState.update { current ->
            current.copy(welcomeMessage = messagesForToday(activeEvent).random())
        }
    }
}