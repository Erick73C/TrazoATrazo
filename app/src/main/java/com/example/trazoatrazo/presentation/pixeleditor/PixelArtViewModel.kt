package com.example.trazoatrazo.presentation.pixeleditor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trazoatrazo.data.PixelArtwork
import com.example.trazoatrazo.data.PixelArtworkPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class PixelArtViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PixelArtworkPreferences(application)

    private val _artworks = MutableStateFlow<List<PixelArtwork>>(emptyList())
    val artworks: StateFlow<List<PixelArtwork>> = _artworks.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.getArtworksFlow().collect { list ->
                _artworks.value = list.sortedByDescending { it.createdAt }
            }
        }
    }

    fun save(artwork: PixelArtwork) {
        viewModelScope.launch { prefs.saveArtwork(artwork) }
    }

    fun delete(id: String) {
        viewModelScope.launch { prefs.deleteArtwork(id) }
    }

    fun findById(id: String): PixelArtwork? = _artworks.value.find { it.id == id }

    fun newId(): String = UUID.randomUUID().toString()
}