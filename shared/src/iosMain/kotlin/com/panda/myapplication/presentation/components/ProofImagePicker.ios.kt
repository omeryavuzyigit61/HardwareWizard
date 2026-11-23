package com.panda.myapplication.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject
import platform.posix.memcpy
import kotlin.math.max

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberProofImagePicker(
    onImageCompressed: (ByteArray) -> Unit,
    onError: (String) -> Unit,
): ProofImagePickerLauncher {
    val latestOnImageCompressed by rememberUpdatedState(onImageCompressed)
    val latestOnError by rememberUpdatedState(onError)

    val delegate = remember {
        object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>,
            ) {
                val rawImage = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
                picker.dismissViewControllerAnimated(true, completion = null)
                if (rawImage != null) {
                    val resized = resizeImageToMaxEdge(rawImage, maxEdge = 1280.0)
                    var quality = 0.78
                    var nsData = UIImageJPEGRepresentation(resized, quality)
                    while (nsData != null && nsData.length.toInt() > 150 * 1024 && quality > 0.28) {
                        quality -= 0.14
                        nsData = UIImageJPEGRepresentation(resized, quality)
                    }
                    if (nsData != null) {
                        latestOnImageCompressed(nsData.toByteArray())
                    } else {
                        latestOnError("Görsel sıkıştırılamadı.")
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
                presentPicker(
                    sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary,
                    delegate = delegate,
                    onError = { latestOnError(it) },
                )
            }

            override fun launchCamera() {
                if (UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)) {
                    presentPicker(
                        sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera,
                        delegate = delegate,
                        onError = { latestOnError(it) },
                    )
                } else {
                    latestOnError("Kamera bu cihazda kullanılamıyor.")
                }
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun resizeImageToMaxEdge(image: UIImage, maxEdge: Double): UIImage {
    val (width, height) = image.size.useContents { width to height }
    val longest = max(width, height)
    if (longest <= maxEdge || longest <= 0.0) return image
    val ratio = maxEdge / longest
    val targetW = (width * ratio).coerceAtLeast(1.0)
    val targetH = (height * ratio).coerceAtLeast(1.0)
    UIGraphicsBeginImageContextWithOptions(CGSizeMake(targetW, targetH), false, 1.0)
    return try {
        image.drawInRect(CGRectMake(0.0, 0.0, targetW, targetH))
        UIGraphicsGetImageFromCurrentImageContext() ?: image
    } finally {
        UIGraphicsEndImageContext()
    }
}

private fun findTopViewController(): UIViewController? {
    val app = UIApplication.sharedApplication
    val keyWin = app.connectedScenes
        .mapNotNull { it as? UIWindowScene }
        .flatMap { scene -> scene.windows.mapNotNull { it as? UIWindow } }
        .firstOrNull { it.isKeyWindow() }
        ?: app.keyWindow
    var top = keyWin?.rootViewController ?: return null
    while (true) {
        val next = top.presentedViewController ?: break
        top = next
    }
    return top
}

private fun presentPicker(
    sourceType: UIImagePickerControllerSourceType,
    delegate: NSObject,
    onError: (String) -> Unit,
) {
    val topVc = findTopViewController()
    if (topVc == null) {
        onError("Ekran denetleyicisi bulunamadı.")
        return
    }
    val picker = UIImagePickerController()
    picker.sourceType = sourceType
    @Suppress("UNCHECKED_CAST")
    picker.delegate = delegate as? UINavigationControllerDelegateProtocol
    topVc.presentViewController(picker, animated = true, completion = null)
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
