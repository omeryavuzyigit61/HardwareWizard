package com.panda.myapplication.presentation.components

import androidx.compose.runtime.Composable

/**
 * Cihaz galerisinden fotoğraf seçme veya doğrudan kamera ile fotoğraf çekip
 * <= 150 KB akıllı sıkıştırma sonrası ByteArray döndüren KMP başlatıcı arayüzü.
 */
interface ProofImagePickerLauncher {
    fun launchGallery()
    fun launchCamera()
}

@Composable
expect fun rememberProofImagePicker(
    onImageCompressed: (ByteArray) -> Unit,
    onError: (String) -> Unit = {},
): ProofImagePickerLauncher
