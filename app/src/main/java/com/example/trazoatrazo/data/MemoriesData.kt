package com.example.trazoatrazo.data

import com.example.trazoatrazo.R
import com.example.trazoatrazo.domain.model.Memory
import com.example.trazoatrazo.domain.model.MemoryCategory

val memoriesList: List<Memory> = listOf(

    // ── Personales ─────────────────────────────────────────────────
    Memory(
        id          = 1,
        drawableRes = R.drawable.foto_contigo,
        title       = "La foto contigo  💜",
        description = "Un momento especial que guardo siempre en el corazón. Gracias por estar.",
        category    = MemoryCategory.PERSONAL
    ),
    Memory(
        id          = 2,
        drawableRes = R.drawable.foto_grupal_dos,
        title       = "Foto grupal",
        description = "Otra foto, con los mejores gestores empresariales que conoci, y el de recursos humanos (bryan) jaja",
        category    = MemoryCategory.PERSONAL
    ),
    Memory(
        id          = 3,
        drawableRes = R.drawable.foto_grupal_uno,
        title       = "La foto de servicios T",
        description = "Nuestra foto todos juntos, servicios T por siempre 💙",
        category    = MemoryCategory.PERSONAL
    ),
    Memory(
        id          = 4,
        drawableRes = R.drawable.imagen_final,
        title       = "La ultima foto 📷 ",
        description = "Una amistad que siempre apreciare, gracias por ser siempre una gran amiga, tqm  🤍",
        category    = MemoryCategory.PERSONAL
    ),

    // ── Inovatec ──────────────────────────────────────────────────────────────
    Memory(
        id          = 5,
        drawableRes = R.drawable.recuerdo_foto_equipo_inovatec,
        title       = "Equipo Inovatec 🚀",
        description = "El equipo increíble de servicios T.",
        category    = MemoryCategory.INOVATEC
    ),
    Memory(
        id          = 6,
        drawableRes = R.drawable.recuerdo_trofeo_inovatec,
        title       = "Trofeo Inovatec 🏆",
        description = "El reconocimiento a tanto esfuerzo y dedicación. Te lo regale porque ganaste la etapa regional",
        category    = MemoryCategory.INOVATEC
    ),

    // ── Programas ─────────────────────────────────────────────────────────────
    Memory(
        id          = 7,
        drawableRes = R.drawable.recuerdo_primer_programa,
        title       = "Primer programa 🎨",
        description = "El primer paso siempre es el más especial. Aquí empezó todo.",
        category    = MemoryCategory.PROGRAMAS
    ),
    Memory(
        id          = 8,
        drawableRes = R.drawable.recuerdo_img_primerprograma,
        title       = "Inicio del programa 🎯",
        description = "El inicio de un monton de programas que valen mucho.",
        category    = MemoryCategory.PROGRAMAS
    ),
    Memory(
        id          = 9,
        drawableRes = R.drawable.recuerdo_programa_programa_girasol_dos,
        title       = "Programa Girasol 🌻",
        description = "El segundo programa que te hice, fueron dias despues del primero que te hice",
        category    = MemoryCategory.PROGRAMAS
    ),
    Memory(
        id          = 10,
        drawableRes = R.drawable.recuerdo_programa_ramo_girasoles,
        title       = "Ramo de girasoles 💐",
        description = "Un programa de un ramo de girasoles ",
        category    = MemoryCategory.PROGRAMAS
    ),
    Memory(
        id          = 11,
        drawableRes = R.drawable.recuerdo_programa_rosa,
        title       = "Programa Rosa 🌹",
        description = "Un programa que creo nunca te mostre",
        category    = MemoryCategory.PROGRAMAS
    ),
    Memory(
        id          = 12,
        drawableRes = R.drawable.recuerdo_programa_sanvalentin,
        title       = "San Valentín 💝",
        description = "Porque el amor y la amistad se celebran todos los días. Un recuerdo muy especial.",
        category    = MemoryCategory.PROGRAMAS
    ),

    // ── Detalles especiales ───────────────────────────────────────────────────
    Memory(
        id          = 13,
        drawableRes = R.drawable.recuerdo_carta_y_flor_de_cumpleanos,
        title       = "Carta de cumpleaños 🌸",
        description = "Una pequeña carta y una flor, que te di por tu cumpleaños",
        category    = MemoryCategory.ESPECIALES
    ),
    Memory(
        id          = 14,
        drawableRes = R.drawable.recuerdo_flow_tejida,
        title       = "Flor tejida 🧶",
        description = "Un regalo que te di",
        category    = MemoryCategory.ESPECIALES
    ),
    Memory(
        id          = 15,
        drawableRes = R.drawable.recuerdo_llavero_girasol,
        title       = "Llavero girasol 🌻",
        description = "un pequeño girasol que puedes llevar a donde quieras",
        category    = MemoryCategory.ESPECIALES
    ),
)