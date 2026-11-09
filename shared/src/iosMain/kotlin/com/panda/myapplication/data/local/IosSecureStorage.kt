package com.panda.myapplication.data.local

import com.panda.myapplication.domain.util.SecureStorage
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.kCFBooleanFalse
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.CFTypeRefVar
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleWhenUnlockedThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecAttrSynchronizable
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/**
 * iOS Keychain Services güvenli depolama implementasyonu.
 * pre_prompt_kmp.md kuralları gereği:
 * - kSecAttrSynchronizable = false (iCloud Keychain senkronizasyonu kapalı)
 * - ThisDeviceOnly erişim sınıfı (kSecAttrAccessibleWhenUnlockedThisDeviceOnly)
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosSecureStorage : SecureStorage {

    private val serviceName = "com.panda.myapplication.vault"

    override suspend fun put(key: String, value: String) {
        remove(key)

        val nsValue = NSString.create(string = value).dataUsingEncoding(NSUTF8StringEncoding) ?: return
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to serviceName,
            kSecAttrAccount to key,
            kSecValueData to nsValue,
            kSecAttrAccessible to kSecAttrAccessibleWhenUnlockedThisDeviceOnly,
            kSecAttrSynchronizable to kCFBooleanFalse,
        )

        SecItemAdd(CFBridgingRetain(query) as platform.CoreFoundation.CFDictionaryRef, null)
    }

    override suspend fun get(key: String): String? {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to serviceName,
            kSecAttrAccount to key,
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne,
            kSecAttrSynchronizable to kCFBooleanFalse,
        )

        memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(
                CFBridgingRetain(query) as platform.CoreFoundation.CFDictionaryRef,
                result.ptr
            )

            if (status == errSecSuccess && result.value != null) {
                val data = CFBridgingRelease(result.value) as? NSData
                if (data != null) {
                    return NSString.create(data, NSUTF8StringEncoding)?.toString()
                }
            }
        }
        return null
    }

    override suspend fun remove(key: String) {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to serviceName,
            kSecAttrAccount to key,
            kSecAttrSynchronizable to kCFBooleanFalse,
        )
        SecItemDelete(CFBridgingRetain(query) as platform.CoreFoundation.CFDictionaryRef)
    }

    override suspend fun clear() {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to serviceName,
            kSecAttrSynchronizable to kCFBooleanFalse,
        )
        SecItemDelete(CFBridgingRetain(query) as platform.CoreFoundation.CFDictionaryRef)
    }
}
