package com.panda.myapplication.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberProofImagePicker(
    onImageCompressed: (ByteArray) -> Unit,
    onError: (String) -> Unit,
): ProofImagePickerLauncher {
    val delegate = remember {
        object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>,
            ) {
                val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
                picker.dismissViewControllerAnimated(true, completion = null)
                if (image != null) {
                    var quality = 0.78
                    var nsData = UIImageJPEGRepresentation(image, quality)
                    while (nsData != null && nsData.length.toInt() > 150 * 1024 && quality > 0.30) {
                        quality -= 0.14
                        nsData = UIImageJPEGRepresentation(image, quality)
                    }
                    if (nsData != null) {
                        onImageCompressed(nsData.toByteArray())
                    } else {
                        onError("Görsel sıkıştırılamadı.")
                    }
                }
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                picker.dismissViewControllerAnimated(true, completion = null)
            }
        }
    }

    return remember(delegate) {
        object : ProofImagePickerLauncher {
            override fun launchGallery() {
                presentPicker(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary, delegate, onError)
            }

            override fun launchCamera() {
                if (UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)) {
                    presentPicker(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera, delegate, onError)
                } else {
                    onError("Kamera bu cihazda kullanılamıyor.")
                }
            }
        }
    }
}

private fun presentPicker(
    sourceType: UIImagePickerControllerSourceType,
    delegate: NSObject,
    onError: (String) -> Unit,
) {
    val rootVc = UIApplication.sharedApplication.keyWindow?.rootViewController
    if (rootVc == null) {
        onError("Ekran denetleyicisi bulunamadı.")
        return
    }
    val picker = UIImagePickerController()
    picker.sourceType = sourceType
    @Suppress("UNCHECKED_CAST")
    picker.delegate = delegate as? UIImagePickerControllerDelegateProtocol
    rootVc.presentViewController(picker, animated = true, completion = null)
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)
    val result = ByteArray(size)
    result.usePinned { pinned ->
        memcpy(pinned.addressOf(0), bytes, length)
    }
    return result
}
