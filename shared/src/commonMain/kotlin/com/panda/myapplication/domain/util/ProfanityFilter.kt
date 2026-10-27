package com.panda.myapplication.domain.util

/**
 * Topluluk içeriklerinde (yorumlar, mikro ipuçları, rehber notları)
 * küfür ve hakaret içeren kelimeleri otomatik tespit edip yıldızlayan (sansürleyen) yardımcı sınıf.
 */
object ProfanityFilter {

    /**
     * Türkçe ve genel dillerde sık kullanılan küfür, hakaret ve argo kökleri.
     * Donanım terimleriyle ("anakart", "ekran kartı", "bellek", "sıkıntı") çakışmayacak
     * şekilde kelime sınırları (\b) ile derlenmiştir.
     */
    private val BLOCKED_PATTERNS = listOf(
        "(?i)\\b(amk|aq|orospu|orospuçocuğu|oç|piç|pic|yarrak|yarak|yavşak|yavsak|siktir|sikik|sik|göt|got|götveren|ibne|ipne|kahpe|kancık|puşt|pust|pezevenk|ananı|ananı\\s*sik|fuck|shit|bitch|asshole|cunt|dick)\\b",
        "(?i)(o\\s*r\\s*o\\s*s\\s*p\\s*u|s\\s*i\\s*k\\s*t\\s*i\\s*r|a\\s*m\\s*k|a\\s*n\\s*a\\s*n\\s*ı|y\\s*a\\s*r\\s*r\\s*a\\s*k)"
    )

    private val compiledRegexes = BLOCKED_PATTERNS.map { it.toRegex() }

    /**
     * Metindeki uygunsuz/küfürlü sözcükleri tespit edip ortasını '*' ile maskeler.
     * Örneğin: "Bu ayar çok sikik" -> "Bu ayar çok s***k"
     * "amk" -> "***"
     */
    fun mask(input: String?): String {
        if (input.isNullOrBlank()) return ""
        var cleaned: String = input
        for (regex in compiledRegexes) {
            cleaned = regex.replace(cleaned) { match ->
                val word = match.value
                if (word.length <= 3) {
                    "*".repeat(word.length)
                } else {
                    "${word.first()}${"*".repeat(word.length - 2)}${word.last()}"
                }
            }
        }
        return cleaned
    }

    /**
     * Metinde küfür veya hakaret bulunup bulunmadığını kontrol eder.
     */
    fun containsProfanity(input: String?): Boolean {
        if (input.isNullOrBlank()) return false
        return compiledRegexes.any { it.containsMatchIn(input) }
    }
}
