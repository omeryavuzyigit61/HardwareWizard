package com.panda.myapplication.presentation.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
actual fun rememberProofImagePicker(
    onImageCompressed: (ByteArray) -> Unit,
    onError: (String) -> Unit,
): ProofImagePickerLauncher {
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: error("Görsel okunamadı.")
            val compressed = compressBitmapUnder150Kb(bitmap)
            onImageCompressed(compressed)
        }.onFailure { e ->
            onError(e.message ?: "Galeri görseli işlenemedi.")
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap == null) return@rememberLauncherForActivityResult
        runCatching {
            val compressed = compressBitmapUnder150Kb(bitmap)
            onImageCompressed(compressed)
        }.onFailure { e ->
            onError(e.message ?: "Kamera fotoğrafı işlenemedi.")
        }
    }

    return remember(galleryLauncher, cameraLauncher) {
        object : ProofImagePickerLauncher {
            override fun launchGallery() {
                runCatching { galleryLauncher.launch("image/*") }
                    .onFailure { onError("Galeri açılamadı.") }
            }

            override fun launchCamera() {
                runCatching { cameraLauncher.launch(null) }
                    .onFailure { onError("Kamera açılamadı.") }
            }
        }
    }
}

private fun compressBitmapUnder150Kb(source: Bitmap): ByteArray {
    val maxDimension = 1280
    val longestEdge = max(source.width, source.height)
    val scaledBitmap = if (longestEdge > maxDimension) {
        val ratio = maxDimension.toFloat() / longestEdge.toFloat()
        val targetW = (source.width * ratio).roundToInt().coerceAtLeast(1)
        val targetH = (source.height * ratio).roundToInt().coerceAtLeast(1)
        Bitmap.createScaledBitmap(source, targetW, targetH, true)
    } else {
        source
    }

    val maxBytes = 150 * 1024
    var quality = 82
    val out = ByteArrayOutputStream()
    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)

    while (out.size() > maxBytes && quality > 35) {
        out.reset()
        quality -= 12
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
    }
    return out.toByteArray()
}
