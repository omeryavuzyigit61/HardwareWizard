package com.panda.myapplication.presentation.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
actual fun rememberProofImagePicker(
    onImageCompressed: (ByteArray) -> Unit,
    onError: (String) -> Unit,
): ProofImagePickerLauncher {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val latestOnImageCompressed by rememberUpdatedState(onImageCompressed)
    val latestOnError by rememberUpdatedState(onError)

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val bitmap = decodeDownsampledBitmap(context, uri, MAX_DIMENSION)
                        ?: error("Görsel okunamadı.")
                    try {
                        compressBitmapUnder150Kb(bitmap)
                    } finally {
                        if (!bitmap.isRecycled) bitmap.recycle()
                    }
                }
            }.onSuccess { compressed ->
                latestOnImageCompressed(compressed)
            }.onFailure { e ->
                latestOnError(e.message ?: "Galeri görseli işlenemedi.")
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        if (bitmap == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    try {
                        compressBitmapUnder150Kb(bitmap)
                    } finally {
                        if (!bitmap.isRecycled) bitmap.recycle()
                    }
                }
            }.onSuccess { compressed ->
                latestOnImageCompressed(compressed)
            }.onFailure { e ->
                latestOnError(e.message ?: "Kamera fotoğrafı işlenemedi.")
            }
        }
    }

    return remember(galleryLauncher, cameraLauncher) {
        object : ProofImagePickerLauncher {
            override fun launchGallery() {
                runCatching { galleryLauncher.launch("image/*") }
                    .onFailure { latestOnError("Galeri açılamadı.") }
            }

            override fun launchCamera() {
                runCatching { cameraLauncher.launch(null) }
                    .onFailure { latestOnError("Kamera açılamadı.") }
            }
        }
    }
}

private const val MAX_DIMENSION = 1280

private fun decodeDownsampledBitmap(context: Context, uri: Uri, maxEdge: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, bounds)
    } ?: return null

    var sampleSize = 1
    val longest = max(bounds.outWidth, bounds.outHeight)
    if (longest > maxEdge) {
        while ((longest / (sampleSize * 2)) >= maxEdge) {
            sampleSize *= 2
        }
    }

    val decodeOpts = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.RGB_565
    }
    return context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, decodeOpts)
    }
}

private fun compressBitmapUnder150Kb(source: Bitmap): ByteArray {
    val longestEdge = max(source.width, source.height)
    val scaledBitmap = if (longestEdge > MAX_DIMENSION) {
        val ratio = MAX_DIMENSION.toFloat() / longestEdge.toFloat()
        val targetW = (source.width * ratio).roundToInt().coerceAtLeast(1)
        val targetH = (source.height * ratio).roundToInt().coerceAtLeast(1)
        Bitmap.createScaledBitmap(source, targetW, targetH, true)
    } else {
        source
    }

    return try {
        val maxBytes = 150 * 1024
        var quality = 82
        val out = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)

        while (out.size() > maxBytes && quality > 35) {
            out.reset()
            quality -= 12
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }
        out.toByteArray()
    } finally {
        if (scaledBitmap !== source && !scaledBitmap.isRecycled) {
            scaledBitmap.recycle()
        }
    }
}
