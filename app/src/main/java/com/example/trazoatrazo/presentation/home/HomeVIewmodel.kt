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
        "🤍 Te quiero mucho "
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