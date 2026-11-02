# 🔒 Hardware Wizard - Gizlilik Politikası & Kişisel Verilerin Korunması (Privacy Policy)

**Son Güncelleme:** 2 Kasım 2026  
**Yürürlük Tarihi:** 2 Kasım 2026  
**Veri Sorumlusu:** Ömer Yavuz Yiğit (`yavuzyigitomer52@gmail.com`)

---

## 1. Giriş ve Kapsam
Hardware Wizard ("Uygulama"), kullanıcıların donanım parçalarını araştırmalarını, bilgisayar toplama ve uyumluluk kontrolleri yapmalarını, cihaz donanım teşhisi gerçekleştirmelerini ve topluluk bünyesinde donanım fırsatları ile hız aşırtma/voltaj düşürme (OC/UV) deneyimlerini paylaşmalarını sağlayan bir Kotlin Multiplatform mobil ve web ekosistemidir.

Bu Gizlilik Politikası, 6698 sayılı **Kişisel Verilerin Korunması Kanunu (KVKK)** ve **Genel Veri Koruma Yönetmeliği (GDPR)** prensipleri doğrultusunda, verilerinizin nasıl toplandığını, işlendiğini, saklandığını ve korunduğunu açıklar.

---

## 2. Toplanan Veriler ve Toplanma Amaçları

### A. Hesap ve Kimlik Doğrulama Bilgileri
* **Kullanıcı E-Posta Adresi:** Kullanıcı kayıt, giriş ve şifresiz oturum açma (Magic Link / Supabase Auth) işlemleri için işlenir.
* **İki Aşamalı Güvenlik (2FA) Kodları:** Yetkili panel ve moderatör girişlerinde tek kullanımlık 6 haneli güvenlik doğrulama kodları (OTP) üretilir ve geçici olarak oturum süresince doğrulanır.

### B. Canlı Cihaz Donanım Teşhis Bilgileri (Yerel ve Anonim)
* **İşlemci (SoC), RAM Miktarı, Ekran Yenileme Hızı (Hz), Batarya Seviyesi:** "Cihazım Donanım Teşhisi" modülü kapsamında, yalnızca cihazınızın performansını katalogdaki mobil donanımlarla kıyaslamak amacıyla tamamen **cihaz üzerinde (yerel)** işlenir.
* **Gizlilik Güvencesi:** Cihazınızın benzersiz donanım seri numarası (IMEI, MAC adresi vb.) **kesinlikle toplanmaz, kaydedilmez ve üçüncü taraflara iletilmez.**

### C. Topluluk Paylaşımları ve Kullanıcı İçerikleri
* **Silikon Kasası (OC/UV Paylaşımları):** Kullanıcıların isteğe bağlı olarak paylaştığı saat hızı (MHz), voltaj (mV), çalışma sıcaklığı (°C) ve kanıt görselleri.
* **Fiyat Bildirimleri ve Fırsatlar:** Kullanıcıların bildirdiği piyasa fiyatları, mağaza bağlantıları ve kullanıcı notları.
* **Kasa Vitrini Görselleri:** Kullanıcıların masaüstü ve kasa kurulumlarını sergilemek üzere yüklediği fotoğraflar.

---

## 3. Veri Güvenliği ve DevSecOps Standartları
Hardware Wizard, kullanıcı verilerini korumak için sektör standardı modern güvenlik katmanları kullanır:
* **Uçtan Uca Şifreli İletişim (TLS 1.3 / HTTPS):** Uygulama ile sunucu arasındaki tüm veri transferi yüksek seviyeli şifreleme ile korunur.
* **Sıfır Güven (Zero-Trust) Görsel Süzgeci:** Yüklenen veya listelenen tüm medya bağlantıları zararlı kod enjeksiyonuna (XSS, URL Poisoning) karşı beyaz liste süzgecinden geçirilir.
* **Kriptografik Veri Bütünlüğü (SHA-256):** Katalog veritabanı manipülasyonlara karşı SHA-256 hash imzalarıyla korunur.
* **Rol Tabanlı Erişim Denetimi (RBAC & RLS):** Supabase veritabanında Row Level Security (RLS) politikaları aktif olup kullanıcılar yalnızca kendi verilerini yönetebilir.

---

## 4. Çerezler ve Üçüncü Taraf Entegrasyonları
* **Analitik & Reklam Ağları:** Uygulama içerisinde kullanıcıları profilleyen, üçüncü taraf reklam veya veri simsarlığı yapan **hiçbir izleyici (tracker) veya reklam kütüphanesi yer almaz.**
* **Altyapı Sağlayıcıları:** Veritabanı ve kimlik doğrulama altyapısı güvenli açık kaynak standartlarına sahip **Supabase** altyapısında barındırılır.

---

## 5. Kullanıcı Hakları (KVKK & GDPR)
Kullanıcılar aşağıdaki haklara sahiptir:
1. Kişisel verilerinin işlenip işlenmediğini öğrenme,
2. İşlenen verilerine ilişkin bilgi talep etme,
3. Hesaplarının ve paylaşımlarının kalıcı olarak silinmesini talep etme (Unutulma Hakkı),
4. Yanlış veya eksik bilgilerin düzeltilmesini isteme.

Bu haklarınızı kullanmak için dilediğiniz zaman doğrudan **yavuzyigitomer52@gmail.com** adresine e-posta gönderebilirsiniz. Talebiniz en geç 30 gün içinde yanıtlanacaktır.

---

## 6. Değişiklikler ve İletişim
Bu Gizlilik Politikası zaman zaman güncellenebilir. Değişiklikler GitHub deposu üzerinden ilan edildiği tarihten itibaren geçerli olur.

**İletişim:**
* **Geliştirici & Veri Sorumlusu:** Ömer Yavuz Yiğit
* **E-Posta:** `yavuzyigitomer52@gmail.com`
* **GitHub:** [https://github.com/omeryavuzyigit61/HardwareWizard](https://github.com/omeryavuzyigit61/HardwareWizard)
