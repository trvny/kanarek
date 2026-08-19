package com.kanarek.data

/**
 * Favicon-based logo fallback for stations that have no `tvg-logo` and no directory match.
 * The stream host usually shares a domain with the broadcaster, so public favicon services are a
 * useful best-effort fallback before the UI's local drawable.
 */
object Favicons {
    /** Explicit station logo first, then Google and DuckDuckGo favicon fallbacks. */
    fun logoChain(station: Station): List<String> {
        val out = mutableListOf<String>()
        station.logoUrl?.takeIf { it.isNotBlank() }?.let { out += it }
        hostOf(station.streamUrl)?.let { host ->
            out += "https://www.google.com/s2/favicons?domain=$host&sz=256"
            out += "https://icons.duckduckgo.com/ip3/$host.ico"
        }
        return out
    }

    /** First favicon candidate for [streamUrl]'s host, or null if the host can't be parsed. */
    fun firstFor(streamUrl: String): String? =
        hostOf(streamUrl)?.let { "https://www.google.com/s2/favicons?domain=$it&sz=256" }

    /** Lower-cased HTTP(S) host with port and `www.` removed, or null for unsupported input. */
    fun hostOf(url: String): String? = httpUrlHost(url)
}
