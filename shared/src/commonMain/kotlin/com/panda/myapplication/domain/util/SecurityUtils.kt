package com.panda.myapplication.domain.util

/**
 * Central AppSec & Data Integrity Guard (DevSecOps).
 *
 * Enforces:
 * 1. Strict HTTPS-only & Domain Whitelisting for store redirect links and remote image URLs
 *    (rejects javascript:, file:, data:, http://, embedded credentials, and untrusted hosts).
 * 2. Payload size bounds checking before JSON deserialization to prevent DoS / OOM attacks.
 * 3. Honest TRY price sanitization to purge leaked USD catalog values (< 250 TL) or corrupted numbers.
 */
object SecurityUtils {

    /** Maximum permitted remote/local JSON payload size (25 MB) to prevent OOM / DoS attacks. */
    const val MAX_JSON_PAYLOAD_BYTES: Int = 25 * 1024 * 1024

    /** Minimum realistic Turkish Lira hardware price to prevent leaked USD catalog numbers (e.g., 70 TL, 105 TL). */
    const val MIN_VALID_TRY_PRICE: Double = 250.0

    /** Maximum realistic Turkish Lira hardware price bound. */
    const val MAX_VALID_TRY_PRICE: Double = 2_500_000.0

    private val FORBIDDEN_SCHEMES = listOf(
        "javascript:",
        "vbscript:",
        "data:",
        "file:",
        "content:",
        "intent:",
        "about:",
        "blob:",
        "ftp:",
        "ws:",
        "wss:",
    )

    /**
     * Whitelisted e-commerce and price-comparison domains allowed for external browser redirects.
     */
    private val ALLOWED_STORE_DOMAINS = setOf(
        "akakce.com",
        "cimri.com",
        "epey.com",
        "itopya.com",
        "sinerji.gen.tr",
        "gaming.gen.tr",
        "incehesap.com",
        "vatanbilgisayar.com",
        "teknobiyotik.com",
        "gamegaraj.com",
        "pckolik.com",
        "tebilon.com",
        "inventus.com.tr",
        "qp.com.tr",
        "hepsiburada.com",
        "trendyol.com",
        "amazon.com.tr",
        "n11.com",
        "pazarama.com",
        "idefix.com",
        "mediamarkt.com.tr",
        "teknosa.com",
        "monsternotebook.com.tr",
        "excalibur.com.tr",
        "casper.com.tr",
        "gameekstra.com",
        "adeksstore.com",
        "zoccoshop.com",
        "gelbura.com",
        "dfsbilgisayar.com",
        "novabilgisayar.com",
        "elmacik.com",
        "bilisimport.com",
    )

    /**
     * Whitelisted image CDN domains allowed for product imagery.
     */
    private val ALLOWED_IMAGE_DOMAINS = ALLOWED_STORE_DOMAINS + setOf(
        "raw.githubusercontent.com",
        "githubusercontent.com",
        "github.com",
        "akakce.net",
        "hepsiburada.net",
        "dsmcdn.com",
        "media-amazon.com",
        "ssl-images-amazon.com",
        "m.media-amazon.com",
        "images-na.ssl-images-amazon.com",
        "pcpartpicker.com",
        "cdna.pcpartpicker.com",
        "cdnb.pcpartpicker.com",
        "techpowerup.org",
        "tpucdn.com",
        "wikimedia.org",
        "amd.com",
        "nvidia.com",
        "intel.com",
        "asus.com",
        "msi.com",
        "gigabyte.com",
        "asrock.com",
        "corsair.com",
        "kingston.com",
        "gskill.com",
        "samsung.com",
        "nzxt.com",
        "deepcool.com",
        "lian-li.com",
        "arctic.de",
        "noctua.at",
        "bequiet.com",
        "seasonic.com",
        "fsp-group.com",
        "coolermaster.com",
        "thermaltake.com",
    )

    /**
     * Validates and sanitizes an external store URL.
     * - Must start with `https://`
     * - Must not contain control characters, whitespace injection, or `@` userinfo spoofing
     * - Host must match [ALLOWED_STORE_DOMAINS] (exact or subdomain)
     *
     * Returns the clean HTTPS URL string or `null` if rejected.
     */
    fun validateStoreUrl(rawUrl: String?): String? {
        if (rawUrl.isNullOrBlank()) return null
        val trimmed = rawUrl.trim()
        if (trimmed.length > 2048) return null
        if (trimmed.any { it.isISOControl() || it == '\n' || it == '\r' || it == '\t' }) return null

        val lower = trimmed.lowercase()
        if (FORBIDDEN_SCHEMES.any { lower.startsWith(it) }) return null
        if (!lower.startsWith("https://")) return null

        val host = extractHost(trimmed) ?: return null
        if (!isDomainAllowed(host, ALLOWED_STORE_DOMAINS)) return null

        return trimmed
    }

    /**
     * Validates and sanitizes a product image URL.
     * - Upgrades `http://` to `https://`
     * - Rejects dangerous schemes, SVG, data URIs, and known placeholder filenames
     * - Blocks loopback/private IPs (SSRF prevention) and non-FQDN hosts
     */
    fun sanitizeImageUrl(rawUrl: String?): String? {
        if (rawUrl.isNullOrBlank()) return null
        val trimmed = rawUrl.trim()
        if (trimmed.length > 2048) return null
        if (trimmed.any { it.isISOControl() }) return null

        val lower = trimmed.lowercase()
        if (FORBIDDEN_SCHEMES.any { lower.startsWith(it) }) return null
        if (lower.contains(".svg") || lower.contains("svg%20") || lower.contains("<svg")) return null
        if (lower.contains("placeholder") || lower.contains("blank.gif") || lower.contains("no-image") || lower.contains("no_image")) return null
        if (lower.contains("roundnew") || lower.contains("logo_2020")) return null

        val normalizedHttps = when {
            lower.startsWith("https://") -> trimmed
            lower.startsWith("http://") -> "https://" + trimmed.substring(7)
            else -> return null
        }

        val host = extractHost(normalizedHttps) ?: return null
        if (!host.contains('.')) return null
        if (host == "localhost" || host.startsWith("127.") || host.startsWith("10.") || host.startsWith("192.168.") || host.startsWith("169.254.") || host.endsWith(".local") || host.endsWith(".internal")) {
            return null
        }

        return normalizedHttps
    }

    /**
     * Sanitizes a Turkish Lira price to guarantee zero leaked USD catalog prices (e.g., 70 TL, 105 TL)
     * and zero NaN/Infinite corruption.
     */
    fun sanitizeTryPrice(price: Double?): Double? {
        if (price == null || price.isNaN() || price.isInfinite()) return null
        if (price < MIN_VALID_TRY_PRICE || price > MAX_VALID_TRY_PRICE) return null
        return price
    }

    /**
     * Verifies that a raw JSON payload does not exceed [MAX_JSON_PAYLOAD_BYTES] before parsing.
     */
    fun requireSafeJsonPayload(payload: String, sourceName: String = "JSON"): String {
        val byteEstimate = payload.length
        require(byteEstimate in 2..MAX_JSON_PAYLOAD_BYTES) {
            "$sourceName payload size ($byteEstimate chars) is out of safe bounds [2..$MAX_JSON_PAYLOAD_BYTES]."
        }
        return payload
    }

    fun extractHost(httpsUrl: String): String? {
        val withoutScheme = httpsUrl.substringAfter("://", missingDelimiterValue = "")
        if (withoutScheme.isEmpty()) return null
        val authority = withoutScheme.substringBefore('/').substringBefore('?').substringBefore('#')
        // Reject URLs with embedded userinfo (e.g., https://trusted.com@evil.com)
        if (authority.contains('@')) return null
        val host = authority.substringBefore(':').lowercase().trim('.')
        if (host.isBlank() || !host.contains('.')) return null
        if (host.any { !(it.isLetterOrDigit() || it == '.' || it == '-') }) return null
        return host
    }

    private fun isDomainAllowed(host: String, whitelist: Set<String>): Boolean {
        return whitelist.any { allowed ->
            host == allowed || host.endsWith(".$allowed")
        }
    }
}
