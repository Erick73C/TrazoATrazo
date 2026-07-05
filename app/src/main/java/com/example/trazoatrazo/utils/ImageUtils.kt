package com.example.trazoatrazo.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Utilidades para guardar dibujos como imágenes en la galería.
 */
object ImageUtils {

    /**
     * Guarda un Bitmap en la carpeta de Imágenes del dispositivo.
     */
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, folderName: String = "TrazoATrazo"): Boolean {
        val filename = "Dibujo_${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/$folderName")
            }
        }

        return try {
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
                Toast.makeText(context, "¡Imagen guardada en Galería! ✨", Toast.LENGTH_SHORT).show()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
            false
        }
    }

    /**
     * Añade un pie de página con mensaje y submensaje a un Canvas de Android.
     */
    fun drawFooterText(
        canvas: Canvas,
        width: Int,
        startY: Float,
        bgColor: Color,
        message: String,
        subMessage: String
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
        }

        if (message.isNotEmpty()) {
            paint.color = textColorFor(bgColor).toArgb()
            paint.textSize = 44f
            paint.isFakeBoldText = true
            canvas.drawText(message, width / 2f, startY + 70f, paint)
        }

        if (subMessage.isNotEmpty()) {
            paint.color = subtitleColorFor(bgColor).toArgb()
            paint.textSize = 28f
            paint.isFakeBoldText = false
            canvas.drawText(subMessage, width / 2f, startY + 130f, paint)
        }
    }
}
