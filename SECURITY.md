# 🛡️ Hardware Wizard - Güvenlik Politikası & DevSecOps Standartları (Security Policy)

Hardware Wizard projesi, kullanıcı verilerini korumak, donanım kataloğunun bütünlüğünü sağlamak ve potansiyel siber saldırıları önlemek amacıyla katı bir **Sıfır Güven (Zero-Trust)** ve **DevSecOps** yaklaşımı benimser.

---

## 1. Desteklenen Sürümler

Aşağıdaki sürümler aktif güvenlik güncellemeleri ve yamaları almaktadır:

| Sürüm | Durum | Güvenlik Desteği |
| :--- | :---: | :---: |
| 2.0.x (Main Branch) | Güncel | ✅ Tam Destek |
| 1.x.x | Eski | ⚠️ Kritik Yamalar |

---

## 2. DevSecOps Güvenlik Mimarisi

Hardware Wizard ekosisteminde uygulanan çok katmanlı savunma prensipleri:

### A. Sıfır Güven (Zero-Trust) Görsel ve URL Doğrulaması
* **Şema Engelleme:** `javascript:`, `data:`, `file:`, `blob:`, `content:`, `intent:` ve güvenli olmayan `http:` protokolleri kesin olarak reddedilir.
* **Onaylı Alan Adı Beyaz Listesi (Host Whitelist):** Tüm donanım görselleri yalnızca yetkilendirilmiş donanım üreticileri ve güvenli CDN'lerden kabul edilir (`dlcdnwebimgs.asus.com`, `m.media-amazon.com`, `lian-li.com`, `seasonic.com`, `asset.msi.com` vb.).

### B. Kriptografik Bütünlük (SHA-256 Manifestosu)
* Katalogdaki tüm donanım JSON dosyaları (`gpu.json`, `psu.json`, `cpu.json` vb.) SHA-256 algoritmasıyla özetlenir ve `catalog_manifest.json` dosyasına işlenir.
* Veritabanında yapılacak yetkisiz veya kötü amaçlı değişiklikler çalışma anında tespit edilerek engellenir.

### C. SQL Injection & XSS Koruması
* Yerel veritabanı erişimi **Room Multiplatform** ORM katmanı üzerinden parametreli sorgularla yürütülür; raw string birleştirme kullanılmaz.
* Kullanıcı girişleri (topluluk yorumları, parça adları, fiyat notları) HTML ve Script enjeksiyonuna karşı XSS süzgecinden geçirilir.

### D. Çok Aşamalı Kimlik Doğrulama (2FA) & RLS
* Yönetici ve moderatör panelleri tek kullanımlık 6 haneli 2FA güvenlik doğrulama kodu ve zaman aşımı koruması ile korunur.
* Supabase arka yüzünde **Row Level Security (RLS)** mekanizmasıyla yetkisiz kullanıcıların başkalarına ait verilere erişmesi engellenir.

---

## 3. Güvenlik Açığı Bildirimi (Vulnerability Reporting)

Eğer Hardware Wizard kaynak kodunda veya altyapısında potansiyel bir güvenlik açığı tespit ederseniz:

1. **Lütfen açık bir GitHub Issue oluşturmayın.**
2. Bulgularınızı gizli tutarak doğrudan proje sahibine e-posta ile iletin:
   - **E-Posta:** `yavuzyigitomer52@gmail.com`
   - **Konu Başlığı:** `[SECURITY] Hardware Wizard Güvenlik Raporu - <Başlık>`
3. Raporunuzda lütfen şunlara yer verin:
   - Açığın türü ve etki alanı,
   - Açığın yeniden üretilebilmesi için adım adım talimatlar veya PoC (Proof of Concept),
   - Varsa ekran görüntüsü veya log çıktıları.

Raporunuz en geç 48 saat içinde incelenecek ve gerekli güvenlik yaması ivedilikle hazırlanacaktır.
Katkı sağlayan araştırmacılara teşekkür edilir.
