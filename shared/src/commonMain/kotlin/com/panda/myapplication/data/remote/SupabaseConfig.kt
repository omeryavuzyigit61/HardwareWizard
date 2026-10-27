package com.panda.myapplication.data.remote

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers

/**
 * Supabase KMP & REST entegrasyonu yapılandırma sınıfı.
 * Statik ürün katalogları Room DB'de 0ms yerel hızda tutulurken,
 * dinamik topluluk fırsatları, OC/UV havuzu ve moderasyon işlemleri Supabase bulutunda barınır.
 *
 * NOT: Mağaza yayını (Play Store / App Store) tersine mühendislik güvenliği kapsamında
 * hassas uç noktalar ve anahtarlar statik DEX string tablosunda düz metin olarak tutulmaz;
 * çalışma zamanında [SecurityUtils.deobfuscate] ile çözülür.
 */
object SupabaseConfig {

    private val ENCODED_URL = intArrayOf(
        50, 21, 28, 31, 5, 71, 171, 164, 247, 243, 213, 195, 193, 222, 217, 179,
        186, 180, 173, 183, 129, 153, 140, 137, 104, 126, 114, 121, 48, 86, 89, 67,
        91, 35, 41, 60, 51, 115, 7, 4,
    )

    private val ENCODED_ANON_KEY = intArrayOf(
        22, 3, 203, 224, 237, 209, 254, 205, 228, 219, 243, 137, 146, 180, 156, 237,
        173, 131, 184, 139, 182, 104, 95, 33, 120, 97, 96, 6, 126, 85, 53, 20, 5, 25,
        43, 81, 65, 19, 4, 206, 251, 241, 170, 237, 206, 225, 220, 246, 185, 174, 137,
        154, 183, 191, 128, 178, 129, 88, 90, 89, 100, 87, 75, 102, 95, 96, 40, 1, 121,
        31, 48, 50, 26, 22, 46, 210, 241, 239, 167, 202, 212, 201, 246, 238, 142, 167,
        138, 176, 235, 135, 161, 186, 134, 154, 55, 70, 102, 83, 72, 95, 70, 85, 80,
        125, 56, 8, 10, 41, 81, 39, 24, 58, 246, 232, 163, 172, 246, 234, 238, 254, 203,
        155, 145, 129, 190, 145, 143, 169, 192, 181, 85, 65, 63, 88, 89, 101, 31, 125,
        125, 21, 52, 7, 56, 10, 87, 9, 50, 49, 73, 203, 231, 209, 236, 237, 237, 241,
        207, 240, 175, 171, 231, 148, 185, 216, 193, 167, 179, 117, 106, 81, 105, 73,
        81, 100, 112, 95, 32, 37, 9, 26, 29, 84, 12, 2, 36, 210, 220, 253, 206, 245,
        255, 129, 240, 233, 151, 184, 251, 153, 188, 139, 173, 149, 147, 70, 63, 81,
        67, 115,
    )

    // Supabase Proje URL'si (çalışma zamanında çözülür)
    var supabaseUrl: String = SecurityUtils.deobfuscate(ENCODED_URL, 0x5A)

    // Supabase Public Anon Key (çalışma zamanında çözülür)
    var supabaseAnonKey: String = SecurityUtils.deobfuscate(ENCODED_ANON_KEY, 0x73)

    // Tablolar
    const val TABLE_COMMUNITY_DEALS = "community_deals"
    const val TABLE_TUNING_PROFILES = "tuning_profiles"
    const val TABLE_TUNING_COMMENTS = "tuning_comments"
    const val TABLE_TUNING_REACTIONS = "tuning_reactions"
    const val TABLE_HARDWARE_PRO_TIPS = "hardware_pro_tips"
    const val TABLE_PRO_TIP_UPVOTES = "pro_tip_upvotes"
    const val TABLE_PRICE_DROP_ALERTS = "price_drop_alerts"
    const val TABLE_USER_PROFILES = "user_profiles"
    const val TABLE_COMMUNITY_RIG_SHOWCASES = "community_rig_showcases"
    const val TABLE_SHOWCASE_LIKES = "showcase_likes"

    // Güvenli RPC Fonksiyonları
    const val RPC_AUTHENTICATE_USER = "rpc/authenticate_user"

    // Storage Bucket
    const val STORAGE_BUCKET_TUNING = "tuning-proofs"

    // Aktif Supabase REST URL'si
    val restApiUrl: String
        get() = "${supabaseUrl.trimEnd('/')}/rest/v1"

    // Aktif Supabase Storage REST URL'si
    val storageApiUrl: String
        get() = "${supabaseUrl.trimEnd('/')}/storage/v1"

    /**
     * Tüm Supabase HTTP isteklerinde tekrar eden kimlik doğrulama başlıklarını merkezi olarak ekler.
     */
    fun HttpRequestBuilder.applySupabaseHeaders(preferMinimal: Boolean = false) {
        headers {
            append("apikey", supabaseAnonKey)
            append("Authorization", "Bearer $supabaseAnonKey")
            if (preferMinimal) {
                append("Prefer", "return=minimal")
            }
        }
    }
}
