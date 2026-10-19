package com.panda.myapplication.data.remote

/**
 * Supabase KMP & REST entegrasyonu yapılandırma sınıfı.
 * Statik ürün katalogları Room DB'de 0ms yerel hızda tutulurken,
 * dinamik topluluk fırsatları ve moderasyon işlemleri Supabase bulutunda barınır.
 */
object SupabaseConfig {
    // Supabase Proje URL'si (ör. https://xyzcompany.supabase.co)
    var supabaseUrl: String = "https://hardwarewizard-community.supabase.co"

    // Supabase Public Anon Key (İstemci kimlik doğrulayıcı)
    var supabaseAnonKey: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.e30.HardwareWizardCommunityKey"

    // Topluluk fırsatları tablosu
    const val TABLE_COMMUNITY_DEALS = "community_deals"

    // Tek yetkili yönetici için varsayılan Admin Moderasyon PIN kodu
    const val DEFAULT_ADMIN_PIN = "1923"

    // Aktif Supabase REST URL'si
    val restApiUrl: String
        get() = "${supabaseUrl.trimEnd('/')}/rest/v1"
}
