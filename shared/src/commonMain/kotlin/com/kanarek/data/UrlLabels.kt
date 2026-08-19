package com.kanarek.data

private val URL_HOST =
    Regex("""^([A-Za-z][A-Za-z0-9+.-]*)://(?:[^@/?#]*@)?(\[[^]]+]|[^:/?#\s]+)""")

/** Return a compact host label for an absolute URL, or null when the input has no usable host. */
internal fun urlHostLabel(url: String): String? = absoluteUrlHost(url)?.removePrefix("www.")

/** Return a normalized host only for HTTP(S) URLs. */
internal fun httpUrlHost(url: String): String? {
    val match = URL_HOST.find(url.trim()) ?: return null
    if (match.groupValues[1].lowercase() !in setOf("http", "https")) return null
    return normalizedHost(match.groupValues[2])?.lowercase()?.removePrefix("www.")
}

private fun absoluteUrlHost(url: String): String? =
    URL_HOST
        .find(url.trim())
        ?.groupValues
        ?.getOrNull(2)
        ?.let(::normalizedHost)

private fun normalizedHost(raw: String): String? =
    raw
        .removePrefix("[")
        .removeSuffix("]")
        .takeIf { it.isNotBlank() }
