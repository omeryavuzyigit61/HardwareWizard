package com.panda.myapplication.domain.util

/**
 * Güvenli Depolama Soyutlaması (pre_prompt_kmp.md kuralları gereği).
 * Token, oturum sırları ve kişisel veriler düz Room DB veya DataStore'a yazılmaz.
 * Android tarafında Android Keystore (AES-256-GCM), iOS tarafında ise Keychain Services
 * (kSecAttrAccessibleWhenUnlockedThisDeviceOnly, iCloud senkronu kapalı) ile korunur.
 */
interface SecureStorage {
    suspend fun put(key: String, value: String)
    suspend fun get(key: String): String?
    suspend fun remove(key: String)
    suspend fun clear()
}
